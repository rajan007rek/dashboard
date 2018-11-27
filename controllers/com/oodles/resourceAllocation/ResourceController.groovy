package com.oodles.resourceAllocation

import org.springframework.security.access.annotation.Secured
import grails.plugin.springsecurity.*
import com.oodles.security.User
import grails.plugin.springsecurity.ui.*
import org.codehaus.groovy.grails.web.json.JSONObject
import com.google.gson.Gson
import com.oodles.project.*
import org.grails.taggable.Tag

//@Secured(["ROLE_ADMIN"])
class ResourceController {

	def resourceService
	def springSecurityService
	def oodlesMailService
	def userService
	def grailsApplication
	
	
	def getResourceDetails(){
		JSONObject jsonObject = new JSONObject(params.data)
		log.debug jsonObject.jsonData
		if(jsonObject != null) responseObjects(resourceService.getResourceDetails(jsonObject.jsonData),true)
		else responseObjects("Please enter valid input data",false)
	}
	
	def getResourceDemands(){

		JSONObject jsonObject = new JSONObject(params.data)
		log.debug jsonObject.jsonData
		User user = springSecurityService.currentUser
		if(userService.isSupervisor(user) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
		   if(jsonObject != null) responseObjects(resourceService.getResourceDemands(jsonObject.jsonData),true)
		   else responseObjects("Please enter valid input data",false)
		}
		else{
			return responseObjects("You are not authorized to view this section",false)
			}
	}
	def addCandidateInfo(){
		JSONObject jsonObject = new JSONObject(params.data)
		User user = springSecurityService.currentUser
		if(userService.isSupervisor(user) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
			if(jsonObject != null) responseObjects(resourceService.addCandidateInfo(jsonObject),true)
			else responseObjects("Please enter valid input data",false)
		 }
		 else{
			 return responseObjects("You are not authorized to view this section",false)
			 }
	}
	def getCandidateInfo() {
		def demandId = params.demandId;
		User user = springSecurityService.currentUser
		if(userService.isSupervisor(user) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
		   if(demandId != null) responseObjects(resourceService.getCandidateInfo(demandId),true)
		   else responseObjects("Please enter valid input data",false)
		}
		else{
			return responseObjects("You are not authorized to view this section",false)
			}
	}
	
	def removeCandidateInfo() {
		def candidateInfoId = params.id;
		def demandId = params.demandId;
		User user = springSecurityService.currentUser
		if(userService.isSupervisor(user) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
		   if(candidateInfoId != null) responseObjects(resourceService.removeCandidateInfo(candidateInfoId, demandId),true)
		   else responseObjects("Please enter valid input data",false)
		}
		else{
			return responseObjects("You are not authorized to view this section",false)
			}
	}
	
	def editCandidateInfo() {
		JSONObject jsonObject = new JSONObject(params.data)
		User user = springSecurityService.currentUser
		if(userService.isSupervisor(user) || SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
			if(jsonObject != null) responseObjects(resourceService.editCandidateInfo(jsonObject),true)
			else responseObjects("Please enter valid input data",false)
		 }
		 else{
			 return responseObjects("You are not authorized to view this section",false)
			 }
	}
	def getClientProjects(){
		def clientId = params.clientId
		println("got hit in the backend :"+clientId)
		if(clientId != null) responseObjects(resourceService.getclientProjectList(clientId),true)
		else responseObjects("Please enter valid input data",false)
	}
	
	
	def getResourceProjects(){
		def userId = params.userId
		if(userId != null) responseObjects(resourceService.getProjectList(userId),true)
		else responseObjects("Please enter valid input data",false)
	}

	
	def updateResource(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		println ("jsonObject : "+jsonObject)
		
		Employee employee = Employee.get(jsonObject.jsonData.id)
		println employee.billableStatus.getKey()
		/*if(employee.billableStatus.getKey() == "Internal" && jsonObject.jsonData.billableStatus != "Internal" && jsonObject.jsonData.billableStatus != "No"){
			def userInfo = userService.getUserInfoData(employee.user)
			def jsonDemand = [:]
			jsonDemand.jsonData = [:]
			jsonDemand.jsonData.hiringMode = ""
			jsonDemand.jsonData.offersMade = 0
			jsonDemand.jsonData.interviewPanel = []
			jsonDemand.jsonData.recruiters = []
			jsonDemand.jsonData.teamLeads = []
			jsonDemand.jsonData.billableStatus = "INTERNAL"
			jsonDemand.jsonData.project = "Internal Projects"
			jsonDemand.jsonData.description = userInfo.designation
			jsonDemand.jsonData.experience = employee.experience
			jsonDemand.jsonData.numberOfOpenings = 1
			jsonDemand.jsonData.status = "Open"
			jsonDemand.jsonData.priority = "Normal"
			jsonDemand.jsonData.candidateName = ""
			jsonDemand.jsonData.remarks = "Replacement for "+employee.user.firstName + " " +employee.user.lastName
			Gson gson = new Gson();
			String json = gson.toJson(jsonDemand);
			JSONObject jsonObj = new JSONObject(json)
			resourceService.resolveDemandData(jsonObj,null)
		}*/
		employee.deploymentDate = (jsonObject.jsonData.deploymentDate == "NA" || jsonObject.jsonData.deploymentDate == "")?null:Date.parse( "dd-MM-yyyy", jsonObject.jsonData.deploymentDate)
		//employee.releaseDate = (employee.releaseDate == null)?"NA":new SimpleDateFormat("dd-MM-yyyy").format(employee.releaseDate).toString()
		employee.expectedReleaseDate = (jsonObject.jsonData.expectedReleaseDate == "NA" || jsonObject.jsonData.expectedReleaseDate == "" || jsonObject.jsonData.expectedReleaseDate == "On going")?null:Date.parse( "dd-MM-yyyy", jsonObject.jsonData.expectedReleaseDate)
		employee.billableStatus = Billable.getBillableStatus(jsonObject.jsonData.billableStatus)
		employee.officeCode = jsonObject.jsonData.officeCode
		/*if(jsonObject.jsonData.lastVerificationDate.equals("Yes")) {
			employee.lastVerificationDate = true
		} else {
			employee.lastVerificationDate = false
		}*/
		employee.billableFrom = (jsonObject.jsonData.billableFrom == "NA" || jsonObject.jsonData.billableFrom == "" || jsonObject.jsonData.billableFrom == "Not applicable")?null:Date.parse( "dd-MM-yyyy", jsonObject.jsonData.billableFrom)
		employee.lastVerification = (jsonObject.jsonData.lastVerification == "NA" || jsonObject.jsonData.lastVerification == "")?null:Date.parse( "dd-MM-yyyy", jsonObject.jsonData.lastVerification)
		//employee.primarySkill = jsonObject.jsonData.primary
		//employee.secondrySkill = jsonObject.jsonData.secondry
		
		
		employee.remarks = jsonObject.jsonData.remarks
		employee.experience = Float.parseFloat(jsonObject.jsonData.experience.toString())
		employee.expMonth = Integer.parseInt(jsonObject.jsonData.expMonth.toString())
		
		
		Tag[] technologyList = employee.primarySkills.toArray(new Tag[employee.primarySkills.size()])
		for (int i = 0; i < technologyList.length; i++) {
			employee.removeFromPrimarySkills(technologyList[i])
		}
		
		jsonObject.jsonData.primarySkills.name.each {
			def technology = Tag.findByName(it.text)
			if (technology) {
				employee.addToPrimarySkills(technology)
			}
	   }
		
		technologyList = employee.secondrySkills.toArray(new Tag[employee.secondrySkills.size()])
		for (int i = 0; i < technologyList.length; i++) {
			employee.removeFromSecondrySkills(technologyList[i])
		}
		
		jsonObject.jsonData.secondrySkills.name.each {
			def technology = Tag.findByName(it.text)
			if (technology) {
				employee.addToSecondrySkills(technology)
			}
	   }
		
		
		employee.save(flush:true)
		User user = springSecurityService.currentUser
		
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
			
			if(employee != null){
				return responseObjects(employee, true)
			}else{
				return responseObjects(employee, false)
			}
		}
		else{
			return responseObjects(employee,false)
		}
	}
	
	
	def deleteDemand(){
		def demandId = params.id
		def isAdmin = params.isAdmin
		Demand demand = resourceService.deleteDemand(demandId,isAdmin)
		if(demand != null){
			return responseObjects(demand,true)
		}else{
			return responseObjects(demand,false)
		}
	}
	
	def saveDemand(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		println ("jsonObject : "+jsonObject)
		User user = springSecurityService.currentUser
		boolean flag = false
		if(userService.isSupervisor(user)){
		Demand demand = resourceService.resolveDemandData(jsonObject,null)
		flag = true
		def comment = jsonObject.jsonData.comment
		DemandCommunicationData demandCommunicationData = new DemandCommunicationData(isAdmin:flag, message:comment, messageDate:new Date(),msgFrom:user.id)
		demandCommunicationData.save();
		demand.addToDemandCommunicationData(demandCommunicationData)
		demand.save(flush: true)
		demand = resourceService.sendEmail(demand,user,comment)
			
			if(demand != null){
				return responseObjects(demand, true)
			}else{
				return responseObjects(demand, false)
			}
		}else{
		return responseObjects("You are not authorized to take this action",false)
		}	
		
	}
	
	def updateDemand(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		println ("jsonObject : "+jsonObject)
		
		Demand demand = Demand.findById(jsonObject.jsonData.id)
		
		User user = springSecurityService.currentUser
		
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_RESOURCING")){
			
			demand = resourceService.resolveDemandData(jsonObject,demand)
			
			if(demand != null){
				return responseObjects(demand, true)
			}else{
				return responseObjects(demand, false)
			}
		}
		else{
			return responseObjects("You are not authorized to take this action",false)
		}		
		
	}
	
	def responseObjects(def responseList,boolean isSuccess){
		HashMap res = new HashMap()
		if(isSuccess && responseList != null){
			res.content = responseList
			res.result = "success"
		}else {
			res.content = responseList
			res.result = "failed"
		}
		respond res, [formats: ['json']]
	}
	
	def submitComment() {
		HashMap res = new HashMap()
		def demand = Demand.get(request.JSON.demandId)
		def commentedBy
		String comment = request.JSON.comment
		JSONObject jsonObject = new JSONObject(request.JSON)
		commentedBy = springSecurityService.currentUser
		resourceService.saveDemandInfoHistory(jsonObject,demand, comment, commentedBy)
		res = resourceService.generateResponse(res, "success", "Submitted successfully")
		respond res, [formats: ['json']]
	}
	
	def getResourceCommunicationHistory(){
		def communicationHistory = resourceService.getDemandCommunicationHistory(params)
		HashMap res = new HashMap()
		res.communicationHistory = communicationHistory
		respond res, [formats: ['json']]
	}
	
	def sendProjectResourceVerification() {
		HashMap res = new HashMap()
		def projectId = params.projectId
		if(projectId != null) responseObjects(resourceService.sendProjectResourceVerification(projectId),true)
		else responseObjects("Please enter valid input data",false)
	}
	def designationGrade() {
		def designation = params.designation
		if(designation != null)responseObjects(resourceService.designationGrade(params.designation),true)
		else responseObjects("Please enter valid input data",false)
	}
	
}

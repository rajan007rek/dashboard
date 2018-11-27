package com.oodles.dashboard.settings
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.web.json.JSONObject
import com.oodles.security.*
import com.oodles.kpa.AreaOfWork
@Secured(["ROLE_ADMIN", "ROLE_USER","ROLE_HR", "ROLE_BD", "ROLE_IM"])
class DesignationController {
	def springSecurityService
	def dashboardService
	def designationService
	/**
	 *  Call when we select the designation from dashboard home page
	 *  this function give the list of all Designation
	 */
	def getDesignationInformation() {
		HashMap res = new HashMap()
		res.role = "ROLE_NOTFOUND"
		def userDesignationInfoList = UserDesignation.findAll()
		def roles = springSecurityService.getPrincipal().getAuthorities()
		roles.each {
			if (it.toString().trim().equalsIgnoreCase("ROLE_ADMIN")) {
				res.role = it.toString().trim()
			}
		}
		if(params.statusFilter){
			def filteredDesignation = getFilteredDesignation(params.statusFilter)
			
			res.result = "success"
			res.message = "filteredDesignation"
			res.allGrades = getAllGrades()
			res.userDesignationInfoList = filteredDesignation.reverse()
		}
		else{
			res.result = "success"
			res.message = "AllDesignation"
			res.allGrades = getAllGrades()
			res.userDesignationInfoList = designationListToHashMap(userDesignationInfoList).reverse()
		}
		respond res, [formats: ['json']]
	}
	
	def getFilteredDesignation(def statusFilter){
		if(statusFilter.equals("ACTIVE")){
			return designationListToHashMap(UserDesignation.findAllByIsValidAndIsDeleted(true,false));
		}
		else if(statusFilter.equals("DISABLED")){
			return designationListToHashMap(UserDesignation.findAllByIsValidAndIsDeleted(false,false));
		}
		else if(statusFilter.equals("DELETED")){
			return designationListToHashMap(UserDesignation.findAllByIsDeleted(true));
		}
	}
	/**
	 *  Call when we hit the submit button while adding new Designation
	 *  this function add new Designation
	 */
	def createDesignation() {
		HashMap res = new HashMap()
		JSONObject designationData = new JSONObject(params.data)
		if (params.data) {
			UserDesignation existingDesignation = UserDesignation.findByDesignation(designationData.designation)
			if (existingDesignation) {
				res = dashboardService.generateResponse(res, "failed", "Designation Already Exists")
			} else {
				def userDesignation = designationService.createDesignation(designationData)
				res.id = userDesignation.id
				res.designation = userDesignation.designation
				res.grades = userDesignation.grades.grades
				res = dashboardService.generateResponse(res, "success", "Designation Created Successfully")
			}
		} else {
			res = dashboardService.generateResponse(res, "success", "Invalid")
		}
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the edit link 
	 *  this function get the details of Designation for editing
	 */
	def getDesignationDetails() {
		HashMap res = new HashMap()
		def userDesignation = UserDesignation.get(params.id)
		res.designation = userDesignation?.designation?:""
		res.description = userDesignation?.description?:""
		res.grades = userDesignation?.grades?.grades?:""
		res.result="success"
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the submit button while editing
	 *  this function update the Designation details
	 */
	def editDesignation() {
		HashMap res = new HashMap()
		JSONObject editDesignationData = new JSONObject(params.data)
			if (editDesignationData) {
				UserDesignation alreadyExist = UserDesignation.findByDesignationAndIdNotEqual(editDesignationData.designation, editDesignationData.id)
				if (alreadyExist) {
					res = dashboardService.generateResponse(res, "success", "Designation Already Exist")
				} else {
					def userDesignation = designationService.editDesignationSer(editDesignationData)
					res.id = userDesignation.id
					res.designation = userDesignation.designation
					res = dashboardService.generateResponse(res, "success", "Record updated successfully")
				}
			} else {
				res = dashboardService.generateResponse(res, "failed", "Invalid Designation Data")
			}
		respond res, [formats: ['json']]
	}
	
	/*For delete the designation*/
	def deleteDesignation(){
		HashMap res = new HashMap()
		def userDesignation = UserDesignation.get(params.designationId)
		println "userDesignationId : "+(userDesignation.id)
		userDesignation.isDeleted = !userDesignation.isDeleted
		userDesignation.save(flush: true)
		res.result = "success"
		res.massage = "Designation is deleted Successfully"
		res.isDeleted = userDesignation.isDeleted
		respond res, [formats: ['json']]
	}
	
	/*Check the status of designation is in use for area of work */
	def checkInUseStatus() {
		HashMap res = new HashMap();
		def userDesignation = UserDesignation.get(params.designationId);
		def areaList = AreaOfWork.findAll();
		Set userDesignations = []
		areaList.each{
			it.userDesignation.each {
				userDesignations.add(it)
			}
		}
		def designationList = UserInfo.findAllByDesignation(userDesignation.designation)
		res.massageForDelete = "This Designation can not be deleted because it is used in : Many Users"
		res.massageForDisable = "This Designation can not be disabled because it is used in : Many Users"
		res.isDeleted = userDesignation.isDeleted
		res.isValid = userDesignation.isValid
		res.isInUse = ((userDesignations.contains(userDesignation.designation)) || (designationList!=[]))
		res.result = "failed"
		respond res, [formats: ['json']]
	}
	
	
	
	/**
	 *  Call when we hit the disable link 
	 *  this function used to disable or enable the designation 
	 */
	def disableDesignation() {
		HashMap res = new HashMap()
		def userDesignation = UserDesignation.get(params.userDesignationId)
		userDesignation.isValid = !userDesignation.isValid
		userDesignation.save(flush: true)
		res = dashboardService.generateResponse(res, "success", "Status Changed Successfully")
		res.isValid = userDesignation.isValid
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the submit button while adding new Team
	 *  this function add new Team 
	 */
	def addTeam(){
		HashMap res=new HashMap();
		JSONObject newTeamData=new JSONObject(params.jsonData)
		if(newTeamData){
			def add= designationService.createTeam(newTeamData)
			if(add){
				res = dashboardService.generateResponse(res, "success", "Team successfully Added")
			}else{
				res = dashboardService.generateResponse(res, "failed", "Error Occured while Adding the Team")
			}
		}else{
			res = dashboardService.generateResponse(res, "failed", "Error Occured while Adding the Team")
		}
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we select the team from dashboard page
	 *  this function get the list of all teams
	 */
	def getTeamDetails(){
		HashMap res=new HashMap()
		def teamList=UserTeam.list()
		res.teamList=teamList
		res.result='success'
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the disable link 
	 *  this function used to disable or enable the Team status
	 */
	def changeTeamStatus(){
		HashMap res=new HashMap()
		def team=UserTeam.get(params.id)
		if(team){
			team.isValid=!team.isValid
			team.save(flush:true)
			res.result='success'
			res.isValid = team.isValid
		}
		else
		{
			res.result='failed'
		}
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the edit link
	 *  this function get the details of team for editing
	 */
	def getSingleTeam(){
		HashMap res=new HashMap()
		def team=UserTeam.get(params.id)
		res.team=team
		respond res, [formats: ['json']]
	}
	/**
	 *  Call when we hit the update button while editing the team details
	 *  this function update the team details
	 */
	def editTeamDetails(){
		HashMap res=new HashMap()
		def team=UserTeam.get(params.id)
		JSONObject editTeamData=new JSONObject(params.jsonData)
		String url = editTeamData.url.toString().replaceAll(" ", "-").toLowerCase().trim()
		if(team){
			team.title= editTeamData.title
			team.description=editTeamData.description
			team.teamType= editTeamData.teamType
			team.url= url
			team.metaContent= editTeamData.metaContent
			team.pageTitle=editTeamData.pageTitle
			team.metaContentkeywords=editTeamData.metaContentkeywords
			team.save(flush:true)
			res = dashboardService.generateResponse(res, "success", "Team successfully Updated")
		}else {
			res = dashboardService.generateResponse(res, "failed", "Error Occured while Updating the Team")
		}
		respond res, [formats: ['json']]
	}
	
	def getAllGrades(){
		def allGrades = UserGrades.findAllByIsValidAndIsDeleted(true,false)
		return allGrades
	}
	
	def designationListToHashMap(designationObjList){
		def userDesignationList = []
		for(def userDesignationIterator : designationObjList)
		{
			HashMap map =new HashMap();
				map.description = userDesignationIterator.description
				map.designation = userDesignationIterator.designation
				map.grades = userDesignationIterator.grades == null? "": userDesignationIterator.grades.grades
				map.id = userDesignationIterator.id
				map.isDeleted = userDesignationIterator.isDeleted
				map.isValid =  userDesignationIterator.isValid
			userDesignationList.add(map);
		}
		return userDesignationList
	}
}
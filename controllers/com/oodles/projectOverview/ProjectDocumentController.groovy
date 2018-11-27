package com.oodles.projectOverview
import com.oodles.security.User
import com.oodles.security.UserRole
import com.oodles.blog.BlogStatus
import com.oodles.project.Project
import com.oodles.projectOverview.ProjectDocument
import com.oodles.projectOverview.ProjectDocumentService

import grails.plugin.springsecurity.ui.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import static grails.async.Promises.*
import grails.async.Promise

import org.codehaus.groovy.grails.web.json.JSONObject

import java.text.SimpleDateFormat
class ProjectDocumentController {
	
	def projectDocumentService
	def fileuploadService
	def springSecurityService
	def userService
	def oodlesMailService
	def oodlesNotificationService
	def dashboardService
	def mailTrackerService
	def grailsApplication
	def conf = SpringSecurityUtils.securityConfig
	
	def getProjectDetail() {
		HashMap res=new HashMap()
		def projectCommentList = []
		def documentTypes=DocumentTypeList()
		def respList=projectDocumentService.getProjectDocument(params.id)
		res.isAdminRights=respList.isAdminRights
		res.isPMO=respList.isPmo
		res.projectName=respList.projectName
		res.documentTypes=documentTypes
		res.documentList=respList.documentList
		res.projectRemark=respList.remark
		res.result="Success"
		respond res,[formats: ['json']]
	}
	
	def createDocumentEntry(){
		HashMap res=new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		String fileName = jsonObject.name
		String content = jsonObject.content
		content = content.substring(content.indexOf(",")+1)
		boolean upload = false
		boolean flag1=false;
		def flag=projectDocumentService.saveProjectDocument(jsonObject, flag1)
		if(flag){
			res.result="success"
		}else{
		res.result="failed"
		}
		respond res,[formats: ['json']]
	}
	
	
	def editDocumentEntry(){
		HashMap res=new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		def data=projectDocumentService.editProjectDocument(jsonObject)
		if(data!=null){
			res.result="success"
		}else{
		res.result="failed"
		}
		res.document=data
		respond res,[formats: ['json']]
	}
	
	def updateDocumentEntry(){
		HashMap res=new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		boolean upload = false
		boolean flag1=false;
		def flag=projectDocumentService.editProjectDocumentDeatil(jsonObject)
		if(flag){
			res.result="success"
		}else{
		res.result="failed"
		}
		respond res,[formats: ['json']]
	}
	
	def projectFileView(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		String path=jsonObject.jsonData.path
		String type=jsonObject.jsonData.type
		String s3Folder = path.substring(0,path.indexOf("/")) //jsonObject.id.toString() 183
		String fileName = path.substring(path.indexOf("/")+1) // eg. "Tooling-Buyer.pdf"
		String url = fileuploadService.viewProjectFiles(s3Folder, type, fileName)
		responseObjects(url,"success")

	}
	def responseObjects(def responseList,String outcome){
		HashMap res = new HashMap()
		if("success".equalsIgnoreCase(outcome) && responseList != null){
			res.content = responseList
			res.result = "success"
		}else {
			res.content = responseList
			res.result = "failed"
		}
		respond res, [formats: ['json']]
	}
	
	def addNewVersion() {
		HashMap res=new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		boolean flag1=true;
		def flag=projectDocumentService.saveProjectDocument(jsonObject, flag1)
		if(flag){
			res.result="success"
		}else{
		res.result="failed"
		}
		respond res,[formats: ['json']]
	}
	
	def projectFileDownload(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		String path=jsonObject.jsonData.path
		String type=jsonObject.jsonData.type
		String s3Folder = path.substring(0,path.indexOf("/")) //jsonObject.id.toString() 183
		String fileName = path.substring(path.indexOf("/")+1) // eg. "Tooling-Buyer.pdf"
		String url = fileuploadService.downloadProjectFiles(s3Folder, fileName,type)
		responseObjects(url,"success")

	}
	
	def getProjectFiles(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		def files=projectDocumentService.getProjectFiles(jsonObject)
			res.result="success"
			res.files=files
		respond res,[formats: ['json']]
	}
	
	def saveNewDocumentType(){
		HashMap res = new HashMap()
		println("hit got in the saveNewDocumentType")
		println(params.id)
		String docType=params.id
		boolean flag=projectDocumentService.createDocumentType(docType)
		if(flag==true){
			res.message="Document Type Created SuccessFully"
			res.result="Success"
		}else{
			res.message="Document you want to create is already in the list"
			res.result="Failed"
		}

		respond res,[formats: ['json']]
	}
	
	def DocumentTypeList(){
			HashMap res = new HashMap()
			def documetType = DocumentType.list().type
			return documetType
	}
	
	def getTypeFiles(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		def files=projectDocumentService.getFilesForType(jsonObject)
		res.result="success"
		res.files=files
		respond res,[formats: ['json']]
	}
	
	
	//------------@author -----------------------------------------
	/**
	 * 
	 */
	
	def sendMailToManager() {
		HashMap res = new HashMap()

		JSONObject feedback = new JSONObject(request.JSON)
		def currentUser = springSecurityService.currentUser
		boolean isAdmin = request.JSON.isAdmin
		String message = request.JSON.message
		ProjectDocument projectDocument = ProjectDocument.findById(request.JSON.projectDocumentId)
		Project project = Project.findById(projectDocument.projectId)
		def sendMailTo
		def msgFrom
		boolean isAdminPMO=false;
		boolean notPMO=false;
		String managerName= "";
		def flag=0;
		int count = 0;
		//println("current user role::::"+currentUser.getAuthorities())
		def list = currentUser.getAuthorities();
		def roles = []
		for(int i = 0; i < list.size(); i++) {
			//println("roles----"+list[i].authority)
			if(list[i].authority == "ROLE_ADMIN" || list[i].authority == "ROLE_PMO") {
				roles.add(list[i].authority)
			}
		}
		
		/*for(int i = 0; i < roles.size(); i++) {
			println("roles[i].authority:::: "+roles[i].authority)
			if(roles[i].authority == "ROLE_ADMIN") {
				println("role admin and pmo ")
				for(int j = 0; j < roles.size(); j++) {
				//roles.each{ otherRole->
				if(roles[j].authority == "ROLE_PMO"){
					println("role admin and pmo2 ")
				
			isAdmin = true
			sendMailTo = project.team.projectManager.email
			msgFrom = conf.ui.pmomail
			flag=1;
			count = 1;
			break;
				}
				}
				if(count == 1){
					break;
				}
			}
			else  if(roles[i].authority == "ROLE_PMO"){
				println("role pmo only")
				isAdmin = true
				sendMailTo = project.team.projectManager.email
				msgFrom = conf.ui.pmomail
				flag=1;
				break;
			}
			else if(roles[i].authority == "ROLE_ADMIN"){
				println("role admin only")
				notPMO = true
				sendMailTo = project.team.projectManager.email
				msgFrom = currentUser.email
				managerName = currentUser.fullName
				flag=3;
				break;
			}
			else{
				println("role other only")
				isAdmin = false
				sendMailTo = conf.ui.pmomail
				msgFrom = currentUser.email
				flag=2;
				break;
			}
			
		}*/
		
		
		if(roles.size()==2){
			//println("both admin and pmo")
			isAdmin = true
			sendMailTo = project.team.projectManager.email
			msgFrom = conf.ui.pmomail
			flag=1;
		}
		else if(roles.size()==1){
			//println("only one")
			if(roles[0]=="ROLE_ADMIN"){
				//println("only admin")
				notPMO = true
				sendMailTo = project.team.projectManager.email
				msgFrom = currentUser.email
				managerName = currentUser.fullName
				flag=3;
			}
			
			else if(roles[0]=="ROLE_PMO"){
				//println("only pmo")
			isAdmin = true
			sendMailTo = project.team.projectManager.email
			msgFrom = conf.ui.pmomail
			flag=1;
			}
			
		}
		else if(roles.size==0){
			//println("no role admin and pmo")
				isAdmin = false
				sendMailTo = conf.ui.pmomail
				msgFrom = currentUser.email
				flag=2;
			}
		
		
		   
		String userImageUrl = grailsApplication.config.grails.imgURL+currentUser.image
		
		
		projectDocumentService.saveProjectDocumentCommunicationHistory(projectDocument, currentUser, isAdmin, message,notPMO)
		String ApproveTargetUrl="";
		String subject = "Oodles Team"
		boolean mailSent=false;
		boolean result=false;
		
		//Promise p = task{
		if(flag==1){
			//println("flag=1MPO")
			res.result='success'
			ApproveTargetUrl = grailsApplication.config.grails.redirectToProjectDocument+project.id
			
			//subject = "Comment made by PMO Team on "+projectDocument.documentType + " of project "+project.name
			subject = "Comment made by PMO Team on Project Document"
			def body = g.render(template: "/portfolio/projectDocumentMailByPMO", model: [managerName: project.team.projectManager.fullName, comment: message, documentType: projectDocument.documentType,projectName:project.name,Aurl:ApproveTargetUrl]).toString()
			Promise p = task{mailSent = oodlesMailService.mailFromOodles(sendMailTo, msgFrom, subject, body)}
			
		}else if (flag==2){
		
		//check current user if project manager then send manger naem else set current user name
		
		//println("flag=2MPO")
		
		if(currentUser.email==project.team.projectManager.email){
			
		//println("same user as project manager")
		managerName = project.team.projectManager.fullName
		}
		else{
			//println('some other user')
			managerName = currentUser.fullName
		}
		
		 ApproveTargetUrl = grailsApplication.config.grails.redirectToProjectDocument+project.id
		
		//subject = "Reply by "+project.team.projectManager.fullName+" on "+projectDocument.documentType + " of project "+project.name
		subject = "Comment made on Project Document"
		
		//println("managerName---"+managerName)
		//println("project.team.projectManager.fullName---")
		def body = g.render(template: "/portfolio/projectDocumentMailByManager", model: [managerName: managerName, comment: message, documentType: projectDocument.documentType,projectName:project.name,Aurl:ApproveTargetUrl]).toString()
		Promise p = task{mailSent = oodlesMailService.mailFromOodles(sendMailTo, msgFrom, subject, body)}
		/*if(mailSent){
				List<User> userList = new ArrayList();
				userList.add(sendMailTo)
				result=mailTrackerService.SaveEmailRecord(userList,msgFrom,subject,userList)}
	        else{
			   List<User> userList = new ArrayList();
			    userList.add(sendMailTo)
				result=mailTrackerService.FailedEmailRecord(userList,msgFrom,subject,userList)}*/
		}
		else if (flag==3){
			//println("flag=3MPO")
			 ApproveTargetUrl = grailsApplication.config.grails.redirectToProjectDocument+project.id
			
			//subject = "Reply by "+project.team.projectManager.fullName+" on "+projectDocument.documentType + " of project "+project.name
			subject = "Comment made on Project Document"
			def body = g.render(template: "/portfolio/projectDocumentMailByManager", model: [managerName: managerName, comment: message, documentType: projectDocument.documentType,projectName:project.name,Aurl:ApproveTargetUrl]).toString()
			Promise p = task{mailSent = oodlesMailService.mailFromOodles(sendMailTo, msgFrom, subject, body)}
			/*if(mailSent){
					List<User> userList = new ArrayList();
					userList.add(sendMailTo)
					result=mailTrackerService.SaveEmailRecord(userList,msgFrom,subject,userList)}
				else{
				   List<User> userList = new ArrayList();
					userList.add(sendMailTo)
					result=mailTrackerService.FailedEmailRecord(userList,msgFrom,subject,userList)}*/
			}
	//	}
		res.result='success'
		/*if(mailSent){
			String name=userService.getUserName(springSecurityService.currentUser)
			oodlesNotificationService.createNotification(sendMailTo,"Comment Made On "+project.name+"'s "+projectDocument.documentType)
			res = dashboardService.generateResponse(res, "success", "Mail Sent")
		}
		else{
			res = dashboardService.generateResponse(res, "error", "Error! try after sometime")
		}*/
		respond res, [formats: ['json']]
	}
	
	@Secured(["ROLE_USER"])
	def redirectToProjectDocument() {
		/*println "testAction"
		if(params.projectId){
		  String url = grailsApplication.config.grails.projectDocumentRequestURL + params.projectId
		  redirect(url: url)
		}
		else {
			response.sendError 404
		}*/
		
		//println "testActionsss"
		def currentUser = springSecurityService.currentUser
		boolean sameUser=false;
		boolean isPMO=false;
		if(params.projectId){
		Project project = Project.findById(params.projectId)
		User user = project.team.projectManager
		if(currentUser.email==user.email){
			sameUser=true;
			//println "testAction22"
		}
		else{
			sameUser=false;
		}
		
		def list = currentUser.getAuthorities();
		for(int i = 0; i < list.size(); i++) {
			if(list[i].authority == "ROLE_PMO") {
				isPMO=true;
				//println "testAction333"
			}
		}
		}
		
		if(sameUser){
		  String url = grailsApplication.config.grails.projectDocumentRequestURL + params.projectId
		  redirect(url: url)
		}
		else if(isPMO){
		  String url = grailsApplication.config.grails.projectDocumentRequestURL + params.projectId
		  redirect(url: url)
		}
		else {
			response.sendError 404
		}
		
		
	 }
	
	def sendMailForMissingDocumentProjectManager() {
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		def missingDocument = projectDocumentService.sendMailForMissingDocumentProjectManager(jsonObject)
		if(missingDocument != null) {
			responseObject(missingDocument,"success")
		} else {
			responseObject(missingDocument,"false")
		}
	}
	
	def responseObject(def responseList,String outcome){
		HashMap res = new HashMap()
		if("success".equalsIgnoreCase(outcome) && responseList != null){
			res.content = responseList
			res.result = "success"
		}else {
			res.content = responseList
			res.result = "failed"
		}
		respond res, [formats: ['json']]
	}
	
	def saveProjectRemark()
	{
		
		println ("Function hit")
		
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		
		println  jsonObject
		def isSave=projectDocumentService.saveRemarkOfProject(jsonObject.jsonData.id, jsonObject.jsonData.remark.toString())
			if(isSave!=null){
				res.result = "success"
				res.isSaved = isSave
			}else{
			res.result = "failure"
			res.isSaved = isSave
			}
			respond res, [formats: ['json']]
	}
	
	def deleteProjectRemark()
	{
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		def isSave=projectDocumentService.deleteRemarkOfProject(jsonObject.jsonData.id)
			if(isSave==true){
				res.result = "success"
				res.isSaved = isSave
			}else{
			res.result = "failure"
			res.isSaved = isSave
			}
			respond res, [formats: ['json']]
	}
}

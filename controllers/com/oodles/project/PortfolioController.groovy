package com.oodles.project
import grails.gsp.PageRenderer
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.web.json.JSONObject
import org.grails.taggable.Tag

import com.amazonaws.services.s3.model.*
import com.oodles.categories.*
import com.oodles.leads.LeadInfo
import com.oodles.leads.LeadsFile
import com.oodles.security.*
import com.oodles.utils.CommonConstants
@Secured(["permitAll"])
class PortfolioController {
	def dashboardService
	def springSecurityService
	def portfolioService
	def oodlesNotificationService
	def clientManagementService
	def fileuploadService
	def userService
	def oodlesMailService
	def grailsApplication
	def PageRenderer groovyPageRenderer

	/**
	 * This is the default action which is used when PortFolioController is called  
	 * @return
	 */
	def index() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList,projectList: projectList,clients: clients,url: grailsApplication.config.grails.serverURL]
	}
	/**
	 * This action is used to create a New Project
	 * It takes various project details as a parameter
	 */
	def createProject() {
		println("hit got in the create project")
		JSONObject newProjectData = new JSONObject(request.JSON)
		HashMap res = new HashMap()
		def project = Project.findByNameAndIsArchived(newProjectData.projectDetails.projectName,false)
		if(project){
			res = dashboardService.generateResponse(res, "failed", "Project Name Already Exists")
		}
		else {
			project = portfolioService.createProject(newProjectData)
			if(project){
				oodlesNotificationService.projectNotification(project)
				res.id = project.id
				res.projectName = project.name
				res = dashboardService.generateResponse(res, "success", "Project Created Successfully")
			}
			else{
				res = dashboardService.generateResponse(res, "failed", "Please select valid client from list")
			}
		}
		respond res, [formats: ['json']]
	}

	/**
	 * This action gives the list of all ProjectNames when user click on view & edit Details
	 * of any user in AllUser list at very first time. select name from project where parent_project_id=0;
	 */
	def projectNameList() {
		//def projectList = Project.list().name
		def projectList = Project.executeQuery("select name from Project where isArchived ='false' and currentStatus = 'Open'")
		respond projectList, [formats: ['json']]
	}
	def parentProjectNameList() {
		//		def projectList = Project.list().name
		def projectList=Project.executeQuery("select name from Project where parentProjectId='0'and currentStatus='Open' and isArchived='false'")
		respond projectList, [formats: ['json']]
	}

	def getLeadEmailList() {
		def leadEmailList=LeadInfo.executeQuery("select emailId from LeadInfo where leadStatus='ACQUIRED'")
		//               def leadEmailList = LeadInfo.list().emailId
		respond leadEmailList, [formats: ['json']]
	}
	/**
	 *This action gives the list of all Approved Tags
	 *according to user input parameters
	 *Invoked when user create a new project and AddTechnologies on that Particular Project
	 */
	def allApprovedTags() {
		def tagList = ApprovedTags.findAllByTagsIlike("%" + params.query + "%").tags
		respond tagList, [formats: ['json']]
	}

	def getAllTags() {

		boolean exactName = false;
		def searchCriteria = Tag.createCriteria()
		int skip = 0
		int maxSize = 9
		def tags = searchCriteria.list(offset:skip, max:maxSize){

			and {
				ilike("name", "%"+params.query+"%")
			}
		}
		Tag tag = Tag.findByName(params.query)
		def tagList = new ArrayList()
		tags.each {
			if(tag != null){
				if(it.name.equalsIgnoreCase(params.query)){
					exactName = true
				}
			}
			tagList.add(it.name)
		}

		if(!exactName && tag != null){
			tagList.add(tag.name)
		}

		respond tagList, [formats: ['json']]
	}

	/**	
	 * This action is used for changing the status of a project
	 */
	def statusChange() {
		HashMap res = new HashMap()
		def project = Project.get(params.projectId)
		if(project){
			if (project.currentStatus == "Closed") {
				project.currentStatus = "Open"
			} else {
				project.currentStatus = "Closed"
			}
			if(project.save()){
				res = dashboardService.generateResponse(res, "success", "Status Changed Successfully")
				res.status = project.currentStatus
			}else{
				res = dashboardService.generateResponse(res, "success", "Unable to Changed Status")
				res.status = project.currentStatus
			}
		}
		respond res, [formats: ['json']]
	}

	/***
	 * This action is used for publishing or unpublishing a project
	 */
	def publishProject() {
		HashMap res = new HashMap()
		def project = Project.get(params.projectId)
		if(project){
			project.enable = !project.enable
			if(project.save()){
				if (project.enable) {
					res = dashboardService.generateResponse(res, "unpublished", "Project UnPublished Successfully")
				} else {
					res = dashboardService.generateResponse(res, "published", "Project Published Successfully")
				}
			}
			res.enable = project.enable
		}
		respond res, [formats: ['json']]
	}

	/***
	 * This action is used for marking eligibility of a project for code review
	 */
	def codeReviewEligible() {
		User user = springSecurityService.currentUser
		HashMap res = new HashMap()
		def projectStatus
		def project = Project.get(params.projectId)
		if(project){
			project.codeReviewEligible = !project.codeReviewEligible
			if(project.save()){
				if (project.codeReviewEligible) {
					projectStatus=" enabled "
					res = dashboardService.generateResponse(res,"eligible" , "Project is now eligible for code review")
				} else {
					projectStatus=" disabled "
					res = dashboardService.generateResponse(res, "not eligible","Project is now not eligible for code review" )
				}
			}
			res.codeReviewEligible = project.codeReviewEligible
			sendMailOnCodeReviewStatusChange(project,project.codeReviewEligible,projectStatus, user)
		}
		respond res, [formats: ['json']]
	}

	//sending mail as and when code review status change
	def boolean sendMailOnCodeReviewStatusChange(Project project,boolean reviewEligible,String status, User user){
		def subject;
		def conf = SpringSecurityUtils.securityConfig
		def projectName= project.name
		def projectManagerName=project.team.projectManager.firstName+" "+project.team.projectManager.lastName
		subject="Code review status changed"
		String html = groovyPageRenderer.render(template: "/codeReview/onEnableDisableCodeReview", model: [
			user: user.fullName,
			projectName:project.name,
			changedStatus: status
		]).toString()
		def body = html
		boolean mailSent = oodlesMailService.mailFromOodles(grailsApplication.config.grails.plugin.springsecurity.ui.register.emailFromPMO,conf.ui.register.emailFrom, subject, body)
		if(mailSent)
			println ("mail sent successfully")
		else
			println ("mail sending failed")
		return mailSent;
	}


	def deleteProject() {
		HashMap res = new HashMap()
		def isBoolean = false
		def project = Project.get(params.projectId)
		//if(subProjectList.size() == 0) {
		if(project){
			def subProjectList = Project.findAllByParentProjectIdAndIsArchived(project.id, false)
			if(subProjectList.size() > 0) {
				for(int i = 0; i < subProjectList.size(); i++) {
					if(subProjectList[i].currentStatus == "Closed") {
						isBoolean = false
					} else {
						isBoolean = true
						break;
					}
				}
			}
			println("subProjectList.size()::"+subProjectList.size()+"  isBoolean::::"+isBoolean)
			
			if((project.isParent == true && isBoolean == false) || subProjectList.size() == 0) {
				println("go in delete")
				project.isArchived = true;
				project.projectUrl=""
				project.currentStatus = "Closed"
				if(project.save()){
					if (project.isArchived) {
						res = dashboardService.generateResponse(res, "isArchived", "Project deleted Successfully")
					} else {
						res = dashboardService.generateResponse(res, "failure", "Project could not be deleted")
					}
				}
				res.isArchived = project.isArchived
			} else if(subProjectList.size() > 0 && isBoolean == true) {
				println("go in not delete")
				res = dashboardService.generateResponse(res, "failure", "Project could not be deleted")
			}
		}
		respond res, [formats: ['json']]
	}

	/**
	 * This action show the Project Details according the user selected Project
	 * and also include the code for viewing next project or previous project
	 * Invoked when user click on showProject of any  project in AllProject list
	 */
	def showProject() {
		log.debug("showing Project")
		def project = Project.findByProjectUrlAndEnable(params.projectUrl,true)
		String value = "one"
		/*if(springSecurityService.isLoggedIn() && SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {*///}
		def projectList1=[]
		if(project) {

			def categoryId
			switch(params.categoryName){
				case "mobileApplications":
					categoryId="1";
					break;
				case "videoStreaming":
					categoryId="2";
					break;
				case "saasApplications":
					categoryId="3";
					break;
				case "bigDataAndNoSQL":
					categoryId="4";
					break;
				case "blockchainDevelopment":
					categoryId="5";
					break;
				default : categoryId="";
			}
			//categoryId=""
			if(categoryId == null || categoryId == ""){
				log.debug("Category Name found null or empty")
				response.sendError 404
				//model: [projectList: [], category: params.categoryName, projectList1: [], prv: 0, nxt: 0, url:grailsApplication.config.grails.portfolioURL, categoryId:categoryId]
				return
			}
			def catId=Category.findById(categoryId)

			projectList1=catId.project.findAll{!it.enable}

			def prevPage=projectList1.getAt(0)

			def nxtPage=projectList1.getAt(1)

			def projectList = [:]
			projectList.id = project.id
			projectList.name = project.name
			projectList.description = project.description
			projectList.title = project.title
			projectList?.canonicalUrl = project?.canonicalUrl
			projectList.keywords = project.keywords
			def screensots = []
			def screenShotsList = project.screenShots
			if (screenShotsList) {
				screenShotsList.each {screensots.add(fileuploadService.getS3FileUrl(CommonConstants.PROJECT_IMAGE,project.s3FolderName,it))}
			}
			projectList.image = screensots
			projectList.clientName = project.client.name
			projectList.googlePlayUrl = project.googlePlayUrl
			projectList.itunesUrl = project.itunesUrl
			projectList.websitesUrl = project.websiteUrl
			projectList.clientDescription = project.client.description
			projectList.technologies = project.technologies
			projectList.count = Project.count
			List<String> members = new ArrayList<String>()
			List<String> leads = new ArrayList<String>()
			//List<String> projectManagers = new ArrayList<String>()
			project.team.member.each {
				members.add(userService.getUserName(it))
				if (members.size() > 1)
					value = "more"
			}
			project.team.lead.each {
				leads.add(userService.getUserName(it))
				if (leads.size() > 1)
					value = "more"
			}
			/*project.team.projectManager.each {
			 projectManagers.add(userService.getUserName(it))
			 if (members.size() > 1)
			 value = "more"
			 }*/
			projectList.member = members
			projectList.teamLead = leads
			//projectList.projectManager = projectManagers
			projectList.team = project.team
			projectList.projectManager = userService.getUserName(project.team.projectManager)
			//projectList.teamLead = userService.getUserName(project.team.lead)
			projectList.value = value
			model: [projectList: projectList, category: params.categoryName, projectList1: projectList1, prv: prevPage, nxt: nxtPage, url:grailsApplication.config.grails.portfolioURL, categoryId:categoryId]
		}
		else
			response.sendError 404
	}

	/**
	 * get team members of a Project
	 */

	def getProjectTeamMembers(){

		def projectName = params.name;
		def project = Project.findByName(projectName);
		List<String> members = new ArrayList<String>()
		project.team.member.each {
			members.add(userService.getUserName(it));
		}
		def leads = getProjectTeamLeads(projectName)
		leads.each {
			members.add(it);
		}
		//members.add(userService.getUserName(project.team.lead))
		return members

	}

	def getProjectTeamLeads(projectName){

		def project = Project.findByName(projectName);
		List<String> leads = new ArrayList<String>()
		project.team.lead.each {
			leads.add(userService.getUserName(it));
		}
		return leads

	}
	def getProjectTeamLeads2(){
		println("hit got in the getProjectTeamLeads2")
		JSONObject jsonObject= new JSONObject(request.JSON)
		HashMap resp = new HashMap()
		def projectName = jsonObject.project;
		def project = Project.findByName(projectName);
		List<String> leads = new ArrayList<String>()
		String leadEmail=project.leadEmail;
		if(leadEmail!=null){
			LeadInfo leadInfo=LeadInfo.findByEmailId(leadEmail)
			resp.data = LeadDoc(leadInfo.id)
		}
		resp = dashboardService.generateResponse(resp, "success", "Details of User retreived successfully")
		respond resp, [formats: ['json']]
	}
	def LeadDoc(id) {
		def mailNofication
		HashMap res = new HashMap()
		LeadsFile leadfile=new LeadsFile();
		List<LeadsFile>  RecordDetails = leadfile.createCriteria().list(){
			eq('leadId', id+"")
		}
		return RecordDetails
	}

	/**
	 * This action is used to give the description of Project when user click on edit project
	 */
	def projectDescription() {
		HashMap res = new HashMap()
		def project = Project.get(Long.parseLong(params.id))
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy")
		def projectData = [:]
		def technologiesUsed = []
		def memeberEmail = []
		def leadEmail = []
		// def projectManagerEmail = []
		def screensots = []
		project.technologies?.name.each { technologiesUsed.add(it) }
		project.team.member.each {memeberEmail.add(it.email)}
		project.team.lead.each {leadEmail.add(it.email)}
		//project.team.projectManager.each {projectManagerEmail.add(it.email)}
		projectData.name = project.name
		def categoryNames = []
		project.category.each {categoryNames.add(it.name)}
		projectData.category = categoryNames
		projectData.originalDate = project.originalDate?dateFormat.format(project.originalDate):null
		projectData.description = project.description == "null"?"":project.description
		projectData.isParent=project.isParent
		projectData.title = project.title
		projectData?.canonicalUrl = project?.canonicalUrl
		projectData.keywords = project.keywords
		projectData.technology = technologiesUsed
		projectData.clientName = project.client.name
		projectData.googlePlayUrl = project.googlePlayUrl == "null"?"":project.googlePlayUrl
		projectData.itunesUrl = project.itunesUrl == "null"?"":project.itunesUrl
		projectData.leadEmail = project.leadEmail == "null"?"":project.leadEmail
		projectData.websiteUrl = project.websiteUrl == "null"?"":project.websiteUrl
		projectData.projectManager = project.team.projectManager == null?"":project.team.projectManager.email
		//projectData.teamLead = project.team.lead.email
		projectData.member = memeberEmail
		projectData.teamLead = leadEmail
		//projectData.projectManager = projectManagerEmail
		if(project.parentProjectId!=0){
			Project parentProject=Project.get(project.parentProjectId)
			projectData.parentName=parentProject.name
			projectData.ParentId=parentProject.id
		}else{
			projectData.ParentId=0;
		}
		projectData.projectId = project.id

		def screenShotsList = project.screenShots
		if (screenShotsList) {
			screenShotsList.each {screensots.add(fileuploadService.getS3FileUrl(CommonConstants.PROJECT_IMAGE,project.s3FolderName,it))	}
		}
		projectData.screenshots = screensots
		if (project.logoUrl && project.logoUrl != "defaultlogo.jpg" && project.logoUrl != 'null') {
			projectData.logoImage = (fileuploadService.getS3FileUrl(CommonConstants.PROJECT_IMAGE,project.s3FolderName, project.logoUrl))
		} else {
			projectData.logoImage = ''
		}
		res.projectData = projectData
		res = dashboardService.generateResponse(res, "success", "Please Edit Project")
		respond res, [formats: ['json']]
	}

	/**
	 * get all projects name which are opened
	 */
	def getAllProjectNames(){
		def query = Project.where {
			name =~ "%"+params.query+"%" && currentStatus == 'Open' && isArchived == false
		}
		def projectList = query.list();
		ArrayList responseList = new ArrayList()
		projectList.each {
			HashMap map = new HashMap()
			map.put("id", it.id)
			map.put("name", it.name)
			responseList.add(map)
		}
		respond responseList, [formats: ['json']]
	}

	def getProjectNames(){
		boolean exactName = false;
		def searchCriteria = Project.createCriteria()
		int skip = 0
		int maxSize = 10
		def projectList = searchCriteria.list(offset:skip, max:maxSize){

			and {
				eq("currentStatus", 'open')
				ilike("name", "%"+params.query+"%")
				eq("isArchived",false)

			}
		}
		ArrayList responseList = new ArrayList()
		projectList.each {
			HashMap map = new HashMap()
			map.put("id", it.id)
			map.put("name", it.name)
			responseList.add(map)
			if(it.name.equalsIgnoreCase(params.query)){
				exactName = true
			}
		}
		if(!exactName){
			HashMap map = new HashMap()
			map.put("id", "new")
			map.put("name", params.query + " (New Project)")
			responseList.add(map)
		}
		respond responseList, [formats: ['json']]
	}

	/**
	 * This action is used for edit an existing project 
	 * Invoked when click on submit button of edit project
	 */
	def editProject() {
		HashMap res = new HashMap()
		User user = springSecurityService.currentUser
		def roles = UserRole.findAllByUser(user)
		boolean flag = false
		roles.each{ UserRole userRole ->
			if(userRole.role.authority == "ROLE_BD" || userRole.role.authority == "ROLE_ADMIN" ||userRole.role.authority == "ROLE_HR" || userRole.role.authority == "ROLE_PMO" ||
				userRole.role.authority == "ROLE_CATALYST" || userRole.role.authority == "ROLE_DEVOPS" ||userRole.role.authority == "ROLE_PMO_QA")
				flag = true
		}
		if(!flag){
			res.errorMsg = "You are not authorized to view this page"
			res = dashboardService.generateResponse(res, "failed", res.errorMsg)
		}
		else{

			JSONObject editProjectData = new JSONObject(request.JSON)


			def project = Project.findByNameAndIsArchivedAndIdNotEqual(editProjectData.projectDetails.projectName,false,editProjectData.projectDetails.projectId)

			if(project){
				res = dashboardService.generateResponse(res, "failed", "Project Name Already Exists")
			}else {
				project = portfolioService.editProject(editProjectData)
				if(project){
					res.id = project.id
					res.projectName = editProjectData.projectDetails.projectName
					res = dashboardService.generateResponse(res, "success", "Project Updated Successfully")
				}else{
					res = dashboardService.generateResponse(res, "failed", "Unable to Update Project, chose correct client from list")
				}
			}
		}
		respond res, [formats: ['json']]
	}

	def saasApplications() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList, projectList: projectList, clients: clients, url: grailsApplication.config.grails.serverURL, categorySelected: params.action]
	}
	def videoStreaming() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList,projectList: projectList,clients: clients,url:grailsApplication.config.grails.serverURL, categorySelected: params.action]
	}
	def mobileApplications() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList,projectList: projectList,clients: clients,url:grailsApplication.config.grails.serverURL, categorySelected: params.action]
	}
	def bigDataAndNoSQL() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList,projectList: projectList,clients: clients,url:grailsApplication.config.grails.serverURL, categorySelected: params.action]
	}
	def blockchainDevelopment() {
		def categoryList =Category.listOrderByPosition()
		def clients = clientManagementService.getClientList();
		def projectList = Project.findAllByEnable(false);
		[categoryList: categoryList,projectList: projectList,clients: clients,url:grailsApplication.config.grails.serverURL, categorySelected: params.action]
	}

	/*
	 * get all team members of particular project
	 */
	def getAllTeamMembersOfProject(){
		//log.debug("get all team members")
		JSONObject jsonObject = new JSONObject(params.jsonData)
		//ArrayList members = portfolioService.getAllProjectTeamMembers(jsonObject)
		long projectId = jsonObject.schedule.project.id
		//ArrayList members = portfolioService.getAllProjectTeamMembers(jsonObject.schedule.project.id)
		ArrayList members = portfolioService.getAllProjectTeamMembers(projectId)
		if(members == null || members.isEmpty())
			return responseObject(members,"failed")
		else
			return responseObject(members,"success")
	}

	def getLeadsAndSupervisiors(){
		log.debug("get lead and supervisior")
		JSONObject jsonObject = new JSONObject(params.jsonData)
		ArrayList members = portfolioService.getLeadsAndSupervisior(jsonObject)
		if(members == null || members.isEmpty())
			return responseObject(members,"failed")
		else
			return responseObject(members,"success")
	}

	def getAllProjectByUser(){
		//log.debug("get all project by user...")
		def user = springSecurityService.currentUser
		ArrayList projectList = new ArrayList()
		String role
		ArrayList responseList
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_PMO,ROLE_CATALYST,ROLE_DEVOPS,ROLE_PMO_QA")){
			projectList = Project.findAllByCurrentStatusAndIsArchived("Open",false)
			role = "ROLE_ADMIN"
			responseList =  portfolioService.getResponseOfProjectWithUserAndRole(projectList, user, role)
		}else {
			projectList = userService.getUserProjects(user)
			def copyProjectList = new ArrayList()
			copyProjectList.addAll(projectList)
			copyProjectList.each {
				log.debug(it.name)
				if(!("Open".equalsIgnoreCase(it.currentStatus))){
					projectList.remove(it)
				}
			}
			log.debug(projectList.size())
			responseList =  portfolioService.getResponseOfProjectWithUserAndRole(projectList, user, role)
		}

		if(responseList.isEmpty())
			return responseObject(responseList, "failed")
		else
			return responseObject(responseList, "success")
	}

	def getAllUser(){
		def qList = params.query.split("[ ]+")
		def query
		def userList
		if(qList.length == 2) {
			userList = User.createCriteria().list(){
				eq("isLock",false)
				or{
					ilike("firstName", "%"+qList[0]+"%")
					ilike("lastName", "%"+qList[1]+"%")
					eq("hasLeft", false)
					ne("username", "Oodles Technologies")
				}
			}
		}else if(qList.length == 1){
			userList = User.createCriteria().list(){
				eq("isLock",false)
				or{
					ilike("firstName", "%"+qList[0]+"%")
					ilike("lastName", "%"+qList[0]+"%")
					ilike("email", "%"+qList[0]+"%")
				}
				eq("hasLeft", false)
				ne("username", "Oodles Technologies")
			}
		}else {
			userList = User.createCriteria().list(){
				eq("isLock",false)
				or{
					ilike("firstName", "%"+params.query+"%")
					ilike("lastName", "%"+params.query+"%")
					like("email", "%"+params.query+"%")
				}
				eq("hasLeft", false)
				ne("username", "Oodles Technologies")
			}
		}

		def responseList = new ArrayList()
		for(user in userList){
			if(user.firstName!=null &&user.lastName!=null)
			{
				HashMap responseUser = new HashMap()
				responseUser.id = user.id
				responseUser.name = user.firstName + " " + user.lastName + " - " + user.email
				responseUser.firstName = user.firstName
				responseUser.lastName = user.lastName
				responseUser.email = user.email
				responseList.add(responseUser)
			}
		}
		respond responseList, [formats: ['json']]
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
}
package com.oodles.security
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.ui.RegistrationCode

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.web.json.JSONObject

import com.google.gdata.client.GoogleService.InvalidCredentialsException
import com.google.gdata.data.appsforyourdomain.generic.GenericEntry
import com.oodles.EmploymentStatus
import com.oodles.MailNotification.MailNotifications;
import com.oodles.mailTracker.*
import com.oodles.project.Project
import com.oodles.resourceAllocation.Employee
import com.oodles.resourceAllocation.OfficeCode
import com.oodles.utils.CommonConstants
import com.oodles.web.googleApi.GoogleManageUsers
import com.oodles.project.ProjectBillable
import com.oodles.resourceAllocation.Billable
import com.oodles.util.MessageAndConstant;
import com.oodles.util.MailTemplateUrls;
import com.oodles.util.InternalConstants;


@Secured(["ROLE_ADMIN", "ROLE_USER", "ROLE_HR", "ROLE_BD", "ROLE_IM"])
class UserController extends grails.plugin.springsecurity.ui.UserController {

	def messageSource;
	def dashboardService
	def groovyPageRenderer
	def fileuploadService1
	def springSecurityService
	def amazonWebService
	def userService
	def fileuploadService
	def oodlesNotificationService
	def oodlesMailService
	def assetService
	def mailTrackerService
	def portfolioService

	final static String profile_Image = "profileImage"
	final String emailFrom=grailsApplication.config.grails.plugin.springsecurity.ui.register.emailFrom
	/*
	 * Search users by auto search
	 */
	def getAllUserNames(){
		/*def userList = User.findAll("from User as u " +
		 "where u.enabled = true and u.hasLeft = false and u.username != 'Oodles Technologies' and u.firstName like :search or u.lastName like :search",
		 [search: "%"+params.query+"%"])*/

		def qList = params.query.split("[ ]+")
		def query

		if(qList.length == 2) {
			query = User.where {
				(firstName=~ "%"+qList[0]+"%" && lastName =~ "%"+ qList[1]+"%") && hasLeft == false && username != 'Oodles Technologies'
			}
		}else if(qList.length == 1){
			query = User.where {
				(lastName =~ "%"+qList[0]+"%" || firstName=~ "%"+qList[0]+"%" || email=~ "%"+qList[0]+"%") && hasLeft == false && firstName != "null" && username != 'Oodles Technologies'
			}
		}else {
			query = User.where {
				(lastName =~ "%"+params.query+"%" || firstName=~ "%"+params.query+"%" || email=~ "%"+params.query+"%") && hasLeft == false && username != 'Oodles Technologies'
			}
		}

		def userList = query.list()
		
		def users = User.findAllByEnabledAndIsLockAndEmailIlike(true, false, "%" + params.query + "%")
		userList = users
		
		def responseList = new ArrayList()
		for(user in userList){
			HashMap responseUser = new HashMap()
			responseUser.id = user.id
			responseUser.name = user.firstName + " " + user.lastName + " - " + user.email
			responseUser.firstName = user.firstName
			responseUser.lastName = user.lastName
			responseUser.email = user.email
			responseList.add(responseUser)
		}
		respond responseList, [formats: ['json']]
	}

	/**
	 * Task is to set the details for send Mail
	 */
	def mailMessage() {
		HashMap res = new HashMap()
		JSONObject jsonData = new JSONObject(params.data)
		def user = User.get(params.id)
		def tomail;
		if (user) {
			def url = grailsApplication.config.grails.serverURL
			def conf = SpringSecurityUtils.securityConfig
			def username = user.firstName + " " + user.lastName
			def message = jsonData.message
			if(user!=null)
				tomail= Arrays.asList(user);
			else
				tomail=null;

			def ccmail= null;
			boolean result=false;
			def body = g.render(template: MailTemplateUrls.DASHBOARD_MESSAGE_BODY, model: [email: username, url: url, message: message]).toString()
			boolean mailSent = oodlesMailService.mailFromOodles(user.email, conf.ui.register.emailFrom, conf.ui.mailMessage.messageSubject, body)
			if (mailSent) {
				User ccmsg=null
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.MAIL_SENT_SUCCESS,messageSource))
				result=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.mailMessage.messageSubject,ccmail)
			} else {
				res = dashboardService.generateResponse(res,  MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.TRY_AFTER_SOME_TIME,messageSource))
				result=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.mailMessage.messageSubject,ccmail)
			}
		} else {
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ID_NOT_EXIST,messageSource))
		}
		respond res, [formats: ['json']]
	}

	def getAllEmploymentStatus(){
		HashMap res = new HashMap()
		res.employmentStatusList = userService.getAllEmploymentStatus();
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource),MessageAndConstant.fetchMessage(MessageAndConstant.LIST_EMPLOY_STATUS,messageSource))
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to send an email notifying user of the completion of Probation period
	 */
	def completeProbation() {
		HashMap res = new HashMap()
		boolean result=false;
		def user = User.get(params.id)
		if (user) {
			def conf = SpringSecurityUtils.securityConfig
			def message = conf.ui.probation.emailtoUser
			String type = user.employmentStatus.getKey()
			if(params.type == InternalConstants.TRAINING)
				message = conf.ui.probation.emailtoUserTrainee

			user.employmentStatus=InternalConstants.REGULAR;
			user.save(flush: true);
			def tomail;
			def supervisormail;
			if(user!=null)
				tomail= Arrays.asList(user)
			else
				tomail=null;

			User supervisor=user.supervisor;
			if(supervisor!=null)
				supervisormail = Arrays.asList(supervisor)
			else
				supervisormail=null;
			def url = grailsApplication.config.grails.serverURL
			log.debug (params.type + message)
			def username = user.firstName + " " + user.lastName
			String userImageUrl = grailsApplication.config.grails.imgURL+user.image
			def body = g.render(template: MailTemplateUrls.DASHBOARD_MESSAGE_BODY, model: [email: username, url: url, message: message, userImg: userImageUrl]).toString()
			boolean mailSent = oodlesMailService.mailFromOodles(user.email,supervisor.email, conf.ui.register.emailFrom, conf.ui.mailMessage.messageSubject, body)
			if (mailSent) {
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.NOTIFICATION_EMAIL_SENT_TO_USER,messageSource))
				result=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.mailMessage.messageSubject,supervisormail)
			} else {
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.TRY_AFTER_SOME_TIME,messageSource))
				result=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.mailMessage.messageSubject,supervisormail)
			}
		} else {
			res = dashboardService.generateResponse(res,MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ID_NOT_EXIST,messageSource))
		}
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to return a list of all users
	 */
	def getAllUser() {
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(params.jsonData)
		def users = userService.getAllUsers(jsonObject)
		def designations = userService.getDesignation(users);
		def usersData
		if(users == null){
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource),MessageAndConstant.fetchMessage(MessageAndConstant.USER_LIST_RETRIEVED_UNSUCCESSFULLY,messageSource))
			res.currentUser = userService.getCurrentUserData()
			res.switchUserURL = grailsApplication.config.grails.spring.switchuserurl
			res.totalCount = 0
			
		}else {
			usersData = userService.getUsersData(users)
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ASSIGNED_PROJECT_SUCCESSFULLY,messageSource))
			
			//res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_LIST_RETRIEVED_UNSUCCESSFULLY,messageSource))
			
			res.usersData = usersData
			res.currentUser = userService.getCurrentUserData()
			res.switchUserURL = grailsApplication.config.grails.spring.switchuserurl
			res.totalCount = users.totalCount
			res.designations = designations
		}
		
		respond res, [formats: ['json']]
	}


	/**
	 *	Task is to delete a user 
	 */
	def deleteUser() {
		HashMap res = new HashMap()
		def user = User.get(params.id)
		println (user.id)
		user.isLock = true
		user.save(flush: true)
		res.result = MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
		respond res, [formats: ['json']]
	}

	/**
	 * Task is to add user to a project(s)
	 *
	 */
	def addUserToProjects(){
		
		Project project = null
		HashMap res = new HashMap()
		res.projectList = new ArrayList()
		JSONObject jsonObject = new JSONObject(params.data)
		
		User user = User.findById(jsonObject.id)
		ProjectBillable projectBillable = new ProjectBillable()
		for(int i=0; i < jsonObject.projectList.length();i++){


			if(jsonObject.projectList[i].id == InternalConstants.NEW){
				jsonObject.projectList[i].name = jsonObject.projectList[i].name.replace(" (New Project)", "")
				project = portfolioService.addProjectByHR(jsonObject.projectList[i].name)
			}

			project = Project.findByNameAndIsArchived(jsonObject.projectList[i].name,false);
			
			projectBillable.billableStatus = Billable.getBillableStatus(jsonObject.billable)
			projectBillable.user = user
			projectBillable.project = project
			if(jsonObject.role ==InternalConstants.MANAGER)
				project.team.projectManager = user;
			else if(jsonObject.role == InternalConstants.LEAD)
				project.team.addToLead(user);
			else
				project.team.addToMember(user)
				
			user.addToProjectList(project)
			project.save(flush: true)
			user.save(flush: true)
			projectBillable.save(flush: true)
			def userProjectMap = [:]
			userProjectMap.projectDetails = project;
			userProjectMap.projectRole = jsonObject.role;
			userProjectMap.billable = jsonObject.billable
			res.projectList.add(userProjectMap)
		}
		/*
		 params.data.each{ data ->
		 println "data: "+(new JSONObject(data)).name
		 def name = (new JSONObject(data)).name 
		 Project project = Project.findByName(name);
		 println ("project: "+project.name)
		 project.team.addToMember(user)
		 }
		 */
		//println (res.projectList)
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ASSIGNED_PROJECT_SUCCESSFULLY,messageSource))
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to remove user from a project
	 *
	 */
	def removeUserFromProject(){
		HashMap res = new HashMap()
		User user = User.findById(params.id)
		Project project = Project.findByNameAndIsArchived(params.name, false);
		ProjectBillable projectBillable = ProjectBillable.findByUserAndProject(user, project)
		if(project.team.projectManager == user)
			project.team.projectManager = null;
		project.team.removeFromMember(user)
		project.team.removeFromLead(user)
		user.removeFromProjectList(project)
		if(projectBillable != null) {
			projectBillable.delete(flush: true)
		}
		project.save(flush: true)
		user.save(flush: true)
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_DELETED,messageSource))
		respond res, [formats: ['json']]
	}

	def addOrEditUserBillable(){
		HashMap res = new HashMap()
		ProjectBillable projectBillable
		User user = User.findById(params.id)
		Project project = Project.findByNameAndIsArchived(params.name, false);
		projectBillable = ProjectBillable.findByUserAndProject(user, project)
		if(projectBillable == null) {
			 projectBillable = new ProjectBillable()
			 projectBillable.billableStatus = Billable.getBillableStatus(params.billable)
			 projectBillable.user = user
			 projectBillable.project = project
			 projectBillable.save(flush: true)
		} else {
				projectBillable.billableStatus = Billable.getBillableStatus(params.billable)
				projectBillable.save(flush: true)
		}
		res = dashboardService.generateResponse(res,MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_BILLABLE_ADDED_TO_PROJECT_SUCCESSFULLY,messageSource))
		respond res, [formats: ['json']]
	}
	
	def replaceManager(){
		HashMap res = new HashMap()
		User user = User.findById(params.id)
		Project project = Project.findByName(params.name);
		User oldManager = project.team.projectManager;
		project.team.projectManager = user;
		project.team.lead?.each {
			if(it.supervisor == oldManager){
				it.supervisor = user;
				it.save(flush:true);
			}
		}
		project.team.member?.each {
			if(it.supervisor == oldManager){
				it.supervisor = user;
				it.save(flush:true);
			}
		}
		
		project.team.removeFromLead(oldManager)
		project.team.removeFromMember(oldManager)
		project.save(flush: true)
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.PROJECT_MANAGER_REPLACED,messageSource))
		respond res, [formats: ['json']]
	}

	def replaceLead(){
		HashMap res = new HashMap()
		User user = User.findById(params.id)
		Project project = Project.findByName(params.name);
		User oldLead =User.findById params.olduserId
		

		project.team.member?.each {
			if(it.supervisor == oldLead){
				it.supervisor = user;
				it.save(flush:true);
			}
		}
		project.team.removeFromLead(oldLead)
		project.team.removeFromMember(oldLead)
		project.save(flush: true)
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.LEAD_REPLACED,messageSource))
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to provide services for edit user password
	 */
	def editUserPassword() {
		JSONObject secure = new JSONObject(params.secure)
		def user = User.findByUsername(springSecurityService.currentUser.username)
		HashMap res = new HashMap()
		def msg = InternalConstants.EMPTY
		def msgClass
		if (user) {
			String encodedOldpassword = springSecurityService.encodePassword(secure.oldPassword)
			boolean result = user.password.equals(encodedOldpassword)
			if (result) {
				String encodednewpassword = springSecurityService.encodePassword(secure.newpassword)
				user.password = encodednewpassword
				user.save(flush: true)
				msg = MessageAndConstant.fetchMessage(MessageAndConstant.PASSWORD_CHANGED,messageSource)
				msgClass = InternalConstants.GREEN
			} else {
				msgClass = InternalConstants.RED
				msg = MessageAndConstant.fetchMessage(MessageAndConstant.ENTER_CORRECT_PASSWORD,messageSource)
			}
			res = dashboardService.generateResponse(res,  MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), msg)
		} else {
			msg = MessageAndConstant.fetchMessage(MessageAndConstant.SESSION_OUT,messageSource)
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), msg)
		}
		res.msgClass = msgClass
		respond res, [formats: ['json']]
	}
	/**
	 * 
	 * Task is to edit user Details
	 */
	def editUserAccountInfo() {
		HashMap res = new HashMap()
		def user = User.get(request.JSON.id)
		if (springSecurityService.currentUser.username == user.username || springSecurityService.currentUser.role == "ROLE_ADMIN") {
			user.firstName = request.JSON.firstName
			user.lastName = request.JSON.lastName
			def userInfo = UserInfo.findByUser(user)
			userInfo.description = request.JSON.description?request.JSON.description : ""
			userInfo.save()
			user.save()
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.RECORD_UPDATED_SUCCESSFULLY,messageSource))
		} else {
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.UNABLE_TO_UPDATE,messageSource))
		}
		respond res, [formats: ['json']]
	}
	/**
	 * USER_DETAILS_RETRIEVED_SUCCESSFULLY
	 * Task is to provide the data for a single user
	 */
	def getSingleUserInfo() {

		HashMap res = new HashMap()
		def user = User.get(params.id)
		res.isSwitch = false
		if (user && (springSecurityService.currentUser.username == user.username || springSecurityService.currentUser.role == "ROLE_ADMIN" || springSecurityService.currentUser.role == "ROLE_HR")) {
			def userInfoDetails = userService.getUserInfoData(user)
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_DETAILS_RETRIEVED_SUCCESSFULLY,messageSource))
			res.supervisor = userService.getUserName(user.supervisor)
			res.userInfoData = userInfoDetails
			if (SpringSecurityUtils.isSwitched() || springSecurityService.currentUser.role == "ROLE_ADMIN") {
				res.isSwitch = true
			}
		} else {
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.UNABLE_TO_GET_DATA,messageSource))
		}
		
		respond res, [formats: ['json']]
	}

	def getUserTreeView(){
		HashMap res = new HashMap()
		//def user = springSecurityService.currentUser;
		def user = User.get(params.id)
		def teamDetails = userService.getUserTeamView(user);
		res.teamView = teamDetails;
		res.supervisor = userService.getUserName(user.supervisor);
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_DETAILS_RETRIEVED_SUCCESSFULLY,messageSource))
		respond res, [formats: ['json']]
	}

	def getTeamMemberInfo() {
		HashMap res = new HashMap()
		def user = User.get(params.id)
		if (user) {
			def userInfoDetails = userService.getUserInfoData(user)
			res = dashboardService.generateResponse(res,MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_DETAILS_RETRIEVED_SUCCESSFULLY,messageSource))
			res.supervisor = userService.getUserName(user.supervisor)
			res.userInfoData = userInfoDetails
		} else {
			res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.UNABLE_TO_GET_DATA,messageSource))
		}
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to find out the list of users that are uninvited  
	 * @return back to js a hashmap of res
	 */
	def unInvitedUserList() {
		String gDomain = params?.email?.split("@")[1]
		String loginIDpasswordError
		String userDomainCheck
		GoogleManageUsers googleManageUsers
		List <GenericEntry> users
		List <String> currentUsersEmailList
		List <Map<String, String>> unInvitedUserList
		def roles
		String isAlreadyInvited = "no"
		try {
			googleManageUsers = new GoogleManageUsers(params.email, params.password, gDomain, "multidomain-api-sample-" + gDomain)
			loginIDpasswordError = "ok"
		} /*catch (GoogleService$InvalidCredentialsException e) */
			catch (Exception e) {
			loginIDpasswordError = "wrong"
		} catch (Exception e) {
			loginIDpasswordError = "Internet"
		}
		if (loginIDpasswordError.equalsIgnoreCase("ok")) {
			try {
				users = googleManageUsers.retrieveAllUsers()
				userDomainCheck = "ok"
			} catch (Exception e) {
				userDomainCheck = "disabled"
			}
			currentUsersEmailList = new ArrayList <String> ()
			List <User> allUsersInSystem = User.findAllByEnabled(true)
			for (User user: allUsersInSystem) {
				currentUsersEmailList.add(user.email)
			}
			unInvitedUserList = userService.getUnInvitedUserList(users, currentUsersEmailList)
			roles = Role.getAll()
		}
		if (unInvitedUserList == null || unInvitedUserList == []) {
			isAlreadyInvited = "yes"
		}
		HashMap res = new HashMap()
		res.unInvitedUserList = unInvitedUserList
		res.roles = roles
		res.STATUS = loginIDpasswordError
		res.domain = userDomainCheck
		res.role = springSecurityService.currentUser?.role
		res.invited = isAlreadyInvited
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to find out the user and create notification for it if any notifiable changes are made
	 * and save the user Details afterwards
	 * if user is invalid simply returns invalid userId
	 * @return
	 */
	def userInfo() {
		SimpleDateFormat joiningDateFormat = new SimpleDateFormat("dd-MM-yyyy")
		SimpleDateFormat releasedDateFormat = new SimpleDateFormat("dd-MM-yyyy")
		Date joiningDate
		Date releasedDate
		JSONObject jsonData = new JSONObject(request.JSON)
		
		if(jsonData.releasedDate){
		releasedDate = releasedDateFormat.parse(jsonData.releasedDate)
		}
		if (jsonData.joiningDate) {
			joiningDate = joiningDateFormat.parse(jsonData.joiningDate)
		}
		HashMap res = new HashMap()
		def userInfo
		if (params.userId) {
			def user = User.get(params?.userId)
			if (user) {
				Employee employee = Employee.findByUser(user)
				if(jsonData.officeCode != ""){

					employee.officeCode = jsonData.officeCode

					employee.save()
				}
				def supervisor = User.findByEmail(jsonData?.supervisor)
				def team = UserTeam.findByTeamType(jsonData?.teamType)
				user.userTeam = team?team:null
				if(user.supervisor?.id != supervisor.id){
					
					sendFeedBackMail(user,supervisor);
					user.supervisor = supervisor?supervisor:null
					oodlesNotificationService.createNotification(user, MessageAndConstant.fetchMessage(MessageAndConstant.SUPERVISOR_CHANGED,messageSource), userService.getUserName(user.supervisor) +MessageAndConstant.fetchMessage(MessageAndConstant.NEW_SUPERVISOR,messageSource))
				}
				user.firstName = jsonData.firstName
				user.lastName = jsonData.lastName
				user.dob = joiningDateFormat.parse(jsonData.dob)

				user.employeeId = jsonData.employeeId
				user.employmentStatus = EmploymentStatus.getEmploymentStatus(jsonData.employmentStatus)
				def project = Project.findByName(jsonData.projectName)
				if (project) {
					project.team.addToMember(user)
					user.currentProject = project;
					project.save(flush: true)
				}

				String s3FolderName = "oodles" + user.username.replaceAll("\\s+", "")
				boolean b=userService.updateUserRole(user.id, jsonData?.rolesList)
				if(b==true) {
					if (jsonData.userImage != null && !jsonData.userImage.contains(grailsApplication.config.grails.amazon.s3URL)) {
						String newfilename = fileuploadService.base64SaveFile(jsonData.userImage, s3FolderName, profile_Image)
						fileuploadService.imageUpload(s3FolderName, user.id, newfilename, profile_Image)
					}
					userInfo = UserInfo.findWhere(user: user)
					if(jsonData.joiningDate!=null && ((new Date()-joiningDate)<90)){
						user.employmentStatusReview = joiningDate+89;
						res.employmentStatusReview = user.employmentStatusReview?new SimpleDateFormat("dd-MM-yyyy").format(user.employmentStatusReview):""
					}
					else{
						res.employmentStatusReview = user.employmentStatusReview?new SimpleDateFormat("dd-MM-yyyy").format(user.employmentStatusReview):""
					}
					def designationValue = UserDesignation.findByDesignation(jsonData?.designation)
					res = userService.setUserInfo(userInfo, res, jsonData, designationValue, joiningDate, releasedDate)
					res.userImage = fileuploadService.getS3FileUrl(CommonConstants.USER_PROFILEIMAGE,"", user.image)
				}
				else {
					res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.SOME_ERROR_OCCURED_TRY_AFTER_SOME_TIME,messageSource))
				}
			}
			else {
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ID_NOT_EXIST,messageSource))
			}
		}
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to toggle approved status of user 
	 * @return the hashmap of res to js
	 */
	def userApproval() {
		HashMap res = new HashMap()
		if (params.userId) {
			def user = User.get(params.userId)
			if (user) {
				user.approved = !user.approved
				user.save(flush: true)
			} else {
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ID_NOT_EXIST,messageSource))
			}
		}
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to update the details of user if lefted  
	 * @return
	 */
	def employeeLeft() {
		HashMap res = new HashMap()
		User user = User.get(params.userId)
		if (user.hasLeft == false) {
			def assetList = assetService.getAssetListByUser(user)
			
			if(!assetList.isEmpty()){
				assetList = assetService.resolveResponseAssetList(assetList)
				res.content = assetList
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ASSIGNED_ASSETS_SUCCESSFULLY,messageSource))
				respond res, [formats: ['json']]
				return
			}

			
		} else {
			user.hasLeft = false
			user.leavingDate = null
			user.save(flush: true)
		}
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.STATUS_CHNAGED_SUCCESSFULLY,messageSource))
		res.hasLeft = user.hasLeft
		res.leavingDate = user.leavingDate
		respond res, [formats: ['json']]
	}
	
	
	
	def employeeLeft1() {
		log.debug(params)
		HashMap res = new HashMap()
		User user = User.get(params.userId)
		log.debug(user.hasLeft)
		if (user.hasLeft == false) {
			def assetList = assetService.getAssetListByUser(user)
			if(!assetList.isEmpty()){
				log.debug("user can't be remove")
				assetList = assetService.resolveResponseAssetList(assetList)
				res.content = assetList
				res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource), MessageAndConstant.fetchMessage(MessageAndConstant.USER_ASSIGNED_ASSETS_SUCCESSFULLY,messageSource))
				respond res, [formats: ['json']]
				return
			}

			//user.hasLeft = true
			//user.enabled = false
			//user.approved = false
			//Date leavingDate = new Date()
			//user.leavingDate = leavingDate
			//user.registerStatus = "TERMINATED"

			//			user.save(flush: true)
			//			if(!user.save(flush: true)){
			//				user.errors.allErrors.each {
			//				}
			//				return null
			//			}
		} else {
			user.hasLeft = false
			user.leavingDate = null
			user.save(flush: true)
		}
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource),MessageAndConstant.fetchMessage(MessageAndConstant.STATUS_CHNAGED_SUCCESSFULLY,messageSource))
		res.hasLeft = user.hasLeft
		res.leavingDate = user.leavingDate
		//	res.approved = user.approved
		//  res.enabled=user.enabled
		//	res.registerStatus=user.registerStatus
		respond res, [formats: ['json']]
	}
	/**
	 * Task is to update register status of user as per id coming from params
	 */
	def inactiveUser() {
		HashMap res = new HashMap()
		User user = User.get(params.userId)
		String statusFilter=params.statusFilter
		String msg
		log.debug("user enabled  "+user.enabled+"  user hasleft  "+user.hasLeft+  "status  "+user.registerStatus+" statusFilter   "+statusFilter);
		if (user.enabled == false && !user.hasLeft) {

			user.enabled = true
			if (user.firstName) {
				user.registerStatus = InternalConstants.ACTIVE
			} else {
				user.registerStatus = InternalConstants.DISABLED
			}
			msg = MessageAndConstant.fetchMessage(MessageAndConstant.USER_ENABLED_SUCCESSFULLY,messageSource)
			user.hasLeft=false
		}else if(user.enabled == false && user.hasLeft) {
			if(statusFilter==InternalConstants.DISABLED) {
				user.enabled = false
				user.registerStatus = InternalConstants.DISABLED
				msg = MessageAndConstant.fetchMessage(MessageAndConstant.USER_DISABLED_SUCCESSFULLY,messageSource)
				user.hasLeft=false
			}else {
				user.enabled = true
				user.registerStatus = InternalConstants.DISABLED
				msg = MessageAndConstant.fetchMessage(MessageAndConstant.USER_ENABLED_SUCCESSFULLY,messageSource)
				user.hasLeft=false
			}
		}
		else {

			user.enabled = false
			user.registerStatus = InternalConstants.DISABLED
			msg = MessageAndConstant.fetchMessage(MessageAndConstant.USER_DISABLED_SUCCESSFULLY,messageSource)
			user.hasLeft=false
		}

		user.save(flush:true);

		if(!user.save(flush: true)){
			user.errors.allErrors.each {
				log.debug("error occurred " + it)
			}
			return null
		}

		

		//res.userId = user.userId
		res.registerStatus = user.registerStatus
		res.enabled = user.enabled
		res.hasLeft = user.hasLeft

		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource), msg)
		respond res, [formats: ['json']]
	}

	def terminDoc() {
		def mailNofication
		HashMap res = new HashMap()
		UserFile userfile=new UserFile();
		List<UserFile>  RecordDetails=userfile.findAllWhere(usersId:params.id,isActive:false)
//		List<UserFile>  RecordDetails = userfile.createCriteria().list(){
//			eq('usersId', params.id)
//		}
		res.RecordDetails = RecordDetails
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource),MessageAndConstant.fetchMessage(MessageAndConstant.USER_DETAILS_RETRIEVED_SUCCESSFULLY,messageSource))
		respond res, [formats: ['json']]
		//	return RecordDetails
	}
	def userDoc() {
		def mailNofication
		HashMap res = new HashMap()
		UserFile userfile=new UserFile();
		List<UserFile>  RecordDetails=userfile.findAllWhere(usersId:params.id,isActive:true)
		def DocTypeList=JoiningDocumentsType.findAll().DocumentType;
		def availableType=[]
		RecordDetails.each{
			availableType.add(it.documentType)
		}
		res.availableTypes=availableType.unique();
		res.RecordDetails = RecordDetails
		res.typeList=DocTypeList
		res = dashboardService.generateResponse(res, MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource),MessageAndConstant.fetchMessage(MessageAndConstant.USER_DETAILS_RETRIEVED_SUCCESSFULLY,messageSource))
		respond res, [formats: ['json']]
	}
		
	
	def getAllDesignationList(){
		def designationList = UserDesignation.findAllByIsValidAndIsDeleted(true, false).designation
		respond designationList, [formats: ['json']]
	}

	
	/**
	 * Task is to return the list of email of all users
	 */
	def getAllUserEmails() {
		def users = User.findAllByEnabledAndIsLockAndRegisterStatus(true, false, 'ACTIVE').email
		respond users, [formats: ['json']]
	}
	/**
	 * Task is to return the user email which are likely and then
	 * @return back to js
	 */
	def findUserEmails() {
		def users = User.findAllByEnabledAndIsLockAndRegisterStatusAndEmailIlike(true, false, 'ACTIVE', "%" + params.query + "%").email
		respond users, [formats: ['json']]
	}
	
	
	/**
	 * Task is to find out name of user and then
	 * @return back to js
	 */
	def getUserNameFromEmail() {
		HashMap res = new HashMap();
		def user = User.findByEmail(params.email)
		res.name = userService.getUserName(user)
		respond res, [formats: ['json']]
	}
	/* upload document of all user*/
	def  allUserDoc(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		User user = User.get(jsonObject.userId)
		def type = jsonObject.docType
		if(jsonObject.user.files.length() > 0){
			ArrayList files = new ArrayList()
				String fileName = jsonObject.user.files[1].name
				String content = jsonObject.user.files[1].content
				content = content.substring(content.indexOf(",")+1)
				boolean upload = false
						upload = fileuploadService.joiningDoc(content,user.id.toString(),fileName)
						UserFile userfile = new UserFile()
						userfile.primaryName = fileName
						userfile.path = user.id.toString() + "/" + fileName
						userfile.usersId =user.id.toString()
						userfile.createdOn = new Date()
						userfile.documentType=type
						userfile.isActive=true
						userfile.save(flush:true)
						res.result=MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
						respond res, [formats: ['json']]
	}
	}
	
	/* upload terminUser documents */
	def uploaddoc(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		User user = User.get(jsonObject.user.id)
		if(jsonObject.user.files.length() > 0){
			ArrayList files = new ArrayList()
			for(int index = 0; index < jsonObject.user.files.length(); index++){
				String fileName = jsonObject.user.files[index].name
				String content = jsonObject.user.files[index].content
				content = content.substring(content.indexOf(",")+1)
				boolean upload = false
				try {
					if((jsonObject.project==null || jsonObject.project.length()==0) && !jsonObject.finalSub) {
						upload = fileuploadService.uploadFile1(content,user.id.toString(),fileName)
						
						if(upload==true){
						UserFile userfile = new UserFile()
						userfile.primaryName = fileName
						userfile.path = user.id.toString() + "/" + fileName
						userfile.usersId =user.id.toString()
						userfile.createdOn = new Date()
						userfile.isActive=false
						userfile.save(flush:true)
						boolean resp=terminUser(user.id, jsonObject.status);
						if(resp==true){
							res.result=MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
						}else{
						res.result=MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource)
						}
					}
					}
						else if(jsonObject.project.length()>0 && jsonObject.finalSub){
						upload = fileuploadService.uploadFile1(content,user.id.toString(),fileName)
						if(upload==true){
						UserFile userfile = new UserFile()
						userfile.primaryName = fileName
						userfile.path = user.id.toString() + "/" + fileName
						userfile.usersId =user.id.toString()
						userfile.createdOn = new Date()
						userfile.isActive=false
						userfile.save(flush:true)
						boolean resp=terminUser(user.id, jsonObject.status);
						if(resp==true){
							res.result=MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
						}else{
						res.result=MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource)
						}
					}
					}
					}
				catch(Exception e){

					log.debug("Exception in uploading : " + e)
				}
			}
		}
		respond res, [formats: ['json']]
	}

	def terminUser(userId,status) {
		boolean termination=false;

//		HashMap res = new HashMap()
		User user = User.findById(userId)
		user.hasLeft = true
		user.enabled = false
		user.approved = false
		Date leavingDate = new Date()
		user.leavingDate = leavingDate
		if(status == InternalConstants.RELEASED){
			user.registerStatus = InternalConstants.RELEASED
		}else{
			user.registerStatus = InternalConstants.TERMINATED
		}
		user.save(flush: true)
		termination=true;
//		res.result="success"
//		respond res, [formats: ['json']]
		return termination;
	}
	

	def terminUser1() {
		boolean termination=false;

		HashMap res = new HashMap()
		User user = User.findById(params.id)
		user.hasLeft = true
		user.enabled = false
		user.approved = false
		Date leavingDate = new Date()
		user.leavingDate = leavingDate
		/*if(status && status == 'RELEASED'){
			user.registerStatus = "RELEASED"
		}else{*/
			user.registerStatus = InternalConstants.TERMINATED
//		}
		user.save(flush: true)
		termination=true;
		res.result=MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
		respond res, [formats: ['json']]
//		return termination;
	}

	def deleteTermination() {
		HashMap res = new HashMap()
		User user = User.findById(params.id)
		log.debug("user-----"+user);
		user.hasLeft=false;
		user.enabled=true;
		user.registerStatus=InternalConstants.ACTIVE;
		user.approved=true;
		user.leavingDate=null;
		user.save(flush:true)
		res.result=MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
		respond res, [formats: ['json']]
	}
	def downloadSingleFile(){
		HashMap res = new HashMap()
		String path=params.path
		String s3Folder = path.substring(0,path.indexOf("/")) //jsonObject.id.toString() 183
		String fileName = path.substring(path.indexOf("/")+1) // eg. "Tooling-Buyer.pdf"

		String url = fileuploadService.downloadFile1(s3Folder, fileName)
		//res.url=url;
		//respond res, [formats: ['json']]
		responseObjects(url,MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource))

	}
	def allUSerFileView(){
		HashMap res = new HashMap()
		String path=params.path
		String s3Folder = path.substring(0,path.indexOf("/")) //jsonObject.id.toString() 183
		String fileName = path.substring(path.indexOf("/")+1) // eg. "Tooling-Buyer.pdf"

		String url = fileuploadService.viewAllUserFiles(s3Folder, fileName)
		//res.url=url;
		//respond res, [formats: ['json']]
		responseObjects(url,MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource))

	}
	def responseObjects(def responseList,String outcome){
		HashMap res = new HashMap()
		if(MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource).equalsIgnoreCase(outcome) && responseList != null){
			res.content = responseList
			res.result = MessageAndConstant.fetchMessage(MessageAndConstant.SUCCESS,messageSource)
		}else {
			res.content = responseList
			res.result = MessageAndConstant.fetchMessage(MessageAndConstant.FAILURE,messageSource)
		}
		respond res, [formats: ['json']]
	}

	
	
	
	
	
	def list() {
		params.max = 10
		[documentInstanceList: Document.list(params), documentInstanceTotal: Document.count()]
	}
	
	
	
	
	
	
	

	def fileUpdation() {
		List<JSONObject> jsonData = new ArrayList<JSONObject>(request.JSON);
		for(JSONObject data :jsonData) {
			boolean upload = false
			String fileid=data.id
			String path=data.path
			String primaryName=data.primaryName
			UserFile userFile =UserFile.findById(fileid);
			String userId=userFile.usersId;
			try {
				if(data.fileContent!=null) {
					String fileName = data.fileContent.name
					String content = data.fileContent.content
					upload = fileuploadService.uploadFile2(content,userId,fileName)
					if(upload) {

						userFile.path=path;
						userFile.primaryName=primaryName
						userFile.updatedOn=new Date();
						userFile.save(flush: true)
					}
				}else {

					userFile.path=path;
					userFile.primaryName=primaryName
					userFile.save(flush: true)
				}
			}catch(Exception e){

				log.debug("Exception in uploading : " + e)
			}
		}
	}

	def joiningFileUpdation() {
		List<JSONObject> jsonData = new ArrayList<JSONObject>(request.JSON);
		for(JSONObject data :jsonData) {
			boolean upload = false
			String fileid=data.id
			String path=data.path
			String primaryName=data.primaryName
			UserFile userFile =UserFile.findById(fileid);
			String userId=userFile.usersId;
			try {
				if(data.fileContent!=null) {
					String fileName = data.fileContent.name
					String content = data.fileContent.content
					upload = fileuploadService.joiningFileUpdateService(content,userId,fileName)
					if(upload) {

						userFile.path=path;
						userFile.primaryName=primaryName
						userFile.updatedOn=new Date();
						userFile.save(flush: true)
					}
				}else {

					userFile.path=path;
					userFile.primaryName=primaryName
					userFile.save(flush: true)
				}
			}catch(Exception e){

				log.debug("Exception in uploading : " + e)
			}
		}
	}

	def userStatus(){
		def supervisorName
		String userEmail
		boolean result = false
		def registrationCode = params.t ? RegistrationCode.findByToken(params.t) : null
		if(registrationCode != null){
			def email = registrationCode.username
			User user = springSecurityService.currentUser;
			if(email == user.email)
		       render (view : MailTemplateUrls.RESOURCE_ALLOCATION_USER_RESPONSE_RESOURCE_ALLOCATION, model: [uemail:params.uemail ,uname: params.uname, sname:params.sname, uImage: params.img ,t:params.t])
			else{
				log.debug("redirect to error page")
				render (view : MailTemplateUrls.LOGIN_DENIED,model:[uname: params.uname])
			}   
		}
		else{
			log.debug ("second time click")
			render (view :MailTemplateUrls.LEAVE_TRACKER_TOKEN_EXPIRED)
		}
	}
	
	
	def submitResponse(){
		
		String uname = params.uname
		User user = User.findByEmail(params.userEmail)
		println user.firstName
		def conf = SpringSecurityUtils.securityConfig
		boolean result=false;
		def registrationCode = params.token ? RegistrationCode.findByToken(params.token) : null
		if(registrationCode != null){
			def userEmail = registrationCode.username
			User immediateSupervisor = User.findByEmail(userEmail)
			def toMail = new ArrayList<User>()  //mail send to HR
			toMail =userService.HrRoleSearch();
			def ccMail  = user.supervisor.email
			def subject = MessageAndConstant.fetchMessage(MessageAndConstant.REQUEST_CHANGES_IN_PROFILE_DASHBOARD,messageSource)
			def html = groovyPageRenderer.render(template: MailTemplateUrls.RESOURCE_ALLOCATION_REQUEST_FOR_CHANGE, model: [ uname: params.uname, comment:params.comment]).toString()
			def body = html
			boolean mailSent = oodlesMailService.mailFromOodles(conf.ui.probation.emailtoHR,ccMail, params.userEmail, subject, body)
			if (mailSent) {
				def regCode = params.token ? RegistrationCode.findByToken(params.token) : null
				regCode = null;
				registrationCode.delete(flush:true)
				//result=oodlesMailService.mailFromOodles( toMail,ccMail,conf.ui.register.emailFrom, subject, body)
				render (view :MailTemplateUrls.RESOURCE_ALLOCATION_FEEDBACK_DONE_FOR_USER_STATUS)
			}else{
			    log.debug ("email sending failed for : " + params.userEmail)
				//result=mailTrackerService.FailedEmailRecord(toMail,params.email,subject,ccMail)
				log.debug("Error! try after sometime")

			}
		}else{
			log.debug ("second time click")
			render (view :MailTemplateUrls.LEAVE_TRACKER_TOKEN_EXPIRED)
		}
	}
	
	
	/**
	 * the task is  to send mail having send feedback button to the old supervisor when supervisor is
	 * changed for an employee
	 * @return
	*/
	
	def sendFeedBackMail(User user,User newSupervisor){
		
			
		def supervisor
		
		String userImageURL
		String userName
		String newSuperVisorMailId
		String newSuperVisorName
		String newSupervisorImageURL
		
		String oldSuperVisorMailId
		String oldSuperVisorName
		String oldSupervisorImageURL
		
		
		if(newSupervisor)
		{
			newSuperVisorMailId=newSupervisor.email
			newSuperVisorName=userService.getUserName(newSupervisor)
			newSupervisorImageURL = grailsApplication.config.grails.imgURL+(newSupervisor.image) ;
		}
		
						 
		
		if(user)
		{
		userName=userService.getUserName(user)
		supervisor=user.supervisor
		userImageURL = grailsApplication.config.grails.imgURL+(user.image) ;
				
		}
		
		
		
		if(supervisor)
		{
		oldSuperVisorMailId=supervisor.email
		oldSuperVisorName=userService.getUserName(supervisor)
		oldSupervisorImageURL = grailsApplication.config.grails.imgURL+(supervisor.image) ;
				
		}
				
		
						
		def regCode = new RegistrationCode(username: oldSuperVisorMailId);
		regCode.save(flush: true);
						
		String feedbackStatusURL = grailsApplication.config.grails.feedbackStatusURL+"true&newSuperVisorMailId="+newSuperVisorMailId+"&oldSuperVisorName="+oldSuperVisorName+"&userName="+userName+"&t="+regCode.token+"&newSupervisorImageURL="+newSupervisorImageURL+"&userImageURL="+userImageURL+"&newSuperVisorName="+newSuperVisorName
		
		String html = groovyPageRenderer.render(template: MailTemplateUrls.FEEDBACK_FEEDBACK_SUBMISSION, model: [oldSupervisorName: oldSuperVisorName,newSupervisorMailId:newSuperVisorMailId ,uname:userName,oldSupervisorImage:oldSupervisorImageURL,Rurl:feedbackStatusURL]).toString()
		
		def body = html
		
									
		if(body)
		{
		boolean mailSent = oodlesMailService.mailFromOodles(oldSuperVisorMailId,emailFrom,MessageAndConstant.fetchMessage(MessageAndConstant.PLEASE_PROVIDE_FEEDBACK_FOR,messageSource)+userName, body)
		}
				
	}
	
	/**
	 * the task is to open feedback form when <send feedback> button is clicked
	 *
	 * @return
	 */
	
	def feedbackStatus(){
				
		def supervisorName
		String userEmail
		boolean result = false
		def registrationCode = params.t ? RegistrationCode.findByToken(params.t) : null
		if(registrationCode != null){
			def email = registrationCode.username
			User user = springSecurityService.currentUser;
			if(email == user.email)
			   render (view : MailTemplateUrls.FEEDBACK_FEEDBACK_INPUT, model: [userName:params.userName,userImageURL:params.userImageURL,newSuperVisorName:params.newSuperVisorName,newSuperVisorMailId:params.newSuperVisorMailId,t:params.t])
			else{
				render (view : MailTemplateUrls.LOGIN_DENIED,model:[uname: params.uname])
			}
		}
		else{
			render (view :MailTemplateUrls.LEAVE_TRACKER_TOKEN_EXPIRED)
		}
	}
	
	/**
	 * the task is to accept the feedback and send it to the new supervisor and render thank you page
	 *
	 */
	def feedbackResponse(){
		
			
		String comments=params.comment
		String userName=params.uname
		
		String newSuperVisorMailId=params.newSuperVisorMailId
				
		String newSuperVisorName=params.newSuperVisorName
		
		String userImageURL=params.userImageURL
	
					
		String html = groovyPageRenderer.render(view: MailTemplateUrls.FEEDBACK_FEEDBACK_MAIL_TEMPLATE, model: [feedback: comments,newSuperVisorName: newSuperVisorName ,uname: userName,userImageURL:userImageURL]).toString()
		
			
		def body = html
												
		boolean mailSent = oodlesMailService.mailFromOodles(newSuperVisorMailId,emailFrom,MessageAndConstant.fetchMessage(MessageAndConstant.FEEDBACK_MAIL_FOR,messageSource)+userName, body)
		
		if (mailSent) {
			
			render (view :MailTemplateUrls.RESOURCE_ALLOCATION_FEEDBACK_DONE_FOR_USER_STATUS)// rendering thank you page
		}
	}
	
	def deleteJoiningDocument(){
		HashMap res = new HashMap()
		UserFile userFile=UserFile.findById(params.id)
		userFile.delete(flush:true);
		res.result="success"
		respond res, [formats: ['json']]
	}
		
	
	def getListForTechInvoke() {
		println("hit got in the get list for tech invoke")
		def users = User.findAllByEnabledAndIsLockAndRegisterStatus(true, false, 'ACTIVE').fullName
		respond users, [formats: ['json']]
	}
}
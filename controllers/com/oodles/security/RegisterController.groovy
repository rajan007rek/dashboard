package com.oodles.security
import grails.converters.*
import com.oodles.mailTracker.*;
import grails.plugin.springsecurity.*
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.ui.*
import org.codehaus.groovy.grails.web.json.*
import com.amazonaws.services.s3.model.*
import com.amazonaws.services.s3.transfer.*
import com.oodles.RegisterStatus
import com.oodles.EmploymentStatus
import static grails.async.Promises.*
import grails.async.Promise
import com.oodles.resourceAllocation.Employee
class RegisterController extends grails.plugin.springsecurity.ui.RegisterController {
	def springSecurityService
	def amazonWebService
	def dashboardService
	def userService
	def fileUploadService
	def oodlesMailService
	def mailTrackerService
	def showAddUserForm() {
		def roleList = Role.findAll()
		render(template: "addUser", model: [command: new UserCommand(), roleList: roleList])
	}
	/**
	 * Called when there is a click on invite from invite people button
	 * Task is to send back response after checking user exist or a new user for verification it calls
	 * sendInviteToUser()
	 */
	def addUser() {
		def res
		res = sendInviteToUser(params)
		respond res, [formats: ['json']]
	}
	/**
	 * this function call when we hit the invite all user button in google User 
	 */
	def inviteAllUser() {
		JSONObject emailList = new JSONObject(params.jsonEmailList)
		HashMap res = new HashMap()
		boolean inviteSend = false
		def inviteSendEmailList = []
		emailList.get("allUninvitedEmailList").each {
			def inviteEmailRoll = [: ]
			inviteEmailRoll.email = it
			inviteEmailRoll.roleAuthority = "ROLE_USER"
			def userRes
			try {
				userRes = sendInviteToUser(inviteEmailRoll)
				inviteSend = true
			} catch (Exception e) {
				inviteSend = false
			}
			if (inviteSend) inviteSendEmailList.add(it)
		}
		res.put("Result", inviteSendEmailList)
		respond res, [formats: ['json']]
	}
	/**
	 * Called from addUser
	 * Task is to check the user exists or not if exist then simply sends message already invited
	 * else send invitation after verifying email
	 * it calls getUserData() of userService to fetch hashmap to return response.
	 */
	private def sendInviteToUser(params) {
		println("......sendInviteToUser called.....")
		boolean result=false;
		HashMap res = new HashMap()
		String lockStatus=""
		if (params.email && params.roleAuthority && params.employmentStatus) {
			User checkEmail = User.findByEmail(params.email)
			if (checkEmail) {
				if (checkEmail.isLock) {
					checkEmail.isLock = false
					checkEmail.save(flush: true)
					lockStatus = ", User Unlocked"
				}
				println("email id "+params.email)
				def emailIdList = getSuggestionList(params.email)
				res.put("suggestions",emailIdList)
				res = checkEmail.enabled ? dashboardService.generateResponse(res, "failed", "Email is already in use" + lockStatus) : dashboardService.generateResponse(res, "invalid", "User Disabled"+ lockStatus)
				println "res------"+res
			} else {
				Role role = Role.findByAuthority(params.roleAuthority)
				def regCode = new RegistrationCode(username: params.email, role: role.authority)
				regCode.save(flush: true)
				String encodedPassword = springSecurityService.encodePassword('P@ssw0rd')
				User user = new User(firstName: params.firstName, lastName: params.lastName, email: params.email, password: encodedPassword, fullName :params.firstName+" "+params.lastName, username: params.email, role: params.roleAuthority, enabled: true)
				user.employmentStatus = EmploymentStatus.getEmploymentStatus(params.employmentStatus)
				user.save(flush: true)
				UserInfo userInfo = new UserInfo(user: user)
				userInfo.save(flush: true)
				
				String username = user.email
				def tomail;
				if(user!=null)
				   tomail=Arrays.asList(user);
				else
				   tomail=null;
				def ccmail = null;
				res.userData = userService.getUserData(user)
				//String url = generateLink('verifyRegistration', [firstName: params.firstName, lastName: params.lastName, t: regCode.token])
				String url = grailsApplication.config.grails.serverURL + "/register/verifyRegistration?firstName="+params.firstName+"&lastName="+params.lastName+"&t=" + regCode.token
				def conf = SpringSecurityUtils.securityConfig
				def body
				boolean mailSent
				//Promise p = task {
					body = g.render(template: "emailBody", model: [firstName: params.firstName, email: params.email, url: url]).toString()
					mailSent = oodlesMailService.mailFromOodles(params.email, conf.ui.register.emailFrom, conf.ui.register.emailSubject, body)
				//}			
				//res = mailSent ? dashboardService.generateResponse(res, "success", "Invite Sent") : dashboardService.generateResponse(res, "failed", "Error! try after sometime")
		        if(mailSent)
				{
				     result=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailSubject,ccmail)
					 res = dashboardService.generateResponse(res, "success", "Invite Sent")
				}else
			    {
					result=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailSubject,ccmail)
					res = dashboardService.generateResponse(res, "success", "Error! try after sometime")
					
			    }
				
			}
		} else {
			res = dashboardService.generateResponse(res, "failed", "Invalid Parameters")
		}
		return res
	}
	
	
	def getSuggestionList(String emailId){
		String[] splitEmailId = emailId.split('@')
		def suggestedEmailId = []
		int count = 1
		while(suggestedEmailId.size()!=4){
			String newEmailId = splitEmailId[0]+(count++)+"@"+splitEmailId[1]
			User checkEmail = User.findByEmail(newEmailId)
			if (checkEmail){
				println newEmailId+" is already exist"
			}
			else{
				suggestedEmailId.add(newEmailId)
			}
		}
		println "suggestedEmailId-----"+suggestedEmailId
		return suggestedEmailId
	}
	/**
	 * Called when there is a click on re-send invite icon from all user page.
	 * Task is to check if the user already registered or a new one.
	 */
	def resendInvite() {
		HashMap res = new HashMap()
		def message, result
		User user = User.get(params.userId)
		def regCode = RegistrationCode.findByUsername(user.email)
		def tomail;
		if(user!=null)
		 tomail=Arrays.asList(user);
	    else
		 tomail=null;
		 def ccmail = null;
		 boolean result1=false;
	   	if (regCode) {
			//String url = generateLink('verifyRegistration', [t: regCode.token])
			String url = grailsApplication.config.grails.serverURL + "/register/verifyRegistration?t=" + regCode.token
			def conf = SpringSecurityUtils.securityConfig
			def body = g.render(template: "emailBody", model: [email: user.email, url: url]).toString()
			boolean mailSent = oodlesMailService.mailFromOodles(user.email, conf.ui.register.emailFrom, conf.ui.register.emailSubject, body)
			if(mailSent)
			{
				res = dashboardService.generateResponse(res, "success", "Invite Sent")
			    result1=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailSubject,ccmail)
				
			}	
			else
				res = dashboardService.generateResponse(res, "failed", "Error! try after sometime")
				result1=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailSubject,ccmail)
		}
		else
			res = dashboardService.generateResponse(res, "failed", "User Already Registered")

		respond res, [formats: ['json']]
	}
	/**
	 * this function call from sendInviteToUser() and resendInvite() function in RegisterController
	 */
	def verifyRegistration() {
		String defaultTargetUrl = grailsApplication.config.grails.successHandler.defaultTargetUrl
		String token = params.t
		ArrayList < String > designationList = new ArrayList < String > ()
		def allDesignationList = UserDesignation.findAllByIsValid(true)
		allDesignationList.each {
			designationList.add(it.designation)
		}
		def allUsernameList = User.list()
		allDesignationList.each {
			designationList.add(it.designation)
		}
		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			render (view : "/leaveTracker/tokenExpired")
		} else {
			render view: 'userRegisterForm', model: [email: registrationCode.username, firstName: params.firstName, lastName: params.lastName, desList: designationList, usernameList: allUsernameList]
		}
	}
	/**
	 * Task is to register a new user.
	 * @param command
	 * @return
	 */
	def registerUser(UserRegisterCommand command) {
		println("....registerUser called....")
		HashMap res = new HashMap()
		def user1=null;
		boolean result=false;
		def url
		if (command.hasErrors()) {
			render view: 'userRegisterForm', model: [command: command]
		} else {
			def user = User.findByEmail(command ?.email)
			def role = Role.findByAuthority(user.role)
			def registrationCode = RegistrationCode.findByUsername(command.email)
			user.username = command ?.username
			String encodedPassword = springSecurityService.encodePassword(command ?.password)
			user.password = encodedPassword
			user.firstName = command ?.firstName
			user.lastName = command ?.lastName
			user.fullName = user.firstName + user.lastName
			user.registerStatus = RegisterStatus.ACTIVE
			new UserRole(user: user, role: role).save(flush: true)
			user.save(flush: true)
			
			Employee employee = Employee.findByUser(user)
			if(employee == null){
				employee = new Employee(user: user)
				employee.save(flush:true)
			}
			
			url = grailsApplication.config.grails.serverURL + "/dashboard"
			def conf = SpringSecurityUtils.securityConfig
			def username = user.firstName
			def tomail;
			if(user!=null)
			   tomail=Arrays.asList(user)
			 else
			   tomail=null
			
			def body = g.render(template: "registeredBody", model: [firstName: username, url: url]).toString()
			boolean mailSent  = oodlesMailService.mailFromOodles(params.email, conf.ui.register.emailFrom, conf.ui.register.emailRegisteredSubject, body)
			
		    if(mailSent)
			{
				result=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailRegisteredSubject,user1)
				
			}else
		    {
				result=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.register.emailRegisteredSubject,user1)
			}
			RegistrationCode.executeUpdate("delete RegistrationCode where id="+registrationCode.id)
		}
		render "You have registered successfully <a href =" + url + ">Click here</a> to login"
	}
	/**
	 * this function call when we select the forgot password from the login page
	 * this function send a mail for reset our password after enter email 
	 */
	def forgotPassword() {
		println ("in register controller")
		boolean result=false;
		HashMap res = new HashMap()
		if (!request.post) {
			return
		}
		
		String username = params.username
		if (!username) {
			flash.error = message(code: 'spring.security.ui.forgotPassword.username.missing')
			redirect action: 'forgotPassword'
			return
		}
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		println ("usernameFieldName : "+usernameFieldName)
		def user = lookupUserClass().findWhere((usernameFieldName): username)
		if (!user) {
			flash.error = message(code: 'spring.security.ui.forgotPassword.user.notFound')
			redirect action: 'forgotPassword'
			return
		}
		def ccmail=null;
		def tomail;
		if(user!=null)
		   tomail=Arrays.asList(user)
		else
		   tomail=null;
		def registrationCode = new RegistrationCode(username: user.
		"$usernameFieldName")
		registrationCode.save(flush: true)
		//String url = generateLink('resetPassword', [t: registrationCode.token])
		def conf = SpringSecurityUtils.securityConfig
		String url = grailsApplication.config.grails.serverURL + "/register/resetPassword?t=" + registrationCode.token
		def body = g.render(template: "passwordEmail", model: [email: username, url: url]).toString()
		boolean mailSent = oodlesMailService.mailFromOodles(user.email, conf.ui.register.emailFrom, conf.ui.forgotPassword.emailSubject, body)
		if(mailSent){
			[emailSent: true]
		   result=mailTrackerService.SaveEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.forgotPassword.emailSubject,ccmail)	
		}else{
		    result=mailTrackerService.FailedEmailRecord(tomail,conf.ui.register.emailFrom,conf.ui.forgotPassword.emailSubject,ccmail)
			[emailSent: false]
		}
	}
	/**
	 * this function call when we click the reset link , which is present in our receiving a mail after forget password
	 * this function update your account password
	 */
	def resetPassword(ResetPasswordCommand command) {
		String token = params.t
		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}
		if (!request.post) {
			return [token: token, command: new ResetPasswordCommand()]
		}
		command.username = registrationCode.username
		String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
		RegistrationCode.withTransaction { status ->
			def user = lookupUserClass().findByEmail(registrationCode.username)
			user.password = springSecurityService.encodePassword(command.password)
			user.save(flush:true)
			registrationCode.delete()
		}
		springSecurityService.reauthenticate registrationCode.username
		flash.message = message(code: 'spring.security.ui.resetPassword.success')
		def conf = SpringSecurityUtils.securityConfig
		String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
		redirect uri: postResetUrl
	}
}
class UserCommand {
	String email
	String password
	String role
	def grailsApplication
	static constraints = {
		email blank: false, nullable: false, email: true
		role nullable: true
	}
}
class UserRegisterCommand {
	String username
	String firstName
	String lastName
	String password
	String email
	String password2
	static constraints = {
		username nullable: true
		firstName nullable: true
		lastName nullable: true
		password blank: false, nullable: false
		password2 blank: false, nullable: false
	}
}
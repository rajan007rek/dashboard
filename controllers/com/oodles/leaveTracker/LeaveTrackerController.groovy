package com.oodles.leaveTracker

import org.codehaus.groovy.grails.web.json.JSONObject;

import java.text.DateFormat
import java.text.SimpleDateFormat;
import java.util.Date;

import grails.plugin.springsecurity.annotation.Secured

import com.oodles.security.*

import grails.plugin.springsecurity.SpringSecurityUtils

import com.oodles.mailTracker.*;

import grails.plugin.springsecurity.ui.*

import com.oodles.security.User
import com.oodles.EmploymentStatus


class LeaveTrackerController {
	
	def leaveTrackerService
	def springSecurityService
	def mailTrackerService
	def oodlesMailService
	def groovyPageRenderer
	def grailsApplication
	def userService
	def attendanceService
	def oodlesNotificationService
	def user
	def sendPushNotificationService
	
	def supervisorCheck(User userA,User userB){
		boolean isSupervisor = false;
		while(userB.id != 1){
			println (userB.email)
			if(userA.email == userB.email){
				isSupervisor = true;
				break
			}
	      userB = userB.supervisor
		}	
		return isSupervisor
	}

	
	@Secured(["ROLE_USER"])
	def userRequestResponse(){
		log.debug("redirecting to Leave Tracker")
		def conf = SpringSecurityUtils.securityConfig
		def registrationCode = params.t ? RegistrationCode.findByToken(params.t) : null
		if(registrationCode != null){
			def email = registrationCode.username
			User immediateSupervisor = User.findByEmail(email)
			User supervisor = springSecurityService.currentUser;
			boolean isAuthorized = supervisorCheck(supervisor,immediateSupervisor)
			log.debug("isAuthorized : "+isAuthorized)
			if(supervisor.id == 1)
				isAuthorized = true;
			if(isAuthorized){ //supervisor check
				def supervisorName = userService.getUserName(supervisor)	
				Requests application = Requests.get(params.id)
				UserRequest userRequest = UserRequest.get(params.uid)
				String type = application.type.getKey()
				def subject = type+" request by "+params.uname
				def user = User.findByImage(params.img)
				String userEmail = user.email
				
				
				def toMail = new ArrayList<User>()
				 toMail =userService.HrRoleSearch();
								
				def ccMail;
				User ccmail=User.findByEmail(user.email)
				if(ccmail!=null)
					ccMail=Arrays.asList(ccmail)
				else
					ccMail=null
				String userImageUrl = grailsApplication.config.grails.imgURL+params.img
				String html = "";
				def decision = ""
				String comments = ""
				boolean result=false;
				log.debug (registrationCode.token);
				if(params.approve == "true"){
					render (view : "/leaveTracker/commentsForApproval", model: [userEmail: userEmail, email: supervisor.email,uname: params.uname, sname: supervisorName, uImage: params.img,type: type,id: application.id,uid: params.uid,t:params.t])
					/*DateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");
					String from = outputFormatter.format(leaveApplication.startDate);
					String to = outputFormatter.format(leaveApplication.endDate);*/
					//oodlesNotificationService.createNotification(user, "Leave Approved",userService.getUserName(user.supervisor)+" has approved your leave request ");
					//println "notification Created"
				}
				else{
					render (view : "/leaveTracker/feedbackComment", model: [userEmail: userEmail, email: supervisor.email,uname: params.uname, sname: supervisorName, uImage: params.img,type: type,id: application.id,uid: params.uid,t:params.t])
					/*DateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");
					String from = outputFormatter.format(leaveApplication.startDate);
					String to = outputFormatter.format(leaveApplication.endDate);*/
					//oodlesNotificationService.createNotification(user, "Leave DisApproved",userService.getUserName(user.supervisor)+" has DisApproved your leave request ");
					//println "notification Created"
				}
			}//end if supervisor check
			else{
				log.debug("redirect to error page")
				render (view : "/onProbation/accessDenied",model:[uname: params.uname])
			}
		}
		else{
			println "**********Second Click***********"
			log.debug ("second time click")
			render (view : "/leaveTracker/tokenExpired")
		}
	}
	/*
	 * approve done
	 */
	def approve(){
		Requests application = Requests.get(params.id)
		UserRequest userRequest = UserRequest.get(params.uid)
		def conf = SpringSecurityUtils.securityConfig
		def subject = params.type+" request by "+params.uname
		String html = "";
		def decision = "";
		def ccMail;
		boolean result=false;
		String comments = "Comments : "+params.comment
		if(params.comment == "")
		 comments = ""
		String userImageUrl = grailsApplication.config.grails.imgURL+params.userImageUrl
		
		def toMail = new ArrayList<User>()
		toMail =userService.HrRoleSearch();
					   
		User ccmail=User.findByEmail(params.userEmail)
		if(ccmail!=null)
			ccMail=Arrays.asList(ccmail)
		 else
			ccMail=null;
		
		User user = User.findByEmail(params.userEmail)
		decision = "approved";
		User supervisor = springSecurityService.currentUser
		def ccSupervisorList = userService.getSupervisorList(user.supervisor)
		ccSupervisorList.add(user.email)
		println ccSupervisorList
		ccSupervisorList.remove(supervisor.email)
		html = groovyPageRenderer.render(template: "/leaveTracker/responseToHR", model: [decision: decision, uname: params.uname, sname: params.sname, uImage: userImageUrl,comment: comments,type: params.type,leave: application]).toString()
		def body = html
		boolean mailSent = oodlesMailService.mailFromOodles( conf.ui.probation.emailtoHR,ccSupervisorList,params.email, subject, body)
		if (mailSent) {
			log.debug("Mail sent successfully by "+params.email)
			DateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");
				String from = outputFormatter.format(application.startDate);
				String to = outputFormatter.format(application.endDate);
			
			oodlesNotificationService.createNotification(user,userService.getUserName(user.supervisor)+" has approved your "+params.type, userService.getUserName(user.supervisor) + " has approved your "+params.type+" from "+from+" to "+to)
			sendPushNotificationService.sendNotification(user,"Leave Approved","Your "+params.type+" has been approved for "+from+" to"+to,"Leave Approved")
			
			def registrationCode = params.t ? RegistrationCode.findByToken(params.t) : null
			application.regCode = null;
			registrationCode.delete(flush:true);
			
			/*if(application.status.getKey() != "Leave Without Approval"){
			
		   switch (params.type) {
			case 'Unpaid Leave'     :   userRequest.unpaidLeaves = userRequest.unpaidLeaves + Float.parseFloat(application.duration);
			                            break
			case 'Paid Leave'       :   userRequest.paidLeaves = userRequest.paidLeaves + Float.parseFloat(application.duration);
						                break
			case 'Sick Leave'       :	userRequest.sickLeaves = userRequest.sickLeaves + Float.parseFloat(application.duration);
						                break
			case 'Casual Leave'     :   userRequest.sickLeaves = userRequest.sickLeaves + Float.parseFloat(application.duration);
						                break	
			case 'Comp Off'	        :	userRequest.compOff = userRequest.compOff + Float.parseFloat(application.duration);
						                break	
			case 'Work from Home'	:   userRequest.workFromHome = userRequest.workFromHome + Float.parseFloat(application.duration);	
			                            break
			case 'Half Day Leave'	:   if(user.employmentStatus == EmploymentStatus.REGULAR){if(userRequest.sickLeaves < userRequest.totalSickLeaves){userRequest.sickLeaves = userRequest.sickLeaves + Float.parseFloat(application.duration)}
										else userRequest.paidLeaves = userRequest.paidLeaves + Float.parseFloat(application.duration);}
										else{userRequest.unpaidLeaves = userRequest.unpaidLeaves + Float.parseFloat(application.duration);}
										break
		     }
			}
		     
		     userRequest.save(flush:true)
		      if(!userRequest.save(flush : true)){
			    userRequest.errors.allErrors.each {
			   }
			return null
		    }*/
			application.status="APPROVED";
			result=mailTrackerService.SaveEmailRecord(toMail,params.email,subject,ccMail)
			render (view : "/site/feedbackdone")
		} else {
			result=mailTrackerService.FailedEmailRecord(toMail,params.email,subject,ccMail)
			log.debug("Error! try after sometime")
		}//end of if-else
		render (view : "/site/feedbackdone")
	}
	/*
	 * disapprove
	 */
	
	def disapprove(){
		Requests application = Requests.get(params.id)
		UserRequest userRequest = UserRequest.get(params.uid)
		def conf = SpringSecurityUtils.securityConfig
		def subject = params.type+" request by "+params.uname
		String html = "";
		def decision = "";
		def ccMail;
		boolean result=false;
		String comments = "Comments : "+params.comment
		String userImageUrl = grailsApplication.config.grails.imgURL+params.userImageUrl
		
		def toMail = new ArrayList<User>()
		toMail =userService.HrRoleSearch();
					   
		User ccmail=User.findByEmail(params.userEmail)
		if(ccmail!=null)
			ccMail=Arrays.asList(ccmail)
		 else
			ccMail=null;
		
		User user = User.findByEmail(params.userEmail)
		decision = "disapproved";
		def ccSupervisorList = userService.getSupervisorList(user.supervisor)
		ccSupervisorList.add(user.email)
		println ccSupervisorList
		html = groovyPageRenderer.render(template: "/leaveTracker/responseToHR", model: [decision: decision, uname: params.uname, sname: params.sname, uImage: userImageUrl,comment: comments,type: params.type,leave: application]).toString()
		def body = html
		boolean mailSent = oodlesMailService.mailFromOodles( conf.ui.probation.emailtoHR,ccSupervisorList,params.email, subject, body)
		if (mailSent) {
			def registrationCode = params.t ? RegistrationCode.findByToken(params.t) : null
			log.debug("Mail sent successfully by "+params.email)
			DateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy");
			String from = outputFormatter.format(application.startDate);
			String to = outputFormatter.format(application.endDate);
			
			oodlesNotificationService.createNotification(user, userService.getUserName(user.supervisor)+" has disapproved your "+params.type, userService.getUserName(user.supervisor) + " has disapproved your "+params.type+" from "+from+" to "+to)
			sendPushNotificationService.sendNotification(user,"Leave Disapproved","Your "+params.type+" has been disapproved for "+from+" to"+to,"Leave Approved")
			
			println "notification sent"
			application.regCode = null;
			registrationCode.delete(flush:true);
			if(application.status.getKey() == "Leave Without Approval" || application.status.getKey() == "Pending"){
				leaveTrackerService.decreaseLeaveCount(userRequest,application,user);
				/*switch (type) {---------------
					case 'Unpaid Leave'     :   userRequest.unpaidLeaves = userRequest.unpaidLeaves - Float.parseFloat(application.duration);
												break
					case 'Paid Leave'       :   userRequest.paidLeaves = userRequest.paidLeaves - Float.parseFloat(application.duration);
												break
					case 'Sick Leave'       :	userRequest.sickLeaves = userRequest.sickLeaves - Float.parseFloat(application.duration);
												break
					case 'Casual Leave'     :   userRequest.sickLeaves = userRequest.sickLeaves - Float.parseFloat(application.duration);
												break
					case 'Comp Off'	        :	userRequest.compOff = userRequest.compOff - Float.parseFloat(application.duration);
												break
					case 'Work from Home'	:   userRequest.workFromHome = userRequest.workFromHome - Float.parseFloat(application.duration);
												break
					case 'Half Day Leave'	:   if(user.employmentStatus == EmploymentStatus.REGULAR){if(userRequest.paidLeaves == 0){userRequest.sickLeaves = userRequest.sickLeaves - Float.parseFloat(application.duration)}
												else userRequest.paidLeaves = userRequest.paidLeaves - Float.parseFloat(application.duration);}
												else{userRequest.unpaidLeaves = userRequest.unpaidLeaves - Float.parseFloat(application.duration);}
												break
				}
				
				userRequest.save(flush:true)
				if(!userRequest.save(flush : true)){
					userRequest.errors.allErrors.each {
					}
					return null
				}*/
				  
				}
			application.status = "DISAPPROVED"
			
			result=mailTrackerService.SaveEmailRecord(toMail,params.email,subject,ccMail)
			render (view : "/site/feedbackdone")
		} else {
			result=mailTrackerService.FailedEmailRecord(toMail,params.email,subject,ccMail)
			log.debug("Error! try after sometime")
		}//end of if-else
		render (view : "/site/feedbackdone")
	}
	
	/*
	 * Add New Request
	 */
	def addLeaveApplication(){
		HashMap res = new HashMap()
		JSONObject jsonObject = new JSONObject(request.JSON)
		SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");

		User user = springSecurityService.currentUser
		def validationData = leaveTrackerService.validateLeaveApplication(jsonObject,user)
		println "ValidationData : "+validationData
		if(validationData.isValidated){
			Requests leaveApplication = leaveTrackerService.resolveLeaveData(jsonObject,null)
			log.debug ("leaveApplication : "+leaveApplication.status+newFormat.format(leaveApplication.endDate)+leaveApplication.reason)
			def data = leaveTrackerService.addLeaveApplication(leaveApplication,user)
			if(data != null){
				return responseObjects(data,"success")
			}else{
				return responseObjects(data,"failed")
			}
		}
		else{
			return responseObjects(validationData.message,"failed")
		}
	}

	/*
	 * Add New CompOff
	 */
    
	def addCompOffApplication(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
		UserCompOff compOffApplication = leaveTrackerService.resolveCompOffData(jsonObject,null)
		User user = springSecurityService.currentUser
		def data = leaveTrackerService.addCompOffApplication(compOffApplication,user)
		if(data != null){
			return responseObjects(data,"success")
		}else{
			return responseObjects(data,"failed")
		}
	}
	
	

	/*
	 * Update Request done
	 */
	def updateRequest(){
		JSONObject jsonObject = new JSONObject(request.JSON)
		Requests application = Requests.get(jsonObject.jsonData.id)
		Requests oldApplication = new Requests();
		oldApplication.type = application.type
		oldApplication.startDate = application.startDate
		oldApplication.endDate = application.endDate
		oldApplication.duration = application.duration
		oldApplication.reason = application.reason
		oldApplication.startDate = application.startDate
		oldApplication.status = application.status
		oldApplication.submitDate = application.submitDate
		oldApplication.regCode = application.regCode
		oldApplication.deductedFrom = application.deductedFrom
		User user = springSecurityService.currentUser
		def validationData = leaveTrackerService.validateLeaveApplication(jsonObject,user)
		println "ValidationData : "+validationData
		if(validationData.isValidated){
			Requests newApplication = leaveTrackerService.resolveLeaveData(jsonObject, application)
			log.debug ("newApplication : "+newApplication)
			application = leaveTrackerService.updateLeaveData(newApplication,oldApplication,user)
			if(application != null){
				return responseObjects(application, "success")
			}else{
				return responseObjects(application, "failed")
			}
		}
		else{
			return responseObjects(validationData.message,"failed")
		}
	}
	
	/*
	 * Update Team Request
	 */
	def updateTeamRequest(){
		println "---------updateTeamRequest----------"
		JSONObject jsonObject = new JSONObject(request.JSON)
		println (jsonObject)
		Requests application = Requests.get(jsonObject.jsonData.id)
		def oldStatus = application.status.getKey()
		println ("oldStatus : "+oldStatus)
		
		User user = User.get(jsonObject.jsonData.userId)
		println(jsonObject.jsonData.userId)
		
		log.debug ("application1 : "+application)
		application = leaveTrackerService.updateTeamLeaveData(jsonObject, application)
		log.debug ("application2 : "+application)
		User supervisor = springSecurityService.currentUser
		def userLeaveData = leaveTrackerService.updateUserLeaveData(user,application,jsonObject,oldStatus,supervisor)
		log.debug ("userLeaveData : "+userLeaveData)
		def resp = [:]
		resp.application = application
		resp.userLeaveData = userLeaveData
		if(userLeaveData != null){
			//leaveTrackerService.updateMailNotification(application, jsonObject)
			return responseObjects(resp, "success")
		}else{
			return responseObjects(resp, "failed")
		}
	}
	
	/*
	 * Delete Request
	 */
	def deleteRequest(){
		println "delete Request in Controller called";
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		Requests application = Requests.get(jsonObject.id)
		User user = User.get(jsonObject.userId)
		log.debug("application " + application)
		User currentUser = springSecurityService.currentUser
		if(application != null){
			application = leaveTrackerService.deleteLeaveApplication(user,application,currentUser)
			log.debug(application)
			def returnMap=[:]
			returnMap.id = application.id
			returnMap.status = application.status?.getKey()
			return responseObjects(returnMap, "success")
		}else{
			return responseObjects(application, "failed")
		}
			
	}
	
	/*
	 * Delete Team Request
	 */
	def deleteTeamRequest(){
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		println("jsonObject=="+jsonObject)
		Requests application = Requests.get(jsonObject.id)
		println("sapp="+application)
		User user = User.get(jsonObject.userId)
		log.debug("application " + application)
		User supervisor = springSecurityService.currentUser
		if(application != null){
			application = leaveTrackerService.deleteTeamLeaveApplication(user,application,jsonObject,supervisor)
			log.debug(application)
			def returnMap=[:]
			returnMap.id = application.id
			returnMap.status = application.status?.getKey()
			returnMap.endDate = new SimpleDateFormat("dd-MM-yyyy").format(application.endDate).toString()
			return responseObjects(returnMap, "success")
		}else{
			return responseObjects(application, "failed")
		}
			
	}
	
	/*
	 * Delete Team CompOff
	 */
	def deleteTeamCompOff(){
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		UserCompOff application = UserCompOff.get(jsonObject.id)
		User user =User.get(jsonObject.user)
		if(application != null){
			application = leaveTrackerService.deleteTeamCompOffApplication(user,application,jsonObject)
			def returnMap=[:]
			returnMap.id = application.id
			return responseObjects(returnMap, "success")
		}else{
			return responseObjects(application, "failed")
		}
			
	}
	
	def getUserRequestList(){
		Date currentDate = new Date()
		def year = params.year
		User user = springSecurityService.currentUser
		def data = [:]
		boolean isAdmin = false;
		boolean isHr = false;
		if(user?.role == "ROLE_ADMIN" ||user?.role == "ROLE_HR"){
		  isAdmin = true;
		  isHr = true;
		}
		boolean hasTeam;
		def teamList = userService.getAllTeamMembers(springSecurityService.currentUser)
		println "teamList--"+teamList
		if(teamList!=[]){
			hasTeam = true;
		}
		else{
			hasTeam = false;
		}
		data.employmentStatus = user.employmentStatus.getKey();
		data.userRequestDetails = leaveTrackerService.getUserRequestList(user,year)
		data.teamRequestList = leaveTrackerService.getTeamRequestList(user,year)
		data.isAdmin = isAdmin;
		data.hasTeam = hasTeam;
		data.isHr = isHr;
		if(data != null)
		   return responseObjects(data,"success")
		else
		   return responseObjects(data,"failed") 
	}
	
	def getUserCompOffList(){
		Date currentDate = new Date()
		def halfYearAndYear = leaveTrackerService.getHalfYearAndYear(currentDate)
		Integer month = currentDate.month + 1
		def halfYear = halfYearAndYear.halfYear
		def year = String.valueOf(halfYearAndYear.year)
		User user = springSecurityService.currentUser
		def data = [:]
		data.teamCompOffList = leaveTrackerService.getTeamCompOffList(user,year)
		if(data != null)
			{
		   return responseObjects(data,"success")
			}
		else
		   return responseObjects(data,"failed")
	}
	/*
	 * get all Status of Leave Application
	 */
	def getAllLeaveStatus(){
		def leaveStatusList = new ArrayList()
		LeaveStatus.getAllLeaveStatus().each {
			leaveStatusList.add(it.getKey())
		}
		if(leaveStatusList == null || leaveStatusList.empty){
			return responseObjects(leaveStatusList,"failed")
		}
		return responseObjects(leaveStatusList,"success")
	}
	
	def getAllLeaveType(){
		def leaveTypeList = new ArrayList()
		LeaveType.getAllLeaveType().each {
			println (it.getKey())
			leaveTypeList.add(it.getKey())
		}
		if(leaveTypeList == null || leaveTypeList.empty){
			return responseObjects(leaveTypeList,"failed")
		}
		return responseObjects(leaveTypeList,"success")
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
	
}

package com.oodles.leaveTracker

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.plugin.springsecurity.SpringSecurityUtils

class AttendanceController {

	def attendanceService
	def grailsApplication
	
	/**
	 * @author karanbhardwaj
	 * @return
	 */
	def saveAttendanceRecords(){
		
		def ipList = grailsApplication.config.grails.attendanceServer.IP
		//if(!ipList.contains(request.getRemoteAddr())) return responseObjects("Invalid IP",false)
		if(params.file != null){
			HashMap resp = attendanceService.saveAttendanceSheet(params.file)
			return responseObjects(resp.get("DATA"),resp.get("isSuccess"))
		}
		else return responseObjects("Please enter valid input data",false)
	}
	
	/**
	 * @author karanbhardwaj
	 * @return
	 */
	def getUserPunchInDetails(){
		if(params.month != null && params.year !=null && params.empId != null) responseObjects(attendanceService.getUserPunchInsDetails(params.month,params.year,params.empId),true)	
		else responseObjects("Please enter valid input data",false)
	}
	
	/**
	 * @author karanbhardwaj
	 * @return
	 */
	def getHrPunchInDetails(){
		println (params)
		JSONObject jsonObject = new JSONObject(params.jsonData)
		if(jsonObject.from != null && jsonObject.to !=null) responseObjects(attendanceService.getUserPunchInsDetailsForHR(jsonObject),true)
		else responseObjects("Please enter valid input data",false)
	}
	
	/**
	 * @author karanbhardwaj
	 * @return
	 */
	def editPunchInDetails(){
		log.debug("requestdetails: "+request.JSON)
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")){
		   if(request.JSON != null) {
			     def punchInDetails = new JSONObject(request.JSON.jsonData);
			     log.debug("editDetails : "+punchInDetails);
			     responseObjects(attendanceService.editAttendanceDetails(punchInDetails),true)
		       }
		   else responseObjects("Please enter valid input data",false)
		}
		else responseObjects("Unauthorized action. An alert message has been sent to the HR",false)
	}
	
	def getUserRequestListforHR(){
		println (params)
		JSONObject jsonObject = new JSONObject(params.jsonData)
		println jsonObject
		if(params.month != null && params.year !=null) {
			responseObjects(attendanceService.getUserRequestListforHR(params.month,params.year,jsonObject),true)
		}
		else responseObjects("Please enter valid input data",false)
	}
	
	/**
	 * @author karanbhardwaj
	 * @param responseList
	 * @param isSuccess
	 * @return
	 */
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
}

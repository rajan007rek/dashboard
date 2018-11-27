package com.oodles.interview

import com.oodles.security.User
import org.codehaus.groovy.grails.web.json.JSONObject
import com.oodles.resourceAllocation.Demand

class InterviewController {
	
	def interviewService
	def dashboardService
	
	def addInterviewInfo(){
		
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		
		Interview interview = interviewService.resolveInterviewData(jsonObject,null) 
		
		if(jsonObject.demandId!="")
		{
            println("in demand if")
			def demand = Demand.findById(jsonObject.demandId)
			demand.addToInterview(interview)
			demand.save(flush:true)
		}
		else{
		interview = interviewService.addInterviewInfo(interview)
		}
		if(interview != null){
			HashMap map = interviewService.resolveResponseInterview(interview)
			return responseObjects(map,"success")
		}else{
			return responseObjects(interview,"failed")
		}
	}

	def responseObjects(def responseList,String outcome){
		HashMap res = new HashMap()
		if("success".equalsIgnoreCase(outcome) && responseList != null){
			res.content = responseList
			res.result = "success"
		}else {
			res.content = responseList
			res.result = outcome
		}
		respond res, [formats: ['json']]
	}
	
	def updateInterviewInfo(){
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		Interview interview = Interview.findById(jsonObject.id)
		println (interview)
		if(interview==null)
		return responseObjects(jsonObject, "not exist")
		interview = interviewService.resolveInterviewData(jsonObject, interview)
		interview = interviewService.addInterviewInfo(interview)
		if(interview != null){
			HashMap map = interviewService.resolveResponseInterview(interview)
			return responseObjects(map,"success")
		}else{
			return responseObjects(interview,"failed")
		}
	}
	
	def getAllInterviewInfo(){
		JSONObject jsonObject = new JSONObject(params.data)
		int skip = jsonObject.offset
		int limit = jsonObject.maximum instanceof String ? Integer.parseInt(jsonObject.maximum) : jsonObject.maximum
		def demandId = jsonObject.demandId
		
		def interviewList
		if(demandId!="")
		{
			def demand = Demand.get(demandId)
		    def query = demand.interview.findAll { it.isDeleted == false } 
			interviewList = query.asList();
					
		}
		else{
		def query = Interview.where {
			isDeleted == false
		}
		 interviewList = query.list();
		}
		HashMap map = new HashMap()
		int totalInterviewFound = 0
		println("interviewList"+interviewList)
		if(interviewList == null){
			map = interviewService.resolveInterviewCount(null,totalInterviewFound)
			return responseObjects(map,"failed")
		}
		totalInterviewFound = interviewList.size()
		if(limit != 0){
			if(interviewList.size() < (skip+limit)){
				interviewList = interviewService.resolveResponseInterviewList(interviewList.subList(skip, interviewList.size()))
			}else {
				interviewList = interviewService.resolveResponseInterviewList(interviewList.subList(skip,(skip+limit)))
			}
		}else{
			interviewList = interviewService.resolveResponseInterviewList(interviewList)
		}
		map = interviewService.resolveInterviewCount(interviewList,totalInterviewFound)
		return responseObjects(map,"success")
	
	}
	
	def getAllInterviewStatus(){
		ArrayList statusList = new ArrayList()
		Status.getAllStatus().each {
			statusList.add(it.getKey())
		}
		if(statusList == null || statusList.empty){
			return responseObjects(statusList,"failed")
		}
		return responseObjects(statusList,"success")
	}
	
	def addInterviewerDetails(){
		
		HashMap res = new HashMap()
		res.interviewerListData = new ArrayList()
		JSONObject jsonObject = new JSONObject(params.data)
		Interview interview = Interview.findById(jsonObject.id)
   		for(int i=0; i < jsonObject.interviewerList.length();i++){

			User user = User.findByEmail(jsonObject.interviewerList[i].email);
			println (user.email);
			
			Interviewer interviewer = new Interviewer()
			interviewer.user = user
			interview.addToInterviewer(interviewer)
			interview.save(flush: true)
			println("interviewer  "+interview.interviewer.id)		
			def userProjectMap = [:]
			userProjectMap.interviewerId = interviewer.id
			userProjectMap.interviewerName = user.firstName+user.lastName
			userProjectMap.points = interviewer.points
			userProjectMap.comments = interviewer.comments
			userProjectMap.roundTaken = interviewer.roundTaken
			res.interviewerListData.add(userProjectMap)
	
		}
		res = dashboardService.generateResponse(res, "success", "Interviewer assigned to interview successfully.")
		respond res, [formats: ['json']]
	}
	
	def getInterviewerInfo() {
		
		HashMap res = new HashMap()
		res.interviewerListData = new ArrayList()
		
		def interview = Interview.get(params.id)
		def interviewerList = []
		interviewerList = interview.interviewer
		interviewerList.each {
		def userProjectMap = [:]
		println(it.user.firstName)
		println(it.id)
		userProjectMap.interviewerId = it.id
		userProjectMap.interviewerName = it.user.firstName+it.user.lastName
		userProjectMap.points = it.points
		userProjectMap.comments = it.comments
		userProjectMap.roundTaken = it.roundTaken
		res.interviewerListData.add(userProjectMap)
		}
		res = dashboardService.generateResponse(res, "success", "Interviewer assigned to interview successfully.")
		respond res, [formats: ['json']]
	}
	
	def getInterviewDetails() {
		
		println("in get interview details")
		HashMap res = new HashMap()
		def userProjectMap = [:]
		def interview = Interview.get(params.id)
		res = interviewService.resolveResponseInterview(interview)
		return responseObjects(res,"success")
	}
	
	def updateInterviewer() {
		HashMap res = new HashMap()
		
		JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		println(jsonObject)
		Interview interview = Interview.findById(jsonObject.id)
		
		 	  def interviewerList = []
			  interviewerList = interview.interviewer	
			    
			  interviewerList.each{
				  if(it.id==jsonObject.interviewer.interviewerId)
				  {
					  Interviewer interviewer = it
					  interviewer.points = jsonObject.interviewer.points
					  interviewer.comments = jsonObject.interviewer.comments
					  interviewer.roundTaken = jsonObject.interviewer.roundTaken
					  interview.addToInterviewer(interviewer)
					  
					  interview = interviewService.addInterviewInfo(interview)
					  if(interview != null)
					  {
						  res.put("interviewerId" , it.id)
						  res.put("points", interviewer.points)
						  res.put("comments", interviewer.comments)
						  res.put("roundTaken", interviewer.roundTaken)
						 res = dashboardService.generateResponse(res, "success", "Interviewer assigned to interview successfully.")
		                 respond res, [formats: ['json']]
					  }
				  }
			  }
		}
	
	  def deleteInterview() {
	   HashMap res = new HashMap()
	   def interview = Interview.get(params.id)
	   println (interview.id)
	   interview.isDeleted = true
	   interview.save(flush: true)
	   res.result = "success"
	   respond res, [formats: ['json']]
   }
	  
	  def deleteInterviewer() {
		  HashMap res = new HashMap()
		  JSONObject jsonObject = new JSONObject(request.JSON.jsonData)
		  println("lllll"+jsonObject.id)
		  def result = Interview.executeQuery(
			  "select r from Interview i JOIN i.interviewer r where i.id = :interviewId and r.id = :id" , [id:(Long)jsonObject.id, interviewId:(Long)jsonObject.interviewId])
		  
		 if(result != null)
		  {
		  def interview = Interview.get(jsonObject.interviewId)
		  Interviewer interviewer = (Interviewer) result.get(0)
		  
		 def resultt =  interview.removeFromInterviewer(interviewer)
		 interview.save(flush: true)
		
		  res.result = "success"
		  respond res, [formats: ['json']]
		  }
	  }
}

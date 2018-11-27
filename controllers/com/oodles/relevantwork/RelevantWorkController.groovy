package com.oodles.relevantwork
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import com.oodles.relevantWork.Page
import com.oodles.relevantWork.RelevantWork
import com.oodles.security.User
import com.oodles.utils.Status



class RelevantWorkController {
	
	def springSecurityService
	def relevantWorkService
	def dashboardService

	def index() {
		
	}

	def underConstruction() {
		render (view : "/underConstruction")
	}

	def showRelevantWork() {
		[message:flash.message]
		//		def recentRelevantWorks = RelevantWork.findAllByLocked(true,[max:5, cache:true, offset:params.offset, order:"desc", sort:"dateCreated"])
		//		[recentRelevantWorks:recentRelevantWorks,totalRelevantWorks:recentRelevantWorks.size]
	}
	
	def createRelevantWork(){ 
		RelevantWork relevantWork = session.entry?:new RelevantWork()
		def pages = Page.list();
		[pages:pages,entry:  relevantWork]
	}
	
	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def submitRelevantWork() 
	{
		session.entry = null
		log.debug("submitRelevantWork")
		RelevantWork relevantWork = params.id? RelevantWork.get(params.id): new RelevantWork()
		relevantWorkService.saveRelevantWork(params,relevantWork)
		if(relevantWork.hasErrors()){
			session.entry = relevantWork
		    redirect (action: "createRelevantWork")
		}
		else{
			redirect(action: "showCreated", params: [title: relevantWork.url])
		}
	}
	
	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def showCreated()
	{
		def relevantWorks = RelevantWork.findByTitle(params.title.toString().replaceAll("-", " "))
		relevantWorks ?
				render (view: "/relevantWork/show", model: [entry: relevantWorks,  username: springSecurityService.currentUser?.username]) 
				: {response.sendError 404}
	}
	
	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def editEntry()
	{	
		def relevantWork = RelevantWork.get(params.id)
		def pages = Page.list();
		relevantWork? render( view: "/relevantWork/createRelevantWork", model: [entry: relevantWork,pages:pages,message:flash.message],params:[:])
					: response.sendError( 404)
	}

	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def deleteRelevantWork()
	{	
		if(params.id)
		{
			def noOfRowDeleted = RelevantWork.executeUpdate("delete from RelevantWork where id= :id",[id:params.id as long]);
			if(noOfRowDeleted>0){
				flash.message = "Page Deleted Successfully"
				redirect controller : "relevantwork"
			}
			else{
				flash.message = "Page does not exists!"
				redirect uri:request.getHeader("Referer")
			}
		}
		else if(params.relevantWorkId)
		{
			def noOfRowDeleted = RelevantWork.executeUpdate("delete from RelevantWork where id= :id",[id:params.relevantWorkId as long]);
			HashMap res = new HashMap()
			res = noOfRowDeleted > 0 ?
					dashboardService.generateResponse(res, "success", "Relevant Work deleted successfully") :
					dashboardService.generateResponse(res, "failed", "Something went wrong please try again later!!!")
			respond res, [formats: ['json']]
		}
	}

	/*def relevantWorksList()
	{
		def offset = Integer.parseInt(params.start) * 5
		render template:"/site/blogs",model:[recentBlogs:RelevantWork.findAllByLocked(true,[max:5, cache:true, offset:offset, order:"desc", sort:"dateCreated"])]
	}*/

	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def getAllRelevantWork()
	{
		HashMap res = new HashMap()
		def relevantWorks = RelevantWork.list()
		res.isAdmin = true
		def relevantWorksData = relevantWorkService.getRelevantWorksData(relevantWorks)
		res.relevantWorksData = relevantWorksData
		respond res, [formats: ['json']]
	}

	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def changeRelevantWorkStatus()
	{
		HashMap res = new HashMap()
		RelevantWork relevantWork = RelevantWork.get(Long.parseLong(params.id))
		if(relevantWork){
			relevantWork.isActive = !relevantWork.isActive
			relevantWork.save(flush: true)
			res = dashboardService.generateResponse(res, "success", "status changed successfully")
		}
		else{
			res = dashboardService.generateResponse(res, "failed", "Relevant Work not found!")
		}
		respond res, [formats: ['json']]
	}
}
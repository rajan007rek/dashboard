package com.oodles.announcement
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils

import com.oodles.blog.TitleUrl
import com.oodles.security.User
import com.oodles.utils.Status
class AnnouncementController {
	def springSecurityService
	def announcementService
	def dashboardService
	def userService
	def oodlesNotificationService
	
	def index() { }
	
	def underConstruction()
	{
		 render (view : "/underConstruction")
	}
	
	def showAnnouncements()
	{	
		def recentAnnouncements = Announcement.findAllByLocked(true,[max:5, cache:true, offset:params.offset, order:"desc", sort:"dateCreated"])
		[recentAnnouncements:recentAnnouncements,totalAnnouncements:recentAnnouncements.size]
	}
	
	def createAnnouncement(){ }
	
	def submitAnnouncement()
	{
		log.debug("tags "+params.tags)
		String errorMessage
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD"))
		{
			switch(announcementService.checkConstraints(params))
			{
				case "emptyTitle" 	: 	errorMessage = "Please Fill Title!!!"
										break
				case "existingTitle":	errorMessage = "Title Already Exist!!!"
										break		
			}		
			def announcement = Announcement.get(params.id)?: new Announcement()
			
			if(errorMessage)
			{	
				render view: "/announcement/createAnnouncement", model: [entry: announcement, errorMessage: errorMessage]
				return
			}
			else
			{
				boolean isSaved = announcementService.saveAnnouncement(params,announcement)
				log.debug(isSaved)
				try{
					if(params.tags.size() != 0) {
						announcement.parseTags(params.tags, ",")
						log.debug("announcement tags "+announcement.tags)
					}
				}catch (Exception e){
				}
				def finalTagList = announcementService.getAllTags()
				log.debug("finalTaglist "+ finalTagList)
				isSaved ?
				redirect(action: "showCreated", params: [title: TitleUrl.urlTitleValue(announcement)]) :
				//render (view: "/announcement/createAnnouncement", model: [entry: announcement, finalTagList: finalTagList, errorMessage: "Error while saving Data"])
				  render (view: "/announcement/createAnnouncement", model: [entry:  announcement, finalTagList: finalTagList])
				}
			
		}
	}
	
	def showCreated()
	{	
		def announcements = Announcement.findByTitle(params.title.toString().replaceAll("-", " "))
		announcements ?
		render (view: "/announcement/show", model: [entry: announcements,  currentuser:springSecurityService.currentUser, username: springSecurityService.currentUser?.username]) :
		{response.sendError 404}
	}
	
	def removeTags() {
		log.debug("inController "+params)
		def announcement = Announcement.get(params.id)
		def removeTag = params.removeTag
		
		if(announcement)
		{
			def tagList = announcement.tags;
			log.debug("before "+announcement.tags)
			tagList.remove(removeTag)
			
			announcement.tags = tagList
			log.debug("after "+announcement.tags)
			announcement.save(flush:true)
			
			
			render tagList as JSON
		 }
		else {
			response.sendError 404
		}
	}
	
	def editEntry() 
	{
		def announcement = Announcement.get(params.id)
		if(announcement)
		{
			def tagArrayList = new ArrayList()
			for(def tag: announcement.tags) {
				def listString = '"' + tag + '"'
				
				tagArrayList.add(listString)
				
			}
			log.debug("tagArrayList "+tagArrayList)
			if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || springSecurityService.currentUser.username == announcement.author)
			{
				render view: "/announcement/createAnnouncement", model: [entry: announcement,tagArrayList:tagArrayList]
				return
			}
		}
		response.sendError 404
	}
	
	def deleteAnnouncement()
	{
		if(params.id)
		{
			boolean isDeleted = Announcement.get(params.id).delete(flush:true)
			redirect action : "showAnnouncements"
		}
		else if(params.announcementId)
		{
			HashMap res = new HashMap()
			boolean isDeleted=Announcement.get(params.announcementId).delete(flush:true)
			res = isDeleted ? 
			dashboardService.generateResponse(res, "success", "Announcement deleted successfully") :
			dashboardService.generateResponse(res, "failed", "Something went wrong please try again later!!!")
			respond res, [formats: ['json']]
		}
	}
	
	def announcementsList() 
	{
		def offset = Integer.parseInt(params.start) * 5
		render template:"/site/blogs",model:[recentBlogs:Announcement.findAllByLocked(true,[max:5, cache:true, offset:offset, order:"desc", sort:"dateCreated"])]
	}
	
	def getAllAnnouncement() 
	{
		HashMap res = new HashMap()
		def announcements
		if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD")) 
		{
			announcements = Announcement.findAllByLocked(true)
			res.isAdmin = true
			def announcementsData = announcementService.getAnnouncementsData(announcements)
			res.announcementsData = announcementsData
		} 
		else
		{
			res = dashboardService.generateResponse(res, "failed", "Not Authorized to View This Page")
		}
		respond res, [formats: ['json']]
	}
	
	def changeAnnouncementStatus()
	{
		HashMap res = new HashMap()
		Announcement announcement = Announcement.get(Long.parseLong(params.id))
		def user = User.findByUsername(announcement.author)
		if (announcement.status.getKey().equals("Published")) 
		{
			announcement.status = Status.getStatus("Pending")
			announcement.published = false
			announcement.isProtected = false
			String name = userService.getUserName(springSecurityService.currentUser)
			oodlesNotificationService.createNotification(user,"Your Announcement "+ announcement.title +" has been Published","Your Announcement"+announcement.title +" has been Published By "+name)
		} 
		else if (announcement.status.getKey().equals("Pending")) 
		{
			announcement.status = Status.getStatus("Published")
			announcement.published = true
			announcement.isProtected = true
		} 
		announcement.save(flush: true)
		res = dashboardService.generateResponse(res, "success", "status changed successfully")
		respond res, [formats: ['json']]
	}
	
}

package com.oodles.blog
import grails.converters.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import groovy.sql.Sql

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.web.json.JSONObject
import org.grails.taggable.TagException
import org.xml.xas.*

import com.amazonaws.services.s3.model.*
import com.oodles.categories.*
import com.oodles.mailTracker.*
import com.oodles.performance.PagePerfomanceTime
import com.oodles.security.*
import static grails.async.Promises.*
import grails.async.Promise

class BlogController extends org.grails.blog.BlogController {
	
	def springSecurityService
	def simpleCaptchaService
	def memcachedService
	def dashboardService
	def blogsService
	def userService
	def oodlesNotificationService
	def oodlesMailService
	def sessionFactory
	def dataSource
	def mailTrackerService
	def groovyPageRenderer
	def fileuploadService
	
	
	
	final static String feedback_Image = "feedbackImage"
	def conf = SpringSecurityUtils.securityConfig
	/**
	 * This action returns a blog create from to create blog.
	 */
	@Secured(["ROLE_USER"])
	def createEntry() {
		def entry = new OodlesBlogEntry(request.method == 'POST' ? params['entry'] : [: ])
		def blogStatus = new BlogStatus()
		def finalTagList = blogsService.getTagList()
		def temptaglist =setTagtoArray()
		
		def recentEntries = OodlesBlogEntry.findAllByPublishedAndLocked(true, false, [max: 10, cache: true, offset: params.offset, order: "desc", sort: "dateCreated"])
		render view: "/blogEntry/create", model: [blogStatus: blogStatus, entry: entry, recentEntries: recentEntries, finalTagList: finalTagList, temptaglist: temptaglist]
	}
	/**
	 * To show communication between admin and blog author in all blogs.
	 */
	@Secured(["ROLE_USER"])
	def getBlogChatHistory(){
		def chatHistory = blogsService.getBlogChatHistory(params)
		HashMap res = new HashMap()
		res.chatHistory = chatHistory
		respond res, [formats: ['json']]
	}
	
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
	def viewBlog() {
		redirect(action: "showEntry", params: [title: params.title])
	}
	/**
	 * This action is used to Show blog
	 */
	@Secured(["ROLE_USER"])
	def showEntry() {
		if(params.author) {
			redirect(action: "filterByAuthor", params: [author: params.author])
		}
		def title = params.title.toString().replaceAll("-", " ")
		def entry = blogsService.getQueryToShowEntry(title)
		BlogStatus blogStatus = BlogStatus.findByBlogEntry(entry)
		
		
		
		def imageUrl=""
		String body=OodlesBlogEntry.findAllByTitle(title)?.body
		if(body.contains("https://s3.amazonaws.com/oodles-technologies1/blog-images")){
			def firstIndex = body.indexOf("https://s3.amazonaws.com/oodles-technologies1/blog-images")
			def lastIndex = body.indexOf("\"", firstIndex)
			imageUrl=body.substring(firstIndex, lastIndex)
			
		}	
		
		
		if(entry) {
			def blogList = OodlesBlogEntry.findAll("FROM OodlesBlogEntry a WHERE a.id in  (SELECT MAX(b.id) FROM OodlesBlogEntry b WHERE b.id < :id and b.isProtected= :is_protected and b.published = :published and locked = false)"+
				" or a.id in (SELECT MIN(d.id) FROM OodlesBlogEntry d WHERE d.id > :id and d.isProtected= :is_protected and d.published= :published and locked = false)",
				[id: entry.id,is_protected:entry.isProtected,published:entry.published])
			
			def blogListSize = blogList.size()
			def currentIndex = blogList.indexOf(entry)
			
			def prevBlog = null
			def nextBlog = null
			blogList.each{
				if(it.id < entry.id ){
					nextBlog = it
				}
				else{
					prevBlog = it
				}
			}
			def currentUser = springSecurityService.currentUser
			def user = User.findByUsername(entry.author)
			def isSupervisor = (currentUser==user.supervisor)
			def recentEntries = OodlesBlogEntry.findAllByPublishedAndLocked(true, false, [max: 10, cache: true, offset: params.offset, order: "desc", sort: "lastUpdated"])
			def tagsData = blogsService.getTags()
			render view: "/blogEntry/entry", model: [entry: entry, user: user, tagNames: OodlesBlogEntry.allTags, recentEntries: recentEntries, prevBlog: prevBlog, tagsData: tagsData, nextBlog: nextBlog, currentUser: springSecurityService.currentUser, imageUrl:imageUrl, isApproved : blogStatus.approved, isSupervisor:isSupervisor]
		} else {
			response.sendError 404
		}
	}
	/**
	 * This action is called when we submit blog while creating and editing blogs.
	 */
	@Secured(["ROLE_USER"])
	def createAndEditBlog() {
		String errorMsg=blogsService.validationForCreateBlog(params)
		def entry = OodlesBlogEntry.get(params.id)?: new OodlesBlogEntry()
		entry.properties = params['entry']
		if(!errorMsg.equals("success")) {
			render view: "/blogEntry/create", model: [entry: entry, errorMessage: errorMsg]
			return
		}
		OodlesBlogEntry.withTransaction {
			User user = springSecurityService.currentUser
			def userInfo = UserInfo.findByUser(user)
			if(!params.id) entry.author = user.username
			entry.published = false
			entry.title = entry.title?.trim()
			entry.body = entry.body?.trim().replace("<title></title>", "")
			if(entry.save(flush:true)) {
				try{			
					if(params.tags.size() != 0) {
						entry.parseTags(params.tags, ",")
					}
				}catch(TagException ex){}
				redirect(action: "showEntry", params: [title: TitleUrl.urlTitleValue(entry)])
			} else {
				def finalTagList = blogsService.getTagList()
				render view: "/blogEntry/create", model: [entry: entry, finalTagList: finalTagList]
			}
			BlogStatus blogStatus = BlogStatus.findByBlogEntry(entry.save(flush:true))
			if(blogStatus) {
				
				if(blogStatus.status != "Moderate" && entry.published == false){
				blogStatus.status = "Pending"
				entry.dateModerate = null
				blogStatus.description = params.description
				}
				else{
					blogStatus.status = "Moderate"
					}
				
				} else {
				blogStatus = new BlogStatus(status: 'Pending', blogEntry: entry.id, description: params.description)
				if(userInfo.designation=="PROJECT MANAGER"){
					blogStatus.approved = true;
				}
				blogStatus.save(flush: true)
			}
			
			if(params.id==null){
				def conf = SpringSecurityUtils.securityConfig
				String userName = user.firstName+" "+user.lastName
				def supervisorName = userService.getUserName(user.supervisor)
				String url = grailsApplication.config.grails.serverURL + "/viewBlog/" + params.entry.title
				def ccSupervisorList = userService.getSupervisorList(user.supervisor)
				if(!(ccSupervisorList==[] || userInfo.designation=="PROJECT MANAGER")){
					def subject =userName+" has submitted a blog"
					String userImageUrl = grailsApplication.config.grails.imgURL+user.image
					String html = groovyPageRenderer.render(template: "/blogEntry/blogSubmission", model: [sname: supervisorName, uname: userName, uImage: userImageUrl, title:params.entry.title, url : url]).toString()
					def body = html
					boolean mailSent = oodlesMailService.mailFromOodles(ccSupervisorList,conf.ui.register.emailFrom, subject, body)
					if(mailSent)
						println ("mail sent successfully")
					else
						println ("mail sending failed")
				}
			}
			
		}
	}
	
	/** 
	 * Invoked when user gives a tagName while creating a blog
	 */
	def isTagExist() {
		def tagsList = OodlesBlogEntry.allTags
		render tagsList
	}
	/**
	 * Invoked when user remove a tag while creating or editing a blog
	 */
	def removeTags() {
		
		def blog = OodlesBlogEntry.get(params.id)
		def removeTag = params.removeTag
		
		if(blog)
		{
			def tagList = blog.tags;
			
			tagList.remove(removeTag)
			
			blog.tags = tagList
			blog.save(flush:true)
			
			
			render tagList as JSON
 		}
		else {
			response.sendError 404
		}
	}
	
	/**
	 * called when user click on editEntry under view in Blogs
	 * This action is used for editing the selected blog
	 */
	@Secured(["ROLE_USER"])
	def editEntry() {
		def entry = OodlesBlogEntry.get(params.id)
		def temptaglist =setTagtoArray()
		
		if(entry)
		{

			def tagArrayList = new ArrayList()
			for(def tag: entry.tags) {
				def listString = '"' + tag + '"'
				
				tagArrayList.add(listString)
				

			}
			if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_BD,ROLE_IM") || springSecurityService.currentUser.username == entry.author) {
				def finalTagList = blogsService.getTagList()
				render view: "/blogEntry/create", model: [entry: entry, tagArrayList: tagArrayList, finalTagList: finalTagList, temptaglist:temptaglist]
			} else {
				response.sendError 404
			}
		}
		 else {
			response.sendError 404
		}
	}
	/**
	 * Invoked when user click on author of the blog
	 * This action is used to show all the blogs of particular selected author 
	 */
	def filterByAuthor() {
		log.debug("inside filterByAuthor")
		def entries = []
		def recentEntries = []
		String author = params.author
		flash.countAuthorBlogs = 0
		if(author) {
			author = author.toString().trim()
			def strList = author.split("")
			if(author.contains(".")) {
				author = author.replace(".","-")
			}
			if(author.contains(" ")) {
				author = author.replace(" ","-")
			}
			if(author.contains("_")) {
				author = author.replace("_","-")
			}
			if(author.matches(".*\\d+.*")){
				author = author.replaceAll("[0-9]","-")
			}
			if(author.matches("Divya-Gupta")){
				redirect(action:"filterByAuthor",params: [author: "Oodles-Admin"])
				return;
			}
			
			ArrayList authors =  new ArrayList();
			String firstName="";
			String lastName="";
			if(author.contains("-")) {
				String[] authorName = author.split("-")
				if(authorName.length>2){
					authorName.eachWithIndex{name,index->
						if(index!=authorName.length-1){
							firstName = firstName+" "+name
						}
						if(index==(authorName.length-1)){
							lastName = authorName[authorName.length-1]
						}
					}
				}
				else{
					firstName= authorName[0]
					lastName = authorName[1]
					
				}
		
				authors = User.findAllByFirstNameAndLastName(firstName.trim(), lastName)?.username
			
			}else if(!author.contains("-")) {
				final Sql sql = new Sql(dataSource)
				String authorss= "%$author%"
				final results = sql.rows("select * from user where concat(first_name,last_name) like ?", authorss)
				authors << results[0]?.username
				sql.close();
			}
			else{
				authors = User.findAllByFirstName(author)?.username
			}
			
			if(authors.size()>0){
				entries = OodlesBlogEntry.findAllByAuthorInListAndLockedAndPublished(authors, false, true, [max: 5, offset: params.offset, sort: "lastUpdated", order: "desc"])
				recentEntries = OodlesBlogEntry.findAllByPublishedAndLocked(true, false, [max: 5, cache: true, offset: params.offset, order: "desc", sort: "dateCreated"])
				flash.countAuthorBlogs = authors.size()>0?OodlesBlogEntry.countByAuthorInListAndLockedAndPublished(authors, false, true):0
			}
		} 
		
		flash.author = author
		def tagsData = blogsService.getTags()
		render(view: "/blogEntry/list", model: [tagsData: tagsData, 
												entries: entries,
												tagNames: OodlesBlogEntry.allTags, 
												recentEntries: recentEntries, 
												tagEntry: (flash.countAuthorBlogs?: 0), 
												tag: flash.author, 
												cat: "Author"])
	}
	/**
	 * This action is used to delete any blog
	 * called from viewBlog action delete and from deleteEntry
	 */
	//@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def deleteBlog() {
		boolean isDeleted
		HashMap res = new HashMap()
		if(params.id){
		    def entry = OodlesBlogEntry.get((params.id.toString()).toLong())
			BlogStatus blogStatus = BlogStatus.findByBlogEntry(entry)
		    User currentUser = springSecurityService.currentUser
		    User user = User.findByUsername(entry.author)
		    boolean isSupervisor = (currentUser==user.supervisor)
			if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_BD,ROLE_IM") || (isSupervisor && !blogStatus.approved)){
			    isDeleted = blogsService.deleteBlog(params.id)
//			    redirect(controller: "site", action: "blogs")
				res.result = "success"
			  }
			else {
				response.sendError 404
				}
		} else {
			response.sendError 404
		}
		respond res, [formats: ['json']]
	}

	def list() {
		redirect uri: '/blogs'
	}
	
	/**
	 * Invoked when user click on any particular tag
	 * This action is used to get Blogs by tagName
	 */
	def byTag() {
		log.debug("byTag")
		if(params.tag) {
			long offset = params.offset?params.offset as long:0
			offset = offset*5
			def gormSession = sessionFactory.currentSession
			final String query = 'select * from blog_entry be where be.id in (select tag_ref from tag_links where tag_id = (select id from tags where name like :tagname)) and published = true and is_protected = false order by  last_updated desc limit 10 offset '+offset
			final sqlQuery = gormSession.createSQLQuery(query)
			final entries = sqlQuery.with {
				addEntity(OodlesBlogEntry)
				setString('tagname', params.tag.toString())
				list()
			}
			//resultTransformer = AliasToEntityMapResultTransformer.INSTANCE
			//def entries = OodlesBlogEntry.findAllByTag(params.tag.toString().trim(), [max:5, offset:params.offset, sort:"dateCreated", order:"desc"])
			def tagEntry = entries.size()
			if(tagEntry != 0){	
				def tagsData = blogsService.getTags()
				render(view: "/blogEntry/list", model:[entries: entries.findAll{!it.locked}, authors: findBlogAuthors(), tagNames: OodlesBlogEntry.allTags, tag:params.tag,tagsData:tagsData,tagEntry:tagEntry])
				return
			}
		}
		//redirect action:"list"
		response.sendError 404
	}
	/**
	 *This action is invoked when user click on more articles button 
	 *It shows the list of tag Blog
	 */
	def tagBloglist() {
		log.debug("tagBloglist")
		def max = 10 //Integer.parseInt(params.max)
		if(!params.start) {
			forward controller: "book", action: "blogs"
			return
		}
		def offset = Integer.parseInt(params.start) * max
		def entries
		if(params.category.contains("Author")){
			params.tag = params.tag.toString().trim()
			def authors
			if(params.tag.contains("-")) {
				String[] authorName = params.tag.split("-")
				authors = User.findAllByFirstNameAndLastName(authorName[0], authorName[1]).username
			}
			else{
				authors = User.findAllByFirstName(params.tag).username
			}
			entries = OodlesBlogEntry.findAllByAuthorInListAndLockedAndPublishedAndIsProtected(authors, false, true, false, [max: max, offset: offset, sort: "lastUpdated", order: "desc"])
		}else if(params.category.toString().contains("Archives")) {
			def year = params.tag.toString().substring(0, 4)
			def month = params.tag.toString().substring(4)
			def startDate = year + "-" + month + "-" + 1
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")
			def calender = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month), 1)
			def daysInMonth = calender.getActualMaximum(Calendar.DAY_OF_MONTH)
			def endDate = year + "-" + month + "-" + daysInMonth
			entries = OodlesBlogEntry.findAllByLastUpdatedBetweenAndPublishedAndLockedAndIsProtected(formatter.parse(startDate), formatter.parse(endDate), true, false, false, [max: 5, offset: offset, cache: true, order: "desc", sort: "lastUpdated"])
		} else {
			def entriesPage = OodlesBlogEntry.findAllByTag(params.tag.toString().trim(), [sort: "lastUpdated", order: "desc"])
			def filteredEntries = entriesPage.findAll {
				it.published && !it.locked
			}
			Collections.reverse(filteredEntries.sort() { it.lastUpdated })
			if(filteredEntries && filteredEntries.size < offset + max) {
				max = filteredEntries.size() - offset
			}
			if(filteredEntries) {
				entries = filteredEntries[offset..<offset + max]
			}
		}
		render template: "/blogEntry/listTitle", model: [entries: entries]
	}
	/**
	 * It is used for SEO purpose mapping exists in URLMapping file
	 */
	def imFacadeUrl() {
		def entries = []
		def tagNames = OodlesBlogEntry.allTags
		def tagsData = blogsService.getTags()
		def url = request.forwardURI
		url = url.substring(url.lastIndexOf("/") + 1)
		def keyword = url.split("-")
		keyword.each {
			if(tagNames.contains(it.toLowerCase().trim())) {
				def entry = OodlesBlogEntry.findAllByTag(it.toLowerCase().trim(), [max: 5, offset: params.offset, sort: "dateCreated", order: "desc"])
				entries.addAll(entry)
			}
		}
		def startTime = PagePerfomanceTime.getCurrentTime()
		def recentEntries = OodlesBlogEntry.findAllByPublishedAndLocked(true, false, [max: 5, cache: true, order: "desc", sort: "dateCreated"])
		def totalRecords = OodlesBlogEntry.list().size()
		render(view: "/blogEntry/list", model: [entries: entries, offset: params.offset, authors: findBlogAuthors(), tagNames: tagNames, recentEntries: recentEntries, totalRecords: totalRecords, tagsData: tagsData])
	}
	/**
	 *Invoked when user click on particular archives
	 *This action is used to show the blogs according to selected archives
	 */
	def archives() {
		def tagsData = blogsService.getTags()
		if(params.id) {
			def year = params.id.substring(0, 4)
			def month = params.id.substring(4)
			def startDate = year + "-" + month + "-" + 1
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")
			def calender = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month), 1)
			def daysInMonth = calender.getActualMaximum(Calendar.DAY_OF_MONTH)
			def endDate = year + "-" + month + "-" + daysInMonth
			def entries = OodlesBlogEntry.findAllByDateCreatedBetweenAndPublishedAndLocked(formatter.parse(startDate), formatter.parse(endDate), true, false, [max: 5, offset: 0, cache: true, order: "desc", sort: "dateCreated"])
			def entriesCount = OodlesBlogEntry.findAllByDateCreatedBetweenAndPublishedAndLocked(formatter.parse(startDate), formatter.parse(endDate), true, false).size()
			def recentEntries = OodlesBlogEntry.findAllByPublishedAndLocked(true, false, [max: 5, cache: true, order: "desc", sort: "dateCreated"])
			render(view: "/blogEntry/list", model: [entries: entries, authors: findBlogAuthors(), tagNames: OodlesBlogEntry.allTags, recentEntries: recentEntries, tagsData: tagsData, isArchive: true, tagEntry: entriesCount, tag: params.id, cat: "Archives"])
		} else {
			redirect action: "list"
		}
	}
	/**
	 * Invoked when user select a file for upload in blog
	 */
	def blogFileUpload() {
		render(blogsService.fileUpload(params.file, "blog"))
	}
	/**
	 * called when user click on viewBlogs
	 * This method is used to gets the data of all Blogs
	 */
	@Secured("ROLE_USER")
	def getAllBlog() {
		log.debug("get all blogs")
		JSONObject jsonObject = new JSONObject(params.jsonData)
		HashMap res = new HashMap()
		HashMap result = blogsService.getAllBlogs(jsonObject)
		def blogs
		/*if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_BD,ROLE_IM")) {
			result = blogsService.getAllBlogs(jsonObject)
			res.put("isAdmin",true)
		} else if (SpringSecurityUtils.ifAnyGranted("ROLE_USER")) {
		result = blogsService.getAllCurrentUserBlogs(jsonObject)
			res.put("isAdmin",false)
		}*/
		res.put("isAdmin",result.isAdmin)
		res.put("totalCount", result.totalCount)
		res.put("hasTeam", result.hasTeam)
		
		def blogsData
		if(result.content != null){
			blogsData = result.content
			blogsData = blogsService.getBlogsData(blogsData)
			res.blogsData = blogsData
			res.result = "success"
		}else {
			res.blogsData = result.content
			res.result = "failed"
		}
		respond res, [formats: ['json']]
		
	}
	/**
	 * called from  statusChange under Blogs section
	 * This action is used to change the status of a Blog
	 */
	@Secured(["ROLE_ADMIN","ROLE_BD","ROLE_IM"])
	def changeBlogStatus() {
		HashMap res = new HashMap()
		OodlesBlogEntry entry = OodlesBlogEntry.get(Long.parseLong(params.blogId))
		def blogStatus = BlogStatus.findByBlogEntry(entry)
		
		def user = User.findByUsername(entry.author)
		if (params.statusValue == 'Published') {
			blogStatus.status = params.statusValue
			entry.published = true
			entry.isProtected = false
			blogStatus.save(flush: true)
			entry.save(flush: true)
			String blogUserName=userService.getUserName(user)
			def message = "Blog has been Published"
			User tomail = User.findByEmail(user.email)
			def toMail;
			if(tomail!=null)
			   toMail=Arrays.asList(tomail)
			else
	    	    toMail=null;
			def ccmail=null
			boolean result1=false
			String userImageUrl = grailsApplication.config.grails.imgURL+user.image
			def body = g.render(template: "/blogEntry/blogEditMail", model: [username:blogUserName , message: message, blogTitle: entry.title, uImage:userImageUrl]).toString()
			String subject="Your blog has been Published at Oodlestechnologies"
			String name=userService.getUserName(springSecurityService.currentUser)
			boolean result=oodlesMailService.mailFromOodles(user.email, conf.ui.register.emailFrom, subject, body)
			if(result)
			{
			  result1=mailTrackerService.SaveEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccmail)
			}else
		    {
			  result1=mailTrackerService.FailedEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccmail)
			}
			oodlesNotificationService.createNotification(user,"Your blog "+ entry.title +" has been Published","Your Blog "+entry.title +" has been Published By "+name)
		} else if (params.statusValue == 'Protected') {
			blogStatus.status = params.statusValue
			entry.published = true
			entry.isProtected = true
			entry.save(flush: true)
			blogStatus.save(flush: true)
		}
		else if (params.statusValue == 'Moderate') {
			blogStatus.status = "Moderate"
			blogStatus.approved = false
			entry.dateModerate = new Date()
			entry.published = false
			entry.save(flush: true)
			blogStatus.save(flush: true)
			
			def conf = SpringSecurityUtils.securityConfig
				String userName = user.firstName+" "+user.lastName
				User tomail = User.findByEmail(user.email)
				def toMail;
				if(tomail!=null)
				   toMail=Arrays.asList(tomail)
				else
					toMail=null;
					
				def supervisorName = userService.getUserName(user.supervisor)
				String url = grailsApplication.config.grails.serverURL + "/viewBlog/" + entry.title
				def ccSupervisorList = userService.getSupervisorList(user.supervisor)
				if(!(ccSupervisorList==[])){
					def subject ="Your blog has been Moderated "
					String userImageUrl = grailsApplication.config.grails.imgURL+user.image
					String html = groovyPageRenderer.render(template: "/blogEntry/moderateAlert", model: [sname: supervisorName, uname: userName, uImage: userImageUrl, title:entry.title, url : url]).toString()
				def body = html
					
					boolean mailSent = oodlesMailService.mailFromOodles(user.email,ccSupervisorList,conf.ui.register.emailFrom, subject, body)
					if(mailSent)
						println ("mail sent successfully")
					else
						println ("mail sending failed")
				}
				
		} 
		else {
			blogStatus.status = params.statusValue
			entry.published = false
			entry.save(flush: true)
			blogStatus.save(flush: true)
		}
		res = dashboardService.generateResponse(res, "success", "status changed successfully")
		respond res, [formats: ['json']]
	}
	
	def approveBlog(){
		println "params---"+params
		HashMap res = new HashMap()
		OodlesBlogEntry entry = OodlesBlogEntry.get(Long.parseLong(params.id))
		Date currentdate=new Date()
		entry.dateApproved=currentdate
		entry.dateModerate = null
		entry.save(flush:true)
		def blogStatus = BlogStatus.findByBlogEntry(entry)
		blogStatus.status="Pending"
		blogStatus.approved = !blogStatus.approved
		println("STATUS"+blogStatus.approved);
		blogStatus.save(flush:true)
		def user = User.findByUsername(entry.author)
		if(blogStatus.approved==true){
			String blogUserName=userService.getUserName(user)
			def message = "Blog has been Approved"
			User tomail = User.findByEmail(user.email)
			String userImageUrl = grailsApplication.config.grails.imgURL+user.image
			def toMail;
			if(tomail!=null)
			   toMail=Arrays.asList(tomail)
			else
				toMail=null;
			def ccmail=null
			boolean result1=false
			def body = groovyPageRenderer.render(template: "/blogEntry/blogEditMail", model: [username:blogUserName , message: message, blogTitle: entry.title, uImage : userImageUrl]).toString()
			String subject="Your blog has been Approved"
			String name=userService.getUserName(springSecurityService.currentUser)
			boolean result=oodlesMailService.mailFromOodles(user.email, conf.ui.register.emailFrom, subject, body)
			if(result){
				println "Mail has been Sent"
				result1=mailTrackerService.SaveEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccmail)
			}else{
				println "Mail has been failed"
			  	result1=mailTrackerService.FailedEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccmail)
			}
			oodlesNotificationService.createNotification(user,"Your blog "+ entry.title +" has been Approved","Your Blog"+entry.title +" has been Approved By "+userService.getUserName(springSecurityService.currentUser))
		}
		redirect(action: "showEntry", params: [title: entry.title])
		/*res.approved = blogStatus.approved
		res.msg = "success"
		respond res, [formats: ['json']]*/
	}
	/**
	 * This action is invoked when user click on send mail button under BlogChatHistory
	 * This action is used to send a mail to Author of that blog
	 */
	@Secured("ROLE_USER")
	def sendMailToAuthor() {
		HashMap res = new HashMap()

		JSONObject feedback = new JSONObject(request.JSON)
		def currentUser = springSecurityService.currentUser
		OodlesBlogEntry entry = OodlesBlogEntry.findByTitle(request.JSON.blogTitle)
		BlogStatus status = BlogStatus.findByBlogEntry(entry)
		def sendMailTo
		def msgFrom	
		User author = User.findByUsername(entry.author)
		boolean isAdmin = request.JSON.isAdmin
		String message = request.JSON.message
		if(author == currentUser){
			sendMailTo = blogsService.getLastEmailIdOfBlogCommment(entry)
			msgFrom = author
		}else{
			sendMailTo = author
			msgFrom = currentUser
		}
		def toMail;
		def ccMail
		User tomail = User.findByEmail(sendMailTo.email)
		if(tomail!=null)
		   toMail=Arrays.asList(tomail)
		else
		   toMail=null
		   
		String userImageUrl = grailsApplication.config.grails.imgURL+currentUser.image
		User ccmail=User.findByEmail(request.JSON.blogMsgCC)
		if(ccmail!=null)
		  ccMail=Arrays.asList(ccmail)
		else
		  ccMail=null
		def CCmail=null;
		
		
		String s3FolderName = "Feedback_folder"
		
		log.debug(request.JSON.blogMsgCC)
		log.debug(ccMail)
		  
		String newFileName
		if (feedback.jsonData != null) {
			newFileName = fileuploadService.base64SaveFile(feedback.jsonData.img, s3FolderName, feedback_Image)
				
		}
		
		String filePath = grailsApplication.config.grails.image.path + s3FolderName +"/"+newFileName
		
		
		String mailToName = userService.getUserName(sendMailTo)
		blogsService.saveBlogCommunicationHistory(entry, msgFrom, isAdmin, message)
		def body = g.render(template: "/blogEntry/blogCommentMail", model: [username: mailToName, message: request.JSON.message, blogTitle: request.JSON.blogTitle, uImage:userImageUrl, uname:currentUser.firstName, type:"commented"]).toString()
		String subject ='A Review comment is made on Blog Title \"' + request.JSON.blogTitle + "\""
		boolean mailSent=false;
		boolean result=false;
		
		if(newFileName == null){
			if(request.JSON.blogMsgCC == null)
			   mailSent = oodlesMailService.mailFromOodles(sendMailTo.email, conf.ui.register.emailFrom, subject, body)
			else
			   mailSent = oodlesMailService.mailFromOodles(sendMailTo.email, request.JSON.blogMsgCC,conf.ui.register.emailFrom, subject, body)
			if(mailSent)
			     result=mailTrackerService.SaveEmailRecord(toMail,conf.ui.register.emailFrom,subject,CCmail)
		    else
			     result=mailTrackerService.FailedEmailRecord(toMail,conf.ui.register.emailFrom,subject,CCmail)
		}else{
		    if(request.JSON.blogMsgCC == null)
			   mailSent = oodlesMailService.addScreenShot(sendMailTo.email,conf.ui.register.emailFrom, subject, body,filePath)
			else 
			   mailSent = oodlesMailService.addScreenShot(sendMailTo.email,request.JSON.blogMsgCC,conf.ui.register.emailFrom, subject, body,filePath)
			if(mailSent)
			     result=mailTrackerService.SaveEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccMail)
	        else
			     result=mailTrackerService.FailedEmailRecord(toMail,conf.ui.register.emailFrom,subject,ccMail)
		}

		if(mailSent){
			String name=userService.getUserName(springSecurityService.currentUser)
			oodlesNotificationService.createNotification(sendMailTo,"Comment Made On Blog "+request.JSON.blogTitle,"Comment Made On Blog "+request.JSON.blogTitle+" By "+name)
			res = dashboardService.generateResponse(res, "success", "Mail Sent")
		}
		else{
			res = dashboardService.generateResponse(res, "error", "Error! try after sometime")
		}
		respond res, [formats: ['json']]
	}
	
	def updateLastModifiedDate(){
		def blogId = params.blogId;
		if(!blogId){
			return
		}
		else{
			def blog = OodlesBlogEntry.get(blogId.toInteger());
			blog.lastUpdated = new Date();
			blog.save(flush:true);
			render "";
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
	
	def setTagtoArray(){
		def allTags =blogsService.getalltemplatesTags()
		int index=0
		String[] allList=new String[allTags.size]
		for(String all:allTags)
		{
			String s1="'"
			String s2=s1.concat(all)
			String s3=s2.concat("'")
			allList[index]=s3
			index++
		}
		ArrayList temptaglistarr=new ArrayList();
		for(String j:allList){
			temptaglistarr.add(j)
		}
		return temptaglistarr
	}
	
	
	def savePostComments(){
		HashMap res = new HashMap()
		def sendMailTo
		def msgFrom
		def toMail
		def ccMail
		boolean mailSent = false
		boolean result = false
		def currentUser = springSecurityService.currentUser
		log.debug(params);
		def comment = params.comment
		def userName = params.name
		def userEmail = params.email
		def blogTitle = params.title
		def userImage = params.userImg
		def IsSend = true
		OodlesBlogEntry entry = OodlesBlogEntry.findByTitle(blogTitle)
		User author = User.findByUsername(entry.author)
		if(author == currentUser){
			sendMailTo = author
		}else{
			sendMailTo = author
			User tomail = User.findByEmail(sendMailTo.email)
		}
		String mailToName = userService.getUserName(sendMailTo)
		String userImageUrl = grailsApplication.config.grails.imgURL+currentUser.image
		def body = g.render(template: "/blogEntry/blogCommentMail", model: [username: mailToName, message: comment, type:"commented" , blogTitle: blogTitle, uImage:userImageUrl, uname:currentUser.fullName]).toString()
		String subject ='A comment is made on Blog Title \"' + blogTitle + "\""
		def CCmail = null
		mailSent = oodlesMailService.mailFromOodles(sendMailTo.email, conf.ui.register.emailFrom, subject, body)
			if(mailSent){
					BlogComments saveComments = new BlogComments()
					saveComments.comment = comment
					saveComments.name = userName
					saveComments.email = userEmail
					saveComments.blogDetails = entry
					saveComments.userImage = userImage
					saveComments.commentDate = new Date()
					saveComments.save(flush: true)
					log.debug("mail sent successfully")
					SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm a");
					String commentDate = formatter.format(saveComments.commentDate);
					res.comment = comment
					res.name = userName
					res.userImage = userImage
					res.commentDate = commentDate
					res.result = "success"
					res.message = "Feedback submitted successfully"
				}
				else{
					log.debug("mail sending failed")
					res.result = "failed"
				}
				respond res, [formats: ['json']]
	}
	
	def savePostCommentsReply(){
	log.debug(params)
	def title = params.title
	def parentComment = params.parentComment
	def parentName = params.parentName
	def commentReply = params.commentReply
	def email = params.email
	def name = params.name
	def userImage = params.userImg
	def sendMailTo
	def msgFrom
	def toMail
	def ccMail
	boolean mailSent=false
	boolean result=false
	def CCmail=null 
	def currentUser =springSecurityService.currentUser
	HashMap res = new HashMap()
		OodlesBlogEntry entry = OodlesBlogEntry.findByTitle(title)
		User author = User.findByUsername(entry.author)
		BlogComments commentObj = BlogComments.findByCommentAndName(parentComment,parentName)
		String userName
		if(author == currentUser){
			sendMailTo = commentObj
			userName = commentObj.name
		}else{
			sendMailTo = author
			userName = author.firstName+" "+author.lastName
		}
		User parentCommentUserDetails = User.findByEmail(sendMailTo.email)
		String userImageUrl = grailsApplication.config.grails.imgURL+currentUser.image
		def body = g.render(template: "/blogEntry/blogCommentMail", model: [username: userName, message: commentReply, blogTitle: title, type:"replied", uImage:userImageUrl,uname:currentUser.fullName]).toString()
		String subject ='A Reply is made on blog \"' + title + "\""
		mailSent = oodlesMailService.mailFromOodles(sendMailTo.email,conf.ui.register.emailFrom, subject, body)
				if(mailSent){
					BlogCommentReply saveCommentsReply = new BlogCommentReply()
					saveCommentsReply.commentReply = commentReply
					def countOfReplies = BlogCommentReply.countByBlogcomments(commentObj)
					saveCommentsReply.blogcomments = commentObj
					saveCommentsReply.blogDetails = entry
					saveCommentsReply.name = name
					saveCommentsReply.email = email
					saveCommentsReply.userImage = userImage
					saveCommentsReply.commentReplyDate = new Date();
					saveCommentsReply.save(flush: true)
					SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm a")
					String commentReplyDate = formatter.format(saveCommentsReply.commentReplyDate)
					log.debug("mail sent successfully")
					res.commentReply = commentReply
					res.commentReplyCount = countOfReplies
					res.commentReplyDate = commentReplyDate
					res.userImage = userImage;
					res.name = name;
					res.result = "success"
					res.message = "Feedback submitted successfully"
				}
				else{
					log.debug("mail sending failed")
					res.result = "failed"
				}
				respond res, [formats: ['json']]
		
	}
	
	def getBlogComments(){
		log.debug(params);
		Map res = new HashMap();
				OodlesBlogEntry entry = OodlesBlogEntry.findByTitle(params.blogTitle)
				def commentdetails = []
				def comments = BlogComments.findAllByBlogDetails(entry)
				comments.each{
					def innerMap = [:]
					innerMap.commentId =it.id
					innerMap.comment = it.comment
					innerMap.name = it.name
					innerMap.userImage =it.userImage
					SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm a");  
					String commentDate = formatter.format(it.commentDate);  
					def currentDate = new Date();
					String currentDateFormat = formatter.format(currentDate);
					innerMap.commentDate = commentDate;
					innerMap.replies = []
					innerMap.replies = getBlogReplyComments(it);
					commentdetails.add(innerMap)
				}
				res.commentList = commentdetails;
				respond res, [formats: ['json']]
	}
	
	def getBlogReplyComments(def comments){
		Map res = new HashMap();
				def replies = []
				def  commentReply = BlogCommentReply.findAllByBlogcomments(comments);
				
				commentReply.each{
					def innerMap = [:]
					innerMap.commentReplyId = it.id
					innerMap.replyComment = it.commentReply
					innerMap.replyName = it.name
					innerMap.userReplyImage = it.userImage
					SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm a");
					String commentReplyDate = formatter.format(it.commentReplyDate);
					innerMap.commentReplyDate =commentReplyDate
					replies.add(innerMap)
				}
				return replies
	}
	
}

package com.oodles.policy
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import com.oodles.policies.Policies;
import com.oodles.relevantWork.Page
import com.oodles.relevantWork.RelevantWork
import com.oodles.security.User
import com.oodles.utils.Status



class PolicyController {
	
	def springSecurityService
	def policyService
	def dashboardService

	def index() {
		
	}

	def underConstruction() {
		render (view : "/underConstruction")
	}
	def renderRequestedPolicy(){
		if(springSecurityService.isLoggedIn()){
		def policies = Policies.findByTitle(params.requestedPage.toString().replaceAll("-", " "))
		policies ?
				render (view: "/policy/show", model: [entry: policies,  username: springSecurityService.currentUser?.username])
				: {response.sendError 404}
		}
		else{
			response.sendError 404
		}
	}
	
	def showPolicy() {
		[message:flash.message]
	}
	
	def createPolicy(){
		Policies policy = session.entry?:new Policies()
		def pages = Page.list();
		[pages:pages,entry:  policy]
	}
	
	@Secured(["ROLE_ADMIN"])
	def submitPolicy()
	{
		session.entry = null
		Policies policy = params.id? Policies.get(params.id): new Policies()
		policyService.savePolicy(params,policy)
		if(policy.hasErrors()){
			session.entry = policy
			redirect (action: "createPolicy")
		}
		else{
			redirect(action: "showCreated", params: [title: policy.url])
		}
	}
	
	def showCreated()
	{
		def policies = Policies.findByTitle(params.title.toString().replaceAll("-", " "))
		policies ?
				render (view: "/policy/show", model: [entry: policies,  username: springSecurityService.currentUser?.username])
				: {response.sendError 404}
	}
	
	@Secured(["ROLE_ADMIN"])
	def editEntry()
	{
		def policy = Policies.get(params.id)
		def pages = Page.list();
		policy? render( view: "/policy/createPolicy", model: [entry: policy,pages:pages,message:flash.message],params:[:])
					: response.sendError( 404)
	}

	@Secured(["ROLE_ADMIN"])
	def deletePolicy()
	{	
		HashMap res = new HashMap()
		long policyId
		if(params.id){
			policyId = Long.valueOf(params.id.toString())
		}else if(request.JSON.id)(
			policyId = Long.valueOf(request.JSON.id.toString())
		)
		Policies.executeUpdate("delete Policies p where p.id = :id", [id:policyId])
		res.result = "success"
		respond res, [formats: ['json']]
		}

	def policyList()
	{
		def offset = Integer.parseInt(params.start) * 5
		render template:"/site/blogs",model:[recentBlogs:Policies.findAllByLocked(true,[max:5, cache:true, offset:offset, order:"desc", sort:"dateCreated"])]
	}
	def getAllPolicy()
	{
		HashMap res = new HashMap()
		def policies = Policies.list()
		res.isAdmin = true
		def policyData = policyService.getPoliciesData(policies)
		res.put("policiesData", policyData)
		respond res, [formats: ['json']]
	}

	@Secured(["ROLE_ADMIN"])
	def changePolicyStatus(){
		HashMap res = new HashMap()
		Policies policy = Policies.get(Long.parseLong(request.JSON.id.toString()))
		
		if(policy){
			policy.isActive = !policy.isActive
			policy.save(flush: true)
			res = dashboardService.generateResponse(res, "success", "status changed successfully")
		}
		else{
			res = dashboardService.generateResponse(res, "failed", "Policy not found!")
		}
		respond res, [formats: ['json']]
	}
}

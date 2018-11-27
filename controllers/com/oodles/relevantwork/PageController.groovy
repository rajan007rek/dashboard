package com.oodles.relevantwork

import grails.plugin.springsecurity.SpringSecurityUtils

import com.oodles.relevantWork.Page
import com.oodles.relevantWork.RelevantWork


class PageController {

    def index() { }
	
	def create(){
		log.debug("create");
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD"))
		{
			HashMap res=new HashMap()
			Page page = new Page()
			page.url = request.JSON.url
			page.description = request.JSON.description
			page.image = request.JSON.image
			if(request.JSON.parentPage){
				page.parentPage = Page.get(request.JSON.parentPage)
				if(!page.parentPage){
					res.errors = ["Invalid Parent Page"]
					res.result='failed'
					respond res, [formats: ['json']]
					return
				}
			}
			page.save(flush:true)
			
			if(page.hasErrors()){
				res.result='failed'
				res.errors = [];
				page.errors.allErrors.each {
					res.errors.add(g.message(error:it))
				}
			}
			else{
				res.id = page.id
				res.result='success'
			}
			respond res, [formats: ['json']]
		}
	}
	
	def edit(){
		log.debug("edit");
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD"))
		{
			HashMap res=new HashMap()
			Page page = Page.get(request.JSON.id)
			
			if(!page){
				res.errors = ["Invalid Page Id"]
				res.result='failed'
				respond res, [formats: ['json']]
				return
			}
			Page parentPage = null
			if(request.JSON.parentPage){
				parentPage = Page.get(request.JSON.parentPage)
				if(!parentPage){
					res.errors = ["Invalid Parent Page"]
					res.result='failed'
					respond res, [formats: ['json']]
					return
				}
			}
			page.url = request.JSON.url
			page.description = request.JSON.description 
			page.image = request.JSON.image
			page.parentPage = parentPage
			page.save(flush:true)
			
			if(page.hasErrors()){
				res.result='failed'
				res.errors = [];
				page.errors.allErrors.each {
					res.errors.add(g.message(error:it))
				}
			}
			else{
				res.id = page.id
				res.result='success'
			}
			respond res, [formats: ['json']]
		}
	}
	
	def delete(){
		log.debug("delete");
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD"))
		{
			HashMap res=new HashMap()
			Page page = Page.get(params.id)
			
			if(!page){
				res.errors = ["Invalid Page Id"]
				res.result='failed'
				respond res, [formats: ['json']]
				return
			}
			
			if(RelevantWork.findByParentPage(page)){
				res.errors = ["This Page is parent page of other Relevant pages so it can't be deleted"]
				res.result='failed'
				respond res, [formats: ['json']]
				return
			}
			
			page.delete(flush:true);
			res.id = page.id
			res.result='success'
			respond res, [formats: ['json']]
		}
	}
	
	def getAllPage(){
		if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")||SpringSecurityUtils.ifAnyGranted("ROLE_BD"))
		{
			HashMap res=new HashMap()
			res.pages = []
			def pages = Page.list()
			pages.each{ page ->
				Map entry = [:]
				entry.id = page.id
				entry.url = page.url
				entry.description = page.description
				entry.image = page.image
				entry.parentPage = page.parentPage?.id
				res.pages.add(entry)
			}
			res.result = 'success'
			res.isAdmin = true
			respond res, [formats: ['json']]
		}
	}
}

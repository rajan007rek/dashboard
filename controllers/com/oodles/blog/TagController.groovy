package com.oodles.blog
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.grails.taggable.Tag
import org.grails.taggable.TagLink
import com.oodles.categories.ApprovedTags
import com.oodles.categories.TagCategory

class TagController {
	def dashboardService
	/**
	 * called when user click on Tag Button under settings in dashboard
	 * This action gives the tagList and tagCategoryList
	 */
	@Secured(["ROLE_USER"])
	def tagList() {
		HashMap res = new HashMap()
		def tagListWithCategory = []
		def categoryList = []
		def tagsList = Tag.findAll()

		def category = TagCategory.findAll()

		category.each {
			categoryList.add(it.category)
		}
		tagsList.each {
			def tagMapWithCategory = [: ]
			def matchedTags = ApprovedTags.findByTags(it)
			if (matchedTags != null) {
				tagMapWithCategory.put("tagName", it.name)
				tagMapWithCategory.put("tagId", it.id)
				tagMapWithCategory.put("ApprovedTagId", matchedTags.id)
				tagMapWithCategory.put("ApprovedTagCategory", matchedTags.tagCategory.category)
			} else {
				tagMapWithCategory.put("tagName", it.name)
				tagMapWithCategory.put("tagId", it.id)
				tagMapWithCategory.put("ApprovedTagId", "")
				tagMapWithCategory.put("ApprovedTagCategory", "")
			}
			tagListWithCategory.add(tagMapWithCategory)
		}
		res = dashboardService.generateResponse(res, "success", "all tags retrieved successfully")
		res.tagList = tagListWithCategory
		res.isAdmin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_BD")
		res.tagCategory = categoryList

		respond res, [formats: ['json']]
	}
	/**
	 * called from Tag under settings when user click or change the category
	 * in AllTag List
	 * This action add a Tag into ApprovedTag List if not added and 
	 * also add or change the category
	 */
	
	@Secured(["ROLE_USER"])
	def tagListByID() {
		HashMap res = new HashMap()
		def tagWithCategory=[]
		def categoryList = []
		def tag = Tag.findById(params.id)
		def category = TagCategory.findAll()
		category.each {
			categoryList.add(it.category)
		}
			def tagMapWithCategory = [: ]
			def matchedTags = ApprovedTags.findByTags(tag)
			if (matchedTags != null) {
				tagMapWithCategory.put("tagName", tag.name)
				tagMapWithCategory.put("tagId", tag.id)
				tagMapWithCategory.put("ApprovedTagId", matchedTags.id)
				tagMapWithCategory.put("ApprovedTagCategory", matchedTags.tagCategory.category)
			} else {
				tagMapWithCategory.put("tagName", tag.name)
				tagMapWithCategory.put("tagId", tag.id)
				tagMapWithCategory.put("ApprovedTagId", "")
				tagMapWithCategory.put("ApprovedTagCategory", "")
			}
			tagWithCategory.add(tagMapWithCategory)
		res = dashboardService.generateResponse(res, "success", "all tags retrieved successfully")
		res.tag = tagWithCategory
		res.isAdmin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_BD")
		res.tagCategory = categoryList
		res = dashboardService.generateResponse(res, "success", "all tags retrieved successfully")
		respond res, [formats: ['json']]
	}
	
	
	def editTag(){
		HashMap res = new HashMap()
		def tag = Tag.findById(params.editTagId)
		tag.name = params.newTagName
		tag.save(flush: true)
		if(tag.save(flush: true)) {
			res = dashboardService.generateResponse(res, "success", "tags updated successfully")
		}else{
			res = dashboardService.generateResponse(res, "failed", "unable to update tag")
		}
		def tagEntry = ApprovedTags.findByTags(params.newTagName)
		if(!tagEntry) {
			tagEntry = new ApprovedTags(tags : params.newTagName)
		}
		def newCategory = TagCategory.findWhere(category: params.category)
		tagEntry.tagCategory = newCategory
		tagEntry.save(flush: true)
	
	respond res, [formats: ['json']]
	}
	
	@Secured(["ROLE_ADMIN","ROLE_BD"])
	def updateTag() {
		HashMap res = new HashMap()
		if(params != null) {
			def tagEntry = ApprovedTags.findByTags(params.name)
			if(!tagEntry) {
				tagEntry = new ApprovedTags(tags : params.name)
			}
			def newCategory = TagCategory.findWhere(category: params.category)
			tagEntry.tagCategory = newCategory
			if(tagEntry.save(flush: true)) {
				res = dashboardService.generateResponse(res, "success", "tags updated successfully")
			}else{
				res = dashboardService.generateResponse(res, "failed", "unable to update tag")
			}
		}
		respond res, [formats: ['json']]
	}
	
	/**
	 * called when user click on uncategorize link under Settings in dashboard
	 * This action delete any category tag from ApprovedTags list
	 */
	@Secured(["ROLE_ADMIN","ROLE_BD"])
	def uncategoriesTag() {
		HashMap res = new HashMap()
		def approveTags = ApprovedTags.get(Long.parseLong(params.id))
		approveTags.delete(flush: true)
		res = dashboardService.generateResponse(res, "success", "all tags retrieved successfully")
		respond res, [formats: ['json']]
	}
	/**
	 * called from deleteTag under Settings in dashboard
	 * This action is used to delete any tag
	 */
	@Secured(["ROLE_ADMIN","ROLE_BD"])
	def deleteTag() {
		HashMap res = new HashMap()
		def tag = params.name
		def tagDelete = Tag.findByName(params.name)
		def tagLink = TagLink.findAllByTag(tagDelete)
		tagLink.each {
			it.delete(flush: true)
		}
		tagDelete.delete(flush: true)
		res = dashboardService.generateResponse(res, "success", "all tags retrieved successfully")
		respond res, [formats: ['json']]
	}
	/**
	 * called  from  addTag under Settings in dashboard
	 * This action is used to save any new Tag and put the details of tag
	 * into HashMap and returns the (HashMap)data into json format
	 */
	@Secured(["ROLE_ADMIN","ROLE_BD"])
	def addTag() {
		HashMap res = new HashMap()
		def tag = new Tag()
		tag.name = params.tagName
		if(tag.save(flush: true)){
			def tagMapWithCategory = [: ]
			tagMapWithCategory.put("tagName", tag.name)
			tagMapWithCategory.put("tagId", tag.id)
			tagMapWithCategory.put("ApprovedTagId", "")
			tagMapWithCategory.put("ApprovedTagCategory", "")
			res.data = tagMapWithCategory
			res = dashboardService.generateResponse(res, "success", "Tags Added Successfully")
		}else{
			res = dashboardService.generateResponse(res, "failed", "Tag Name Already Exists")
		}
		respond res, [formats: ['json']]
	}
}

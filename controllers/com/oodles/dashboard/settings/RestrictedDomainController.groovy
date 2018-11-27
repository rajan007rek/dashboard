package com.oodles.dashboard.settings
import com.oodles.dashboard.RestrictedDomainRegistry;
import com.oodles.dashboard.SalesQuery;
import com.oodles.security.User
class RestrictedDomainController {
	def dashboardService
	def springSecurityService
	def userService
	/**
	 * Call when select the Restricted Domain
	 * this function get the list of domain 
	 */
	def viewRestictedDomain() {
		HashMap res = new HashMap()
		def domainName = []
		def restrictedDomain = RestrictedDomainRegistry.findAll()
		restrictedDomain.each {
			domainName.add(getDomainMap(it))
		}
		res = dashboardService.generateResponse(res, "success", "List of Restricted Domain restreived successfully")
		res.restrictedDomain = domainName
		res.currentUser = userService.getCurrentUserData()
		respond res, [formats: ['json']]
	}
	/**
	 * Call when ViewRestictedDomain() function execute 
	 * this function return the name and id of domain 
	 */
	def getDomainMap(domain) {
		def domainMap = [: ]
		domainMap.name = domain.name
		domainMap.id = domain.id
		return domainMap
	}
	/**
	 * Call when we click on delete link in restriction domain page
	 * this function delete the particular domain
	 */
	def unrestrictedDomainDelete() {
		def domain = RestrictedDomainRegistry.get(params.id)
		def salesQueries = SalesQuery.findAllByIsDeleted(false)
		salesQueries.each { salesQuery ->
			def index = salesQuery.email.indexOf('@')
			def domainName = salesQuery.email.substring(index + 1)
			if (domainName.contains(domain.name)) {
				salesQuery.isRestricted = false
				salesQuery.save(flush: true)
			}
		}
		domain.delete()
		HashMap res = new HashMap()
		res = dashboardService.generateResponse(res, "success", "Domain deleted successfully")
		respond res, [formats: ['json']]
	}
	/**
	 * Call when hit the delete link inside salesLead
	 * this function delete the particular user data
	 */
	def deleteSalesQuery() {
		def domain = SalesQuery.findByIdAndIsDeleted(params.id, false)
		domain.isDeleted = true
		domain.save(flush: true)
		HashMap res = new HashMap()
		res = dashboardService.generateResponse(res, "success", "SalesQuery deleted successfully")
		respond res, [formats: ['json']]
	}
}
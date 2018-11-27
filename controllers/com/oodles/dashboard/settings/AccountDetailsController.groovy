package com.oodles.dashboard.settings

import com.oodles.security.AccountDetails

class AccountDetailsController {
	
	def accountDetailsService
	def dashboardService
	
    def index() { }
	
	
	def addAccountDetails() {
		log.debug("addAccountDetails " + params)
		log.debug("addAccountDetails " + request.JSON)
		HashMap res = new HashMap()
		if(request.JSON.accountInfo){
			res.newaccountDetails = accountDetailsService.addAccountDetails(request.JSON.accountInfo)
			if(res.newaccountDetails){
				//res.result="success"
				res = dashboardService.generateResponse(res, "success", "Payment Details added successfuly")
			}
			else{
				res = dashboardService.generateResponse(res, "failed", "Account Identifier already exist")
			}
			
		}
		respond res, [formats: ['json']]
	}
	
	def getAccountsDetails() {
		HashMap res = new HashMap()
		res.accountDetailsList = AccountDetails.findAll()
		respond res, [formats: ['json']]
		
	}
	
	def getAccountDetails() {
		HashMap res = new HashMap()
		res.accountDetail = AccountDetails.findById(params.accountId)
		respond res, [formats: ['json']]
	}
	
	def editAccountDetails() {
		HashMap res = new HashMap()
		res.accountDetails = accountDetailsService.editAccountDetails(request.JSON)
		respond res, [formats: ['json']]
	}
	
}

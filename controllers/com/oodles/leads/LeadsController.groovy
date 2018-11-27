package com.oodles.leads


import org.codehaus.groovy.grails.web.json.JSONObject
import com.oodles.leads.FileType;
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.SpringSecurityUtils

import com.oodles.mailTracker.*;

import grails.plugin.springsecurity.ui.*
import io.intercom.api.Contact
import io.intercom.api.Intercom

import com.oodles.security.*


class LeadsController {
	def clientManagementService
	def leadsManagementService
	def springSecurityService
	def dashboardService
	def fileuploadService
	
		def createLead() {
			HashMap res = new HashMap()
			println("createLead Called.......")
			JSONObject newClientDetails = new JSONObject(params.data)
			leadsManagementService.createNewLead(newClientDetails)
			res = dashboardService.generateResponse(res, "success", "Lead Created Successfully")
			respond res, [formats: ['json']]
//			/*
//			 *to send information of Leads from contact form to the intercom account *
//			 *start
//			 */
//			 Map map = new HashMap()
//			 map.put("leadSource", "Creat-Lead")
//			 map.put("clientName", newClientDetails.clientName)
//			 map.put("organizationName", newClientDetails.organizationName)
//			 map.put("address", newClientDetails.streetAddress1)
//			 map.put("mobile", newClientDetails.mobile)
//			 map.put("city", newClientDetails.city)
//			 map.put("phone", newClientDetails.phone)
//			 map.put("state", newClientDetails.state)
//			 map.put("skypeId", newClientDetails.skypeId)
//			 map.put("country", newClientDetails.country)
//			 map.put("linkedInId", newClientDetails.linkedInId)
//			 map.put("emailId", newClientDetails.emailId)
//			 Intercom intercom = new Intercom()
//			 intercom.setToken("dG9rOmM3MzUyNTZhX2IxNjFfNDc5MF85ZDMzXzY0NGZmNGQ0ZThhZToxOjA=");
//			 Contact contact = new Contact()
//			 contact.setName(newClientDetails.emailId);
//			 contact.setCustomAttributes(map)
//			  contact = Contact.create(contact);
//			 /*
//			 *end
//			 */
			
		}
		def updateLeadInfo() {
			HashMap res = new HashMap()
			println("saveLeadInfo Called.......")
			JSONObject updateLeadInfo = new JSONObject(params.data)
			leadsManagementService.updateLeadInfo(updateLeadInfo)
			res = dashboardService.generateResponse(res, "success", "Lead Data Updates successfully")
			respond res, [formats: ['json']]
//			/*
//			 *to send information of visitor from contact form to the intercom account *
//			 *start
//			 */
//			 Map map = new HashMap()
//			 map.put("leadSource", "Update-Lead")
//			 map.put("projectName", updateLeadInfo.projectName)
//			 map.put("bdInvokeName", updateLeadInfo.bdInvokeName)
//			 map.put("description", updateLeadInfo.description)
//			 map.put("techInvoke", updateLeadInfo.techInvoke)
//			 map.put("statusCode", updateLeadInfo.statusCode)
//			 map.put("emailId", updateLeadInfo.emailId)
//			 map.put("source", updateLeadInfo.source)
//			 map.put("emailDate", updateLeadInfo.emailDate)
//			 map.put("leadStatus", updateLeadInfo.leadStatus)
//			 map.put("firstMonthBill", updateLeadInfo.firstMonthBill)
//			 map.put("interactedDate", updateLeadInfo.interactedDate)
//			 map.put("ActionDate", updateLeadInfo.nextActionDate)
//			 Intercom intercom = new Intercom()
//			 intercom.setToken("dG9rOmM3MzUyNTZhX2IxNjFfNDc5MF85ZDMzXzY0NGZmNGQ0ZThhZToxOjA=");
//			 Contact contact = new Contact()
//			 contact.setEmail(updateLeadInfo.emailId)
//			 contact.setCustomAttributes(map)
//			 contact = Contact.create(contact);
//			 /*
//			 *end
//			 */
		}
		
		def getLeadInfo() {
			HashMap res = new HashMap()
			User user = springSecurityService.currentUser
			def roles = UserRole.findAllByUser(user)
			boolean flag = false
			roles.each{ UserRole userRole ->
				if(userRole.role.authority == "ROLE_BD" || userRole.role.authority == "ROLE_ADMIN")
				flag = true
			}
			if(!flag){
				res.errorMsg = "You are not authorized to view this page"
				res = dashboardService.generateResponse(res, "failed", res.errorMsg)	
			}
			else{
				JSONObject jsonObject = new JSONObject(params.jsonData)
				String startDate= jsonObject.startDate ==""? null :jsonObject.startDate;
				String endDate=jsonObject.endDate ==""? null :jsonObject.endDate;
				Date  startDate1=null;
				Date  endDate1=null;
				try
				{
					 startDate1 = new Date(Long.parseLong(startDate));
					 endDate1 = new Date(Long.parseLong(endDate));
				}
				catch(Exception e)
				{
					log.debug(e)
				}
				def leadInfoListCount = (LeadInfo.findAllByIsDeleted(false)).size()
				def leadData = leadsManagementService.getResponseLeadDate( jsonObject,startDate1,endDate1)
				def leadInfoList = leadData.leadList
				res.statusCodeList = leadsManagementService.getAllStatusCode()
				res.leadSourceList = leadsManagementService.getAllLeadSource()
				res.leadStatusList = leadsManagementService.getAllLeadStatus()
				res.fileType= leadsManagementService.getAllFileType()
				def actionDataList= leadsManagementService.getAllleadActionDateData(jsonObject)
				def leadActionDateData =actionDataList.leadListData
				res.leadActionDateData=leadActionDateData
				res.tCount=actionDataList.totalCount
				res.tResult=leadActionDateData.size()
				res.leadInfoList=leadInfoList
				res.totalCount = leadData.totalCount
				res.totalResult = leadInfoList.size()
				res = dashboardService.generateResponse(res, "success", "AllLeads")
			}
			respond res, [formats: ['json']]
			
		}
		def getClient() {
			HashMap res = new HashMap()
			def lead = LeadInfo.findById(params.id)
			res.client = lead.client
			res = dashboardService.generateResponse(res, "success", "Client Data retrieved")
			respond res, [formats: ['json']]
		}
		def deleteLead() {
			HashMap res = new HashMap()
			def lead = LeadInfo.get(params.id)
			println (lead.emailId)
			def client = ClientDetails.get(lead.id)
			println (client.emailId)
			lead.isDeleted = !(lead.isDeleted)
			client.isDeleted = !(client.isDeleted)
			lead.save(flush: true)
			client.save(flush: true)
			res = dashboardService.generateResponse(res, "success", "Lead is deleted succesfully")
			respond res, [formats: ['json']]
		}
		
		//@Secured(["ROLE_USER"])
		def getLeadCommunicationHistory(){
			def communicationHistory = leadsManagementService.getLeadCommunicationHistory(params)
			HashMap res = new HashMap()
			res.communicationHistory = communicationHistory
			respond res, [formats: ['json']]
		}
		
		def getleadQualityComments(){
			def leadQualityComments = leadsManagementService.getleadQualityComments(params)
			HashMap res = new HashMap()
			res.leadQualityComments = leadQualityComments
			respond res, [formats: ['json']]
		}
		
		def submitComment() {
			HashMap res = new HashMap()
			def lead = LeadInfo.get(request.JSON.leadId)
			def commentedBy	
			String comment = request.JSON.comment
			JSONObject jsonObject = new JSONObject(request.JSON)
			commentedBy = springSecurityService.currentUser
			leadsManagementService.saveLeadInfoHistory(jsonObject,lead, comment, commentedBy)
			res = dashboardService.generateResponse(res, "success", "Lead Data Comment Submitted successfully")
			respond res, [formats: ['json']]
		}
		def submitLeadQuality() {
			HashMap res = new HashMap()
			def lead = LeadInfo.get(request.JSON.leadId)
			String comment = request.JSON.leadQualityComments
			JSONObject jsonObject = new JSONObject(request.JSON)
			leadsManagementService.saveLeadInfoQuality(jsonObject,lead)
			res = dashboardService.generateResponse(res, "success", "Lead Quality Comment Submitted successfully")
			respond res, [formats: ['json']]
		}
		
		
		def getLeadData(){
			HashMap res = new HashMap()
			def monthlyLeadData=[]
			def isAdmin
			if (springSecurityService.isLoggedIn() && SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
				isAdmin = true
			}
			JSONObject jsonObject = new JSONObject(params.jsonData)
			String yearFilter= jsonObject.yearFilter ==""? null :jsonObject.yearFilter;
			String sourceFilter= jsonObject.sourceFilter ==""? null :jsonObject.sourceFilter;
			String leadQuality= jsonObject.leadQuality ==""? null :jsonObject.leadQuality;
			res.weeklyLead = weeklyLead(yearFilter,sourceFilter)
			res.monthlyLead = monthlyLead(yearFilter,sourceFilter,leadQuality)
			res.totalMonthlyBill=monthlyBill(yearFilter) 
			res.isAdmin = isAdmin
			respond res, [formats: ['json']]
		}
		
		def monthlyBill(yearFilter){
			def monthlyBillData = leadsManagementService.getTotalMonthlyBill(yearFilter)
			def totalBill = []
			monthlyBillData.each {
				def bill = [: ]
				bill.month = leadsManagementService.getBlogMonthName(it.key)
				bill.value = it.value
				totalBill.add(bill)
			}
			HashMap res = new HashMap()
			res = dashboardService.generateResponse(res, "success", "Monthly Bill restreived successfully")
			return totalBill
		}
		
		def weeklyLead(yearFilter,sourceFilter){
			def wLeadData = leadsManagementService.getWeeklyLead(yearFilter,sourceFilter)
			def wAcquireLead = leadsManagementService.getWeeklyAcquireLead(yearFilter,sourceFilter)
			def dataLead = []
			int i=0;
			wLeadData.each {
				def lead = [: ]
				lead.month = it.key
				lead.value = it.value
				lead.avalue=wAcquireLead[i++]
				dataLead.add(lead)
			}
			HashMap res = new HashMap()
			res = dashboardService.generateResponse(res, "success", "List of Leads restreived successfully")
			return dataLead
		}
		def monthlyLead(yearFilter,sourceFilter,leadQuality) {
			def mAcquireLead= leadsManagementService.getAcquireLead(yearFilter,sourceFilter)
			def mLeadData = leadsManagementService.getMonthlyLead(yearFilter,sourceFilter)
			def leadQualityData = leadsManagementService.getleadQualityData(yearFilter,sourceFilter,leadQuality)
			def mdataLead = []
			int i=0,j=0;
			mLeadData.each {
				def mLead = [: ]
				mLead.month = leadsManagementService.getBlogMonthName(it.key)
				mLead.value = it.value
				mLead.avalue=mAcquireLead[i++]
				mLead.lQuality=leadQualityData[j++]
				mdataLead.add(mLead)
			}
			HashMap res = new HashMap()
			res = dashboardService.generateResponse(res, "success", "List of Monthly Leads retreived successfully")
			return mdataLead
		}
		
		def leadsFileUpload(){
			HashMap res = new HashMap()
			JSONObject jsonObject = new JSONObject(request.JSON.jsonData.metaData)
			LeadInfo leadinfo = LeadInfo.get(jsonObject.lead.id)
			if(jsonObject.lead.files.length() > 0){
					ArrayList files = new ArrayList()
					String fileName = jsonObject.lead.files.name[0]
					String content = jsonObject.lead.files.content[0]
					content = content.substring(content.indexOf(",")+1)
					String fType=jsonObject.lead.files.type[0]
					FileType fileType = FileType.getFileType(fType)
					boolean upload = false
					try {
							upload =fileuploadService.leadsDocUpload(content,leadinfo.id.toString(),fileName)
							LeadsFile leadsfile = new LeadsFile()
							leadsfile.primaryName = fileName
							leadsfile.path = leadinfo.id.toString() + "/" + fileName
							leadsfile.leadId =leadinfo.id.toString()
							leadsfile.createdOn = new Date()
							leadsfile.fileType=fileType
							leadsfile.save(flush:true)
							res = dashboardService.generateResponse(res, "success", "File Upload done")
							respond res, [formats: ['json']]
					}catch(Exception e){
						res = dashboardService.generateResponse(res, "failed", "File Failed to uploaded ")
						respond res, [formats: ['json']]
						log.debug("Exception in uploading : " + e)
					}
			}
			else{
				res = dashboardService.generateResponse(res, "failed", " Fail to upload file ")
				respond res, [formats: ['json']]
			}
		}
		
		def getleadUploadFiles() {
				HashMap res = new HashMap()
				String leadId=params.leadId
				def RecordDetails=[]
				LeadsFile leadsfile = new LeadsFile();
				def  searchCriteria = LeadsFile.createCriteria()
				RecordDetails=searchCriteria.list(){
					eq('leadId', leadId)
					order('createdOn', 'desc')
				}
				for(int i=0;i<RecordDetails.size();i++){
					def a=RecordDetails.get(i);
				}
				def allFileLeads=[];
				def latestLeadsFile=[];
				int l= 0,j=0,k=0,m=0;
				 for(int i = 0; i < RecordDetails.size(); i++){
					 String fileTyp= RecordDetails[i].fileType
					  if(fileTyp=="SOW"){
						 if(l==0){
							def sowFirstL=RecordDetails[i]
							latestLeadsFile.add(sowFirstL);
							l++;
						 }
						 else{
							 def  sowL=RecordDetails[i]
							 allFileLeads.add(sowL);
							 }
					}
					  else if(fileTyp=="OTHERS"){
						  if(m==0){
							  def others1=RecordDetails[i]
							  latestLeadsFile.add(others1);
							  m++;
						  }
					  else{
						  def others=RecordDetails[i]
						  allFileLeads.add(others);
						  }
					   }
					else if(fileTyp=="MSA"){
						if(j==0){
							def msaFirstL=RecordDetails[i]
							latestLeadsFile.add(msaFirstL);
							j++;
						}
					else{
						def msaL=RecordDetails[i]
						allFileLeads.add(msaL);
						}
					 }
					else if(fileTyp=="NDA"){
						if(k==0){
							def ndaFirstL=RecordDetails[i]
							latestLeadsFile.add(ndaFirstL);
							k++;
						 }
						else{
							def ndaL=RecordDetails[i]
							allFileLeads.add(ndaL);
							}
					 } else if(fileTyp=="SA"){
						if(k==0){
							def ndaFirstL=RecordDetails[i]
							latestLeadsFile.add(ndaFirstL);
							k++;
						 }
						else{
							def ndaL=RecordDetails[i]
							allFileLeads.add(ndaL);
							}
					 } else if(fileTyp=="CHAT"){
						if(k==0){
							def ndaFirstL=RecordDetails[i]
							latestLeadsFile.add(ndaFirstL);
							k++;
						 }
						else{
							def ndaL=RecordDetails[i]
							allFileLeads.add(ndaL);
							}
					 }
				}
				res.allFileLeads=allFileLeads
				res.latestLeadsFile=latestLeadsFile
				res.RecordDetails = RecordDetails
				res = dashboardService.generateResponse(res, "success", "Leads File retreived successfully")
				respond res, [formats: ['json']]
			}
		
		def leadDownloadSingleFile(){
			HashMap res = new HashMap()
			String path=params.path
			String s3Folder = path.substring(0,path.indexOf("/")) //jsonObject.id.toString() 183
			String fileName = path.substring(path.indexOf("/")+1) // eg. "Tooling-Buyer.pdf"
			String url = fileuploadService.downloadLeadFile(s3Folder, fileName)
			leadResponseObjects(url,"success")
	}
		
		def leadResponseObjects(def responseList,String outcome){
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
		
		def getAllSalesQueryQuestionsPerLead(){
			
			JSONObject jsonObject = new JSONObject(request.JSON)
			def leadInfo = LeadInfo.get(jsonObject.jsonData.leadId);
			Map res= new HashMap();
			def allQuestions = Questions.findAll();
			def allQuestionsArray = []
			allQuestions.each{
				def allQuestionsMap = [:]
				allQuestionsMap.question = it.question
				allQuestionsMap.questionId = it.id
				allQuestionsMap.choices = []
				allQuestionsMap.choices = it.choices.findAll()
				allQuestionsArray.add(allQuestionsMap)
				def dataCheck = SalesQueryQuestionnaire.findByLeadInfoAndQuestion(leadInfo,it)
				if(dataCheck != null){
				allQuestionsMap.answerIs = dataCheck.answer
				}
				else{
				allQuestionsMap.answerIs = ""
				SalesQueryQuestionnaire salesQueryObj = new SalesQueryQuestionnaire(leadInfo: leadInfo, question:it, answer : Choices.get('1'));
				salesQueryObj.save(flush:true);
				}
			}
			res.questions = allQuestionsArray
			respond res, [formats: ['json']]
			
		}
		
		
		def setSalesQueryAnswers(){
			Map res = new HashMap()
			JSONObject jsonObject = new JSONObject(request.JSON)
			def flag = false
			def lead = LeadInfo.get(jsonObject.jsonData.leadId)
			def leadCheck = SalesQueryQuestionnaire.findAllByLeadInfo(lead);
			if(leadCheck != null){
			jsonObject.jsonData.questions.each{ VarType ->
				leadCheck.each{ VarType2 ->
					if(((VarType.questionId).toInteger()) == VarType2.question.id){
						def val = Choices.get((VarType.answerId).toInteger())
						VarType2.answer = Choices.get((VarType.answerId).toInteger())
						VarType2.save(flush:true)
						flag = true
					}
				}
			}
			if(flag)
			res.result = "success"
			else
			res.result = "failed"
			respond res, [formats: ['json']]
			
			}
		}
}

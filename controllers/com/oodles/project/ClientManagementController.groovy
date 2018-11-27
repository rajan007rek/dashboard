package com.oodles.project

import grails.plugin.springsecurity.SpringSecurityService;
import java.text.DateFormat
import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONObject
import com.oodles.portfolio.*
import com.oodles.security.User
import com.oodles.utils.CommonConstants

class ClientManagementController {
	def springSecurityService
	def dashboardService
	def clientManagementService
	def fileuploadService	
	
	/**
	 *This action gives the information of all clients when user click on clients
	 */
	def getClientInformation() {
			HashMap res = new HashMap()
			User user = springSecurityService.currentUser
			
				JSONObject jsonObject = new JSONObject(params.jsonData)
				String statusFilter= jsonObject.statusFilter ==""? null :jsonObject.statusFilter;
				String publishFilter = jsonObject.publishFilter ==""? null :jsonObject.publishFilter;
				String clientFilter = jsonObject.clientFilter
				def clientInfoListCount = (ClientInfo.findAllByIsDeleted(false)).size()
				def clientData = clientManagementService.getClientResponseData(jsonObject,statusFilter,publishFilter)
				def projectList = []
				for (item in clientData.clientList.id) {
					String techInvokeNames = ""
					Map map = [:]
				def client = ClientInfo.findById(item)
				def arrayList= client.project.name
				for(int i=0;i < arrayList.size();i++){
					def techNames = arrayList[i]
				   if(i==0)
					techInvokeNames = techInvokeNames + techNames
				   else
					techInvokeNames = techInvokeNames + ", " + techNames
					 }
				map.projectData=techInvokeNames
				projectList.add(map)
				}
				def clientInfoList = clientData.clientList
				res.projects=projectList
				res.clientInfoList = clientInfoList
				res.totalCount = clientData.totalCount
				res.totalResult = clientInfoList.size()
				res.role = user.role
				res = dashboardService.generateResponse(res, "success", "AllClients")
				respond res, [formats: ['json']]
			
		}
	
	
	/**
	 * This action is used to create a new Client as user click on AddClient Button
	 */
	def createClient() {
		JSONObject newClientData = new JSONObject(params.data)
		HashMap res=new HashMap()
		def isExists = ClientInfo.findByEmailID(newClientData.emailID)
		if(isExists == null){
			def client = clientManagementService.createNewClient(newClientData)
			println("Existd: "+isExists);
			if(client){
				res.id = client.id
				res.name = client.name
				res = dashboardService.generateResponse(res, "success", "Client Created Successfully")
			}
			else{
				res = dashboardService.generateResponse(res, "failed", "Client name is already existed")
			}
		}else{
			res = dashboardService.generateResponse(res, "failed", "Account already exists with emailId "+newClientData.emailID)
		}
		respond res, [formats: ['json']]
	}
	
	/**
	 * This action is used when user click on edit client
	 * This action gives all the existing clientDetails for editing 
	 */	
	def getClientDetails() {
		String logo
		HashMap res = new HashMap()
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy")
		def date = null
		def client = ClientInfo.get(params.id)
		if(client.date) {
			date = dateFormat.format(client.date)
		}		
		if (client.logoUrl) {
			logo= fileuploadService.getS3FileUrl(CommonConstants.CLIENT_LOGO, "", client.logoUrl)
		} else {
			logo =''
		}
		res.contactName=client.contactName?:""
		res.website=client.website?:""
		res.logoImage=logo
		res.name = client.name?:""
		res.date = date
		res.emailID = client.emailID?:""
		res.officeContactNo = client.officeContactNo?:""
		res.address = client.address?:""
		res.description = client.description?:""
		res.secondaryContactNo=client.secondaryContactNo?:""
		res.secondaryContactName=client.secondaryContactName?:""
		res.secondaryEmailId=client.secondaryEmailId?:""
		respond res, [formats: ['json']]
	}
	
	/**
	 * This action is used for update an existing client record	 
	 */
	def editClient() {
		JSONObject editClientData = new JSONObject(params.data)
		HashMap res = new HashMap()
		def client = clientManagementService.editClientDetails(editClientData)
		if(client)
		{
		res.id = client.id
		res.name = client.name
		res = dashboardService.generateResponse(res, "success", "Record updated successfully")		
		}
		else
		res = dashboardService.generateResponse(res, "success", "Error Occured While Updating Record")
		respond res, [formats: ['json']]
		
	}
	
	def getClientStatus(){
		HashMap res = new HashMap()
		def status = ClientInfo.get(params.clientId)
		res = dashboardService.generateResponse(res, "success", "Status Changed Successfully")
		res.isActive = status.isActive
		res.enable = status.enable
		respond res, [formats: ['json']]
	
		
	}
	
	/**
	 * This action is used  to change the status of a client
	 */	
	def clientStatus() {
		HashMap res = new HashMap()
		def status = ClientInfo.get(params.clientId)
		if(status.isActive!=params.clientStatus) {
			status.isActive =params.clientStatus		
		} 
		if(params.projectStatus=="Published"){
			status.enable=true;
		}
		else{
			status.enable=false;
		}
		
		status.save(flush:true)
		res = dashboardService.generateResponse(res, "success", "Status Changed Successfully")
		res.isActive = status.isActive
		res.enable = status.enable
		respond res, [formats: ['json']]
	}
	
	def deleteClient() {
		HashMap res = new HashMap()
		def ClientInfo = ClientInfo.get(params.id)
		ClientInfo.isDeleted = !(ClientInfo.isDeleted)
		ClientInfo.save(flush: true)
		res.id=ClientInfo.id;
		res = dashboardService.generateResponse(res, "success", "Client is deleted succesfully")
		respond res, [formats: ['json']]
	}
	
	/**
	 * This action gives the name of all clients when user click on editClient at very first time
	 */
	def getAllClientNames() {
		def names = ClientInfo.list().name
		respond names, [formats: ['json']]
	}
	
	/**
	 * This action is used when user add a ClientName while creating or editing a project 
	 * It shows all the existing ClientNames
	 */
	def findClientNames() {
		def existingClients = ClientInfo.findAllByNameIlikeAndIsActiveAndisDeleted("%" + params.query + "%","Active", false).name
		respond existingClients, [formats: ['json']]
	}
}
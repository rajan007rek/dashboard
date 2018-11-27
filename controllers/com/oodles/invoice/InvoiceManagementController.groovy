package com.oodles.invoice
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import org.codehaus.groovy.grails.web.json.JSONObject

import com.oodles.portfolio.InvoiceStatus
import com.oodles.security.*
import com.oodles.utils.CommonConstants
@Secured(["ROLE_ADMIN"])
class InvoiceManagementController {
	def springSecurityService
	def invoiceManagementService
	def pdfRenderingService
	def fileuploadService
	def dashboardService
	def oodlesMailService
	
	/**
	 * Called when there is a hit on create invoice. 
	 * This action calls getInvoiceDetails of service to fetch invoice details and responds back in jSon.
	 */
	def getInvoiceDetails(){
		JSONObject invoiceJson = new JSONObject(params)
		def invoiceData = invoiceManagementService.getInvoiceDetails(invoiceJson)
		HashMap res = new HashMap()
		res.invoiceData = invoiceData
		respond res, [formats: ['json']]
	}
	/**
	 * Called when there is a hit on save invoice from create invoice page.
	 * Task is to save invoice data coming from create invoice page.
	 */
	def saveInvoiceInfo(){
		HashMap res = new HashMap()
		JSONObject invoiceData = new JSONObject(params.jsonData)
		def invoice=Invoice.findByInvoiceNumber(invoiceData.invoiceInfoData.invoiceNumber)
		if(invoice){
			invoiceManagementService.updateInvoiceInfo(invoiceData,invoice)?
			dashboardService.generateResponse(res, "success", "Invoice data saved successfully"):
			dashboardService.generateResponse(res, "failed", "Error occurred while saving Invoice data")
		}
		else
		{
			dashboardService.generateResponse(res, "failed", "Invoice Not exist!!!")
		}
		respond res, [formats: ['json']]
	}

	/**
	 * Called when there is a hit on preview from create invoice page.
	 * Task is to create preview for invoice.
	 */
	def preview(){
		JSONObject invoiceData = new JSONObject(params)
		def previewData=[:]
		HashMap res=new HashMap()
		def invoice=Invoice.findByInvoiceNumber(invoiceData.id.toString().trim())
		if(invoice)
		{
			previewData.invoiceData=invoiceManagementService.getInvoiceData(invoice)
			/*String fileLocation = grailsApplication.config.grails.image.path + grailsApplication.config.grails.amazon.s3invoicepdffolder
			new File(fileLocation).mkdirs()
			String fileName = UUID.randomUUID().toString() + ".pdf"*/
			Map map = writeInvoiceFile(previewData)
			fileuploadService.upload(map.fileName, map.fileLocation, grailsApplication.config.grails.amazon.s3invoicepdffolder)
			invoice.invoicePdfFileName = map.fileName
			invoice.save(flush:true)
			previewData.invoiceData.invoiceS3URL = fileuploadService.getS3FileUrl(CommonConstants.INVOICE_IMAGE,"", invoice.invoicePdfFileName) 
			res.previewData = previewData.invoiceData
			respond res, [formats: ['json']]
		}
		else{	
			dashboardService.generateResponse(res, "failed", "Invalid invoice number")
		}
			
	}
	/**
	 * Called when there is a click on send from create invoice page to send invoice
	 */
	def sendInvoice(){
		def invoiceNumber
		String title
		boolean isResend
		if(params.invoiceId){
			invoiceNumber = params.invoiceId
			isResend = true
			title="Invoice (${invoiceNumber}) has been updated"
		}
		else{
			invoiceNumber = params.invoiceNumber
			title = "You've received an invoice (${invoiceNumber}) from Oodles Technologies Pvt Ltd"
			isResend = false
		}
		HashMap res=new HashMap()
		boolean mailSent
		def invoiceDetails=[:]
		def invoice = Invoice.findByInvoiceNumber(invoiceNumber)
		if(invoice)
		{
			invoiceDetails.invoiceData=invoiceManagementService.getInvoiceData(invoice)
			Map map = writeInvoiceFile(invoiceDetails)
			def conf = SpringSecurityUtils.securityConfig
			String body=g.render(template: "invoiceMail", model: [invoice : invoice, payViaPayPal : invoice.payViaPayPal, isResend: isResend]).toString()
			mailSent = oodlesMailService.mailFromOodles(invoice.invoiceSentTo, invoice.invoiceSentCC,invoice.invoiceSentBCC ,conf.ui.register.emailFrom, title ,body, map.fileLocation+"/"+map.fileName)
			if(mailSent)
			{
				fileuploadService.upload(map.fileName, map.fileLocation, grailsApplication.config.grails.amazon.s3invoicepdffolder)
				invoice.invoicePdfFileName = map.fileName
				if(!invoice.invoiceStatus.toString().trim().equals("SENT"))
				{
					invoice.invoiceStatus = "SENT"
					invoice.save(flush:true)
				}
				//To handle StaleObjectStateException
				res.result = "success"
				res.message = "Mail Sent Succesfully"
				res.mailSent = mailSent
			}
			else
			{
				dashboardService.generateResponse(res, "failed", "Error in sending mail Please Try agrain later")
				res.mailSent = mailSent
			}
		}
		else
		{
			dashboardService.generateResponse(res, "failed", "Invoice Not exist!!! Check invoice number")
			res.mailSent = mailSent
		}
				respond res, [formats: ['json']]
	}

	/**
	 * Task is to provide pdf rendering service.
	 */
	def writeInvoiceFile(invoiceDetails){
		Map map = new HashMap();
		map.fileLocation = grailsApplication.config.grails.image.path + grailsApplication.config.grails.amazon.s3invoicepdffolder
		new File(map.fileLocation).mkdirs()
		log.debug("invoicedetails-------> "+invoiceDetails.invoiceData.currentOffice.officeName)
		map.fileName = UUID.randomUUID().toString() + ".pdf"
		pdfRenderingService.render(template:'/portfolio/pdfTemplateForInvoice', model:[invoiceDetails : invoiceDetails,accountDetailsSelected : false],
		new File(map.fileLocation+"/"+ map.fileName).newOutputStream())
		return map
	}
	/**
	 * Called when there is a click on Invoices from project 
	 * Task is to fetch all Invoices to show on invoices page 
	 */
	def showAllInvoices()
	{
		HashMap res = new HashMap()
		def invoiceDetails=[]
		def invoiceInfoList = Invoice.listOrderByInvoiceDate()
		invoiceInfoList.each {
			def invoiceDetail=[:]
			invoiceDetail.invoiceStatus=it.invoiceStatus
			invoiceDetail.projectName=it.project.name
			invoiceDetail.clientName=it.clientInfo.name
			invoiceDetail.invoiceDate=it.invoiceDate
			invoiceDetail.dueDate=it.dueDate
			invoiceDetail.invoiceTotalAmount=it.invoiceTotalAmount
			invoiceDetail.invoiceNumber=it.invoiceNumber
			invoiceDetail.invoiceS3URL=fileuploadService.getS3FileUrl(CommonConstants.INVOICE_IMAGE,"", it.invoicePdfFileName)
			invoiceDetails.add(invoiceDetail)
		}
		res.result = "success"
		res.invoiceDetails=invoiceDetails.reverse()
		respond res, [formats: ['json']] 		
	}
	/**
	 * Called from all invoices  page when there is a click on re-send invoice icon 
	 * Task is to re-send the invoice which was sent earlier to the client this action fetches the old pdf and send the same
	 */
	def resendInvoice()
	{	String title
		HashMap res=new HashMap()
		def invoice=Invoice.findByInvoiceNumber(params.invoiceNumber)
		String fileLocation = fileuploadService.getS3FileUrl(CommonConstants.INVOICE_IMAGE,"",invoice.invoicePdfFileName)
		def conf = SpringSecurityUtils.securityConfig
		switch(params.mailType)
		{
			case "cancel" 	: 	title="This invoice has been cancelled (${invoice.invoiceNumber})"
								invoice.invoiceStatus=InvoiceStatus.CANCELLED.getKey()
								res.status=invoice.invoiceStatus.getKey()
								invoice.save(flush:true)
								log.debug(invoice.invoiceStatus)
								break
			case "reminder" : 	title="Reminder: Your payment for this invoice (${invoice.invoiceNumber}) is due"
								break
			default 		: 	title="Invoice ${invoice.invoiceNumber} has been updated"
		}
		String body=g.render(template: "invoiceMail", model: [invoice : invoice, payViaPayPal : invoice.payViaPayPal, isResend: true, cancelMessage:params.message , mailType:params.mailType]).toString()
		String pdfFile=grailsApplication.config.grails.amazon.s3invoicepdffolder
		new File(pdfFile).mkdir()
		pdfFile=pdfFile+invoice.invoicePdfFileName
		URL readUrl =new URL(fileLocation)
		byte[] bytes = new byte[1024]
		int lengthOfPdf
		FileOutputStream fileOS = new FileOutputStream(pdfFile)
		URLConnection urlConn = readUrl.openConnection()
		InputStream inputStream = readUrl.openStream()
		while ((lengthOfPdf = inputStream.read(bytes)) != -1) {
			fileOS.write(bytes, 0, lengthOfPdf)
		}
		boolean mailSent = oodlesMailService.mailFromOodles(invoice.invoiceSentTo, invoice.invoiceSentCC,invoice.invoiceSentBCC ,conf.ui.register.emailFrom, title, body, pdfFile)
		if(mailSent)
		{
			dashboardService.generateResponse(res, "success", "Mail Sent Succesfully")
		}
		else
		{
			dashboardService.generateResponse(res, "success", "Error in sending mail Please try after sometime")
		}
		inputStream.close()
		
		respond res, [formats: ['json']]
			
	}
	
	/**
	 * Called from all invoices  page when there is a click on change invoice status icon
	 * Task is to change the status of "SENT" invoice to "PAID" and vice-versa
	 */
	def toggleInvoiceStatus(){
		log.debug(params)
		HashMap res=new HashMap()
		def invoice=Invoice.findByInvoiceNumber(params.invoiceNumber)
		if(invoice.invoiceStatus.toString().trim()==InvoiceStatus.SENT.getKey()){
			invoice.invoiceStatus=InvoiceStatus.PAID.getKey()
			}
		else if(invoice.invoiceStatus.toString().trim()==InvoiceStatus.PAID.getKey()){
			invoice.invoiceStatus=InvoiceStatus.SENT.getKey()
			}
		if(invoice.save(flush:true)){
			res.status=invoice.invoiceStatus.getKey()
			dashboardService.generateResponse(res, "success", "Invoice Status changed successfuly")
		}
		
		else{
			dashboardService.generateResponse(res, "failed", "Invoice Status not changed ")
		}
		respond res, [formats: ['json']]
	}
	
	/**
	 * Called from all invoices  page when there is a click on delete invoice icon
	 * Task is to delete the invoice
	 */
	def deleteInvoice(){
		HashMap res=new HashMap()
		def invoice=Invoice.findByInvoiceNumber(params.invoiceNumber)
		if(invoice){
			invoice.invoiceStatus=InvoiceStatus.DELETED.getKey()
			if(invoice.save(flush:true)){
				res.status=invoice.invoiceStatus.getKey()
				dashboardService.generateResponse(res, "success", "Invoice deleted successfuly")
			}
			else{
				dashboardService.generateResponse(res, "failed", "Invoice not deleted")
			}
		}
		else{
			dashboardService.generateResponse(res, "failed", "Invoice does not exist")
		}
		respond res, [formats: ['json']]

	}
}
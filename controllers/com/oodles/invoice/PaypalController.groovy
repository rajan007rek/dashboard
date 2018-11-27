package com.oodles.invoice

import org.grails.paypal.Payment

class PaypalController extends org.grails.paypal.PaypalController{

    def index() { }
	
	def success(){
		
		def payment = Payment.findByTransactionId(params.transactionId)
		log.debug "My Success notification received from PayPal for $payment with transaction id ${params.transactionId}"
		if (payment) {
			request.payment = payment
			if (payment.status != Payment.COMPLETE) {
				payment.status = Payment.COMPLETE
				payment.save(flush: true)
			}

			if (params.returnAction || params.returnController) {
				def args = [:]
				if (params.returnAction) args.action = params.returnAction
				if (params.returnController) args.controller = params.returnController
				args.params = params
				redirect(args)
			}
			else {
				return [payment: payment]
			}
		}
		else {
			response.sendError 403
		}
	
	}
	
}

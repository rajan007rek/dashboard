package com.oodles.invoice

class InvoicePaymentController {

    def index() { 
		
		log.debug("Invoice InvoicePayment ")
		Map item = new HashMap();
		Map user = new HashMap();
		item.name = "OD-10001"
		item.price = 1
		user.id = "2222"
		[item:item, user:user]
	}
}

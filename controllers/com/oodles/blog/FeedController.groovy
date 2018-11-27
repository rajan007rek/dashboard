package com.oodles.blog
import java.text.DateFormat
import java.text.SimpleDateFormat
import com.oodles.blog.OodlesBlogEntry
class FeedController {
    def index () { 
		DateFormat pubDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
		def blogList = OodlesBlogEntry.findAllByPublished(true,[max: 10,order :"desc",sort:"id"])
		response.setContentType("application/xml")
		[blogs: blogList, formatter: pubDateFormatter,serverUrl : grailsApplication.config.grails.serverURL]
    }
}

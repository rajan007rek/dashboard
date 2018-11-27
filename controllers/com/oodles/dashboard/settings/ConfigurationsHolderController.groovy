package com.oodles.dashboard.settings
import java.util.HashMap;
import java.util.Map;

import com.oodles.dashboard.ConfigurationHolder
import com.oodles.utils.Key
class ConfigurationsHolderController {
	def dashboardService
    def index() {
		
	}
	def addConfigurations(){
		HashMap res = new HashMap()
		if(request.JSON.key){
			def existingConfiguration = ConfigurationHolder.findByConfigurationKey(request.JSON.key)
			if(existingConfiguration){
				existingConfiguration.configurationValue = request.JSON.value
				existingConfiguration.save(flush:true)
				res.newConfigurations = existingConfiguration 
			}
			else{
				
				ConfigurationHolder configurationsHolder = new ConfigurationHolder(configurationValue:request.JSON.value , configurationKey :request.JSON.key.toString().trim())
				configurationsHolder.save(flush:true)
				res.newConfigurations = configurationsHolder
			}
			res = dashboardService.generateResponse(res, "success", "Configurations added successfuly")		
		}
		else{
			res = dashboardService.generateResponse(res, "failed", "Please enter Values !!!")
		}
		
		respond res, [formats: ['json']]
	}
	def getConfigurations(){
		HashMap res = new HashMap()
		def keyValueMap
		def keyValue = []
		for(Key key : Key.values()){
			keyValueMap = [:]
			keyValueMap.value = ConfigurationHolder.findByConfigurationKey(key)?.configurationValue
			keyValueMap.key = key
			keyValue.add(keyValueMap)
		}
		 
		res.configurations = keyValueMap
		res.keys = keyValue
		respond res, [formats: ['json']]
	
	}
}

package com.smr.location


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder

class LocationController {
	static responseFormats = ['json', 'xml']
	
    def index() { }
    
    def autocompleteProvincias(){
        def list = Provincia.list()
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            list.forEach(){
                id = it.id
                description = it.nombre
            }
        }
        render(status: 200, contentType: 'application/json', text: json)
        
    }
    
    def autocompleteLocalidades(){
        
    }
}

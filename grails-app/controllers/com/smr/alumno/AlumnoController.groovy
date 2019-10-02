package com.smr.alumno


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder

class AlumnoController {
	static responseFormats = ['json', 'xml']
	
    def index() { }
    
    def autocompleteParentescoTutor(){
        def list = ParentescoTutor.list()
        render(view:'/alumno/autocompleteparentescotutor',model:[list:list])
    }
    
    def save(){
        log.info("Parametros de save alumno: "+request.JSON)
        def alumnoInstance = new Alumno(request.JSON)
        log.info("Alumno Instance: "+alumnoInstance.properties)
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contentType: 'application/json', text: json)        
    }
}

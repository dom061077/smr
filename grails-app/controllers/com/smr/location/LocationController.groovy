package com.smr.location


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder

import com.smr.alumno.Provincia
import com.smr.alumno.Localidad

class LocationController {
	static responseFormats = ['json', 'xml']
	
    def index() { }
    
    def autocompleteProvincias(){
        def list = Provincia.list()
        /*def json = []
        list.each{p->
            json << [id:p.id,nombre:p.nombre]
        }*/
       
        //render json as grails.converters.JSON
        render(view:"/alumno/autocompleteloc",model:[list:list])
    }
    
    def autocompleteLocalidades(){
        def list = Localidad.list()
        def json = []
        list.each{l->
            json << [id:l.id,nombre:l.nombre]
        }
        //render json as 
    }
}

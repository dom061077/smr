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
        log.info("Autocompleteprovincias parametros: "+params)
        def c = Provincia.createCriteria()
        def list = c.list{
            ilike("nombre",'%'+params.search+'%')
        }
        /*def json = []
        list.each{p->
            json << [id:p.id,nombre:p.nombre]
        }*/
       
        //render json as grails.converters.JSON
        render(view:"/alumno/autocompleteprov",model:[list:list])
    }
    
    def autocompleteLocalidades(){

        def c = Localidad.createCriteria()
        def list = c.list{
            ilike("nombre",'%'+params.search+'%')
            provincia{
                eq("id",new Long(params.provincia))
            }
        }
        render(view:"/alumno/autocompleteloc",model:[list:list])
    }
}

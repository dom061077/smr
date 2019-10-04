package com.smr.alumno


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import java.text.SimpleDateFormat

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
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Date date = sdf.parse(request.JSON.fechaNacimientoUnbinding)
        log.info("Date java.util: "+date)
        alumnoInstance.fechaNacimiento = new java.sql.Date(date.getTime())
        
        log.info("Alumno Instance: "+alumnoInstance.properties)
        
        if(alumnoInstance.hasErrors()){
            alumnoInstance.errors.each{
                log.debug('ERROR!!!: '+it)
            }
            render (view:"/errors/_errors",model:[errors:alumnoInstance.errors])
            return            
        }
        
        alumnoInstance.save(flush:true)
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contentType: 'application/json', text: json)        
    }
    
    def listAlumnos(String search){
        log.info('Listado de alumnos, filtro '+search)
        def list = Alumno.createCriteria().list{
            or{
                ilike("apellido",'%'+search+'%')
                ilike("nombre",'%'+search+'%')
            }
            
        }
        render(view:'/alumno/listAlumnos',model:[list:list])
    }
}

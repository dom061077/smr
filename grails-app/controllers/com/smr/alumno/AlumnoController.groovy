package com.smr.alumno


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import java.text.SimpleDateFormat
import grails.plugin.springsecurity.annotation.Secured
import com.smr.enums.EstudioEnum

class AlumnoController {
	static responseFormats = ['json', 'xml']
	
    def index() { }
    
    
    def autocompleteParentescoTutor(){
        def list = ParentescoTutor.list()
        render(view:'/alumno/autocompleteparentescotutor',model:[list:list])
    }
    
    @Secured(['ROLE_ALUMNO_SAVE'])
    def save(){
        log.info("Parametros de save alumno: "+request.JSON)
        
        def alumnoInstance = new Alumno(request.JSON)
        alumnoInstance.estudioPrimarioTutor = request.JSON.estudioPrimarioTutor_parm.code as EstudioEnum
        alumnoInstance.estudioSecundarioTutor = request.JSON.estudioSecundarioTutor_parm.code as EstudioEnum
        alumnoInstance.estudioTerUnivTutor = request.JSON.estudioUniversitarioTutor_parm.code as EstudioEnum
        //alumnoInstance.estudioSecundarioTutor
        //alumnoInstance.estudioTerUnivTutor
        log.info("Estudio Enumeracion: "+alumnoInstance.estudioPrimarioTutor)
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Date date = sdf.parse(request.JSON.fechaNacimientoUnbinding)
        log.info("Date java.util: "+date)
        alumnoInstance.fechaNacimiento = new java.sql.Date(date.getTime())
        
        log.info("Alumno Instance: "+alumnoInstance.properties)
        
        
        
        if (!alumnoInstance.save(flush:true)){
            if(alumnoInstance.hasErrors() && !alumnoInstance.validate()){
                alumnoInstance.errors.each{
                    log.info('ERROR!!!: '+it)
                }
                render (view:"/errors/_errors",model:[errors:alumnoInstance.errors])
                return            
            }            
        }
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contentType: 'application/json', text: json)        
    }
    
    def listAlumnos(String filter,int start, int limit){
        log.info('Listado de alumnos, filtro '+filter)
    	def pagingconfig = [
    		max: limit as Integer?:20,
    		offset: start as Integer?:0
    	]        
        def list = Alumno.createCriteria().list(pagingconfig){
            if(filter.compareTo("")!=0){
                or{
                    ilike("apellido",'%'+filter+'%')
                    ilike("nombre",'%'+filter+'%')
                }
                
            }
            
        }
        render(view:'/alumno/listAlumnos',model:[list:list])
    }
    
    def count(String filter){
        log.info('Cantidad de alumnos ')
        int totalAlumnos =0
        
        def c = Alumno.createCriteria()
        totalAlumnos = c.count{
            if(filter?.compareTo("")!=0){
                or{
                    ilike("apellido",'%'+filter+'%')
                    ilike("nombre",'%'+filter+'%')
                }
                
            }
        }
            
        JSONBuilder jsonBuilder = new JSONBuilder()

        def json = jsonBuilder.build{
            count = totalAlumnos
        }
        render(status: 200, contectType:'application/json',text:json)
    }
    
    def show(Long id){
        log.info('Retornar alumno')
        def alumnoInstance = Alumno.get(id)

        render(view:'/alumno/show',model:[alumno:alumnoInstance])
    }
    
    def getalumnobydni(int dni){
        log.info("Parametros dni: "+dni)
       
        def alumnoInstance = Alumno.findByDni(dni)
        render(view:'/alumno/show',model:[alumno:alumnoInstance])
    }    
    

    
    @Secured(['ROLE_ALUMNO_UPDATE'])
    def update(){
        log.info("Parametros update alumno "+request.JSON)
        def alumnoInstance = Alumno.get(request.JSON.id)
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = null
        
        if(!alumnoInstance){
            json = jsonBuilder.build{
                success = false
                msg     = 'No existe el registro con id '+request.JSON.id+'. Operación cancelada'
            }
            render(status:200, contentType:'application/json',text: json)
            return
        }
        alumnoInstance.properties = request.JSON
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Date date = sdf.parse(request.JSON.fechaNacimientoUnbinding)
        alumnoInstance.fechaNacimiento = new java.sql.Date(date.getTime())        
        if(!alumnoInstance.save(flush:true)){
            if(alumnoInstance.hasErrors() && !alumnoInstance.validate()){
                render (view:"/errors/_errors",model:[errors:alumnoInstance.errors])
                return                    
            }
        }
        
        
        
        
        json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contentType: 'application/json', text: json)        
        
    }
    

    
}

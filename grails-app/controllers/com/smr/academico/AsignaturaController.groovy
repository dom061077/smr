package com.smr.academico


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import com.smr.security.User

class AsignaturaController {
	static responseFormats = ['json', 'xml']
        def asignaturaService
	
    def index() { }
    
    def getAsignaturas(){
        log.info("Ingresando a getAsignaturas")
        def list = Asignatura.list(sort:"descripcion")
        render(view:"/asignatura/getAsignaturas",model:[list:list])
    }
    
    def saveAsignaturaUser( ){
        log.info("Ingresando a saveAsignaturaUser. userId: "+request.JSON.userId+" asignaturasJson "+request.JSON)
        def userProcesado
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        try{
            asignaturaService.saveUserAsignatura(request.JSON)
        }catch(Exception e){
            log.error("Error al modificar el usuario",e)
            userProcesado = new User()
            usuarioProcesado.errors.rejectValue("asignaturas","",e.message)
            
        }
        if(userProcesado.hasErrors()){
            render (view:"/errors/_errors",model:[errors:usuarioProcesado.errors])
            return           
        }
        render(status: 200, contentType: 'application/json', text: json)
        
    }
    
    def showUserAsignaturas(Long id){
        log.info("Ingresando a showUserAsignaturas. Id: "+id)
        def userInstance = User.load(id)
        render(model:[user:userInstance])
    }
}

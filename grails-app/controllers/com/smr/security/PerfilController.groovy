package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*
import grails.web.JSONBuilder

class PerfilController {
	static responseFormats = ['json', 'xml']
        def perfilUserService
	
    def index() { }
    
    def save(){
        log.info("Ingresando a save "+request.JSON);
        def perfilInstance = new Perfil(descripcion:request.JSON.descripcion)
        def perfilProcesado 
        try{
            perfilProcesado=perfilUserService.savePerfil(perfilInstance,request.JSON.authorities)
        }catch(Exception e){
            log.error('Error al salvar perfil',e)
            perfilInstance.errors.rejectValue('descripcion','','Algún un rol no fue cargado correctamente')
        }
        if(perfilInstance.hasErrors()){
            render (view:"/errors/_errors",model:[errors:perfilInstance.errors])
            return
        }
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
            id      = perfilProcesado?.id
        }
        log.info("JSON devuelto: "+json)
        render(status: 200, contentType: 'application/json', text: json)         
    }
    
    def count(String filter){
        log.info('Cantidad de perfiles')
        JSONBuilder jsonBuilder = new JSONBuilder()
        totalPerfiles = perfilUserService.count(filter)
        def json = jsonBuilder.build(){
            count = totalPerfiles
        }
        render(status: 200, contectType:'application/json',text:json)
    }
    
}

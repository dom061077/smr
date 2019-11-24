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
        def perfilProcesado = perfilUserService.savePerfil(perfilInstance,request.JSON.authorities)
        if(perfilProcesado.hasErrors()){
            render (view:"/errors/_errors",model:[errors:perfilProcesado.errors])
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
    
}

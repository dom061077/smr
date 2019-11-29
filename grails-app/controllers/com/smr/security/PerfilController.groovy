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
            perfilInstance.errors.rejectValue('descripcion','','Algún rol no fue cargado correctamente')
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
    
    def update(){
        log.info("Ingresando a update "+request.JSON)
        def perfilInstance = Perfil.get(request.JSON.id)
        perfilInstance.properties = request.JSON
        def perfilProcesado
        try{
           perfilProcesado = perfilUserService.updatePerfil(perfilInstance,request.JSON.authorities) 
        }catch(Exception e){
            log.error('Error al modificar el perfil',e)
            perfilinstance.errors.rejectValue('descripcion','','Algún rol no fue cargado correctamente')
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
        
        render(status:200, contentType: 'application/json', text:json)

        
        
        
        
    }
    
    
    def count(String filter){
        log.info('Cantidad de perfiles')
        JSONBuilder jsonBuilder = new JSONBuilder()
        def totalPerfiles = perfilUserService.count(filter)
        def json = jsonBuilder.build(){
            count = totalPerfiles
        }
        render(status: 200, contectType:'application/json',text:json)
    }
    
    def listPerfiles(String filter,int start, int limit){
        log.info('Listado de perfiles, filtro '+filter)
    	def pagingconfig = [
    		max: limit as Integer?:20,
    		offset: start as Integer?:0
    	]        
        def list = Perfil.createCriteria().list(pagingconfig){
            if(filter.compareTo("")!=0){
                    ilike("descripcion",'%'+filter+'%')
                
            }
            
        }
        render(view:'/perfil/listPerfiles',model:[list:list])
    }
    
    def show(Long id){
        log.info("Retornar perfil "+id)
        def perfilInstance = Perfil.get(id)

        render(view:"/perfil/show",model:[perfil:perfilInstance])
    }
    
    def getAuthorities(Long id){
        log.info("Retornar authorities")
        def list = PerfilAuthority.createCriteria().list(){
            eq("perfil.id",id)
        } 
        render(view:"/perfil/getAuthorities",model:[list:list])
    }
}

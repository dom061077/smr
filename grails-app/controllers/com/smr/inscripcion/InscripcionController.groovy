package com.smr.inscripcion


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import grails.plugin.springsecurity.annotation.Secured

class InscripcionController {
	static responseFormats = ['json', 'xml']
    
    def inscripcionService
    
    def periodoLectivos(){
        log.info("Periodos lectivos ")
        def list = PeriodoLectivo.list()
        
        //render(view:'/inscripcion/')
        return [list:list]
    }
    
    def turnos(){
        def list = Turno.list()
        return [list:list]
        
    }
    
    def cursos(Long turnoId){
        def list = Curso.createCriteria().list(){
            turno{
                eq("id",turnoId)
            }
        }
        return [list:list]
    }
    
    def divisiones(Long cursoId, Long turnoId){
        def list = Division.createCriteria().list(){
            curso{
                eq("id",cursoId)
            }
            turno{
                eq("id",turnoId)
            }
        }
        return [list:list]
    }
    
    def save(){
        log.info("Parámetros: "+request.JSON)
        def inscProcesada
        //save(Long periodoLectivoId,Long divisionId, Long alumnoId)
        def inscJson = request.JSON
        try{
            inscProcesada=inscripcionService.save(inscJson.periodoLectivo.id
            ,inscJson.division.id,inscJson.alumnoId)
        }catch(Exception e){
            log.error("Error en save de service",e);
            inscProcesada.errors.rejectValue("periodoLectivo")
            
        }
        
        if(inscProcesada.hasErrors()){
            log.info("Errores de validacion: "+inscProcesada.errors)
            render(view:"/errors/_errors",model:[errors:inscProcesada.errors])
            return
        }
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        render (status:200, contentType: 'application/json', text:json)
    }
    
    def index() { }
}

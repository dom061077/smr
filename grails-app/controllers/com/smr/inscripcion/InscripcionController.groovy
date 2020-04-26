package com.smr.inscripcion


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import grails.plugin.springsecurity.annotation.Secured
import java.text.SimpleDateFormat

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
    
    def listInsc (String filterField,String filter,int start, int limit,String sortField,String ascDesc){
        log.info("Ingresando a listInsc. Filter: "
            +filter+" sortField: "+sortField+" ascDesc: "
            +ascDesc+" start: "+start+" limit:"+limit)
        def pagingconfig = [
            max: limit as Integer?:10,
            offset:start as Integer?:0
        ]
        def list = Inscripcion.createCriteria().list(pagingconfig){
            if(filterField.compareTo("")!=0){
                if(filterField.compareTo("fecha")==0 && filter.comparaTo("")!=0){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    Date date = sdf.parse(filter)
                    eq("fecha",new java.sql.Date(date.getTime()))
                }
                alumno{
                    if(filterField.compareTo("dni")==0)
                        eq(filterField,filter)
                    else{
                        ilike(filterField,"%"+filter+"%")
                    }
                        
                }
            }
            if(sortField.compareTo("")!=0 && sortField.compareTo("undefined")){
                if(sortField.compareTo("apellidonombre")==0){
                    alumno{
                        order("apellido",ascDesc)
                        order("nombre",ascDesc)
                    }
                }else{
                    alumno{
                        order(sortField,ascDesc)
                    }
                }
                
                
            }
        }
        render(view:'/inscripcion/listInc',model:[list:list])
    }
    
    
    def count(String filterField,String filter){
        log.info("Cantidad de inscripciones")
        def totalInsc = 0
        def c = Inscripcion.createCriteria()
        totalInsc = c.count{
            if(filterField.compareTo("")!=0){
                if(filterField.compareTo("fecha")==0 && filter.comparaTo("")!=0){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    Date date = sdf.parse(filter)
                    eq("fecha",new java.sql.Date(date.getTime()))
                }
                alumno{
                    if(filterField.compareTo("dni")==0)
                        eq(filterField,filter)
                    else{
                        ilike(filterField,"%"+filter+"%")
                    }                        
                }
            }
            
        }
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            count = totalInsc
        }
        render(status: 200, contentType: 'application/json', text: json)
    }    
    
    def index() { }
}

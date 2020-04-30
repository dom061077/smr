package com.smr.inscripcion


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import grails.plugin.springsecurity.annotation.Secured
import java.text.SimpleDateFormat
import grails.plugins.jasper.JasperExportFormat
import grails.plugins.jasper.JasperReportDef
import org.apache.commons.io.FileUtils
import java.util.Base64;


class InscripcionController {
	static responseFormats = ['json', 'xml']
    def jasperService
    
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
        log.info("Ingresando a listInsc. Filter Field: "+filterField
            +" filtro: "+filter+" sortField: "+sortField+" ascDesc: "
            +ascDesc+" start: "+start+" limit:"+limit)
        def pagingconfig = [
            max: limit as Integer?:10,
            offset:start as Integer?:0
        ]
        def list = Inscripcion.createCriteria().list(pagingconfig){
            if(filterField.compareTo("")!=0){
                if(filterField.compareTo("fecha")==0 && filter!=null &&
                       filter.compareTo("null")!=0 && filter.compareTo("")!=0){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    Date date = sdf.parse(filter)
                    eq("fecha",new java.sql.Date(date.getTime()))
                }else{
                    alumno{
                        if(filterField.compareTo("dni")==0 && filter.compareTo("")!=0)
                            eq(filterField,Integer.parseInt(filter))
                        if(filterField.compareTo("apellido")==0 || 
                            filterField.compareTo("nombre")==0)
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
                if(filterField.compareTo("fecha")==0 && filter!=null &&
                    filter.compareTo("null")!=0 && filter.compareTo("")!=0){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    Date date = sdf.parse(filter)
                    eq("fecha",new java.sql.Date(date.getTime()))
                }else{
                    alumno{
                        if(filterField.compareTo("dni")==0 && filter.compareTo("")!=0)
                            eq(filterField,Integer.parseInt(filter))
                        if(filterField.compareTo("apellido")==0 || 
                            filterField.compareTo("nombre")==0)
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
    
    def generarReporte(){
        log.info("Controller generarReporte: "+params)
        //def params=[]
        def inscList = Inscripcion.list()
        def data=[data:inscList]
        String reportsDirPath = servletContext.getRealPath("/reports/");
        log.info("path to dir: "+reportsDirPath)
        JasperReportDef reportDef = jasperService.buildReportDefinition(params, request.getLocale(), data)
        //FileUtils.writeByteArrayToFile(new File("inscripcion.pdf"), jasperService.generateReport(reportDef).toByteArray())
        //render(status: 200, contentType: 'application/json', text: '{"reporte":"ok"}')
        byte[] encoded = jasperService.generateReport(reportDef).toByteArray()
        
        Base64.Encoder enc = Base64.getEncoder();
        byte[] strenc = enc.encode(encoded);
        String encode = new String(strenc, "UTF-8");
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            count = encode
        }
        
        render(status: 200, contentType: 'application/json', text: json)      
    }
    
    def index() { }
}

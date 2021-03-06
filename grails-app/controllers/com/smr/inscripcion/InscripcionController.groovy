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
import com.smr.escuela.Escuela
import com.smr.alumno.Alumno

import com.smr.enums.EstudioEnum


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream
import com.smr.utils.Utils
import org.grails.core.DefaultGrailsDomainClass


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
        def list = Curso.withCriteria(){
            turnos{
                eq('id',turnoId)
            }
            order("nombre","asc")
        }
        
        return [list:list]
    }
    
    def divisiones(Long cursoId,Long turnoId){
        log.info("Ingresando a divisiones: "+cursoId+"-"+turnoId)
        def list = TurnoCursoDivision.all.findAll{
            it.curso.id == cursoId &&  it.turno.id == turnoId
        }
        return [list:list]
    }
    
    def save(){
        log.info("Parámetros: "+request.JSON)
        def inscProcesada
        //save(Long periodoLectivoId,Long divisionId, Long alumnoId)
        def inscJson = request.JSON
        try{
            inscProcesada=inscripcionService.save(request.JSON/*inscJson.periodoLectivo.id
            ,inscJson.division.id,inscJson.alumnoId*/)
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
            eq("anulada",false)
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
                        if(filterField.compareTo("apellidoNombre")==0)
                            ilike(filterField,'%'+filter+'%') 
                    }
                    if(filterField.compareTo("periodoLectivo")==0 && 
                            filter.compareTo("")!=0 && 
                            filter.compareTo("null")!=0)    
                        periodoLectivo{
                            eq("anio",Integer.parseInt(filter))
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
            eq("anulada",false)
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
                        if(filterField.compareTo("apellidoNombre")==0)
                            ilike(filterField,"%"+filter+"%")                    
                    }
                    if(filterField.compareTo("periodoLectivo")==0
                        && filter.compareTo("")!=0
                        && filter.compareTo("null")!=0)    
                        periodoLectivo{
                            eq("anio",Integer.parseInt(filter))
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
    
    
    private List getListReporte(String filterField,String filter,String sortField,String ascDesc){
        def dataList = Inscripcion.createCriteria().list(){
            eq("anulada",false)
            if(filterField?.compareTo("")!=0 && filterField?.compareTo("null")!=0){
                if(filterField?.compareTo("fecha")==0 && filter!=null &&
                       filter?.compareTo("null")!=0 && filter?.compareTo("")!=0){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    Date date = sdf.parse(filter)
                    eq("fecha",new java.sql.Date(date.getTime()))
                }else{
                    alumno{
                        if(filterField?.compareTo("dni")==0 && filter?.compareTo("")!=0)
                            eq(filterField,Integer.parseInt(filter))
                        if(filterField?.compareTo("apellidoNombre")==0)
                            ilike(filterField,"%"+filter+"%")
                    }
                    
                }
                if(filterField?.compareTo("periodoLectivo")==0
                        && filter?.compareTo("")!=0)    
                        periodoLectivo{
                            eq("anio",Integer.parseInt(filter))
                        }
                
            }
            /*if(sortField?.compareTo("")!=0 && sortField?.compareTo("undefined")!=0 && filterField?.compareTo("null")!=0){
                if(sortField?.compareTo("apellidonombre")==0){
                    alumno{
                        order("apellido",ascDesc)
                        order("nombre",ascDesc)
                    }
                }else{
                    alumno{
                        order(sortField,ascDesc)
                    }
                }
            }*/
            
        }
        def detalleInsc="detalleInsc"
        dataList.each{
            log.info("detalle de inscripcion: "+it."${detalleInsc}")
        }
        return dataList
    }
    
    def generarReporte(String filterField,String filter,String sortField,String ascDesc){
        log.info("Controller generarReporte: "+params)
        
        params.put("_file","inscripcion");
        params.put("_format","PDF")        
        
        def inscList=getListReporte( filterField, filter, sortField, ascDesc)
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
            report = encode
        }
         
        render(status: 200, contentType: 'application/json', text: json)      
    }
      
    
    def exportExcel( ){
        log.info('Ingresando a exportExcel, columns en formato JSON: '+request.JSON)
        def jParams = request.JSON
        //log.info(columns.toString())
        /*String[] columns = ["Name", "Email", "Date Of Birth", "Salary"]
        
        Workbook workbook = new XSSFWorkbook();        
        
       CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Employee");    
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);       
        
       for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }         
        

        
        
        
        ByteArrayOutputStream arrayByte = new ByteArrayOutputStream();
        workbook.write(arrayByte);
        //workbook.write(encodedByte)
        //fileOut.close();
        workbook.close();
        byte[] encodedByte = arrayByte.toByteArray()
        
        
        Base64.Encoder enc = Base64.getEncoder();
        byte[] strenc = enc.encode(encodedByte);
        String encode = new String(strenc, "UTF-8");
        */
        log.info("fielterField: "+jParams.filterField)
        def alumnoList=getListReporte( jParams.filterField, jParams.filter, jParams.sortField, jParams.ascDesc)
        String encode
        if(alumnoList.size()>0)
          encode=Utils.exportarxls(["APELLIDO","NOMBRE","FECHA INSCRIPCION","PERIODO LECTIVO","DETALLE INSCRIPCION"]
                ,["alumno.apellido","alumno.nombre","fecha","periodoLectivo.anio","detalleInsc"],alumnoList)
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            report = encode
        }
        
        render(status: 200, contentType: 'application/json', text: json)      
        
    }
    
    def show(Long id){
        log.info("Ingresando a show con id: "+id)
        def inscInstance = Inscripcion.get(id)
        render(view:'/inscripcion/show',model:[inscripcion:inscInstance])
    }
    
    def anular(Long id){
        log.info("Ingresando a anular con id: "+id)
        def inscProcesada
        try{
            inscProcesada = inscripcionService.invalidate(id)
        }catch(Exception e){
            log.error("Error en invalidate de service",e)
            inscProcesada.errors.rejectValue("anulada")
            
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
        
        render(status:200,contentType:'application/json', text:json)
    }
    
    def estudios(){
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            estudio = EstudioEnum.list()
        }
        render(status:200,contentType:'application/json',text:json)
    }
    
    def escuelas(){
        def escuelas = Escuela.list()
        return  [list:escuelas]
    }
    
    def index() { }
}

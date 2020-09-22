package com.smr.academico


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import com.smr.security.User
//import org.hibernate.Criteria
import java.util.Collections
import java.util.ArrayList
import grails.web.JSONBuilder
import com.smr.inscripcion.Inscripcion
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.hssf.usermodel.HSSFPrintSetup
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell


class AsignaturaController {
	static responseFormats = ['json', 'xml']
        def asignaturaService
        def springSecurityService
        def dataSource
	
    def index() { }
    
    def getAsignaturas(String filter){
        log.info("Ingresando a getAsignaturas. Filter: "+filter)
        def list = Asignatura.createCriteria().list(){
            ilike("descripcion","%"+filter+"%")
            order("descripcion","desc")
        }
        render(view:"/asignatura/getAsignaturas",model:[list:list])
    }
    
    def getAsignaturasPorUser(){
        log.info("Ingresando a getAsignaturasPorUser")
        def currentUser = springSecurityService.getCurrentUser()
        def list =  new ArrayList()
        Collections.addAll(list,currentUser.asignaturas.toArray())
        Collections.sort(list)
        return[list:list]
    }
    
    def saveAsignaturaUser( ){
        log.info("Ingresando a saveAsignaturaUser. userId: "+request.JSON.id+" asignaturasJson "+request.JSON)
        def asigProcesada
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        try{
            asigProcesada=asignaturaService.saveUserAsignatura(request.JSON)
        }catch(Exception e){
            log.error("Error al modificar el usuario",e)
            asigProcesada = new Asignatura()
            asigProcesada.errors.rejectValue("users","",e.message)
            
        }
        if(asigProcesada.hasErrors()){
            render (view:"/errors/_errors",model:[errors:asigProcesada.errors])
            return           
        }
        render(status: 200, contentType: 'application/json', text: json)
        
    }
    
    def showUserAsignaturas(Long id){
        log.info("Ingresando a showUserAsignaturas. Id: "+id)
        def userInstance = User.load(id)
        render(view:'/asignatura/showUserAsignatura',model:[user:userInstance])
    }
    
    def turnos(Long cursoId){
        log.info("Ingresando a turnos. cursoId: "+cursoId)
        def list = Turno.createCriteria().list{
            cursos{
                idEq(cursoId)
            }
        }
        return[list:list]
    }
    
    def countExamenesAsig( Long asigId, String filter){
        def currentUser=springSecurityService.getCurrentUser()
        log.info("Ingresando a countExamenesAsig userId: "+currentUser.id)
        log.info("asigId: "+asigId)
        def parameters = ["userId":currentUser.id]
        def hql="""
            SELECT count(*) FROM Inscripcion i
            INNER JOIN i.detalle di
            INNER JOIN di.tcDivision tcd
            INNER JOIN tcd.curso.asignaturas a
            INNER JOIN a.users u
            WHERE i.anulada = false and i.periodoLectivo.state = true
            and u.id = :userId
            %asigId
            GROUP BY i.alumno.id,tcd.curso.id,tcd.division.id,a.id

            """
        if(asigId){
            hql=hql.replaceAll("%asigId",asigId)
            parameters.put("asigId",asigId)
        }else{
            hql=hql.replaceAll("%asigId","")
        }
        
        def list=Inscripcion.executeQuery(hql,parameters)
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            count = list[0]
        }
        render (status: 200, contentType: 'application/json', text: json)
    }
    
    def listExamenes( Long asigId, String filter,int start, int limit){
        log.info("Ingresando a listExamenes.  asigId: "+asigId+" filter: "+filter)
        def currentUser=springSecurityService.getCurrentUser()
        def parameters=[
            max: limit as Integer?:10,
            offset:start as Integer?:0,
            "userId":currentUser.id,
        ]
        

        def hql = """ 
                    SELECT i.id, i.alumno.id,i.alumno.apellido,i.alumno.nombre
                                ,pe.asignatura.id
                                   ,pe.asignatura.descripcion,tcd.curso.nombre,tcd.division.nombre,
                            SUM(    case 
                                     when 
                                         (
                                            SELECT
                                            SUM(puntuacion)/count(id)
                                                
                                            FROM 
                                            Examen where periEvalInscAsig.id = pe.id
                                            and  tipoExamen.promediable=true
                                          ) < 6
                                     
                                     then 
                                         (
                                             SELECT
                                            SUM(puntuacion)/count(id)
                                                
                                            FROM 
                                            Examen where periEvalInscAsig.id = pe.id
                                            and  tipoExamen.complementario=true
                                          ) 
                                     else 
                                         (
                                             SELECT
                                            SUM(puntuacion)/count(id)
                                                
                                            FROM 
                                            Examen where periEvalInscAsig.id = pe.id
                                            and periEvalInscAsig.asignatura.id=pe.asignatura.id
                                            and periEvalInscAsig.inscripcion.id=pe.inscripcion.id
                                            and periEvalInscAsig.inscripcion.anulada=false
                                            and  tipoExamen.promediable=true
                                          ) 
                                     
                                end 
                               )/(select count(id) FROM PeriodoEvalInscAsignatura WHERE  inscripcion.id = i.id and asignatura.id=pe.asignatura.id)
                                       
                               
                    FROM Inscripcion i
                    INNER JOIN i.periodosInscEval pe
                    INNER JOIN pe.asignatura.users u
                    INNER JOIN i.detalle di
                    
                    INNER JOIN di.tcDivision tcd
                    where i.anulada=false and i.periodoLectivo.state = true
                        and u.id = :userId
                        %asigId
                    GROUP BY i.id, i.alumno.id,i.alumno.apellido,pe.asignatura.id
                                   ,pe.asignatura.descripcion,tcd.curso.nombre,tcd.division.nombre
                
                """
        if(asigId){
            hql = hql.replaceAll("%asigId"," and pe.asignatura.id = :asigId")
            parameters.put("asigId",asigId)
        }else
            hql = hql.replaceAll("%asigId","")
            
            
        
        def list=Inscripcion.executeQuery(hql,parameters)       
        
        
        
        return[list:list]
    }
    
    def showExamenes(Long asigId, Long alumnoId){
        log.info("Ingresando a show Examen. asigId: "+asigId+" alumnoId: "+alumnoId)
        
        def currentUser=springSecurityService.getCurrentUser()
        log.info("UserId: "+currentUser.id)
        String hql = """ SELECT e.id,a.id,a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,pe.periodoEval.descripcion
                         ,e.tipoExamen.descripcion ,e.puntuacion,i.periodoLectivo.anio
                         ,pe.cantInasist,pe.id,e.tipoExamen.ordenCompendio,pe
                        FROM Inscripcion i inner join i.periodosInscEval pe inner  join i.detalle d 
                         inner join pe.asignatura a
                         inner join pe.examenes e
                         inner join a.users u
                         inner join d.tcDivision tcd 
                         where u.id=:userId and i.periodoLectivo.state=true
                         and a.id = :asigId and i.alumno.id = :alumnoId                       
                        
                         order by   pe.periodoEval.descripcion,e.tipoExamen.ordenCompendio,e.id"""
        
        def parameters = [userId:currentUser.id,asigId:asigId,alumnoId:alumnoId]

    
        def list = Examen.executeQuery(hql,parameters)  
        def periodos = list.collect{it[15]}
        periodos = periodos.unique{
            it.id
        }
        log.info("Periodos: "+periodos)
        return[list:list,periodos:periodos]
    }
     
    def showExamenesPorPeriodo(Long asigId, Long alumnoId, Long perId){
        log.info("Ingresando a showExamenesPorPeriodo, asigId: "+asigId+" alumnoId: "+alumnoId+" perId: "+perId)
        def currentUser=springSecurityService.getCurrentUser()
        
    }
    
    def showExamenes2(Long asigId, Long alumnoId,Long perId){
        log.info("Ingresando a show Examen. asigId: "+asigId+" alumnoId: "+alumnoId+" perId: "+perId)
        
        def currentUser=springSecurityService.getCurrentUser()
        log.info("UserId: "+currentUser.id)
        String hql = """ SELECT e.id,a.id,a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,pe.periodoEval.descripcion
                         ,e.tipoExamen.descripcion ,e.puntuacion,i.periodoLectivo.anio
                         ,pe.cantInasist,pe.id,e.tipoExamen.ordenCompendio,pe
                        FROM Inscripcion i inner join i.periodosInscEval pe inner  join i.detalle d 
                         inner join pe.asignatura a
                         inner join pe.examenes e
                         inner join a.users u
                         inner join d.tcDivision tcd 
                         where u.id=:userId and i.periodoLectivo.state=true
                         and a.id = :asigId and i.alumno.id = :alumnoId                       
                         %filtroperiodo
                         order by   pe.periodoEval.descripcion,e.tipoExamen.ordenCompendio,e.id"""
        def parameters = [userId:currentUser.id,asigId:asigId,alumnoId:alumnoId]
        if(perId && perId.compareTo(Long.parseLong("0"))>0){
            hql=hql.replaceAll("%filtroperiodo"," and pe.id = %perId")
            paremeters.push(perId,perId)
        }else
            hql=hql.replaceAll("%filtroperiodo","")
        

    
        def list = Examen.executeQuery(hql,parameters)  
        def periodosEval = list.collect{e->
            e[15]
        }
        periodosEval = periodosEval.unique{
            it.id
        }
        log.info("Alumno: "+periodosEval.getAt(0).inscripcion.alumno.apellido)
        JSONBuilder jsonBuilder = new JSONBuilder()
        log.info("Periodos: "+periodosEval)
        def json = jsonBuilder.build{
            periodos = periodosEval.collect{p->
                def examenes = p.examenes.sort{a,b->
                    if(a.tipoExamen.ordenCompendio==b.tipoExamen.ordenCompendio)
                        a.id-b.id
                    else
                        a.tipoExamen.ordenCompendio - b.tipoExamen.ordenCompendio
                }
                return [id:p.id,descripcion:p.periodoEval.descripcion
                            ,inscripcion:p.inscripcion
                            ,cantInasist:p.cantInasist
                            ,examenes:examenes.collect{e->
                               [id:e.id,descripcion:e.descripcion
                                    ,puntuacion:e.puntuacion
                                    ,promediable:e.tipoExamen.promediable
                                    ,complementario:e.tipoExamen.complementario]
                             }
                            ,promedio:p.totalPromedio
                       ]
            }
            alumno = periodosEval.getAt(0).inscripcion.alumno
            asignatura = periodosEval.getAt(0).asignatura
            periodoLectivo = periodosEval.getAt(0).inscripcion.periodoLectivo.anio
            cursoDivision = periodosEval.getAt(0).inscripcion.detalleInsc
            
        } 

        render(status: 200,contentType:'application/json',text:json)
    }
    
    def saveExam(){
        log.info("Ingresando a saveExam: "+request.JSON)
        JSONBuilder jsonBuilder = new JSONBuilder()
        def examId=request.JSON.examId as Integer?:0
        def puntuacion=(request.JSON.puntuacion?new BigDecimal(request.JSON.puntuacion):BigDecimal.ZERO)
        def totalPromedio=0
        def s=true
        def retorno = asignaturaService.saveExam(examId,new BigDecimal(puntuacion))
        if(retorno?.hasErrors()){
            log.info("Retorna por error")
            render (view:"/errors/_errors",model:[errors:retorno.errors])
            return
        }else{
            if(retorno){
                totalPromedio = retorno.periEvalInscAsig.totalPromedio
                s = true
            }else
                s = false
        }
        def json = jsonBuilder.build{
            success = s
            promedio = totalPromedio
        }
        render(status: 200, contentType: 'application/json',text: json)
    }
    
    def savePromedios(){
        log.info("Ingresando a saveProvedios: "+request.JSON)
        JSONBuilder jsonBuilder = new JSONBuilder()
        
        request.JSON.each{
            log.info("Variable nombre: "+it.getKey().replaceAll("exam","")+" variable valor: "+it.getValue())
        }
        
        def retorno=asignaturaService.savePromedios(request.JSON)
        if(retorno){
                render (view:"/errors/_errors",model:[errors:retorno.errors])
                return               
        }
        
        
        def json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contentType: 'application/json', text: json) 
        
         
    }
    
    def invalidComplementario(Long perId,int puntuacion){
        log.info("Ingresando a validateComplementario: perId: "+perId)
        JSONBuilder jsonBuilder = new JSONBuilder()
        //def examInstance = Examen.get(examId)
        def perEvalInsc = PeriodoEvalInscAsignatura.get(perId)
        def error = false
        BigDecimal puntuacionComp=BigDecimal.ZERO
        perEvalInsc.examenes.each{ e->
            if(e.tipoExamen.complementario)
                puntuacionComp = puntuacionComp.add(e.puntuacion)
        }
        log.info("Nota complemantario: "+puntuacionComp)
        log.info("Nota total promedio: "+perEvalInsc.totalPromedio)
        if(perEvalInsc.totalPromedio.compareTo(new BigDecimal(6))>=0 && puntuacion.compareTo(BigDecimal.ZERO)!=0)
            error = true
        def json = jsonBuilder.build{
            success = error
        }     
        render(status:200, contentType: 'application/json',text:json)    
    }
    
    def listPromediosPorPeriodoEval(Long inscId, Long asigId){
        log.info("Ingresando a listPromediosPorPeriodoEval. inscId:"
            +inscId+" asigId"+asigId)


        def hql = """
                    SELECT pe.periodoEval.descripcion,SUM(e.puntuacion),count(e.id),SUM(e.puntuacion)/count(e.id) FROM PeriodoEvalInscAsignatura pe
                    INNER JOIN pe.examenes e
                    WHERE pe.inscripcion.id = :inscripcionId and pe.asignatura.id = :asignaturaId
                    GROUP BY pe.periodoEval.descripcion

                  """
        def parameters=[inscripcionId:inscId,asignaturaId:asigId]          
        def list = PeriodoEvalInscAsignatura.executeQuery(hql,parameters)         
        return[list:list]
    }
    
    private void setCellStyleCuerpo(def workbook,def cell){
        def headerCellStyle = workbook.createCellStyle()
        def headerFont = workbook.createFont();
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 10);
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )        
        cell.setCellStyle(headerCellStyle)        
    }
    
    
    
    String exportarCompendioXls(){
        /*
         * asignaturaId:number,periLectivoId:number
            ,turnoId:number,cursoId:number,divisionId:number
         * */
        log.info("Ingresndo alexportarCompendioXls. Parametros: "+request.JSON)
        //String[] columns = ["Name", "Email", "Date Of Birth", "Salary"]
        
        String hql = """ FROM AsignaturaPeriodoEvaluacion ape
                         WHERE ape.periodoEvaluacion.periodoLectivo.id = :periodoLectivoId
                         AND ape.asignatura.id = :asigId\n\
                         ORDER BY ape.periodoEvaluacion.ordenCompendio
                     """
        def parameters=[asigId:new Long(request.JSON.asigId)
                , periodoLectivoId:new Long(request.JSON.periLectivoId)]
        List listAsigPeriodoEval = PeriodoEvaluacion.executeQuery(hql,parameters)          
        log.info("Cantidad de periodos: "+listAsigPeriodoEval.size()+" con parametros: "+parameters)
        
        Workbook workbook = new XSSFWorkbook();        
        
        CreationHelper createHelper = workbook.getCreationHelper();
 
        // Create a Sheet
        Sheet sheet = workbook.createSheet("Compendio");  
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.LEGAL_PAPERSIZE); 
        //------fijando tamaños de columnas----
        for(int i=2;i<34;i++){
            sheet.setColumnWidth(i,256*3+100)
            
        }
        sheet.setColumnWidth(0,256*3+10)
         sheet.setColumnWidth(1,256*3*10)
        //----------------------------------------------
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 16);
       
        //headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )
        headerCellStyle.setFont(headerFont);
        
        // Create a Row
        Row headerRow = sheet.createRow(0);       
        
        Cell cell = headerRow.createCell(0)
        cell.setCellStyle(headerCellStyle);
        cell.setCellValue("ESCUELA SECUNDARIA BARRIO LOS PINOS")
        sheet.addMergedRegion(new CellRangeAddress(
                0, //first row (0-based)
                0, //last row  (0-based)
                0, //first column (0-based)
                34  //last column  (0-based)
        ));
        //----------------------------------------------
        headerRow = sheet.createRow(1);
        cell = headerRow.createCell(0)
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();
        headerCellStyle.setFont(headerFont)
        
        headerFont.setColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerFont.setBold(true)
        headerCellStyle.getFont().setFontHeightInPoints((short) 14);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )        
        cell.setCellStyle(headerCellStyle)
        


        cell.setCellValue("PLANILLA ANUAL DE CALIFICACIONES - AÑO 2020")
        sheet.addMergedRegion(new CellRangeAddress(
                1, //first row (0-based)
                1, //last row  (0-based)
                0, //first column (0-based)
                34  //last column  (0-based)
        ));    
        
        //----------------------------------------
        //--------------- datos de profesor--------------------
        headerRow = sheet.createRow(2)
        headerRow.setHeight((short)(256*2))
        cell = headerRow.createCell(0)
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 10);
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )        
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("NOMBRE DE MATERIA")
        sheet.addMergedRegion(new CellRangeAddress(
                2,2,0,2
        ))
        cell = headerRow.createCell(3)
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();        
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 11);
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )        
        cell.setCellStyle(headerCellStyle)
        
        cell.setCellValue("PROFESOR XXX")
        sheet.addMergedRegion(new CellRangeAddress(2,2,3,5))
        
        cell = headerRow.createCell(6)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("Nombre Profesor Probando")
        sheet.addMergedRegion(new CellRangeAddress(2,2,6,14))
        
        cell = headerRow.createCell(15)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("Curso")
        sheet.addMergedRegion(new CellRangeAddress(2,2,15,16))
        
        cell = headerRow.createCell(17)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("1°")
        
        cell = headerRow.createCell(18)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("Division")
        sheet.addMergedRegion(new CellRangeAddress(2,2,18,20))
        
        cell = headerRow.createCell(21)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("2°")        
        
        cell = headerRow.createCell(22)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("MODALIDAD")
        sheet.addMergedRegion(new CellRangeAddress(2,2,22,24))
        
        cell = headerRow.createCell(25)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("C.B.S")
        sheet.addMergedRegion(new CellRangeAddress(2,2,25,27))
        
        cell = headerRow.createCell(28)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("TURNO:")
        sheet.addMergedRegion(new CellRangeAddress(2,2,28,29))
        
        cell = headerRow.createCell(30)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("MAÑANA")
        sheet.addMergedRegion(new CellRangeAddress(2,2,30,32))        
        
        headerRow = sheet.createRow(3)
        def periodoLectHeaderRow = headerRow
        cell = headerRow.createCell(0)
        cell.setCellValue("Orden")
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();        
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 11);
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )  
        headerCellStyle.setRotation((short)90);
        cell.setCellStyle(headerCellStyle)     
        cell.setCellStyle(headerCellStyle)         
        
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();        
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 6);
        headerCellStyle.setAlignment(HorizontalAlignment.LEFT )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )  
        headerCellStyle.setRotation((short)90);
        
          
        cell = headerRow.createCell(2)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("CONDICION")
        
        //-------------above from here is the header----

        //cell = headerRow.createCell(3)
        //cell.setCellStyle(headerCellStyle)
        //cell.setCellValue("DIAGNOSTICO")
        //sheet.addMergedRegion(new CellRangeAddress(3,4,3,3))
        
        cell = headerRow.createCell(1)
        headerCellStyle = workbook.createCellStyle()
        headerFont = workbook.createFont();        
        headerFont.setBold(true)
        headerCellStyle.setFont(headerFont)
        headerCellStyle.getFont().setFontHeightInPoints((short) 11);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER )
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER )  
        cell.setCellStyle(headerCellStyle)        
        
        cell.setCellValue("APELLIDO Y NOMBRE")
        headerRow.setHeight((short)(256*2))      
        headerRow = sheet.createRow(4)
        headerRow.setHeight((short)(256*2+100))        
        sheet.addMergedRegion(new CellRangeAddress(3,4,0,0))
        cell.setCellStyle(headerCellStyle)   
        
        //headerRow = sheet.createRow(4)
        //headerRow.setHeight((short)(256*2+100))        
        sheet.addMergedRegion(new CellRangeAddress(3,4,1,1))   
        sheet.addMergedRegion(new CellRangeAddress(3,4,2,2))
        //--------------Periodos-------------------------------------
                //-----estilo de columna---
        def cellStyle = workbook.createCellStyle()
        def cellFont = workbook.createFont();        
        cellFont.setBold(true)
        cellStyle.setFont(headerFont)
        cellStyle.getFont().setFontHeightInPoints((short) 10);
        cellStyle.setAlignment(HorizontalAlignment.CENTER )
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER )  
        cellStyle.setRotation((short)90)
        
        
        
        
        int lastColumn=3
        listAsigPeriodoEval.each{
            cell = periodoLectHeaderRow.createCell(lastColumn)
            cell.setCellValue(it.periodoEvaluacion.descripcion)
            cell.setCellStyle(headerCellStyle)
            int examColumn = lastColumn
            def configExams = it.periodoEvaluacion.configExamenes.sort{a,b->
                    a.tipoExamen.ordenCompendio-b.tipoExamen.ordenCompendio
            }
            configExams.each{cnf->

                
                if(cnf.cantidad>1){
                    for(int i=0;i<cnf.cantidad;i++){
                        
                        cell=headerRow.createCell(examColumn)
                        cell.setCellStyle(cellStyle)
                        cell.setCellValue(""+(i+1))
                        examColumn++
                    }
                }else{
                    if(cnf.tipoExamen.complementario){
                        cell = headerRow.createCell(examColumn)//uso la fila debajo de trimestre para crear celdas
                        cell.setCellStyle(cellStyle)                    
                        cell.setCellValue("PROM.")
                        examColumn++
                    }
                    cell = headerRow.createCell(examColumn)//uso la fila debajo de trimestre para crear celdas
                    cell.setCellStyle(cellStyle)                    
                    cell.setCellValue(cnf.tipoExamen.descripcion)
                    examColumn++

                }
                
            }
            

            
            int periLastColumn = 1+/*complementario*/ 1+/*sumo uno de IANSIST.*/1/*sumo uno del promedio*/+it.periodoEvaluacion.configExamenes.size()
            
            cell = headerRow.createCell(lastColumn+periLastColumn)
            cell.setCellStyle(cellStyle)
            cell.setCellValue("INASIST.")
            periLastColumn = periLastColumn/2
            log.info("periLastColumn: "+periLastColumn)
            sheet.addMergedRegion(new CellRangeAddress(3,3,lastColumn,lastColumn+periLastColumn))
            periLastColumn = lastColumn + periLastColumn + 1
            
            lastColumn = lastColumn+it.periodoEvaluacion.configExamenes.size()+1+1/*sumo uno del promedio*/+1/*sumo uno de INASIST.*/+1/*complementario*/
            
            sheet.addMergedRegion(new CellRangeAddress(3,3,periLastColumn,lastColumn-1))
            cell = periodoLectHeaderRow.createCell(periLastColumn)
            cell.setCellValue("N Clases: "+it.cantClases)
            
        }
        
        CellRangeAddress region = new CellRangeAddress(3,4,0,18)
        RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
        Row row = sheet.getRow(4) 
        for(int i=0;i<lastColumn;i++){
            cell=row.getCell(i) 
            cellStyle = cell.getCellStyle()
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN)
            cellStyle.setBorderLeft(BorderStyle.THIN)
            cellStyle.setBorderRight(BorderStyle.THIN)
            cell.setCellStyle(cellStyle);
            
        }
        
        //------------------iterando alumnos-----------------
        hql=''' SELECT  i FROM Inscripcion i
                    INNER JOIN i.detalle det
                    INNER JOIN det.tcDivision tcd
                    WHERE tcd.curso.id = :cursoId AND tcd.division.id = :divisionId
                    AND tcd.turno.id = :turnoId
                    AND i.periodoLectivo.id = :periodoLectId
                    AND i.anulada=false            
                    ORDER BY i.alumno.apellido,i.alumno.nombre
                '''
                //
                //,

         parameters = [cursoId:new Long(request.JSON.cursoId),divisionId:new Long(request.JSON.divisionId)
                , periodoLectId:new Long(request.JSON.periLectivoId),turnoId:new Long(request.JSON.turnoId)]        
        def listInscripcion = Inscripcion.executeQuery(hql,parameters)
        Row rowAlumnos 
        int orden=1
        
        listInscripcion.each{
            log.info("Alumno: "+it.alumno.apellido)
            rowAlumnos = sheet.createRow(4+orden);
            cell = rowAlumnos.createCell(0)
            cell.setCellValue(orden)
            cell = rowAlumnos.createCell(1)
            cell.setCellValue(it.alumno.apellido+" "+it.alumno.nombre)
            cell = rowAlumnos.createCell(2)
            cell.setCellValue(it.condicion.name)
            setCellStyleCuerpo(workbook,cell)
            def filteredPie = it.periodosInscEval.findAll{pie->
                pie.asignatura.id.compareTo(new Long(request.JSON.asigId))==0
                
            }
            filteredPie = filteredPie.sort{a,b->
                a.periodoEval.ordenCompendio-b.periodoEval.ordenCompendio
            }
            int column=3
            filteredPie.each{filPie->
                def filteredExams = filPie.examenes.sort {a,b->
                    if(a.tipoExamen.ordenCompendio==b.tipoExamen.ordenCompendio)
                        return a.descripcion.compareTo(b.descripcion)
                    else
                        return a.tipoExamen.ordenCompendio-b.tipoExamen.ordenCompendio
                }
                log.info("    Periodo eval: "+ filPie.periodoEval.descripcion+" pie ID; "+filPie.id)
                filteredExams.each{exam->
                    cell = rowAlumnos.createCell(column)
                    log.info("          TipoExamn: "+exam.tipoExamen.descripcion)
                    setCellStyleCuerpo(workbook,cell)
                    if(exam.tipoExamen.complementario){
                        cell.setCellValue(filPie.totalPromedio)
                        log.info("                      promedio: "+filPie.totalPromedio)
                        column++
                        cell = rowAlumnos.createCell(column)
                        setCellStyleCuerpo(workbook,cell)
                        cell.setCellValue(exam.puntuacion)
                        log.info("                      complementario: "+exam.puntuacion)
                        
                    }else{
                        
                        cell.setCellValue(exam.puntuacion)
                        log.info("                      puntuacion: "+exam.puntuacion)
                    }
                    
                    column++
                    
                        
                }            
                //column++ //sumo una columna para la inasistencia
                cell = rowAlumnos.createCell(column)
                setCellStyleCuerpo(workbook,cell)                
                cell.setCellValue(filPie.cantInasist)
                column++
                 
                
            }

            log.info("Cantidad de periodos para la asignatura: "+filteredPie.size())
            orden++
        } 
        //----------bordes de regiones-------
        /*for(int i = 1;i<=10;i++){
            CellRangeAddress mergedRegions = sheet.getMergedRegion(i);
            RegionUtil.setBorderLeft(BorderStyle.THIN, mergedRegions, sheet);
            RegionUtil.setBorderRight(BorderStyle.THIN, mergedRegions, sheet);
            RegionUtil.setBorderTop(BorderStyle.THIN, mergedRegions, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN, mergedRegions, sheet);
        }*/
        
        
        /*headerRow = sheet.createRow(5)
        cell = headerRow.createCell(2)
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue(5)
        sheet.setColumnWidth(2,256*3+100)*/
        
       /*for(int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }         
        Font rowFont = workbook.createFont();
        rowFont.setFontHeightInPoints((short) 14);
        CellStyle rowCellStyle = workbook.createCellStyle();
        rowCellStyle.setFont(rowFont)
        
        
        for(int i = 0; i < data.size(); i++){
            int j = i+1
            Row row = sheet.createRow(j)
            for(int k = 0;k < propertyNames.size();k++){
                Cell cell = row.createCell(k)
                //DefaultGrailsDomainClass domainClass = ((DefaultGrailsDomainClass)data[i])
                //cell.setCellValue((data[i]).getPersistentValue(propertyNames[k]).toString());
                def fieldValue=getPropertyValue(propertyNames[k],data[i]);
                fieldValue = formatValue(fieldValue)
                
                cell.setCellValue(fieldValue);
                
                cell.setCellStyle(rowCellStyle);
            }
        }*/

        
        
        
        ByteArrayOutputStream arrayByte = new ByteArrayOutputStream();
        workbook.write(arrayByte);
        //workbook.write(encodedByte)
        //fileOut.close();
        workbook.close();
        byte[] encodedByte = arrayByte.toByteArray()
        
        
        Base64.Encoder enc = Base64.getEncoder();
        byte[] strenc = enc.encode(encodedByte);
        String encode = new String(strenc, "UTF-8");
        
        log.info("Encode*****************************");
        log.info(encode)
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            report = encode
        }
        
        render(status: 200, contentType: 'application/json', text: json)   
    }

    
    private List getPeriodosEval(Long turnoId,Long asigId, Long periId){
        log.info("Ingresando a getPeriodosEval")
        String hql ="""         SELECT 
                                    a.descripcion,c.id,c.nombre
                                    ,tcd.division.nombre                           

                                FROM Asignatura a
                                INNER JOIN a.curso c
                                INNER JOIN a.users u
                                INNER JOIN a.detalleAsigPerEval dpe
                                INNER JOIN TurnoCursoDivision tcd on tcd.curso.id = c.id 
                                    and tcd.turno.id=:turnoId
                                WHERE a.id=:asigId 
                     """
        def parameters = [turnoId:turnoId,asigId:asigId,periId:periId]
        def list = Asignatura.executeQuery(hql,parameters)         
    
    /*
                        SELECT 
                            a.descripcion,c.id,c.nombre
                            ,(SELECT tcd.division.nombre FROM TurnoCursoDivision tcd WHERE 
                                tcd.curso.id = c.id AND tcd.turno.id=1
                            )                             

                        FROM Asignatura a
                        INNER JOIN a.curso c
                        INNER JOIN a.users u
                        INNER JOIN a.detalleAsigPerEval dpe
                        WHERE a.id=3 and dpe.periodoEvaluacion.id=2    
    
    */
    }    
    

    

}


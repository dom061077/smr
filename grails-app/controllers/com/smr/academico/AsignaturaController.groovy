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

class AsignaturaController {
	static responseFormats = ['json', 'xml']
        def asignaturaService
        def springSecurityService
        def dataSource
	
    def index() { }
    
    def getAsignaturas(){
        log.info("Ingresando a getAsignaturas")
        def list = Asignatura.list(sort:"descripcion")
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
        log.info("Ingresando a show Examen")
        def currentUser=springSecurityService.getCurrentUser()
        String hql = """ SELECT e.id,a.id,a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,pe.periodoEval.descripcion
                         ,e.tipoExamen.descripcion ,e.puntuacion,i.periodoLectivo.anio
                        FROM Inscripcion i inner join i.periodosInscEval pe inner  join i.detalle d 
                         inner join pe.asignatura a
                         inner join pe.examenes e
                         inner join a.users u
                         inner join d.tcDivision tcd 
                         where u.id=:userId and i.periodoLectivo.state=true
                         and a.id = :asigId and i.alumno.id = :alumnoId                       
                        
                         order by   pe.periodoEval.descripcion,e.tipoExamen.ordenCompendio"""
        
        def parameters = [userId:currentUser.id,asigId:asigId,alumnoId:alumnoId]

    
        def list = Examen.executeQuery(hql,parameters)  
        return[list:list]
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
    
    String exportarCompendioXls(Long asignaturaId,Long periodoLectivoId){
        log.info("Ingresndo alexportarCompendioXls ")
        //String[] columns = ["Name", "Email", "Date Of Birth", "Salary"]
        
        String hql = """ 
                    SELECT pe,u,det FROM PeriodoEvaluacion  pe
                    INNER JOIN pe.detalleAsigPerEval det
                    INNER JOIN det.asignatura a
                    INNER JOIN a.users u
                    WHERE  det.asignatura.id = :asigId
                    AND pe.periodoLectivo.id = :periodoLectivoId
                 """
        def parameters=[asigId:asignaturaId, periodoLectivoId:periodoLectivoId]
        List list = PeriodoEvaluacion.executeQuery(hql,parameters)          
        
       Workbook workbook = new XSSFWorkbook();        
        
       CreationHelper createHelper = workbook.getCreationHelper();
 
        // Create a Sheet
        Sheet sheet = workbook.createSheet("Compendio");  
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(HSSFPrintSetup.LEGAL_PAPERSIZE); 
        //------fijando tamaños de columnas----
        for(int i=0;i<34;i++){
            sheet.setColumnWidth(i,256*3)
            
        }
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
        CellRangeAddress mergedRegions = sheet.getMergedRegion(1);
        RegionUtil.setBorderLeft(BorderStyle.THIN, mergedRegions, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, mergedRegions, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, mergedRegions, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, mergedRegions, sheet);
        
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
        cell.setCellStyle(headerCellStyle)
        cell.setCellValue("PROFESOR")
        sheet.addMergedRegion(new CellRangeAddress(2,2,3,5))
        
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


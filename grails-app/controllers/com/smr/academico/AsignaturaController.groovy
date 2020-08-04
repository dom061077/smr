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

}

/*
 
import com.smr.inscripcion.Inscripcion
import com.smr.academico.PeriodoEvalInscAsignatura
import com.smr.academico.Examen


def list = Inscripcion.executeQuery(""" 
                    SELECT i.id, i.alumno.id,i.alumno.apellido,pe.asignatura.id,
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
                                       
                               ,(select count(id) FROM PeriodoEvalInscAsignatura WHERE  inscripcion.id = i.id and asignatura.id=pe.asignatura.id)
                    FROM Inscripcion i
                    INNER JOIN i.periodosInscEval pe
                    
                    where i.anulada=false
                    GROUP BY i.id, i.alumno.id,i.alumno.apellido,pe.asignatura.id
                
                """)

//def list =PeriodoEvalInscAsignatura.list()
//def list = Examen.executeQuery("SELECT puntuacion FROM Examen where periEvalInscAsig.id=8")                
                
                
// def list2 =    PeriodoEvalInscAsignatura.executeQuery("FROM PeriodoEvalInscAsignatura where inscripcion.id=56") 
 *  
 * */
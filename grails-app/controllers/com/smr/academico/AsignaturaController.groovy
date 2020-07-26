package com.smr.academico


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import com.smr.security.User
//import org.hibernate.Criteria
import java.util.Collections
import java.util.ArrayList
import grails.web.JSONBuilder
import groovy.sql.Sql

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
        
        def c = Examen.createCriteria()
        def cantExamenes = 0
        cantExamenes = c.list{
            projections {
                inscripcion{
                    groupProperty("alumno")
                    countDistinct("id")
                    detalle{
                        tcDivision{
                            curso{
                                groupProperty("nombre")
                            }
                        }
                        
                    }
                }
                
            }
            asignatura{
                if(asigId)
                    eq("id",asigId)
                users{
                    'in' ("id",[currentUser.id])
                }
            }
            inscripcion{
                eq("anulada",false)
                periodoLectivo{
                    eq("state",false)
                }
                alumno{
                    if(filter)
                        ilike("apellidoNombre",'%'+filter+'%')
                }
                
            }
        }
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            count = cantExamenes.size
        }
        render (status: 200, contentType: 'application/json', text: json)
    }
    
    def listExamenes( Long asigId, String filter,int start, int limit){
        log.info("Ingresando a listExamenes.  asigId: "+asigId+" filter: "+filter)
        def currentUser=springSecurityService.getCurrentUser()
        def pagingconfig=[
            max: limit as Integer?:10,
            offset:start as Integer?:0
        ]
        
/*
 * 
 SELECT al.id, al.apellido,al.nombre,a.descripcion,pe.id
	,SUM(IF(te.promediable=TRUE AND tp.complementario=FALSE, e.puntuacion , 0 )) AS totalPromedio FROM inscripcion i
INNER JOIN alumno al ON i.alumno_id = al.id
INNER JOIN periodo_lectivo pl ON i.periodo_lectivo_id = pl.id
INNER JOIN examen e ON i.id=e.inscripcion_id
INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id
INNER JOIN asignatura a ON e.asignatura_id = a.id
INNER JOIN periodo_evaluacion pe ON e.periodo_eval_id = pe.id
WHERE pl.state=0 AND a.id=3
GROUP BY al.id, al.apellido,al.nombre,a.descripcion,pe.id

SELECT CASE 0 WHEN 0  0 ELSE 1 END
 * 
 * */        

        String hql = "SELECT tpe.asignaturaId,tpe.asignaturaDescripcion,tpe.alumnoId,tpe.apellido,tpe.nombre"
	hql = hql+ " ,tpe.descripcionCurso,tpe.descripcionDivision"
	hql = hql+ " ,SUM(IF(totalPromedio<6,totalComplementario,totalPromedio))/COUNT(tpe.periodoEvaluacion)"
        hql = hql+ " FROM (SELECT a.id AS asignaturaId,a.descripcion asignaturaDescripcion,al.id alumnoId,al.apellido,al.nombre"
	hql = hql+ ",c.descripcion descripcionCurso,d.descripcion descripcionDivision,pe.id,pe.descripcion periodoEvaluacion"	
	hql = hql+ " ,SUM(IF(te.promediable=TRUE AND te.complementario=FALSE, e.puntuacion , 0 ))/SUM(IF(te.promediable=TRUE AND te.complementario=FALSE,1, 0 )) AS totalPromedio "
	hql = hql+ " ,SUM(IF(te.promediable=FALSE AND te.complementario=TRUE, e.puntuacion , 0 ))/SUM(IF(te.promediable=FALSE AND te.complementario=TRUE,1, 0 )) AS totalComplementario "
        hql = hql+ " FROM inscripcion i"
        hql = hql+ " INNER JOIN detalle_inscripcion di ON i.id=di.inscripcion_id"
        hql = hql+ " INNER JOIN turno_curso_division tcd ON tcd.id =di.tc_division_id"
        hql = hql+ " INNER JOIN curso c ON tcd.curso_id = c.id"
        hql = hql+ " INNER JOIN division d ON tcd.division_id = d.id"
        hql = hql+ " INNER JOIN alumno al ON i.alumno_id = al.id"
        hql = hql+ " INNER JOIN periodo_lectivo pl ON i.periodo_lectivo_id = pl.id"
        hql = hql+ " INNER JOIN examen e ON i.id=e.inscripcion_id"
        hql = hql+ " INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id"
        hql = hql+ " INNER JOIN asignatura a ON e.asignatura_id = a.id"
        hql = hql+ " INNER JOIN asignatura_users au ON a.id=au.asignatura_id"
        hql = hql+ " INNER JOIN periodo_evaluacion pe ON e.periodo_eval_id = pe.id"
        hql = hql+ " WHERE pl.state=0 and au.user_id = :userId "
        def parameters = [userId:currentUser.id,offset:start,max:limit]
        if(asigId){
            hql = hql + " and a.id = :asigId"
            parameters.put(asigId,asigId)
        }
       
        
        hql = hql+ " GROUP BY al.id, al.apellido,al.nombre,a.descripcion,pe.id,c.descripcion,d.descripcion,pe.descripcion"
        hql = hql+ " ) tpe"
        hql = hql+ " GROUP BY tpe.asignaturaId,tpe.asignaturaDescripcion,tpe.alumnoId,tpe.apellido,tpe.nombre"
        hql = hql+ "        ,tpe.descripcionCurso,tpe.descripcionDivision"        
        
        def list = Examen.executeSqQuery(hql,parameters)
        
        return[list:list]
    }
    
    def showExamenes(Long asigId, Long alumnoId){
        log.info("Ingresando a show Examen")
        def currentUser = springSecurityService.getCurrentUser()
        String hql = "SELECT e.id,e.asignatura.id,a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,e.periodoEval.descripcion"
        hql = hql +" ,e.tipoExamen.descripcion ,e.puntuacion,e.inscripcion.periodoLectivo.anio"
        hql = hql+" FROM Examen e inner join e.inscripcion i inner  join i.detalle d "
        hql = hql+" inner join e.inscripcion.periodoLectivo p"
        hql = hql+" inner join e.asignatura a"
        hql = hql+" inner join e.asignatura.users u"
        hql = hql+" inner join d.tcDivision tcd "
        hql = hql+" inner join tcd.curso"
        hql = hql+" inner join tcd.division"
        hql = hql+" where u.id=:userId and p.state=false"
       
        hql = hql + " and a.id = :asigId and i.alumno.id = :alumnoId"
        hql = hql + " order by   e.periodoEval.descripcion,e.tipoExamen.ordenCompendio"
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

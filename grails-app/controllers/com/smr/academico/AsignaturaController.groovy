package com.smr.academico


import grails.rest.*
import grails.converters.*
import grails.web.JSONBuilder
import com.smr.security.User
//import org.hibernate.Criteria
import java.util.Collections
import java.util.ArrayList

class AsignaturaController {
	static responseFormats = ['json', 'xml']
        def asignaturaService
        def springSecurityService
	
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
        String hql = "SELECT a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,e.periodoEval.descripcion"
        hql = hql+" ,SUM(case when e.tipoExamen.promediable=true and e.tipoExamen.complementario=false then e.puntuacion else 0 end)/count(case when e.tipoExamen.promediable=true and e.tipoExamen.complementario=false then 1 else 0 end)"
        hql = hql+" ,SUM(case when e.tipoExamen.complementario=true then e.puntuacion else 0 end)"
        hql = hql+" FROM Examen e inner join e.inscripcion i inner  join i.detalle d "
        hql = hql+" inner join e.inscripcion.periodoLectivo p"
        hql = hql+" inner join e.asignatura a"
        hql = hql+" inner join e.asignatura.users u"
        hql = hql+" inner join d.tcDivision tcd "
        hql = hql+" inner join tcd.curso"
        hql = hql+" inner join tcd.division"
        hql = hql+" where u.id=:userId and p.state=false"
        def parameters = [userId:currentUser.id,offset:start,max:limit]
        if(asigId){
            hql = hql + " and a.id = :asigId"
            parameters.put('asigId',asigId)
        }
        
        hql = hql +" group by a.descripcion, i.alumno.id,i.alumno.apellido, i.alumno.nombre,tcd.curso.nombre,tcd.division.nombre,e.periodoEval.descripcion"        
        
        def list = Examen.executeQuery(hql,parameters)
        log.info("LIST: "+list)
        return[list:list]
    }

}

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
        def list = Examen.createCriteria().list(pagingconfig){
              
              //createAlias('')
              inscripcion{
 
                    projections{
                         detalle{
                              tcDivision{
                                  curso{
                                      groupProperty("nombre")
                                  }
                              }

                          }      
                        alumno{
                          
                              groupProperty  ('id')
                              groupProperty  ('apellido')
                              groupProperty  ('nombre')

                                                       
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
            //setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        }
        log.info("LIST: "+list)
        return[list:list]
    }

}

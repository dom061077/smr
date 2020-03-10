package com.smr.inscripcion


import grails.rest.*
import grails.converters.*

class InscripcionController {
	static responseFormats = ['json', 'xml']

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
    
    def index() { }
}

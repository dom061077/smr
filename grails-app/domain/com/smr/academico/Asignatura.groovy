package com.smr.academico

import com.smr.inscripcion.Curso
import com.smr.security.User

import groovy.transform.Sortable
import groovy.transform.ToString

@Sortable(includes = ['descripcion'])
@ToString
class Asignatura {
    String descripcion
    
    Curso curso
    
    
    
    
    
    static constraints = {
        descripcion(blank:false,nullable:false)
        //asignaturaPeriodoEvaluacion nullable:true
    }
    
    static mapping = {
        //users lazy : false
        //detalleAsigPerEval lazy : false
    
    }     
    
    
    static belongsTo = [curso:Curso]
    
    static hasMany = [users:User
        ,detalleAsigPerEval:AsignaturaPeriodoEvaluacion]
    
    
        
    
}

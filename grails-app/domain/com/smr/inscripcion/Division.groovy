package com.smr.inscripcion

import grails.plugins.orm.auditable.Auditable

class Division implements Auditable {

    String nombre
    
    static belongsTo = [Curso,Turno]
    
    static hasMany = [cursos:Curso,turnos:Turno]
    
    static constraints = {
    }
}

package com.smr.inscripcion

import com.smr.academico.Asignatura

class Curso {
    String nombre
    Turno turno
    
    static belongsTo = [turno:Turno]
    
    static hasMany = [asignaturas : Asignatura]
    
    static constraints = {
    }
}
    
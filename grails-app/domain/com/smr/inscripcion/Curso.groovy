package com.smr.inscripcion

import com.smr.academico.Asignatura

class Curso {
    String nombre
    
    
    static belongsTo = Turno
    
    static hasMany = [asignaturas : Asignatura, divisiones:Division, turnos:Turno]
    
    static constraints = {
    }
}
    
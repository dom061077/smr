package com.smr.inscripcion

class Division {
    Curso curso
    Turno turno
    String nombre
    
    static belongsTo = [curso:Curso,turno:Turno]
    
    static constraints = {
    }
}

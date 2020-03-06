package com.smr.inscripcion

class Curso {
    String nombre
    Turno turno
    
    static belongsTo = [turno:Turno]
    
    static constraints = {
    }
}
    
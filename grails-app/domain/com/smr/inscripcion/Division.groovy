package com.smr.inscripcion

class Division {
    Curso curso

    String nombre
    
    static belongsTo = [curso:Curso]
    
    static constraints = {
    }
}

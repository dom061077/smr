package com.smr.inscripcion

class Turno {
    String nombre
    
    static hasMany = [cursos:Curso,divisiones:Division]
    
    static constraints = {
        
    }
    
    static mapping={
        
    }
}

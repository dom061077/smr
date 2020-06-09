package com.smr.inscripcion

class TurnoCursoDivision {
    
    
    Turno turno
    Curso curso
    Division division
    

    static constraints = {
        division(unique:['turno','curso'])
        
    }
}

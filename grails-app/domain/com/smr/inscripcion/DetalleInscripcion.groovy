package com.smr.inscripcion

class DetalleInscripcion {
    Inscripcion inscripcion
    TurnoCursoDivision tcDivision
    
    static belongsTo = [inscripcion:Inscripcion]
    static constraints = {
    }
}

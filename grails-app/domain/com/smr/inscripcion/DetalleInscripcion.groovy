package com.smr.inscripcion

class DetalleInscripcion {
    Inscripcion inscripcion
    Division division
    
    static belongsTo = [inscripcion:Inscripcion]
    static constraints = {
    }
}

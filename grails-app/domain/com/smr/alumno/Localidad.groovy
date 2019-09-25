package com.smr.alumno

class Localidad {
    String nombre
    int codigoPostal
    Provincia provincia
    static belongsTo = [provincia:Provincia]
    static constraints = {
    }
}

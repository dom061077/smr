package com.smr.inscripcion

import com.smr.alumno.Alumno


class Inscripcion {
    Alumno alumno
    PeriodoLectivo periodoLectivo
    java.sql.Date fecha
    
    static hasMany = [detalle:DetalleInscripcion]
    
    static constraints = {
    }
}

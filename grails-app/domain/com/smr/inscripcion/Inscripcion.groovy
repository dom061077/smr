package com.smr.inscripcion

import com.smr.alumno.Alumno

import com.smr.escuela.Escuela


class Inscripcion {
    Alumno alumno
    PeriodoLectivo periodoLectivo
    java.sql.Date fecha

    boolean anulada=false
    
    Escuela escuelaProviene

    
    
    
    
    static hasMany = [detalle:DetalleInscripcion]
    
    static constraints = {
        alumno(unique:['periodoLectivo'])
        
        
    }
}

package com.smr.movimiento

import java.sql.Date 
import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.orm.auditable.Auditable
import com.smr.alumno.Alumno
import com.smr.escuela.Escuela
import com.smr.escuela.Division



class Pase implements Auditable{
    Date fecha
    Alumno alumno
    Escuela escuelaDestino
    Division division
    
    static constraints = {
        fecha (nullable:false, blank:false)
        alumno (nullable:false, blank:false)
    }
    
}

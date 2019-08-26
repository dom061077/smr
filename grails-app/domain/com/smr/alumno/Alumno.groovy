package com.smr.alumno

import java.sql.Date 
import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
@Resource(uri='/alumnos',formats=['json', 'xml'])
class Alumno {
    String apellido
    String nombre
    java.sql.Date fechaNacimiento
    
    static constraints = {
        fechaNacimiento(nullable:true, blank:true)
    }
}

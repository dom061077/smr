package com.smr.alumno

import java.sql.Date 
import grails.rest.*


@Resource(uri='/alumnos',formats=['json', 'xml'])
class Alumno {
    String apellido
    String nombre
    java.sql.Date fechaNacimiento
    
    static constraints = {
    }
}

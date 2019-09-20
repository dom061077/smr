package com.smr.alumno

import java.sql.Date 
import grails.rest.*
import grails.plugin.springsecurity.annotation.Secured

@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
@Resource(uri='/api/alumnos',formats=['json', 'xml'])
class Alumno {
    String apellido
    String nombre
    java.sql.Date fechaNacimiento
    int dni
    String cuil
    String direccion
    boolean planSocial
    boolean trabaja
    
    String apellidoTutor
    String nombreTutor
    String parentescoTutor
    int dniTutor
    String cuilTutor
    String telefono1
    String telefono2
    boolean fotoDni
    boolean constanciaCuil
    boolean constancia6grado
    boolean actaNacimiento
    boolean constaciaRegular
    boolean foto4x4
    boolean fotoCarnetVac
    boolean fichaMedica
    boolean aptitudFisica
    boolean grupoSanguineo
    boolean fichaInscripcion
    boolean libreta6grado
    boolean fotocopiaLibroMatriz
    boolean fotocopiaDniTutor
    boolean constanciaCuilTutor
    
    
    
    
    static constraints = {
        fechaNacimiento(nullable:true, blank:true)
    }
}

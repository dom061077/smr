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
    boolean planSocial=false
    boolean trabaja=false
    
    String apellidoTutor
    String nombreTutor
    String parentescoTutor
    int dniTutor
    String cuilTutor
    String telefono1
    String telefono2
    boolean fotoDni=false
    boolean constanciaCuil=false
    boolean constancia6grado=false
    boolean actaNacimiento=false
    boolean constaciaRegular=false
    boolean foto4x4=false
    boolean fotoCarnetVac=false
    boolean fichaMedica=false
    boolean aptitudFisica=false
    boolean grupoSanguineo=false
    boolean fichaInscripcion=false
    boolean libreta6grado=false
    boolean fotocopiaLibroMatriz=false
    boolean fotocopiaDniTutor=false
    boolean constanciaCuilTutor=false
    
    
    
    
    static constraints = {
        fechaNacimiento(nullable:true, blank:true)
        cuil(nullable:true,blank:true)
        cuilTutor(nullable:true,blank:true);
        telefono1(nullable:true,blank:true)
        telefono2(nullable:true,blank:true)
    }
}

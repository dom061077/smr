package com.smr.inscripcion

import com.smr.alumno.Alumno

import com.smr.escuela.Escuela
import grails.plugins.orm.auditable.Auditable


class Inscripcion implements Auditable{
    Alumno alumno
    PeriodoLectivo periodoLectivo
    java.sql.Date fecha = new java.sql.Date(new java.util.Date().getTime())

    boolean anulada=false
    
    Escuela escuelaProviene

    
    
    
    
    static hasMany = [detalle:DetalleInscripcion]
    
    static constraints = {
        alumno(validator:{val,obj->
            if(!obj.id){
                def list = Inscripcion.createCriteria().list(){
                    eq("alumno.id",obj.alumno.id)
                    eq("periodoLectivo.id",obj.periodoLectivo.id)
                    eq("anulada",false)
                }
                
                if(list.size()>0)
                    return "alumno.inscripcion.exists"
            }
            
        })
        escuelaProviene (nullable:true,blank:true)
        
    }
}

package com.smr.inscripcion

import com.smr.alumno.Alumno

import com.smr.escuela.Escuela
import grails.plugins.orm.auditable.Auditable
import com.smr.academico.Examen


class Inscripcion implements Auditable{
    Alumno alumno
    PeriodoLectivo periodoLectivo
    java.sql.Date fecha = new java.sql.Date(new java.util.Date().getTime())
    String detalleInsc

    boolean anulada=false
    
    Escuela escuelaProviene

    String getDetalleInsc(){
        String detStr=''
        
        this.detalle.each{
            detStr = it.tcDivision.curso.nombre+'-'+it.tcDivision.division.nombre+'-'+it.tcDivision.turno.nombre
        }
        return detStr
    }
    
    
    
    static hasMany = [detalle:DetalleInscripcion,examenes:Examen]
    
    static transients = ['detalleInsc']
    
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

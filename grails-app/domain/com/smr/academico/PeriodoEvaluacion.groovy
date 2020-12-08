package com.smr.academico

import com.smr.inscripcion.PeriodoLectivo
import com.smr.inscripcion.Inscripcion

/**
 * Configuración de los periodos y los examenes
 *  
*/

class PeriodoEvaluacion {
    TipoPeriodoEvaluacion tipoPeriodoEval/*indica si es trimestral o cuatrimestral*/
    PeriodoLectivo periodoLectivo
    
    String descripcion 

    int ordenCompendio
    
    int cantClases

    
    static belongsTo = [tipoPeriodoEval: TipoPeriodoEvaluacion

            ,periodoLectivo:PeriodoLectivo]
    
    static hasMany = [detalleAsigPerEval:AsignaturaPeriodoEvaluacion]
    
    static mapping = {
        //tablePerHierarchy false
    }     
    
    static constraints = {
        //asignaturaPeriodoEvaluacion nullable:true
    }
    
    
}

/*
 * 
import com.smr.academico.PeriodoEvalInscAsignatura
import com.smr.academico.AsignaturaPeriodoEvaluacion 
import com.smr.academico.Asignatura
import com.smr.academico.PeriodoEvaluacion

String hql = """ 
                SELECT pe FROM PeriodoEvaluacion  pe
                INNER JOIN pe.detalleAsigPerEval det
                WHERE  det.asignatura.id = 3
                AND pe.periodoLectivo.anio = 2020
             """
             
List list = PeriodoEvaluacion.executeQuery(hql)             

list.each{
    println("Periodo evalucion: "+it.descripcion)
} 


 * 
 * */

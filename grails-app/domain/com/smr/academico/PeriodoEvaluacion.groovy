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

    int cantClases=0

    
    static belongsTo = [tipoPeriodoEval: TipoPeriodoEvaluacion

            ,periodoLectivo:PeriodoLectivo]
    
    static hasMany = [configExamenes:ConfiguracionExamenesDetalle
        ,asignaturaPeriodoEvaluacion:AsignaturaPeriodoEvaluacion]
    
    static mapping = {
        tablePerHierarchy false
    }     
    
    static constraints = {
        asignaturaPeriodoEvaluacion nullable:true
    }
    
    
}

package com.smr.academico

import com.smr.inscripcion.PeriodoLectivo


class PeriodoEvaluacion {
    TipoPeriodoEvaluacion tipoPeriodoEval/*indica si es trimestral o cuatrimestral*/
    PeriodoLectivo periodoLectivo
    String descripcion 
    
    int cantClases=0
    
    
    static belongsTo = [tipoPeriodoEval: TipoPeriodoEvaluacion
            ,periodoLectivo:PeriodoLectivo]
    
    static hasMany = [examenes:Examen,configExamenes:ConfiguracionExamenesDetalle]
    
    static constraints = {
    }
}

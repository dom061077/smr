package com.smr.academico

class ConfiguracionExamenes {
    TipoPeriodoEvaluacion tipoPeriodoEval
    
    Asignatura asignatura
    
    int cantidad

    static constraints = {
    }
    
    static hasMany = [detalle:ConfiguracionExamenesDetalle]
}

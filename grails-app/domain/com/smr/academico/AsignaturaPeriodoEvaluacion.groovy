package com.smr.academico

class AsignaturaPeriodoEvaluacion {
    
    Asignatura asignatura
    PeriodoEvaluacion periodoEvaluacion
    
    static constraints = {
        asignatura nullable:false
        periodoEvaluacion nullable:false
    }
}

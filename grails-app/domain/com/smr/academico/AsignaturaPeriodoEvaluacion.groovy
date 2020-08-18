package com.smr.academico

class AsignaturaPeriodoEvaluacion {
    
    Asignatura asignatura
    PeriodoEvaluacion periodoEvaluacion
    
    int cantClases
    
    static constraints = {
        asignatura nullable:false
        periodoEvaluacion nullable:false
    }
}

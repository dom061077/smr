package com.smr.academico

class PeriodoEvaluacion {
    TipoPeriodoEvaluacion tipoPeriodoEval
    
    
    int cantClases
    
    
    static belongsTo = [tipoPeriodoEval: TipoPeriodoEvaluacion]
    
    static hasMany = [examenes:Examen]
    
    static constraints = {
    }
}

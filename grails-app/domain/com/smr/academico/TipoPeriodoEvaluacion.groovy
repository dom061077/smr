package com.smr.academico

class TipoPeriodoEvaluacion {
    String descripcion
    
    static hasMany = [configExamenes:ConfiguracionExamenesDetalle] 
    
    static constraints = {
    }
}

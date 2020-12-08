package com.smr.academico

class TipoPeriodoEvaluacion {
    String descripcion
    int ordenCompendio
    
    static hasMany = [configExamenes:ConfiguracionExamenesDetalle] 
    
    static constraints = {
    }
}

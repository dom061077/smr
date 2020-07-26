package com.smr.academico

/**
 * Configura el tipo y cantidad de exámenes
*/

class ConfiguracionExamenesDetalle {
    
    TipoExamen tipoExamen
    
    PeriodoEvaluacion periodoEval
    
    int cantidad
    
    static belongsTo = [periodoEval:PeriodoEvaluacion]
    
   
    
    static constraints = {
    }
}

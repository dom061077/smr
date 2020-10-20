package com.smr.academico

/**
 * Configura el tipo y cantidad de exámenes
*/

class ConfiguracionExamenesDetalle {
    
    TipoExamen tipoExamen
    
    
    
    int cantidad
    
    static belongsTo = [configExamenes:ConfiguracionExamenes]
    
   
    
    static constraints = {
        tipoPeriodoEval nullable:true
    }
}

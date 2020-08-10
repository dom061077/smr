package com.smr.academico

import com.smr.inscripcion.Inscripcion




class PeriodoEvalInscAsignatura {
    
    Asignatura asignatura
    Inscripcion inscripcion
    PeriodoEvaluacion periodoEval
    
    int cantInasist
    
    
    BigDecimal totalPromedio
    
    static hasMany = [examenes:Examen]
    
    static belongsTo = [inscripcion:Inscripcion]

    static constraints = {
    }
    
    static transients= ['totalPromedio']
    
    static mapping = {
        totalPromedio formula : "SELECT SUM(IF(te.promediable=TRUE,puntuacion,0)/IF(te.promediable=TRUE,1,0)) FROM examen e INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id WHERE peri_eval_insc_asig_id = id"
        totalComplementario formula : "SELECT SUM(IF(te.complementario=TRUE,puntuacion,0)/IF(te.complementario=TRUE,1,0)) FROM examen e INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id WHERE peri_eval_insc_asig_id = id"
    }
}

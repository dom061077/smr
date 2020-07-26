package com.smr.academico

import com.smr.inscripcion.Inscripcion




class PeriodoEvalInscAsignatura {
    
    Asignatura asignatura
    Inscripcion inscripcion
    PeriodoEvaluacion periodoEval
    
    static hasMany = [examenes:Examen]

    static constraints = {
    }
}

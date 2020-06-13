package com.smr.academico

import com.smr.alumno.Alumno
import com.smr.inscripcion.Inscripcion

class Examen {
    
    String descripcion
    
    TipoExamen tipoExamen
    PeriodoEvaluacion periodoEval
    
    Asignatura asignatura
    
    Inscripcion inscripcion
    
    
    static belongsTo = [tipoExamen:TipoExamen,periodoEval:PeriodoEvaluacion
                        ,inscripcion:Inscripcion
                       ]
    
    

    static constraints = {
    }
}

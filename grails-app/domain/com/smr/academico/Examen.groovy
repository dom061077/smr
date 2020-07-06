package com.smr.academico

import com.smr.alumno.Alumno
import com.smr.inscripcion.Inscripcion
import java.math.BigDecimal
class Examen {
    
    String descripcion
    
    TipoExamen tipoExamen
    PeriodoEvaluacion periodoEval
    
    Asignatura asignatura
    
    Inscripcion inscripcion
    
    BigDecimal puntuacion
    
    
    static belongsTo = [tipoExamen:TipoExamen,periodoEval:PeriodoEvaluacion
                        ,inscripcion:Inscripcion
                       ]
    
    

    static constraints = {
    }
}

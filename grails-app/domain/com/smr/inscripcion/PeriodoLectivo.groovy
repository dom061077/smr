package com.smr.inscripcion

import grails.plugins.orm.auditable.Auditable
import com.smr.academico.PeriodoEvaluacion


class PeriodoLectivo implements Auditable {
    int anio
    boolean state
    
    static hasMany = [periodosEval:PeriodoEvaluacion]
    
    static constraints = {
    }
}

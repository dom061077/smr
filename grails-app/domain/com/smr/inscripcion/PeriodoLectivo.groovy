package com.smr.inscripcion

import grails.plugins.orm.auditable.Auditable
import com.smr.academico.PeriodoEvaluacion


class PeriodoLectivo implements Auditable {
    int anio
    boolean state //0=inactivo, 1=activo
    
    static hasMany = [periodosEval:PeriodoEvaluacion]
    
    static constraints = {
    }
}

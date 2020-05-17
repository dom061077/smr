package com.smr.inscripcion

import grails.plugins.orm.auditable.Auditable


class PeriodoLectivo implements Auditable {
    int anio
    boolean state
    static constraints = {
    }
}

package com.smr.inscripcion

import grails.plugins.orm.auditable.Auditable

class Division implements Auditable {
    Curso curso

    String nombre
    
    static belongsTo = [curso:Curso]
    
    static constraints = {
    }
}

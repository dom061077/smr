package com.smr.escuela

import grails.plugins.orm.auditable.Auditable

class Escuela implements Auditable {
    
    String descripcion
    String cue

    static constraints = {
    }
}

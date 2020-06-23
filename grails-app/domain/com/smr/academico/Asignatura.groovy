package com.smr.academico

import com.smr.inscripcion.Curso
import com.smr.security.User

class Asignatura {
    String descripcion
    
    Curso curso
    
    
    
    static constraints = {
        descripcion(blank:false,nullable:false)
    }
    
    
    
    static belongsTo = [curso:Curso]
    static hasMany = [User]
    
}

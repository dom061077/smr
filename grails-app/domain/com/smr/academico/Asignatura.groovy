package com.smr.academico

import com.smr.inscripcion.Curso

class Asignatura {
    String descripcion
    
    Curso curso
    
    static constraints = {
        descripcion(blank:false,nullable:false)
    }
    
    static belongsTo = [curso:Curso]
    
}

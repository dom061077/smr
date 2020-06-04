package com.smr.academico

import com.smr.inscripcion.Curso

class Asignatura {
    String descripcion
    
    
    
    static constraints = {
        descripcion(blank:false,nullable:false)
    }
    
    static belongsTo = Curso
    static hasMany = [cursos:Curso]
}

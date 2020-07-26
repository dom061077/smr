package com.smr.academico

import com.smr.alumno.Alumno

import java.math.BigDecimal
class Examen {
    
    String descripcion
    
    TipoExamen tipoExamen
    
    PeriodoEvalInscAsignatura periEvalInscAsig
    
    
    
    
    BigDecimal puntuacion
    
    
    static belongsTo = [tipoExamen:TipoExamen
                        ,periEvalInscAsig:PeriodoEvalInscAsignatura ]
    
    static mapping ={
        totalPromedio formula : 'SUM(case when e.tipoExamen.promediable=true and e.tipoExamen.complementario=false then e.puntuacion else 0 end)/sum(case when e.tipoExamen.promediable=true and e.tipoExamen.complementario=false then 1 else 0 end)'
        totalComplementario formula : 'SUM(case when e.tipoExamen.complementario=true then e.puntuacion else 0 end)/sum(case when e.tipoExamen.promediable=false and e.tipoExamen.complementario=true then 1 else 0 end)'
    }
    

    static constraints = {
    }
}

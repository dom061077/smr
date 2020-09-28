package com.smr.academico

import com.smr.inscripcion.Inscripcion
import java.math.RoundingMode




class PeriodoEvalInscAsignatura {
    
    Asignatura asignatura
    Inscripcion inscripcion
    PeriodoEvaluacion periodoEval
    
    int cantInasist

    boolean cerrado
    

    
    
    
    BigDecimal getTotalPromedio(){
        BigDecimal total=BigDecimal.ZERO
        int cant=0
        examenes.each{
            if(it.tipoExamen.promediable){
                total = total.add(it.puntuacion)
                cant++
            }
        }
        return total.divide(cant, RoundingMode.HALF_UP)
    }
    
    BigDecimal getNotaFinal(){
        if(getTotalPromedio().compareTo(new BigDecimal("6"))<0)
            return getPuntuacionComplementaria()
        else
            return getTotalPromedio()
            
    }
    
    BigDecimal getPuntuacionComplementaria(){
        BigDecimal complementario
        examenes.each{
            if(it.tipoExamen.complementario){
                complementario=it.puntuacion
                return
            }
        }
        return complementario
    }
    
    static hasMany = [examenes:Examen]
    
    static belongsTo = [inscripcion:Inscripcion]

    static constraints = {
    }
    
    static transients= ['totalPromedio','puntuacionComplementaria','notaFinal']
    
    static mapping = {
        //totalPromedio formula : "SELECT SUM(IF(te.promediable=TRUE,puntuacion,0)/IF(te.promediable=TRUE,1,0)) FROM examen e INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id WHERE peri_eval_insc_asig_id = id"
        //totalComplementario formula : "SELECT SUM(IF(te.complementario=TRUE,puntuacion,0)/IF(te.complementario=TRUE,1,0)) FROM examen e INNER JOIN tipo_examen te ON e.tipo_examen_id = te.id WHERE peri_eval_insc_asig_id = id"
    }
}

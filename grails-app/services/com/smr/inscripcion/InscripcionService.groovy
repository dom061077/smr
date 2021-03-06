package com.smr.inscripcion

import grails.gorm.transactions.Transactional
import java.sql.Date
import com.smr.alumno.Alumno
import com.smr.enums.CondicionEnum
import com.smr.inscripcion.Inscripcion
import com.smr.academico.Examen
import com.smr.academico.TipoExamen
import com.smr.academico.PeriodoEvaluacion
import com.smr.academico.PeriodoEvalInscAsignatura
import com.smr.academico.ConfiguracionExamenes


@Transactional
class InscripcionService {
    
    private saveExamenes(TurnoCursoDivision tcd,Inscripcion insc){
        List listConfig = ConfiguracionExamenes.list().findAll{p->
            (p.asignatura.curso.id.compareTo(tcd.curso.id)==0)
            
        }
        listConfig.each{ conf->
            def periodoEval
            def pEvalInscAsig
            if(conf.cantidad>1){
                for(int i=1;i<=conf.cantidad;i++){
                  periodoEval = new PeriodoEvaluacion(tipoPeriodoEval:conf.tipoPeriodoEval
                    ,periodoLectivo:insc.periodoLectivo
                    ,descripcion:""+i,ordenCompendio:i).save() 
                  pEvalInscAsig=new PeriodoEvalInscAsignatura(asignatura:conf.asignatura
                    ,inscripcion:insc,periodoEval:periodoEval,cantInasist:0)
                    conf.detalle.each{ ced ->
                        TipoExamen tipoExamen=ced.tipoExamen
                        int cantExamenes = ced.cantidad
                        log.info("Tipo de examen a guardar: "+ced.tipoExamen)
                        for(int j=1; j<=cantExamenes; j++){
                            pEvalInscAsig.addToExamenes(
                                    new Examen(descripcion:j+'°'+tipoExamen.descripcion
                                        ,tipoExamen:tipoExamen,periEvalInscAsig:pEvalInscAsig
                                        ,puntuacion:new BigDecimal("0.00")
                                        ,inscripcion:insc)
                                )
                        }

                    }                
                    pEvalInscAsig.save()                 
                }
            }else{
                periodoEval = new PeriodoEvaluacion(tipoPeriodoEval:conf.tipoPeriodoEval
                    ,periodoLectivo:insc.periodoLectivo
                    ,descripcion:conf.tipoPeriodoEval.descripcion).save()    
                pEvalInscAsig=new PeriodoEvalInscAsignatura(asignatura:conf.asignatura
                    ,inscripcion:insc,periodoEval:periodoEval,cantInasist:0)
                conf.detalle.each{ ced ->
                    TipoExamen tipoExamen=ced.tipoExamen
                    int cantExamenes = ced.cantidad
                    log.info("Tipo de examen a guardar: "+ced.tipoExamen)
                    for(int j=1; j<=cantExamenes; j++){
                        pEvalInscAsig.addToExamenes(
                                new Examen(descripcion:j+'°'+tipoExamen.descripcion
                                    ,tipoExamen:tipoExamen,periEvalInscAsig:pEvalInscAsig
                                    ,puntuacion:new BigDecimal("0.00")
                                    ,inscripcion:insc)
                            )
                    }

                }                
                pEvalInscAsig.save()                 
            }
            
   
            
        }
    }
    /*private saveExamenes(Inscripcion insc){
        List listConfig = PeriodoEvaluacion.findAllByPeriodoLectivo(insc.periodoLectivo)
        log.info("Salvando examenes")
        listConfig.each{pe->
            log.info("Cantidad de asignaturas en periodo evaluacion: "+pe)
            pe.detalleAsigPerEval.each{ asigPE->
                def pEvalInscAsigInstance
                boolean add=false
                insc.detalle.each{ det->
                    log.info("Comparando: "+asigPE.asignatura.curso.id+" con  det.tcDivision.curso.id: "
                        +det.tcDivision.curso.id)
                    if(asigPE.asignatura.curso.id.compareTo(det.tcDivision.curso.id)==0){
                        add=true
                        log.info("Puede agregar periodos de evaluacion y examenes")
                        return
                    }
                }
                if(add){
                   pEvalInscAsigInstance = new PeriodoEvalInscAsignatura(
                                       asignatura: asigPE.asignatura,inscripcion:insc
                                       ,periodoEval:pe)                     

                    pe.configExamenes.each{ ced ->
                        TipoExamen tipoExamen=ced.tipoExamen
                        int cantExamenes = ced.cantidad
                        log.info("Tipo de examen a guardar: "+ced.tipoExamen)
                        for(int i=1; i<=cantExamenes; i++){
                            pEvalInscAsigInstance.addToExamenes(
                                    new Examen(descripcion:i+'°'+tipoExamen.descripcion
                                        ,tipoExamen:tipoExamen,pEvalInscAsigInstance:pEvalInscAsigInstance
                                        ,puntuacion:new BigDecimal("0.00")
                                        ,inscripcion:insc)
                                )
                        }

                    }
                    if(!pEvalInscAsigInstance.save() && pEvalInscAsigInstance.hasErrors())
                        log.info("Errores en periodoevaluacion insc asig: "
                            +pEvalInscAsigInstance.errors)
                    
                    
                   
                }
                
                
                
                
                
                
                
                
                
            }// ultimo bracket de asignaturas
            
        }
    }*/

    def save(def inscJson/*Long periodoLectivoId,Long divisionId, Long alumnoId*/) {
        log.info("Save in service")
        def alumnoInstance = Alumno.load(inscJson.alumnoId)
        //def periodoInstance=PeriodoLectivo.load(periodoLectivoId)
        
        def tcDivisionInstance = TurnoCursoDivision.load(inscJson.division.id)
        //log.info("Despues de Division.load")
        //def inscInstance = new Inscripcion(alumno:alumnoInstance
        //        ,periodoLectivo:periodoInstance,fecha:new Date(new java.util.Date().getTime()))
        def inscInstance = new Inscripcion(inscJson)
        inscInstance.alumno = alumnoInstance
        inscInstance.condicion = inscJson.condicion_param.code as CondicionEnum
        
        def detInscInstance = new DetalleInscripcion(tcDivision:tcDivisionInstance)
        inscInstance.addToDetalle(detInscInstance)
        log.info("Antes de save")
        def inscSavedInstance = inscInstance.save()
        log.info("Despues de salvar la inscripcion")
        if(inscSavedInstance && !inscInstance.hasErrors()){
            log.info("Retornando after save: "+inscSavedInstance)
            saveExamenes(tcDivisionInstance,inscSavedInstance)
            return inscSavedInstance
        }else{
            log.info("Retornando after validation error: "+inscInstance)
            return inscInstance
        }
    }
    
    def invalidate(Long id){
        log.info("Invalidate service")
        def inscInstance = Inscripcion.get(id)
        inscInstance.anulada = true
        def inscSavedInstance = inscInstance.save()
        if(inscSavedInstance && !inscInstance.hasErrors()){
            log.info("Retornado after save: "+inscSavedInstance)
            return inscSavedInstance
        }else{
            log.info("Retornando after validation error: ")
            return inscInstance
        }
    }
}

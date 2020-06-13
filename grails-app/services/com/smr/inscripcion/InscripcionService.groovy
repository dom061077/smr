package com.smr.inscripcion

import grails.gorm.transactions.Transactional
import java.sql.Date
import com.smr.alumno.Alumno
import com.smr.enums.CondicionEnum
import com.smr.inscripcion.Inscripcion
import com.smr.academico.Examen
import com.smr.academico.TipoExamen
import com.smr.academico.PeriodoEvaluacion


@Transactional
class InscripcionService {
    
    private saveExamenes(Inscripcion insc){
        List listConfig = PeriodoEvaluacion.findAllByPeriodoLectivo(insc.periodoLectivo)
        
        listConfig.each{pe->
            pe.configExamenes.each{ ced ->
                TipoExamen tipoExamen=ced.tipoExamen
                int cantExamenes = ced.cantidad
                log.info("Tipo de examen: "+tipoExamen.descripcion+" cantidad: "+cantExamenes)
                ced.asignaturas.each{ a ->
                    for(int i=1; i<=cantExamenes; i++){
                        boolean add=false
                        insc.detalle.each{ det->
                            if(a.curso.id.compareTo(det.tcDivision.curso.id)==0){
                                add=true
                                return
                            }
                        }
                        log.info("Después del return comparando curso")
                        if (add){
                            new Examen(descripcion:i+'°'+tipoExamen.descripcion
                                ,tipoExamen:tipoExamen,periodoEval:pe
                                ,asignatura:a,inscripcion:insc).save()
                        }
                    }
                }

                
            }
            
        }
    }

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
            saveExamenes(inscSavedInstance)
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

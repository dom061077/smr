package com.smr.inscripcion

import grails.gorm.transactions.Transactional
import java.sql.Date
import com.smr.alumno.Alumno

@Transactional
class InscripcionService {

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
        
        def detInscInstance = new DetalleInscripcion(tcDivision:tcDivisionInstance)
        inscInstance.addToDetalle(detInscInstance)
        log.info("Antes de save")
        def inscSavedInstance = inscInstance.save()
        log.info("Despues de salvar la inscripcion")
        if(inscSavedInstance && !inscInstance.hasErrors()){
            log.info("Retornando after save: "+inscSavedInstance)
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

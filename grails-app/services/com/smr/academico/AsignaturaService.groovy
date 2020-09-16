package com.smr.academico

import grails.gorm.transactions.Transactional
import com.smr.security.User
import java.math.BigDecimal

@Transactional
class AsignaturaService {

    def saveUserAsignatura( def userAsigJson) {
        log.info("saveUserAsignatura userId: "+userAsigJson.id+" asignaturasJson: "+userAsigJson.asignaturas)
        def userInstance = User.get(userAsigJson.id)
        def asigInstance
        def savedAsig
        if(userInstance == null){
            throw new Exception("Error al recuperar el registro")
        }
        userInstance.asignaturas.each{
            def asigToRemove = Asignatura.get(it.id)
            asigToRemove.users.remove(userInstance)
            asigToRemove.save()
        }
        userAsigJson.asignaturas.each{
            
            asigInstance = Asignatura.get(it.id)
            log.info("Agregando asignatura: "+asigInstance)
            
            asigInstance.save()
            asigInstance.users=null
            asigInstance.addToUsers(userInstance)
            savedAsig=asigInstance.save()
            if(!savedAsig)
                return
           
        }
        
        
        log.info("Errores: "+asigInstance.errors)
        if(savedAsig && !asigInstance.hasErrors()){
            return savedAsig
        }else
            return asigInstance
    }
    
    def saveExam(Long examId, BigDecimal puntuacion ){
        log.info("saveExam en AsignaturaService examId: "+examId+" puntuacion: "+puntuacion)
        def examInstance = Examen.get(examId)
        if(examInstance){
            examInstance.puntuacion = puntuacion
            examInstance.save()
        }else{
            examInstance = null
        }
        return examInstance
    }
    
    def savePromedios(def promJSON){
        log.info("savePromedios en AsignaturaService "+promJSON)
        def examInstance 
        def periodoEvalInscAsigInstance
        promJSON.each{
            
            String[] splitted = it.key.split("_")
            log.info("Id examen: "+splitted[2])
            log.info("              Id periodo: "+splitted[3])
            log.info("                          Tipo: "+splitted[4])
            if(splitted[4].compareTo("inas")==0){
                periodoEvalInscAsigInstance = PeriodoEvalInscAsignatura.get(splitted[3])
                periodoEvalInscAsigInstance.cantInasist = Integer.valueOf(it.getValue())
                periodoEvalInscAsigInstance.save()
            }else{
                examInstance = Examen.get(splitted[2])
                examInstance.puntuacion=new BigDecimal(it.getValue())
                if(!examInstance.save() || examInstance.hasErrors())
                    return 
                
            }
            
            examInstance = null    
        }
        return examInstance
    }
}

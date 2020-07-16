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
    
    def savePromedios(def promJSON){
        log.info("savePromedios en AsignaturaService "+promJSON)
        def examInstance 
        promJSON.each{
            examInstance = Examen.get(it.getKey().replaceAll("exam",""))
            examInstance.puntuacion=new BigDecimal(it.getValue())
            
            if(!examInstance.save() || examInstance.hasErrors())
                return 
            examInstance = null    
        }
        return examInstance
    }
}

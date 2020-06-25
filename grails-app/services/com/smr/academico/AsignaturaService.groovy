package com.smr.academico

import grails.gorm.transactions.Transactional
import com.smr.security.User

@Transactional
class AsignaturaService {

    def saveUserAsignatura( def userAsigJson) {
        log.info("saveUserAsignatura userId: "+userAsigJson.id+" asignaturasJson: "+userAsigJson.asignaturas)
        def userInstance = User.get(userAsigJson.id)
        if(userInstance == null){
            throw new Exception("Error al recuperar el registro")
        }
        userInstance.asignaturas.clear()
        userAsigJson.asignaturas.each{
            def asigInstance = Asignatura.load(it.id)
            userInstance.addToAsignaturas(asigInstance)
           
        }
        def savedUser = userInstance.save()
        if(savedUser && savedUser.hasErrors()){
            return savedUser
        }else
            return userInstance
    }
}

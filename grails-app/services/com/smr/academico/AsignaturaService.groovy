package com.smr.academico

import grails.gorm.transactions.Transactional

@Transactional
class AsignaturaService {

    def saveUserAsignatura( def userAsigJson) {
        log.info("saveUserAsignatura userId: "+userAsigJson.userId+" asignaturasJson: "+userAsigJson.asignaturasJson)
        def userInstance = User.get(userAsigJson.userId)
        if(userInstance == null){
            throw new Exception("Error al recuperar el registro")
        }
        asignaturasJson.asignaturas.each{
            def asigInstance = Asignatura.load(it.id)
            userInstance.addToAsignaturas(asigInstance)
           
        }
        def savedUser = userInstance.save()
        if(userSaved && userSaved.hasErrors()){
            return savedUser
        }else
            return userInstance
    }
}

package smr

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    def savePerfiles(Long userId, def perfiles) {
        log.info("Id usuario: "+userId+" perfiles: "+perfiles)
        def userInstance = User.get(userId)
        if(userInstance){
            def perfilInstance
            def userPerfil 
            perfiles.each{
                perfilInstance = Perfil.load(it.id)
                userPerfil = UserPerfil.create(userInstance,perfilInstance)
                if(!userPerfil)
                    throw new Exception("Error en carga de perfil")
                return true    
            }
        }else
            return false
    }
}

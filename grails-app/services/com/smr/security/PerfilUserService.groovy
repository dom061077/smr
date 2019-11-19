package com.smr.security

import grails.gorm.transactions.Transactional

@Transactional
class PerfilUserService {
    static transactional = true

    def savePerfil(Perfil perfilInstance,def authorities) {
        def perfilSaved = perfilInstance.save()
        if(perfilSaved){
            authorities.each{
               def authInstance = Authority.findByAuthority(it.authority)
               PerfilAuthority.create(perfilSaved,authInstance)
            }
            return perfilSaved
        }else
            return perfilInstance
        
    }
}

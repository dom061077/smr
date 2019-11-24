package com.smr.security

import grails.gorm.transactions.Transactional

@Transactional
class PerfilUserService {
    

    def savePerfil(Perfil perfilInstance,def authorities) {
        def perfilSaved = perfilInstance.save()
        if(perfilSaved){
            def authInstance
            def perfilAuthInstance 
            authorities.each{
                perfilAuthInstance=null
               //authInstance = Authority.findByAuthority(it.authority)
               perfilAuthInstance = PerfilAuthority.create(perfilInstance,authInstance)
               if(!perfilAuthInstance.validate())
               arreglar esto
                    perfilSaved.errors.rejectValue('descripcion','','Algún un rol no fue cargado correctamente')
               
            }
            return perfilSaved
            
        }else
            return perfilInstance
        
    }
}

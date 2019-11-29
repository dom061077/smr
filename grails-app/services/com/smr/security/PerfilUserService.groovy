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
               authInstance = Authority.findByAuthority(it.authority)
               perfilAuthInstance = PerfilAuthority.create(perfilInstance,authInstance)
               
               if(!perfilAuthInstance.authority){
                    
                    
                    throw new Exception('Error en carga de rol');
                    
               }
               
            }
            return perfilSaved
            
        }else
            return perfilInstance
        
    }
    
    def updatePerfil(Perfil perfilInstance,def authorities){
        def perfilSaved = perfilInstance.save()
        if(perfilSaved){
            def authInstance
            def perfilAuthInstance
            PerfilAuthority.removeAll(perfilInstance)
            authorities.each{
                perfilAuthInstance=null
                authInstance = Authority.findByAuthority(it.authority)
                perfilAuthInstance = PerfilAuthority.create(perfilInstance,authInstance)
                if(!perfilAuthInstance.authority)
                    throw new Exception('Error en carga de rol')
            }
        }else
            return perfilInstance
        
    }
    
    def count(String filter){
        log.info("Cantidad de perfiles")
        int totalPerfiles=0
        def c = Perfil.createCriteria()
        totalPerfiles = c.count{
            if(filter?.compareTo("")!=0){
                or{
                    ilike("descripcion",'%'+filter+'%')
                }
            }
        }
        return totalPerfiles
    }
}

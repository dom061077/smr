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
    
    def updatePerfil(def perfilParm,def authorities){
        log.info("Id de perfil: "+perfilParm.id)
        def perfilInstance = Perfil.get(perfilParm.id)
        perfilInstance.properties = perfilParm
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
            return perfilSaved
        }else
            return perfilInstance
        
    }
    

    
    def savePerfilUrls(Long perfilId,def urls){
        log.info("Id de perfil:"+perfilId+" urls: "+urls)
        def perfilInstance = Perfil.get(perfilId)
        if(perfilInstance){
            def ngUrlInstance 
            def perfilNgUrl
            PerfilNgUrl.removeAll(perfilInstance)
            urls.each{
                ngUrlInstance = NgURL.findByUrl(it.url)
                perfilNgUrl = PerfilNgUrl.create(perfilInstance,ngUrlInstance)
                if(!perfilNgUrl)
                    throw new Exception("Error en carga")
                return true    
            }
            
        }else{
            return false
        }
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

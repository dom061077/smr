package com.smr.security


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
    
    private def getAuthoritiesByPerfil(Long id){
        def list = PerfilAuthority.createCriteria().list(){
            eq("perfil.id",id)
        }
        return list
    }
    
    private void setPerfilesAndAuthorities(User usuarioSaved,def perfiles){
            def perfilInstance
            
            def userPerfilInstance
            def userAuthorityInstance
            def authorities
            log.info("Usuario salvado: "+usuarioSaved)
            UserPerfil.removeAll(usuarioSaved)
            UserAuthority.removeAll(usuarioSaved)
            perfiles.each{
                userPerfilInstance = null
                perfilInstance = Perfil.get(it.id)
                log.info("Perfil encontrado en iteración: "+perfilInstance)
                userPerfilInstance = UserPerfil.create(usuarioSaved,perfilInstance,true)
                authorities = getAuthoritiesByPerfil(perfilInstance.id)
                authorities.each{pa->
                    log.info("Auth de usuario: "+pa)
                    userAuthorityInstance = null
                    log.info("Antes de crear UserAuthority")
                    userAuthorityInstance = UserAuthority.create(usuarioSaved,pa.authority,true)
                    log.info("Después de crear UserAuthority")
                    if(!userAuthorityInstance)
                        throw new Exception("Error en carga de role")
                }
                log.info("userPerfilInstance: "+userPerfilInstance)
                if(!userPerfilInstance.perfil){
                    throw new Exception("Error en carga de perfil")
                }
            }

    }
    
    def save(def usuarioInstance,def perfiles ){
        log.info("Usuario: "+usuarioInstance+" perfiles: "+perfiles)
        def usuarioSaved = usuarioInstance.save()
        if(usuarioSaved && !usuarioInstance.hasErrors()){
            setPerfilesAndAuthorities(usuarioSaved,perfiles)
            return usuarioSaved
                
        }else
            return usuarioInstance
    }
    
    def updatePassword(def id, String password){
        log.info("Cambiar contraseña: id: "+id+", password: "+password)
        def usuarioInstance = User.get(id)
        if(usuarioInstance == null){
            throw new Exception("Error al recuperar el registro")
        }
        usuarioInstance.password = password
        def usuarioSaved = usuarioInstance.save()
        if(usuarioSaved && !usuarioInstance.hasErrors()){
            return usuarioSaved
        }else
            return usuarioInstance
    }
    
    def update(def usuarioParm, def perfiles){
        log.info("Id de usuario: "+usuarioParm.id)
        def usuarioInstance = User.get(usuarioParm.id)
        if(usuarioInstance==null){
            
            throw new Exception("Error al recuperar el registro")
        }
            
        usuarioInstance.properties = usuarioParm
        def usuarioSaved = usuarioInstance.save()
        if(usuarioSaved && !usuarioInstance.hasErrors()){
            setPerfilesAndAuthorities(usuarioSaved,perfiles)
            return usuarioSaved
        }else
            return usuarioInstance
        
    }
    

        
}


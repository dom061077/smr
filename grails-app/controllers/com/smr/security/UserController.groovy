package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*

import org.springframework.beans.factory.annotation.Autowired
import com.smr.security.User


@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
class UserController {
	static responseFormats = ['json', 'xml']
    
    def springSecurityService
	
    def getUserInformation(String userName){
        log.info("Parametros userName: "+userName)
        
        User usuario = User.findByUsername(userName)
        
         
        [user:usuario]
    }
    
    def changePassword(){
        log.info("changePassword, parametros "+request.JSON)
        User usuario = User.get(request.JSON.id)
        
        if(usuario==null){
            render status: NOT_FOUND
            return
        }
        
       String passwordAnterior = springSecurityService.encodePassword("user1");
       log.info("Resultado de comparacion: "+usuario.password.compareTo(passwordAnterior))
       if(usuario.password.compareTo(passwordAnterior)!=0){
            usuario.errors.rejectValue('password',passwordAnterior,'Contraseña anterior inválida')
            log.info "No coincide la contraseña anterior password: "+usuario.password+" passwordAnterior: "+passwordAnterior
            
       }
            
       usuario.password = request.JSON.newPassword
       if (usuario.hasErrors()/* || !turnoInstance.validate()*/){
            usuario.errors.each{
                log.debug('ERROR!!!: '+it)
            }
            render (view:"/errors/_errors",model:[errors:usuario.errors])
            return
       }
        usuario.save(flush:true)
        
    }
}
 
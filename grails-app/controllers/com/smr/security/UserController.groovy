package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*
import grails.web.JSONBuilder


import org.springframework.beans.factory.annotation.Autowired
import com.smr.security.User


@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
class UserController {
	static responseFormats = ['json', 'xml']
    
    def springSecurityService
    def passwordEncoder
	
    def getUserInformation(String userName){
        log.info("Parametros userName: "+userName)
        
        User usuario = User.findByUsername(userName)
        
         
        [user:usuario]
    }
    
    
    
    def oldPasswordValidation(){
        log.info("oldPasswordValidation, parametros: "+request.JSON)
        JSONBuilder jsonBuilder = new JSONBuilder()
        boolean passwordValida = false
        def user = User.findByUsername(request.JSON.username)
        if(passwordEncoder.isPasswordValid(user?.password,request.JSON.oldPassword,null))
            passwordValida = true
        def json = jsonBuilder.build{
            success = passwordValida
        }
        render(status: 200, contentType: 'application/json', text: json)
    }
    
    def changePassword(){
        log.info("changePassword, parametros "+request.JSON)
        User user = User.get(request.JSON.id)
        
        if(user==null){
            render status: NOT_FOUND
            return
        }
        

        
       
       if(!passwordEncoder.isPasswordValid(user.password,request.JSON.oldPassword,null)){
            user.errors.rejectValue('password',request.JSON.oldPassword,'Contraseña anterior inválida')
//            log.info "No coincide la contraseña anterior password: "+user.password+" passwordAnterior: "+request.JSON.oldPassword
            
       }
            
       user.password = request.JSON.newPassword
       if (user.hasErrors()){
            user.errors.each{
                log.debug('ERROR!!!: '+it)
            }
            render (view:"/errors/_errors",model:[errors:user.errors])
            return
       }
        user.save(flush:true)
        
    }
}
 
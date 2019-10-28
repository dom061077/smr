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
        log.info("CURRENT USER: "+springSecurityService.getCurrentUser().username);
        JSONBuilder jsonBuilder = new JSONBuilder()
        boolean passwordValida = false
        def user = User.findByUsername(springSecurityService.getCurrentUser().username)
        if(passwordEncoder.isPasswordValid(user?.password,request.JSON.oldPassword,null))
            passwordValida = true
        def json = jsonBuilder.build{
            success = passwordValida
        }
        render(status: 200, contentType: 'application/json', text: json)
    }
    
    def changePassword(){
        log.info("changePassword, parametros "+request.JSON)
        
        
        
        User user = User.get(springSecurityService.getCurrentUser().id)
        
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
     
    private recursive(def det){
            def innerItems=[]
            if(det.items.size()>0){
                det.items.each{
                    if(it.items.size()>0)
                        innerItems.add( [label:it.label,icon:it.icon,items:recursive(it)])
                        
                    else{
                            
                          if(it.routerLink)  
                                innerItems.add( [label:it.label,icon:it.icon,routerLink:it.routerLink ]   )
                          else      
                                innerItems.add( [label:it.label,icon:it.icon]    )  
                            
                     }
                }
                
                return [label:det.label,icon:det.icon,items:innerItems]
            }else{
                return [label:det.label,icon:det.icon]    
                }
        
    }
    def listMenu(){
        log.info("INGRESANDO AL listMenu XXXXXXXXXXXX")
        def c = MenuItem.createCriteria()

        def list = c.list{
                eq("root",true)
            }

        def finalList=[]
        list.each{
                 finalList.add(recursive(it))
        }            
        

        render(status: 200, contentType: 'application/json', text: finalList as JSON)        
        

    }
    
    
}
 
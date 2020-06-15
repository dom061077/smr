package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import static org.springframework.http.HttpStatus.*
import grails.web.JSONBuilder
import java.text.SimpleDateFormat



import org.springframework.beans.factory.annotation.Autowired
import com.smr.security.User



class UserController {
	static responseFormats = ['json', 'xml']
    
    def springSecurityService
    def passwordEncoder
    def userService
	
    def getUserInformation(String userName){
        log.info("Parametros userName: "+userName)
        
        User usuario = User.findByUsername(userName)
        
        List perfiles = UserPerfil.createCriteria().list{
            eq("user.id",usuario.id)
        }
        List urls
        List finalUrls
        perfiles.each{per->
            urls=PerfilNgUrl.createCriteria().list{
                eq("perfil.id",per.id)
            }
            urls.each{url->
                finalUrls.add(url)
                
            }
            
            
        }
        
         
        [user:usuario,urls:finalUrls]
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
    
    @Secured(['ROLE_USER_CHANGEPASSWORD'])
    def changeUserPassword(){
        log.info("changeUserPassword, parametros "+request.JSON)
        def usuarioProcesado
        try{
            usuarioProcesado = userService.updatePassword(request.JSON.id,request.JSON.password)
        }catch(Exception e){
            log.error("Error al modificar el usuario",e)
            usuarioProcesado = new User()
            usuarioProcesado.errors.rejectValue("username","",e.message)
            
        }
        if(usuarioProcesado.hasErrors()){
            render (view:"/errors/_errors",model:[errors:usuarioProcesado.errors])
            return
        }
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
            id      = usuarioProcesado?.id
        }
        render(status: 200, contectType:'application/json',text:json)        
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
        user = user.save(flush:true)
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
            id      = user?.id
        }
        render(status: 200, contectType:'application/json',text:json)            
        
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
    
    @Secured(['ROLE_USER_UPDATE'])
    def update(){
        log.info("Ingresando a update: "+request.JSON)
        def usuarioProcesado
        try{
            usuarioProcesado = userService.update(request.JSON,request.JSON.perfiles)
        }catch(Exception e){
            log.error("Error al modificar el usuario",e)
            usuarioProcesado = new User()
            usuarioProcesado.errors.rejectValue("username","",e.message)
            
        }
        if(usuarioProcesado.hasErrors()){
            render (view:"/errors/_errors",model:[errors:usuarioProcesado.errors])
            return
        }
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
            id      = usuarioProcesado?.id
        }
        render(status: 200, contectType:'application/json',text:json)
    }
    
    @Secured(['ROLE_USER_SAVE'])
    def save(){
        log.info("Ingresando a save: "+request.JSON)
        /*def usuarioInstance = new User(request.JSON)
        if(!usuarioInstance.save(flush:true)){
            if(usuarioInstance.hasErrors() && !usuarioInstance.validate()){
                render(view:"/errors/_errors",model:[errors:usuarioInstance.errors])
                return
            }
        }*/
        def usuarioInstance = new User(request.JSON)
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Date date = sdf.parse(request.JSON.fechaNacimientoUnbinding)
        
        usuarioInstance.fechaNacimiento = new java.sql.Date(date.getTime())
        
        
        def usuarioProcesado
        try{
            usuarioProcesado = userService.save(usuarioInstance,request.JSON.perfiles)
            
        }catch(Exception e){
            log.error('Error al salvar el usuario',e)
            usuarioInstance.errors.rejectValue('username')
        }
        
        if(usuarioProcesado.hasErrors()){
            render (view:"/errors/_errors",model:[errors:usuarioProcesado.errors])
            return
        }
        
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build{
            success = true
        }
        render(status: 200, contectType:'application/json',text:json)
    }
    
    @Secured(['ROLE_USER_SAVEPERFILES'])
    def saveperfiles(){
        log.info("Ingresando a saveperfiles")
        
    }
    
    def listAuthorities(){
        log.info("Ingresando a listAuthorities")
        def list = Authority.list()
        render (view:'/user/listAuthorities',model:[list:list])
    }
    
    def countAuthorities(){
       // continuar aqui
    }
    
    def count(String filter){
        log.info("Cantidad de usuarios")
        def totalUsuarios = 0
        def c = User.createCriteria()
        totalUsuarios = c.count{
            if(filter.compareTo("")!=0){
                or{
                    ilike("apellido",'%'+filter+'%')
                    ilike("nombre",'%'+filter+'%')
                    ilike("username",'%'+filter+'%')
                }
            }
        }
        
        JSONBuilder jsonBuilder = new JSONBuilder()
        def json = jsonBuilder.build(){
            count = totalUsuarios
        }
        render(status: 200, contentType: 'application/json', text: json)
    }
    
    def listUsuarios(String filter,int start, int limit,String sortField,String ascDesc){
        log.info("Ingresando a listUsuarios. Filter: "
            +filter+" sortField: "+sortField+" ascDesc: "
            +ascDesc+" start: "+start+" limit:"+limit)
        def pagingconfig = [
            max: limit as Integer?:10,
            offset:start as Integer?:0
        ]
        def list = User.createCriteria().list(pagingconfig){
            if(filter.compareTo("")!=0){
                or{
                    ilike("apellido",'%'+filter+'%')
                    ilike("nombre",'%'+filter+'%')
                    ilike("username",'%'+filter+'%')
                }
            }
            if(sortField.compareTo("")!=0 && sortField.compareTo("undefined")){
                if(sortField.compareTo("apellidonombre")==0){
                    order("apellido",ascDesc)
                    order("nombre",ascDesc)
                }else{
                    order(sortField,ascDesc)
                }
                
                
            }
        }
        render(view:'/user/listUsuarios',model:[list:list])
    }
    
    def show(Long id){
        log.info("Retornar usuario")
        def usuarioInstance = User.get(id)
        log.info("Usuario: "+usuarioInstance)
        render (view:'/user/show',model:[user:usuarioInstance])
    }
    
    def getPerfiles(Long id){
        log.info("Ingresando a getPerfiles: "+id)
        def list = UserPerfil.createCriteria().list{
            eq("user.id",id)
        }
        list = list.collect{
            it.perfil
        }
        
        render (view:"/perfil/listPerfiles",model:[list:list])
    }
    
    def getNgUrls(){
        log.info("Ingresando a getNgUrls. Usuario actual: "+springSecurityService.getCurrentUser().id)
        def detachedC=UserPerfil.where{
            user == User.load(springSecurityService.getCurrentUser().id)
        }
        def listPerfiles = detachedC.list()
        def ngUrls = PerfilNgUrl.createCriteria().list{
            inList('perfil.id',/*detachedC.list().each{pn->
                    return pn.perfil.id
            }*/listPerfiles.collect(){it.perfil.id})
        }
        render(view:"/user/getNgUrls",model:[list:ngUrls])
    }
    
    
}
 
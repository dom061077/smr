package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import com.smr.security.User


@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
class UserController {
	static responseFormats = ['json', 'xml']
	
    def getUserInformation(String userName){
        log.info("Parametros userName: "+userName)
        
        User usuario = User.findByUsername(userName)
        
         
        [user:usuario]
    }
}
 
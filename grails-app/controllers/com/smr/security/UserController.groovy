package com.smr.security


import grails.rest.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import com.smr.security.User


@Secured("hasAnyRole('ROLE_USER','ROLE_PROFESIONAL')")
class UserController {
	static responseFormats = ['json', 'xml']
	
    def mostrar(){
        log.info("PROBANDO getUserInformation: ")
        User user = new User()
        log.info("PROBANDO CONTROLLER USER")
        
        [user:user]
    }
}

package com.smr.audit

import grails.plugins.orm.auditable.resolvers.DefaultAuditRequestResolver 
import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired

class CustomAuditRequestResolver extends DefaultAuditRequestResolver {
    @Autowired
    SpringSecurityService springSecurityService    
    
    @Override
    String getCurrentActor(){
        return springSecurityService?.currentUser?.toString()?:super.getCurrentActor()
    }
    
    @Override
    String getCurrentURI(){
        return null;
    }
}


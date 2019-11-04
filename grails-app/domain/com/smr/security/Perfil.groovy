package com.smr.security

class Perfil {
    String descripcion
    
    
    static hasMany = [authorities:Authority]
    
    static constraints = {
        
    }
}

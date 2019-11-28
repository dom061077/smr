package com.smr.security

class Perfil {
    String descripcion
    
    
    
    
    static constraints = {
        descripcion (nullable:false,blank:false,unique:true)
        
    }
}

package com.smr.academico

class TipoExamen {
    String descripcion
    boolean promediable=true
    
    boolean complementario=false
    
    int ordenCompendio
    
    static constraints = {
        ordenCompendio(nullable:true, blank:true)
    }
}

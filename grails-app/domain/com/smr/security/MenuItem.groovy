package com.smr.security

class MenuItem {
    String label
    String icon
    String routerLink
    boolean root
    
    boolean renderizado
    
    static hasMany = [items:MenuItem]
    static constraints = {
    }
    
    static transients = ['renderizado'] 
}

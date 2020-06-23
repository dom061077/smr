package com.smr.security

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic
import com.smr.academico.Asignatura

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User implements Serializable {

    private static final long serialVersionUID = 1

    String username
    String password
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    
    //------------------------
    
    String apellido
    String nombre
    
    Integer dni
    String domicilio
    java.sql.Date fechaNacimiento
    

    Set<Authority> getAuthorities() {
        (UserAuthority.findAllByUser(this) as List<UserAuthority>)*.authority as Set<Authority>
    }
    
    static hasMany = [asignaturas:Asignatura]

    static constraints = {
        password nullable: false, blank: false, password: true
        username nullable: false, blank: false, unique: true
        apellido nullable: true, blank: true
        nombre nullable: true, blank:true
        dni nullable: true, blank: true
        domicilio nullable: true, blank: true
        fechaNacimiento nullable: true, blank: true
    }

    
    static mapping = {
	    password column: '`password`'
    }
}

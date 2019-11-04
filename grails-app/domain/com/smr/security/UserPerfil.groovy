package com.smr.security

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class UserPerfil implements Serializable {
    private static final long serialVersionUID = 1
    User user
    Perfil perfil
    
    
    
    @Override
    boolean equals(other) {
            if (other instanceof UserPerfil) {
                    other.userId == user?.id && other.perfilId == perfil?.id
            }
    }

    @Override
	int hashCode() {
	    int hashCode = HashCodeHelper.initHash()
                if (user) {
                    hashCode = HashCodeHelper.updateHash(hashCode, user.id)
		}
		if (perfil) {
		    hashCode = HashCodeHelper.updateHash(hashCode, perfil.id)
		}
		hashCode
	}

	static UserPerfil get(long userId, long perfilId) {
		criteriaFor(userId, perfilId).get()
	}

	static boolean exists(long userId, long perfilId) {
		criteriaFor(userId, perfilId).count()
	}

	private static DetachedCriteria criteriaFor(long userId, long perfilId) {
		UserPerfil.where {
			user == User.load(userId) &&
			perfil == Perfil.load(perfilId)
		}
	}

	static UserPerfil create(User user, Perfil perfil, boolean flush = false) {
		def instance = new UserPerfil(user: user, perfil: Perfil)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(User u, Perfil p) {
		if (u != null && p != null) {
			UserPerfil.where { user == u && perfil == p }.deleteAll()
		}
	}

	static int removeAll(User u) {
		u == null ? 0 : UserPerfil.where { user == u }.deleteAll() as int
	}

	static int removeAll(Perfil p) {
		p == null ? 0 : UserPerfil.where { perfil == p }.deleteAll() as int
	}

	static constraints = {
                user nullable: false
		perfil nullable: false, validator: { Perfil p, UserPerfil up ->
			if (up.user?.id) {
				if (UserPerfil.exists(up.user.id, p.id)) {
				    return ['userPerfil.exists']
				}
			}
		}
	}

	static mapping = {
		id composite: ['user', 'perfil']
		version false
	}
}

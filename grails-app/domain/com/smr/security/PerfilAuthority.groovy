package com.smr.security

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class PerfilAuthority implements Serializable {
    private static final long serialVersionUID = 1
    
    Perfil perfil
    Authority authority
    
    @Override
    boolean equals(other) {
            if (other instanceof PerfilAuthority) {
                    other.perfilId == perfil?.id && other.authorityId == authority?.id
            }
    }

    @Override
	int hashCode() {
	    int hashCode = HashCodeHelper.initHash()
            if (perfil) {
                 hashCode = HashCodeHelper.updateHash(hashCode, perfil.id)
            }
            if (authority) {
                hashCode = HashCodeHelper.updateHash(hashCode, authority.id)
            }
            hashCode
	}
    
	static PerfilAuthority get(long perfilId, long authorityId) {
		criteriaFor(perfilId, authorityId).get()
	}

	static boolean exists(long perfilId, long authorityId) {
		criteriaFor(perfilId, authorityId).count()
	}

	private static DetachedCriteria criteriaFor(long perfilId, long authorityId) {
		PerfilAuthority.where {
			perfil == Perfil.load(perfilId) &&
			authority == Authority.load(authorityId)
		}
	}

	static PerfilAuthority create(Perfil perfil, Authority authority, boolean flush = false) {
		def instance = new PerfilAuthority(perfil: perfil, authority: authority)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(Perfil p, Authority r) {
		if (p != null && r != null) {
			PerfilAuthority.where { perfil == p && authority == r }.deleteAll()
		}
	}

	static int removeAll(Perfil p) {
		p == null ? 0 : PerfilAuthority.where { perfil == p }.deleteAll() as int
	}

	static int removeAll(Authority r) {
		r == null ? 0 : PerfilAuthority.where { authority == r }.deleteAll() as int
	}
    
    
    
	static constraints = {
                perfil nullable: false
		authority nullable: false, validator: { Authority r, PerfilAuthority pa ->
			if (pa.perfil?.id) {
				if (PerfilAuthority.exists(pa.perfil.id, r.id)) {
				    return ['perfilAuthority.exists']
				}
			}
		}
	}
    
    
	static mapping = {
		id composite: ['perfil', 'authority']
		version false
	}
    
}
    
package com.smr.security

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic


@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class PerfilNgUrl implements Serializable {
    private static final long serialVersionUID = 1
    
    Perfil perfil
    NgURL ngUrl
    
    @Override
    boolean equals(other) {
            if (other instanceof PerfilNgUrl) {
                    other.perfilId == perfil?.id && other.ngUrlId == ngUrl?.id
            }
    }

    @Override
	int hashCode() {
	    int hashCode = HashCodeHelper.initHash()
            if (perfil) {
                 hashCode = HashCodeHelper.updateHash(hashCode, perfil.id)
            }
            if (ngUrl) {
                hashCode = HashCodeHelper.updateHash(hashCode, ngUrl.id)
            }
            hashCode
	}
    
	static PerfilNgUrl get(long perfilId, long ngUrlId) {
		criteriaFor(perfilId, ngUrlId).get()
	}

	static boolean exists(long perfilId, long ngUrlId) {
		criteriaFor(perfilId, ngUrlId).count()
	}

	private static DetachedCriteria criteriaFor(long perfilId, long ngUrlId) {
		PerfilNgUrl.where {
			perfil == Perfil.load(perfilId) &&
			ngUrl == NgURL.load(ngUrlId)
		}
	}

	static PerfilNgUrl create(Perfil perfil, NgURL ngUrl, boolean flush = false) {
		def instance = new PerfilNgUrl(perfil: perfil, ngUrl: ngUrl)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(Perfil p, NgURL ng) {
		if (p != null && ng != null) {
			PerfilNgUrl.where { perfil == p && ngUrl == ng }.deleteAll()
		}
	}

	static int removeAll(Perfil p) {
		p == null ? 0 : PerfilNgUrl.where { perfil == p }.deleteAll() as int
	}

	static int removeAll(NgURL ng) {
		ng == null ? 0 : PerfilNgUrl.where { ngUrl == ng }.deleteAll() as int
	}
    
    
    
	static constraints = {
                perfil nullable: false
		ngUrl nullable: false, validator: { NgURL ng, PerfilNgUrl png ->
			if (png.perfil?.id) {
				if (PerfilNgUrl.exists(png.perfil.id, ng.id)) {
				    return ['perfilNgUrl.exists']
				}
			}
		}
	}
    
    
	static mapping = {
		id composite: ['perfil', 'ngUrl']
		version false
	}

}

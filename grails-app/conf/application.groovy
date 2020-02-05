
// Added by the Spring Security Core plugin:
/**/
grails.plugin.springsecurity.securityConfigType = "Annotation"

/**
 * PARA QUE FUNCIONE LA CONEXION A MYSQL AL GENERAR EL WAR GENERAR EL MISMO
 * CON EL SIGUIENTE COMANDO>
 * grails dev war
 * 
 * */

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.active = true  
//grails.plugin.springsecurity.rest.token.storage.jwt.secret = 'qrD6h8K6S9503Q06Y6Rfk21TErImPYqa'
grails.plugin.springsecurity.rest.token.validation.useBearerToken = true
//grails.plugin.springsecurity.rest.token.validation.headerName = 'X-Auth-Token'

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.smr.security.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.smr.security.UserAuthority'
grails.plugin.springsecurity.authority.className = 'com.smr.security.Authority'


grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']],
        [pattern: '/**',             access: ['permitAll']]// para permitir las peticiones delete, put, post, etc...
]

grails.plugin.springsecurity.filterChain.chainMap = [
    [pattern: '/assets/**',      filters: 'none'],
    [pattern: '/**/js/**',       filters: 'none'],
    [pattern: '/**/css/**',      filters: 'none'],
    [pattern: '/**/images/**',   filters: 'none'],
    [pattern: '/**/favicon.ico', filters: 'none'],
    
    [
        pattern: '/api/**',
        filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'
    ],
    [

                pattern: '/oauth/**',
                filters: 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'
    ],    
    [pattern: '/**',             filters: 'JOINED_FILTERS']
    
]









// Added by the Audit-Logging plugin:
grails.plugin.auditLog.auditDomainClassName = 'com.smr.audit.AuditSmr'



// Added by the Spring Security Core plugin:

grails.plugin.springsecurity.securityConfigType = "Annotation"


// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.active = false  
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
    [pattern: '/**',             filters: 'JOINED_FILTERS']
    
]








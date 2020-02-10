import com.smr.security.UserPasswordEncoderListener
import com.smr.audit.CustomAuditRequestResolver
// Place your Spring DSL code here
beans = {
    userPasswordEncoderListener(UserPasswordEncoderListener)
    auditRequestResolver(CustomAuditRequestResolver)
}

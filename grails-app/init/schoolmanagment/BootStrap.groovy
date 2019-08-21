package schoolmanagment
import com.smr.security.User
import com.smr.security.Authority
import com.smr.security.UserAuthority

class BootStrap {

    def init = { servletContext ->
        def role1 = new Authority(authority:"ROLE_USER").save flush:true
        def user1 = new User(username:"user1",password:"user1").save flush:true
        UserAuthority.create(user1,role1)        
    }
    def destroy = {
    }
}

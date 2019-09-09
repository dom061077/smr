package schoolmanagment

import com.smr.security.User
import com.smr.alumno.Alumno
import com.smr.security.Authority
import com.smr.security.UserAuthority

class BootStrap {

    def init = { servletContext ->
        def role1 = new Authority(authority:"ROLE_USER").save flush:true,failOnError:true
        def user1 = new User(username:"user1",password:"user1",apellido:"Apellido 1",nombre:"Nombre 1").save flush:true,failOnError:true
        UserAuthority.create(user1,role1)       
        
        user1 = new User(username:"user2",password:"user2",apellido:"Apellido 2",nombre:"Nombre 2").save flush:true, failOnError:true
        UserAuthority.create(user1,role1)
        
        new Alumno("apellido": "Gallardo","nombre":"Marcelo").save()
        new Alumno("apellido": "Francescoli","nombre":"Enzo").save()
    }
    def destroy = {
    }
}

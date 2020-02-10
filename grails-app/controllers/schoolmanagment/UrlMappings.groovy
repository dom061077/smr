package schoolmanagment

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
        
        //----------usuario-------
        
        get "/api/showuser" (controller:"user",action:"getUserInformation")
        put "/api/changepassword" (controller:"user",action:"changePassword")
        put "/api/changeuserpassword" (controller:"user",action:"changeUserPassword")
        post "/api/validateoldpassword" (controller:"user",action:"oldPasswordValidation")
        get "/api/getmenu" (controller:"user",action:"listMenu")
        get "/api/getresturls" (controller:"user",action:"listAuthorities")
        post "/api/saveperfil" (controller:"perfil",action:"save")
        get "/api/perfilcount" (controller:"perfil",action:"count")
        get "/api/getperfiles" (controller:"perfil",action:"listPerfiles")
        get "/api/getallperfiles" (controller:'perfil',action:'listAllPerfiles')
        get "/api/getperfil/$id" (controller:"perfil",action:"show")
        get "/api/getroles/$id" (controller:"perfil",action:"getAuthorities")
        post "/api/updateperfil/$id" (controller:"perfil",action:"update")
        get "/api/getngurls" (controller:"perfil",action:"getNgUrls")
        get "/api/getuserngurls" (controller:"user",action:"getNgUrls")
        get "/api/getngperfilurls/$id" (controller:"perfil",action:"getNgPerfilUrls")
        post "/api/saveperfilurls" (controller:"perfil",action:"savePerfilUrls")
        get "/api/getusuarios" (controller:"user",action:"listUsuarios")
        get "/api/getuser/$id" (controller:"user",action:"show")
        post "/api/saveusuario" (controller:"user",action:"save")
        get "/api/getuserperfiles/$id" (controller:"user",action:"getPerfiles")
        get "/api/usercount" (controller:"user",action:"count")
        post "/api/updateusuario" (controller:"user",action:"update")
        //---------------LOCATIONS----------------
        
        get "/api/autocprov" (controller:"location",action:"autocompleteProvincias")
        get "/api/autocloc" (controller:"location",action:"autocompleteLocalidades")
        get "/api/autocparentesco" (controller:"alumno",action:"autocompleteParentescoTutor")
        
        
        //----------Alumno------------------
        post "/api/savealumno" (controller:"alumno",action:"save")
        get "/api/getalumnos"  (controller:"alumno",action:"listAlumnos")
        get "/api/alumnocount" (controller:"alumno", action:"count")
        get "/api/getalumno/$id"   (controller:"alumno", action:"show")
        put "/api/updatealumno/$id" (controller:"alumno",action:"update")
        get "/api/getalumnobydni" (controller:"alumno",action:"getalumnobydni")
        

        
    }
}

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
        post "/api/validateoldpassword" (controller:"user",action:"oldPasswordValidation")
        
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
        
    }
}

package com.smr.security

class NgURL {
    String url
    static constraints = {
        url (nullable:false,blank:false,unique:true)
    }
}

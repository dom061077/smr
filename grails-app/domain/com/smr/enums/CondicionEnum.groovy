
package com.smr.enums

public enum CondicionEnum {
	CONDICION_REGULAR("REGULAR"),
	CONDICION_REPITENTE("REPITENTE")
	
	
	String name
	
	public CondicionEnum(String name){
		this.name=name
	}
	
	static list(){
		[CONDICION_REGULAR,CONDICION_REPITENTE]
	}

}


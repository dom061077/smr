
package com.smr.enums

public enum CondicionEnum {
	CONDICION_REGULAR("REG"),
	CONDICION_REPITENTE("REP")
	
	
	String name
	
	public CondicionEnum(String name){
		this.name=name
	}
	
	static list(){
		[CONDICION_REGULAR,CONDICION_REPITENTE]
	}

}


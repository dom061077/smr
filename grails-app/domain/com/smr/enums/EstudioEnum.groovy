package com.smr.enums;

public enum EstudioEnum {
	ESTUDIO_COMPLETO("Completo"),
	ESTUDIO_INCOMPLETO("Incompleto")
	
	
	String name
	
	public EstudioEnum(String name){
		this.name=name
	}
	
	static list(){
		[ESTUDIO_COMPLETO,ESTUDIO_INCOMPLETO]
	}

}



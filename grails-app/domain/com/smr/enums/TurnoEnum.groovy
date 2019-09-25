
package com.smr.enums

public enum TurnoEnum {
	TURNO_MANIANA("MAÑANA"),
	TURNO_TARDE("TARDE")
	
	
	String name
	
	public TurnoEnum(String name){
		this.name=name
	}
	
	static list(){
		[TURNO_MANIANA,TURNO_TARDE]
	}

}

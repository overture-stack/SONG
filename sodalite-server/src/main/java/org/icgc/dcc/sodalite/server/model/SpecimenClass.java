package org.icgc.dcc.sodalite.server.model;

public enum SpecimenClass {
	NORMAL("Normal"), TUMOR("Tumor"), ADJACENT_NORMAL("Adjacent Normal");
	private final String name;
	SpecimenClass(String name) {
		this.name=name;
	}
	
	public String toString() {
		return name;
	}
}

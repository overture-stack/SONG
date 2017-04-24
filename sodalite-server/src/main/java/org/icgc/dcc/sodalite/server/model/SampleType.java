package org.icgc.dcc.sodalite.server.model;

public enum SampleType {
	DNA("Dna"),FFPE_DNA("FFPE DNA"),AMP_DNA("Amplified_DNA"),RNA("RNA"),TOTAL_RNA("Total RNA"),FFPE_RNA("FFPE RNA");
	
	private final String name;
	SampleType(String name) {
		this.name=name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}


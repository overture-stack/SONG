package org.icgc.dcc.sodalite.server.model;

public enum LibraryStrategy {
	WGS("WGS"), WXS("WXS"), RNA_SEQ("RNA-Seq"), CHIP_SEQ("ChIP-Seq"),MIRNA_SEQ("miRNA-Seq"),
	BISULFITE_SEQ("Bisulfite-Seq"), VALIDATION("Validation"), AMPLICON("Amplicon"), OTHER("Other");
	private final String name;
	
	LibraryStrategy(String name) {
		this.name=name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

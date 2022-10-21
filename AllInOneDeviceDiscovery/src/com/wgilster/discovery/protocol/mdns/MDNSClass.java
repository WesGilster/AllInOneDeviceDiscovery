package com.wgilster.discovery.protocol.mdns;
public enum MDNSClass {
	Internet(1, "IN"),
	CSNet(2, "CS"),
	Chaos(3, "CH"),
	Hesiod(4, "HS"),
	Any(255, "*");
	private int value;
	private String abbreviation;
	
	private MDNSClass(int value, String abbreviation) {
		this.value = value;
		this.abbreviation = abbreviation;
	}
	
	public int getCode() {
		return value;
	}
	
	public static MDNSClass fromCode(int classValue) {
		for (MDNSClass v : MDNSClass.values()) {
			if (v.value == classValue) {
				return v;
			}
		}
		
		return null;
	}
}

package com.wgilster.discovery;

public enum Type {
	Service(Property.ServiceName, Property.TTL, Property.Port, Property.DomainName, Property.TextData),
	Device(Property.IPV4Address, Property.IPV6Address, Property.TTL);
	
	private Property[] properties;
	
	private Type(Property... properties) {
		this.properties = properties;
	}
	
	public Property[] getProperties() {
		return properties;
	}
}

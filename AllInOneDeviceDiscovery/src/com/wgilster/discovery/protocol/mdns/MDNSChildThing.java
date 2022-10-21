package com.wgilster.discovery.protocol.mdns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;

public class MDNSChildThing implements Thing {
	private Map<String, Object> properties;
	private String section;
	private int index;

	public MDNSChildThing(Map<String, Object> properties, String section, int index) {
		this.properties = new HashMap<>(properties);
		this.section = section;
		this.index = index;
	}

	@Override
	public String getId() {
		return MDNSParser.getCompiledNameValue(properties, section, index, MDNSParser.NAME);// + " (Type:" + MDNSParser.getFieldValue(properties, section, index, getTypeName(section)) + ")";
	}

	@Override
	public Consumption getConsumption() {
		String qr = (String)properties.get(MDNSParser.HEADER + "." + MDNSParser.FLAGS + "." + MDNSParser.QR);
		if (qr.equals(MDNSParser.QR_REPLY)) {
			return Consumption.Advertisement;
		}
		
		return Consumption.Seeker;
	}

	@Override
	public Type getType() {
		return (Type)MDNSParser.getFieldValue(properties, section, index, Type.class.getName());
	}

	@Override
	public List<Thing> getChildren() {
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public Object getProperty(Property property) {
		return MDNSParser.getFieldValue(properties, section, index, property.name());
	}
	
	public MDNSType getMDNSType() {
		return (MDNSType)MDNSParser.getFieldValue(properties, section, index, MDNSParser.TYPE);
	}
	
	public String toString() {
		return getId();
	}
}

package com.wgilster.discovery.protocol.wsd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;

public class WSDHelloThing extends WSDThing {
	private Map<String, Object> properties;
	
	public WSDHelloThing() {
		properties = new HashMap<String, Object>();
		properties.put(Property.ServiceName.toString(), UUID.randomUUID());
		properties.put(Property.IPV4Address.toString(), UUID.randomUUID());
	}

	@Override
	public Consumption getConsumption() {
		return Consumption.Advertisement;
	}

	@Override
	public Type getType() {
		return Type.Service;
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
		if (property == Property.ServiceName)
			return properties.get(Property.ServiceName.toString());
		
		if (property == Property.IPV4Address)
			return properties.get(Property.IPV4Address.toString());
					
		return null;
	}
}

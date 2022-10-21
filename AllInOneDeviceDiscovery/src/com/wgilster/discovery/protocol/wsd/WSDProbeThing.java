package com.wgilster.discovery.protocol.wsd;

import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;

public class WSDProbeThing extends WSDThing {
	@Override
	public Consumption getConsumption() {
		return Consumption.Seeker;
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
		return null;
	}

	@Override
	public Object getProperty(Property property) {
		return null;
	}
}

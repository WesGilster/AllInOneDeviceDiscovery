package com.wgilster.discovery;

import java.util.List;
import java.util.Map;

public interface Thing {
	public String getId();
	public Consumption getConsumption();
	public Type getType();
	public List<Thing> getChildren();
	public Map<String, Object> getProperties();
	public Object getProperty(Property property);
}

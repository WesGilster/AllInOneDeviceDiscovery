package com.wgilster.discovery.simple;

import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;
import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Property;

public class PrinterThing implements Thing {
	private String fullName;
	
	public PrinterThing(String printerName, String subprotocol, String networkProrocol) {
		fullName = printerName + "." + subprotocol + "." + networkProrocol + ".local";
		
		//TXT, SRV
				//The domain portion of the service instance name MUST BE "local." for mDNS.
		//"adminurl" -> Suppose to use https
		//"UUID" = random UUID
		//"note" = "Under the arcade
		//"rp" = "ipp/print3d"
				//ipp/print3d/[NAME] for each printer?
		//pdl = "model/3mf"
		//ty = "Make and model"
	}
	
	public PrinterThing() {
		
	}
	
	@Override
	public String getId() {
		return fullName;
	}

	@Override
	public Consumption getConsumption() {
		return Consumption.Advertisement;
	}

	@Override
	public List<Thing> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		return Type.Device;
	}

	@Override
	public Object getProperty(Property property) {
		// TODO Auto-generated method stub
		return null;
	}
}

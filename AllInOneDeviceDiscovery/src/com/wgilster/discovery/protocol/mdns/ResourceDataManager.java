package com.wgilster.discovery.protocol.mdns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;
import com.wgilster.discovery.protocol.mdns.MDNSParser.Field;

public class ResourceDataManager {
	public static class MDNSPackage {
		public MDNSType mtype;
		public MDNSClass mclass;
		public boolean classTopBit;
		public int ttl;
		public Thing thing;
	}
	
	private static void setObject(Map<String, Object> map, String resourceTypeLabel, int resourceIndex, Object value, Property property) {
		String label = MDNSParser.compileFieldLabel(resourceTypeLabel, resourceIndex, property.name());
		map.put(label, value);
	}
	
	public static MDNSPackage convertToMDNSPackage(Thing thing) {
		MDNSPackage pack = new MDNSPackage();
		pack.classTopBit = true;
		pack.mclass = MDNSClass.Internet;
		pack.thing = thing;
		pack.ttl = 1000 * 60 * 2; //Default two minutes;
		
		for (Property property : thing.getType().getProperties()) {
			Object value = thing.getProperty(property);
			if (value == null) {
				continue;
			}

			switch (property) {
			case IPV4Address:
				pack.mtype = MDNSType.HostAddress;
				break;
			case IPV6Address:
				pack.mtype = MDNSType.IPV6Address;
				break;
			case DomainName:
				pack.mtype = MDNSType.DomainNamePointer;
				break;
			case ServiceName:
				pack.mtype = MDNSType.SerivceLocator;
				break;
			case TextData:
				pack.mtype = MDNSType.Text;
				break;
			case TTL:
				pack.ttl = (Integer)value;
				break;
			case ByteData:
			default:
				throw new IllegalArgumentException("I don't know how to categorize:" + property);
			}
		}
		
		return pack;
	}
	
	//Types with compression: NS, CNAME, PTR, DNAME, SOA, MX, AFSDB, RT, KX, RP, PX, SRV, NSEC
	public static void parseResourceData(Map<String, Object> map, byte[] message, int resourceDataOffset, int resourceDataLength, String section, int resourceIndex, int recordType, int recordClass) {
		if (recordType == MDNSType.HostAddress.getCode()) {
			MDNSParser.setType(map, section, resourceIndex, Type.Device);
			try {
				byte[] address = new byte[4];//<--resourceDataLength
				System.arraycopy(message, resourceDataOffset, address, 0, 4);
				setObject(map, section, resourceIndex, InetAddress.getByAddress(address), Property.IPV4Address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else if (recordType == MDNSType.IPV6Address.getCode()) {
			MDNSParser.setType(map, section, resourceIndex, Type.Device);
			try {
				byte[] address = new byte[16];//<--resourceDataLength
				System.arraycopy(message, resourceDataOffset, address, 0, 16);
				setObject(map, section, resourceIndex, InetAddress.getByAddress(address), Property.IPV6Address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else if (recordType == MDNSType.DomainNamePointer.getCode()) {
			MDNSParser.setType(map, section, resourceIndex, Type.Service);
			Field newField = new Field();
			newField.offset = resourceDataOffset;
			MDNSParser.parseName(map, message, resourceDataOffset, newField, section, resourceIndex, Property.DomainName.name());
			setObject(map, section, resourceIndex, MDNSParser.getCompiledNameValue(map, section, resourceIndex, Property.DomainName.name()), Property.DomainName);
		} else if (recordType == MDNSType.SerivceLocator.getCode()) {
			MDNSParser.setType(map, section, resourceIndex, Type.Service);
			int priority = ((message[resourceDataOffset] & 0xFF) << 8) | (message[resourceDataOffset+1] & 0xFF);
			int weight =   ((message[resourceDataOffset+2] & 0xFF) << 8) | (message[resourceDataOffset+3] & 0xFF);
			int port =     ((message[resourceDataOffset+4] & 0xFF) << 8) | (message[resourceDataOffset+5] & 0xFF);
			setObject(map, section, resourceIndex, port, Property.Port);
			Field newField = new Field();
			newField.offset = resourceDataOffset + 6;
			MDNSParser.parseName(map, message, resourceDataOffset + 6, newField, section, resourceIndex, Property.ServiceName.name());
			setObject(map, section, resourceIndex, MDNSParser.getCompiledNameValue(map, section, resourceIndex, Property.ServiceName.name()), Property.ServiceName);
		} else if (recordType == MDNSType.Text.getCode()) {
			MDNSParser.setType(map, section, resourceIndex, Type.Service);
			Field newField = new Field();
			newField.offset = resourceDataOffset;
			StringBuilder builder = new StringBuilder();
			while (newField.offset < resourceDataOffset + resourceDataLength) {
				MDNSParser.parseString(message, newField);
				builder.append(newField.fieldValue);
				builder.append(System.lineSeparator());
			}
			setObject(map, section, resourceIndex, builder.toString(), Property.TextData);
		} else {//TODO: A lot of resource parsing with else ifs go here...
			MDNSParser.setType(map, section, resourceIndex, Type.Device);//For lack of a better thing to call it...
			byte[] data = new byte[resourceDataLength];
			System.arraycopy(message, resourceDataOffset, data, 0, resourceDataLength);
			setObject(map, section, resourceIndex, data, Property.ByteData);
		}
	}
	
	public static int encodeResource(byte[] message, int offset, MDNSPackage pack) {
		InetAddress address = null;
		String name = null;
		switch (pack.mtype) {
		case HostAddress:
			address = (InetAddress)pack.thing.getProperty(Property.IPV4Address);
			System.arraycopy(address.getAddress(), 0, message, offset, 4);
			return 4;
		case IPV6Address:
			address = (InetAddress)pack.thing.getProperty(Property.IPV6Address);
			System.arraycopy(address.getAddress(), 0, message, offset, 16);
			return 16;
		case DomainNamePointer:
			name = (String)pack.thing.getProperty(Property.DomainName);
			return MDNSGenerator.encodeName(message, offset, name) - offset;
		case SerivceLocator:
			name = (String)pack.thing.getProperty(Property.ServiceName);
			offset = MDNSGenerator.encodeShort(message, offset, 0);//priority
			offset = MDNSGenerator.encodeShort(message, offset, 0);//weight
			offset = MDNSGenerator.encodeShort(message, offset, (int)pack.thing.getProperty(Property.Port));
			int slength = MDNSGenerator.encodeName(message, offset, name) - offset;
			return slength + 6;
		case Text:
			name = (String)pack.thing.getProperty(Property.TextData);
			int totalBytes = 0;
			BufferedReader reader = new BufferedReader(new StringReader(name));
			try {
				while ((name = reader.readLine()) != null) {
					int bytes = MDNSGenerator.encodeString(message, offset, name) - offset;
					totalBytes += bytes;
					offset += bytes;
				}
				return totalBytes;
			} catch (IOException e) {
				//We aren't going to get an IOException on a String reader
			}
		default:
			throw new IllegalArgumentException("I don't know how to encode:" + pack.mtype);
		}
	}
}

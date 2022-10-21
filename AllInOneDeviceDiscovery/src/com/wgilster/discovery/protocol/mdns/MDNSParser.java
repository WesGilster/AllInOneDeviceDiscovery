package com.wgilster.discovery.protocol.mdns;

import java.util.Map;
import java.util.TreeMap;

import com.wgilster.discovery.Type;

public class MDNSParser {//udp.port == 5353 && ip.src == 192.168.0.88
	public static final String UNKNOWN = "Unknown";

	public static final String ID = "Id";
	public static final String HEADER = "Header";
	public static final String FLAGS = "Flags";
	
	public static final String QR = "QueryResponse";
	public static final String QR_QUERY = "Query";
	public static final String QR_REPLY = "Reply";
	
	public static final String OPCODE = "OpCode";
	public static final String OPCODE_STANDARD = "StandardQuery";
	public static final String OPCODE_INVERSE = "InverseQuery";
	public static final String OPCODE_STATUS = "Status";

	public static final String AA = "Authoritative";
	public static final String TC = "MessageTruncated";
	public static final String RD = "RecursionDesired";
	public static final String RA = "RecursionAvailable";
	public static final String Z = "ReservedForFuture";
	public static final String AD = "AuthenticData";
	public static final String CD = "CheckingDisabled";
	public static final String RCODE = "ResponseCode";
	public static final String RCODE_NOERROR = "NoError";
	public static final String RCODE_FORMERROR = "FormatError";
	public static final String RCODE_SERVFAIL = "ServerFailure";
	public static final String RCODE_NXDOMAIN = "NonExistantDomain";
	public static final String RCODE_NOTIMP = "NotImplemented";
	public static final String RCODE_REFUSED = "Refused";
	public static final String COUNT = "Count";
	
	public static final String QUESTION_SECTION = "QuestionSection";
	public static final String ANSWER_SECTION = "AnswerSection";
	public static final String NAME_SERVER_SECTION = "NameServerSection";
	public static final String ADDITIONAL_RECORD_SECTION = "AdditionalRecordSection";
	
	public static final String INDEX = "Index:";
	public static final String QU = "PreferUnicast";
	public static final String UR = "UniqueRecordSet";
	
	public static final String TTL = "TimeToLive";
	public static final String RDL = "RecordDataLength";
	public static final String CLASS = "Class";
	public static final String TYPE = "Type";
	public static final String NAME = "Name";
	
	static class Field {
		public String fieldValue;
		public boolean pointer;
		public int consumedBytes;
		public int offset;
		public boolean lastNode;
	}
	
	public static Map<String, Object> getFields(byte[] message) {
		try {
			Map<String, Object> fields = new TreeMap<String, Object>();
			int id     = ((message[0] & 0xFF) << 8) | (message[1] & 0xFF);
			int qr     = (message[2] & 0B10000000) >> 7;
					
			int opcode = (message[2] & 0B01111000) >> 3;
			int aa     = (message[2] & 0B00000100) >> 2;
			int tc     = (message[2] & 0B00000010) >> 1;
			int rd     = (message[2] & 0B00000001);
			
			int ra     = (message[2] & 0B10000000) >> 7;
			int z      = (message[3] & 0B01000000) >> 6;
			int ad     = (message[3] & 0B00100000) >> 5;
			int cd     = (message[3] & 0B00010000) >> 4;
			int rcode  = (message[3] & 0B00001111);
			int qdcount = ((message[4] & 0xFF) << 8) | (message[5] & 0xFF);
			int ancount = ((message[6] & 0xFF) << 8) | (message[7] & 0xFF);
			int nscount = ((message[8] & 0xFF) << 8) | (message[9] & 0xFF);
			int arcount = ((message[10] & 0xFF) << 8) | (message[11] & 0xFF);
			fields.put(HEADER + "." + FLAGS + "." + ID,         id);
			fields.put(HEADER + "." + FLAGS + "." + QR,         qr==0?QR_QUERY:QR_REPLY);
			fields.put(HEADER + "." + FLAGS + "." + OPCODE,     opcode==0?OPCODE_STANDARD:opcode==1?OPCODE_INVERSE:opcode==2?OPCODE_STATUS:(UNKNOWN + opcode));
			fields.put(HEADER + "." + FLAGS + "." + AA,         aa!=0);
			fields.put(HEADER + "." + FLAGS + "." + TC,         tc!=0);
			fields.put(HEADER + "." + FLAGS + "." + RD,         rd!=0);
			fields.put(HEADER + "." + FLAGS + "." + RA,         ra!=0);
			fields.put(HEADER + "." + FLAGS + "." + Z,          z!=0);
			fields.put(HEADER + "." + FLAGS + "." + AD,         ad!=0);
			fields.put(HEADER + "." + FLAGS + "." + CD,         cd!=0);
			fields.put(HEADER + "." + FLAGS + "." + RCODE,      rcode==0?RCODE_NOERROR:rcode==1?RCODE_FORMERROR:rcode==2?RCODE_SERVFAIL:rcode==3?RCODE_NXDOMAIN:rcode==4?RCODE_NOTIMP:rcode==5?RCODE_REFUSED:(UNKNOWN + rcode));
			fields.put(QUESTION_SECTION + "." + COUNT,  				qdcount);
			fields.put(ANSWER_SECTION + "." + COUNT,  					ancount);
			fields.put(NAME_SERVER_SECTION + "." + COUNT,  				nscount);
			fields.put(ADDITIONAL_RECORD_SECTION + "." + COUNT,  		arcount);
			int offset = parseResourceRecord(fields, message, 12, qdcount, QUESTION_SECTION, QU);
			offset = parseResourceRecord(fields, message, offset, ancount, ANSWER_SECTION, UR);
			offset = parseResourceRecord(fields, message, offset, nscount, NAME_SERVER_SECTION, UR);
			offset = parseResourceRecord(fields, message, offset, arcount, ADDITIONAL_RECORD_SECTION, UR);
			return fields;
		} catch (Exception e) {
			//TODO: I've been getting some array references out of bounds:
			e.printStackTrace();
			return null;
		}
	}
	
	public static int parseResourceRecord(Map<String, Object> map, byte[] message, int originalOffset, int resourceCount, String section, String topBitLabel) {
		Field field = new Field();
		field.offset = originalOffset;
		for (int t = 0; t < resourceCount; t++) {
			field = parseName(map, message, originalOffset, field, section, t, NAME);
			int qtype =   ((message[field.offset++] & 0xFF) << 8) | (message[field.offset++] & 0xFF);
			boolean qu =  (message[field.offset] & 0B1000000) == 0B1000000;
			map.put(compileFieldLabel(section, t, topBitLabel),  qu);
			int qclass = ((message[field.offset++] & 0B01111111) << 8) | (message[field.offset++] & 0xFF);
			field.consumedBytes += 4;
			map.put(compileFieldLabel(section, t, TYPE),  MDNSType.fromCode(qtype));
			map.put(compileFieldLabel(section, t, CLASS), MDNSClass.fromCode(qclass));
			
			//Questions can't have resource data
			if (section.equals(QUESTION_SECTION)) {
				setType(map, section, t, Type.Device);
				continue;
			}
			
			int ttl = ((message[field.offset++] & 0xFF) << 24) | ((message[field.offset++] & 0xFF) << 16) | ((message[field.offset++] & 0xFF) << 8) | (message[field.offset++] & 0xFF);
			field.consumedBytes += 4;
			map.put(compileFieldLabel(section, t, TTL), ttl);

			int rdl = ((message[field.offset++] & 0xFF) << 8) | (message[field.offset++] & 0xFF);
			field.consumedBytes += 2;
			map.put(compileFieldLabel(section, t, RDL), rdl);
			
			ResourceDataManager.parseResourceData(map, message, field.offset, rdl, section, t, qtype, qclass);
			field.consumedBytes += rdl;
			field.offset += rdl;
		}
		
		return field.offset;
	}
	
	public static Field parseName(Map<String, Object> map, byte[] message, int originalOffset, Field field, String section, int index, String fieldLabel) {
		int fieldIndex = 0;
		while (!field.lastNode) {
			String fieldPrefix = compileNameLabelWithIndex(section, index, fieldLabel, fieldIndex);
			fieldIndex++;
			field = parseSingle(message, field);
			if (field.fieldValue != null) {
				map.put(fieldPrefix, field.fieldValue);
			}
		}
		field.offset = originalOffset+field.consumedBytes;
		field.lastNode = false;
		field.pointer = false;
		return field;
	}
	
	public static Field parseSingle(byte[] message, Field field) {
		/*if (field.offset < 0) {
			System.out.println("Problem");
		}*/
		boolean thisIsPointer = (message[field.offset] & 0B11000000) == 0B11000000;
		int messageSizeOrPointerIndex = (message[field.offset] & 0B00111111);
		if (thisIsPointer) {
			field.offset = /*(messageSizeOrPointerIndex << 8) |*/ (message[field.offset+1] & 0xFF);
			if (!field.pointer) {
				field.consumedBytes += 2;
			}
			messageSizeOrPointerIndex = (message[field.offset] & 0B00111111);
		}
		field.pointer = field.pointer || thisIsPointer;
		if (messageSizeOrPointerIndex > 0) {
			field.fieldValue = new String(message, field.offset+1, messageSizeOrPointerIndex);
		} else {
			field.fieldValue = null;
			field.lastNode = true;
		}
		if (!field.pointer) {
			field.consumedBytes += messageSizeOrPointerIndex + 1;
		}
		field.offset += messageSizeOrPointerIndex + 1;
		return field;
	}
	
	public static Field parseString(byte[] message, Field field) {
		int messageSizeOrPointerIndex = (message[field.offset] & 0B11111111);
		if (messageSizeOrPointerIndex > 0) {
			field.fieldValue = new String(message, field.offset+1, messageSizeOrPointerIndex);
		} else {
			field.fieldValue = null;
			field.lastNode = true;
		}
		if (!field.pointer) {
			field.consumedBytes += messageSizeOrPointerIndex + 1;
		}
		field.offset += messageSizeOrPointerIndex + 1;
		return field;
	}
	
	public static void setType(Map<String, Object> map, String resourceTypeLabel, int resourceIndex, Type type) {
		String label = MDNSParser.compileFieldLabel(resourceTypeLabel, resourceIndex, Type.class.getName());
		map.put(label, type);
	}

	static String compileFieldLabel(String section, int index, String fieldLabel) {
		return section + "." + INDEX + index + "." + fieldLabel;
	}
	
	public static Object getFieldValue(Map<String, Object> map, String section, int index, String fieldLabel) {
		return map.get(compileFieldLabel(section, index, fieldLabel));
	}
	
	
	private static String compileNameLabelWithIndex(String section, int index, String nameLabel, int nameIndex) {
		return section + "." + INDEX + index + "." + nameLabel + "." + INDEX + nameIndex;
	}
	public static String getCompiledNameValue(Map<String, Object> map, String section, int index, String nameLabel) {
		StringBuilder name = new StringBuilder();
		int nameIndex = 0;
		String nameComponent = null;
		do {
			String nameKey = compileNameLabelWithIndex(section, index, nameLabel, nameIndex);
			nameComponent = (String)map.get(nameKey);
			if (nameComponent != null) {
				if (name.length() > 0) {
					name.append(".");
				}
				name.append(nameComponent);
			}
			nameIndex++;
		} while (nameComponent != null);
		return name.toString();
	}
}
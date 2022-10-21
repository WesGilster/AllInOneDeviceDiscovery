package com.wgilster.discovery.protocol.mdns;

import java.util.ArrayList;
import java.util.List;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.protocol.mdns.ResourceDataManager.MDNSPackage;

public class MDNSGenerator {
	/**
	 * The whole "cool part" about this generator is that it doesn't have to be MDNSThings that are sent to this.
	 * It should deal with "Anything"...
	 */
	public static byte[] generateMessage(Thing thing) {
		byte[] message = new byte[MDNSProtocol.MESSAGE_SIZE];
		
		if (thing instanceof MDNSThing) {
			throw new IllegalArgumentException("MDNSThings can't be replayed back to the network yet");
		}
		
		switch (thing.getConsumption()) {
		case Seeker:
			throw new IllegalArgumentException("Seeker not implemented yet");
		default:
		case Advertisement:
			List<Thing> questions = new ArrayList<>();
			List<Thing> answers = new ArrayList<>();
			for (Thing childThing : thing.getChildren()) {
				if (childThing.getConsumption() == Consumption.Seeker) {
					questions.add(childThing);
				} else if (childThing.getConsumption() == Consumption.Advertisement) {
					answers.add(childThing);
				}
			}
			
			writeHeader(message, 0, true, 0, true, false, false,
					    false, false, false, false, 0,
					    questions.size(), answers.size(), 0, 0);
			int bytesWritten = 12;
			for (Thing question : questions) {
				bytesWritten = writeQuestion(message, bytesWritten, ResourceDataManager.convertToMDNSPackage(question));
			}
			for (Thing answer : answers) {
				bytesWritten = writeResource(message, bytesWritten, ResourceDataManager.convertToMDNSPackage(answer));
			}
			return message;
		}
	}
	
	private static void writeHeader(byte[] message, int id, boolean isResponse, int opcode, boolean authoritative, boolean messageTruncated, boolean recursionDesired, 
										   boolean recursionAvailable, boolean reservedForFuture, boolean authenticData, boolean checkingDisabled, int responseCode, 
										   int questionCount, int answerCount, int namespaceCount, int additionalRecordCount) {
		encodeShort(message, 0, id);
		
		if (isResponse) {
			message[2] = (byte)(message[2] | 0B10000000);
		}	
		opcode = (opcode & 0B1111) << 3;
		message[2] = (byte)(message[2] | opcode);
		if (authoritative) {
			message[2] = (byte)(message[2] | 0B00000100);
		}	
		if (messageTruncated) {
			message[2] = (byte)(message[2] | 0B00000010);
		}	
		if (recursionDesired) {
			message[2] = (byte)(message[2] | 0B00000001);
		}	
		
		responseCode = responseCode & 0B1111;
		if (recursionAvailable) {
			message[3] = (byte)(message[3] | 0B10000000);
		}	
		if (reservedForFuture) {
			message[3] = (byte)(message[3] | 0B01000000);
		}	
		if (authenticData) {
			message[3] = (byte)(message[3] | 0B00100000);
		}	
		if (checkingDisabled) {
			message[3] = (byte)(message[3] | 0B00010000);
		}	
		message[3] = (byte)(message[3] | responseCode);
		encodeShort(message, 4, questionCount);
		encodeShort(message, 6, answerCount);
		encodeShort(message, 8, namespaceCount);
		encodeShort(message, 10, additionalRecordCount);
	}
	
	private static int writeQuestion(byte[] message, int offset, MDNSPackage pack) {
		offset = encodeName(message, offset, pack.thing.getId());
		offset = encodeShort(message, offset, pack.mtype.getCode());
		int code = pack.mclass.getCode();
		if (pack.classTopBit) {
			code = (byte)(code | 0B10000000);
		}
		offset = encodeShort(message, offset, code);
		return offset;
	}

	private static int writeResource(byte[] message, int offset, MDNSPackage pack) {
		offset = writeQuestion(message, offset, pack);
		offset = encodeInt(message, offset, pack.ttl);
		int resourceDataLength = ResourceDataManager.encodeResource(message, offset, pack);
		offset = encodeShort(message, offset, resourceDataLength);
		offset += resourceDataLength;
		return offset;
	}
	
	public static int encodeShort(byte[] message, int offset, int value) {
		message[offset+1] = (byte)value;
		message[offset] = (byte)(value >> 8);
		return offset + 2;
	}
	
	private static int encodeInt(byte[] message, int offset, int value) {
		message[offset+3] = (byte)value;
		message[offset+2] = (byte)(value >> 8);
		message[offset+1] = (byte)(value >> 16);
		message[offset] = (byte)(value >> 24);
		return offset + 4;
	}
	
	public static int encodeString(byte[] message, int offset, String name) {
		byte[] id = name.getBytes();
		if (id.length > 0B11111111) {
			throw new IllegalArgumentException("Length too long for string:" + name);
		}
		message[offset++] = (byte)id.length;
		System.arraycopy(id, 0, message, offset, id.length);
		offset += id.length;//DON'T add any further bytes!!!
		return offset;
	}
	
	//Compression is not supported...
	public static int encodeName(byte[] message, int offset, String name) {
		byte[] id = name.getBytes();
		if (id.length > 0B00111111) {
			throw new IllegalArgumentException("We don't support compression so this line is too long");
		}
		message[offset++] = (byte)id.length;
		System.arraycopy(id, 0, message, offset, id.length);
		offset += id.length + 1;//Add a zero on the end to terminate
		return offset;
	}
}

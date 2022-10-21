package com.wgilster.discovery.protocol.mdns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.Type;

public class MDNSThing implements Thing {
	private Map<String, Object> properties;
	private NetworkTransport transport;
	private List<Thing> questionChildren = new ArrayList<>();
	private List<Thing> answerChildren = new ArrayList<>();
	private List<Thing> nameServerChildren = new ArrayList<>();
	private List<Thing> additionalRecordChildren = new ArrayList<>();
	
	public MDNSThing(Map<String, Object> properties, NetworkTransport transport) {
		this.properties = properties;
		this.transport = transport;
		
		int questions = (Integer)properties.get(MDNSParser.QUESTION_SECTION + "." + MDNSParser.COUNT);
		int answers = (Integer)properties.get(MDNSParser.ANSWER_SECTION + "." + MDNSParser.COUNT);
		int names = (Integer)properties.get(MDNSParser.NAME_SERVER_SECTION + "." + MDNSParser.COUNT);
		int additional = (Integer)properties.get(MDNSParser.ADDITIONAL_RECORD_SECTION + "." + MDNSParser.COUNT);
		for (int t = 0; t < questions; t++) {
			questionChildren.add(new MDNSChildThing(properties, MDNSParser.QUESTION_SECTION, t));
		}
		for (int t = 0; t < answers; t++) {
			answerChildren.add(new MDNSChildThing(properties, MDNSParser.ANSWER_SECTION, t));
		}
		for (int t = 0; t < names; t++) {
			nameServerChildren.add(new MDNSChildThing(properties, MDNSParser.NAME_SERVER_SECTION, t));
		}
		for (int t = 0; t < additional; t++) {
			additionalRecordChildren.add(new MDNSChildThing(properties, MDNSParser.ADDITIONAL_RECORD_SECTION, t));
		}
	}

	
	@Override
	public String getId() {
		return transport.getRoute().getSourceAddress();// + " (Type:" + getType() + ")";
	}

	@Override
	public Consumption getConsumption() {
		String qr = (String)properties.get(MDNSParser.HEADER + "." + MDNSParser.FLAGS + "." + MDNSParser.QR);
		if (qr.equals(MDNSParser.QR_REPLY)) {
			return Consumption.Advertisement;
		}
		
		return Consumption.Seeker;
	}

	public Type getType() {
		return Type.Device;
	}
	
	private static List<Thing> mergeChildren(List<? extends Thing> oldChildren, List<? extends Thing> newChildren) {
		List<Thing> children = new ArrayList<Thing>();
		List<Thing> unmatchedNewChildren = new ArrayList<Thing>();
		unmatchedNewChildren.addAll(newChildren);
		
		for (Thing oldChild : oldChildren) {
			Thing newFound = null;
			for (Thing newChild : newChildren) {
				if (oldChild.getId().equals(newChild.getId()) &&
					oldChild.getConsumption().equals(newChild.getConsumption()) &&
					((MDNSChildThing)oldChild).getMDNSType().equals(((MDNSChildThing)newChild).getMDNSType())) {
					newFound = newChild;
					unmatchedNewChildren.remove(newChild);
				}
			}
			if (newFound != null) {
				children.add(newFound);
			} else {
				children.add(oldChild);
			}
		}
		for (Thing newChild : unmatchedNewChildren) {
			children.add(newChild);
		}
		return children;
	}
	
	public static void mergeChildrenIntoNew(MDNSThing oldThing, MDNSThing newThing) {
		newThing.questionChildren = mergeChildren(oldThing.questionChildren, newThing.questionChildren);
		newThing.answerChildren = mergeChildren(oldThing.answerChildren, newThing.answerChildren);
		newThing.nameServerChildren = mergeChildren(oldThing.nameServerChildren, newThing.nameServerChildren);
		newThing.additionalRecordChildren = mergeChildren(oldThing.additionalRecordChildren, newThing.additionalRecordChildren);		
	}
	
	@Override
	public List<Thing> getChildren() {
		List<Thing> children = new ArrayList<Thing>();
		children.addAll(questionChildren);
		children.addAll(answerChildren);
		children.addAll(nameServerChildren);
		children.addAll(additionalRecordChildren);
		return children;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@Override
	public Object getProperty(Property property) {
		return properties.get(property.name());
	}

	public String toString() {
		return getId();
	}
}

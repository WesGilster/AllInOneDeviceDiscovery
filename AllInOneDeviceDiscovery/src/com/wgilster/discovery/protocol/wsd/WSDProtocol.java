package com.wgilster.discovery.protocol.wsd;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.network.tcpudp.UDPNetwork;
import com.wgilster.discovery.protocol.AbstractProtocol;
import com.wgilster.discovery.protocol.ThingWriter;

public class WSDProtocol extends AbstractProtocol {
	public static final int UDP_PORT = 3702;
	public static final String MULTICAST_ADDRESS = "239.255.255.250";
	private Network multi;
	
	public class UDPListener implements NetworkListener {
		@Override
		public void networkEvent(NetworkTransport meta) {
			System.out.println("====");
			System.out.println("Source:" + meta.getRoute().getSourceAddress() +  ":" + meta.getRoute().getSourcePort());
			System.out.println("Destination:" + meta.getRoute().getDestinationAddress() +  ":" + meta.getRoute().getDestinationPort());
			
		}
	}
	
	@Override
	public void bindToNetwork(Network network) throws IOException {
		NetworkRoute route = new NetworkRoute();
		route.setDestinationAddress(MULTICAST_ADDRESS);
		route.setDestinationPort(UDP_PORT);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(UDPNetwork.CAST, UDPNetwork.MULTICAST);
		
		route.setProperties(properties);
		network.registerRoute(route, new UDPListener());
		
		beginWritingHelloThings(network, route);
		multi = network;
	}
	
	private void beginWritingHelloThings(Network network, NetworkRoute route) throws IOException {
		writeThings(new ThingWriter() {
			@Override
			public void writeThing(int iteration, NetworkRoute route, Thing thing) throws IOException {
				String hello = 
						"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + 
						"<soap:Envelope\r\n" + 
						"    xmlns:soap=\"https://www.w3.org/2003/05/soap-envelope\"\r\n" + 
						"    xmlns:wsa=\"https://schemas.xmlsoap.org/ws/2004/08/addressing\"\r\n" + 
						"    xmlns:wsd=\"https://schemas.xmlsoap.org/ws/2005/04/discovery\"\r\n" + 
						"    xmlns:wsdp=\"https://schemas.xmlsoap.org/ws/2006/02/devprof\">\r\n" + 
						"<soap:Header>\r\n" + 
						"    <wsa:To>\r\n" + 
						"        urn:schemas-xmlsoap-org:ws:2005:04:discovery\r\n" + 
						"    </wsa:To>\r\n" + 
						"    <wsa:Action>\r\n" + 
						"        https://schemas.xmlsoap.org/ws/2005/04/discovery/Hello\r\n" + 
						"    </wsa:Action>\r\n" + 
						"    <wsa:MessageID>\r\n" + 
						"        urn:uuid:%1s\r\n" + 
						"    </wsa:MessageID>\r\n" + 
						"    <wsd:AppSequence InstanceId=\"2\"\r\n" + 
						"        SequenceId=\"urn:uuid:%2s\"\r\n" + 
						"        MessageNumber=\"%3s\">\r\n" + 
						"    </wsd:AppSequence>\r\n" + 
						"</soap:Header>\r\n" + 
						"<soap:Body>\r\n" + 
						"    <wsd:Hello>\r\n" + 
						"        <wsa:EndpointReference>\r\n" + 
						"            <wsa:Address>\r\n" + 
						"                urn:uuid:%4s\r\n" + 
						"            </wsa:Address>\r\n" + 
						"        </wsa:EndpointReference>\r\n" + 
						"        <wsd:Types>wsdp:Device</wsd:Types>\r\n" + 
						"        <wsd:MetadataVersion>2</wsd:MetadataVersion>\r\n" + 
						"    </wsd:Hello>\r\n" + 
						"</soap:Body>";
				network.writeBytes(route, 
						String.format(hello, thing.getId(), 
								thing.getProperty(Property.ServiceName), 
								iteration, 
								thing.getProperty(Property.IPV4Address)
								).getBytes());
			}
		}, Collections.singletonList((Thing)new WSDHelloThing()), route, TimeUnit.SECONDS, 5L, Integer.MAX_VALUE);
	}
	
	@Override
	public List<Thing> readThings() throws IOException {
		return null;
	}
	
	@Override
	public void writeThing(int iteration, NetworkRoute route, Thing thing) throws IOException {
		if (thing instanceof WSDProbeThing) {
			String probe = 
					"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + 
					"<soap:Envelope\r\n" + 
					"    xmlns:soap=\"https://www.w3.org/2003/05/soap-envelope\"\r\n" + 
					"    xmlns:wsa=\"https://schemas.xmlsoap.org/ws/2004/08/addressing\"\r\n" + 
					"    xmlns:wsd=\"https://schemas.xmlsoap.org/ws/2005/04/discovery\"\r\n" + 
					"    xmlns:wsdp=\"https://schemas.xmlsoap.org/ws/2006/02/devprof\">\r\n" + 
					"<soap:Header>\r\n" + 
					"    <wsa:To>\r\n" + 
					"        urn:schemas-xmlsoap-org:ws:2005:04:discovery\r\n" + 
					"    </wsa:To>\r\n" + 
					"    <wsa:Action>\r\n" + 
					"        https://schemas.xmlsoap.org/ws/2005/04/discovery/Probe\r\n" + 
					"    </wsa:Action>\r\n" + 
					"    <wsa:MessageID>\r\n" + 
					"        urn:uuid:%1s\r\n" + 
					"    </wsa:MessageID>\r\n" + 
					"</soap:Header>\r\n" + 
					"<soap:Body>\r\n" + 
					"    <wsd:Probe>\r\n" + 
					"        <wsd:Types>wsdp:Device</wsd:Types>\r\n" + 
					"    </wsd:Probe>\r\n" + 
					"</soap:Body>";
			multi.writeBytes(route, String.format(probe, thing.getId()).getBytes());
		}
	}
}

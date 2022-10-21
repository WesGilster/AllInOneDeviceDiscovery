package com.wgilster.discovery.protocol.mdns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.ProtocolListener;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.network.tcpudp.UDPNetwork;
import com.wgilster.discovery.protocol.AbstractProtocol;

public class MDNSProtocol extends AbstractProtocol {
	public static final int UDP_PORT = 5353;
	public static final String MULTICAST_ADDRESS = "224.0.0.251";
	public static final int MESSAGE_SIZE = 9000;

	private Map<String, Thing> things = new HashMap<>();
	private Network multi;
	private NetworkRoute multicastRoute;
	
	public class UDPListener implements NetworkListener {
		@Override
		public void networkEvent(NetworkTransport meta) {
			Map<String, Object> fields = MDNSParser.getFields(meta.getBytes());
			MDNSThing thing = new MDNSThing(fields, meta);
			
			MDNSThing oldThing = (MDNSThing)things.get(thing.getId());
			if (oldThing != null) {
				MDNSThing.mergeChildrenIntoNew(oldThing, thing);
			} else if (!thing.getConsumption().equals(Consumption.Seeker)){
				things.put(thing.getId(), thing);
			}
			
			for (ProtocolListener listener : getProtocolListeners()) {
				listener.protocolEvent(meta, thing);
			}
		}
	}
	
	@Override
	public void bindToNetwork(Network network) throws IOException {
		multicastRoute = new NetworkRoute();
		multicastRoute.setDestinationAddress(MULTICAST_ADDRESS);
		multicastRoute.setDestinationPort(UDP_PORT);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(UDPNetwork.CAST, UDPNetwork.MULTICAST);
		
		multicastRoute.setProperties(properties);
		network.registerRoute(multicastRoute, new UDPListener());
		multi = network;
	}
	
	@Override
	public List<Thing> readThings() throws IOException {
		return new ArrayList<Thing>(things.values());
	}
	
	public void writeThing(int iteration, NetworkRoute networkRoute, Thing thing) throws IOException {
		byte[] bytes = MDNSGenerator.generateMessage(thing);
		if (networkRoute == null) {
			networkRoute = multicastRoute;
		}
		multi.writeBytes(networkRoute, bytes);
	}
}

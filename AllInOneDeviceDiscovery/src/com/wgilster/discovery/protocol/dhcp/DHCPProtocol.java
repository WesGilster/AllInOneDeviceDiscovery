package com.wgilster.discovery.protocol.dhcp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.Protocol;
import com.wgilster.discovery.ProtocolListener;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.network.tcpudp.UDPNetwork;

public class DHCPProtocol implements Protocol {
	public static final int UDP_PORT = 67;
	
	public static class NameResolutionListener implements NetworkListener {
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
		route.setSourcePort(UDP_PORT);
		route.setSourceAddress(UDPNetwork.WILDCARD_ADDRESS);
		
		Map<String, Object> properties = new HashMap<>();
		properties.put(UDPNetwork.CAST, UDPNetwork.BROADCAST);
		
		route.setProperties(properties);
		network.registerRoute(route, new NameResolutionListener());
	}
	
	@Override
	public ScheduledFuture writeThings(List<Thing> things, NetworkRoute route, TimeUnit timeUnit, long length, int iterations) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List readThings() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void addProtocolListener(ProtocolListener listener) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeProtocolListener(ProtocolListener listener) {
		// TODO Auto-generated method stub
		
	}
}

package com.wgilster.discovery.protocol.upnp;

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

public class UPNPProtocol implements Protocol {
	public static final int UDP_PORT = 1900;
	public static final String MULTICAST_ADDRESS = "239.255.255.250";
	
	public static class UDPListener implements NetworkListener {
		@Override
		public void networkEvent(NetworkTransport meta) {
			System.out.println(new String(meta.getBytes()));
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

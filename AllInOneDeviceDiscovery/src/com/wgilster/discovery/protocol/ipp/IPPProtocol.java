package com.wgilster.discovery.protocol.ipp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.Protocol;
import com.wgilster.discovery.ProtocolListener;
import com.wgilster.discovery.Thing;

public class IPPProtocol implements Protocol {
	public static final int TCP_PORT = 631;
	
	public static class TCPListener implements NetworkListener {
		@Override
		public void networkEvent(NetworkTransport meta) {
			System.out.println(new String(meta.getBytes()));
		}
	}
	
	@Override
	public void bindToNetwork(Network network) throws IOException {
		NetworkRoute route = new NetworkRoute();
		route.setSourcePort(TCP_PORT);
		
		network.registerRoute(route, new TCPListener());
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

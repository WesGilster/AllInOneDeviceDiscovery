package com.wgilster.discovery.network.tcpudp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.NetworkTransport;

public class UDPNetwork implements Network {
	public static final String CAST = "Cast";
	public static final String MULTICAST = "Multicast";
	public static final String BROADCAST = "Broadcast";
	public static final String WILDCARD_ADDRESS = "0.0.0.0";
	
	private Map<NetworkRoute, Acceptor> acceptors = new Hashtable<NetworkRoute, Acceptor>();
	private int messageSize;
	
	private static class Acceptor implements Runnable, Closeable {
		private DatagramSocket datagram;
		private List<NetworkListener> listeners = new ArrayList<NetworkListener>();
		private boolean alive = true;
		private int messageSize;
		
		public Acceptor(NetworkRoute route, int messageSize) throws UnknownHostException, IOException {
			this.messageSize = messageSize;
			String cast = (String)route.getProperties().get(CAST);
			if (cast == null || (!cast.equals(MULTICAST) && !cast.equals(BROADCAST))) {
				throw new IOException("You must specify:" + CAST + " as: " + MULTICAST + " or " + BROADCAST);
			}
			boolean useMulti = cast.equals(MULTICAST);
			if (useMulti) {
				if (route.getDestinationAddress() == null) {
					throw new IOException("You must specify a destination address");
				}
				if (route.getDestinationPort() == null) {
					throw new IOException("You must specify a destination port");
				}
				datagram = new MulticastSocket(route.getDestinationPort());
				if (route.getSourceAddress() != null) {
					if (route.getSourcePort() != null) {
						((MulticastSocket)datagram).joinGroup(new InetSocketAddress(route.getDestinationAddress(), route.getSourcePort()), NetworkInterface.getByName(route.getSourceAddress()));
					} else {
						throw new IOException("You must specify a source port if you specify a source address");
						//((MulticastSocket)datagram).joinGroup(new InetSocketAddress(route.getDestinationAddress(), route.getDestinationPort()), NetworkInterface.getByName(route.getSourceAddress()));
					}
				} else {
					if (route.getSourcePort() != null) {
						((MulticastSocket)datagram).joinGroup(new InetSocketAddress(route.getDestinationAddress(), route.getSourcePort()), null);
					} else {
						((MulticastSocket)datagram).joinGroup(InetAddress.getByName(route.getDestinationAddress()));
					}
				}
			} else {
				if (route.getSourceAddress() != null) {
					if (route.getSourcePort() != null) {
						datagram = new DatagramSocket(new InetSocketAddress(InetAddress.getByName(route.getSourceAddress()), route.getSourcePort()));
					} else {
						throw new IOException("You must specify a source port if you specify a source address.");
					}
				} else {
					if (route.getSourcePort() != null) {
						datagram = new DatagramSocket(route.getSourcePort());
						datagram.setBroadcast(true);
					} else {
						datagram = new DatagramSocket();
					}
				}
			}
		}
		
		public void run() {
			while (alive) {
				try {
					DatagramPacket packet = new DatagramPacket(new byte[messageSize], messageSize);
					datagram.receive(packet);
					NetworkRoute route = new NetworkRoute();
					route.setDestinationAddress(datagram.getLocalAddress().getHostAddress());
					route.setDestinationPort(datagram.getLocalPort());
					route.setSourceAddress(packet.getAddress().getHostAddress());
					route.setSourcePort(packet.getPort());
					
					NetworkTransport meta = new NetworkTransport();
					meta.setBytes(packet.getData());
					meta.setRoute(route);
					for (NetworkListener listener : listeners) {
						try {
							listener.networkEvent(meta);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		
		public void close() {
			alive = false;
			if (datagram != null) {
				datagram.close();
			}
		}
	}
	
	public UDPNetwork(int messageSize) {
		this.messageSize = messageSize;
	}
	
	@Override
	public void registerRoute(NetworkRoute network, NetworkListener listener) throws IOException {
		Acceptor acceptor = acceptors.get(network);
		if (acceptor == null) {
			acceptor = new Acceptor(network, messageSize);
			Thread acceptorThread = new Thread(acceptor, "Acceptor for:" + network);
			acceptorThread.setDaemon(true);
			acceptorThread.start();
		}
		if (listener != null) {
			acceptor.listeners.add(listener);
		}
		acceptors.put(network, acceptor);
	}
	
	@Override
	public void unRegisterRoute(NetworkRoute network, NetworkListener listener) {
		Acceptor acceptor = acceptors.get(network);
		if (acceptor == null) {
			return;
		}
		
		acceptor.listeners.remove(listener);
		if (acceptor.listeners.size() == 0) {
			acceptor.close();
		}
	}

	@Override
	public NetworkTransport startStream(NetworkRoute network) throws IOException {
		throw new IOException("Streams aren't used with UDP networks");
	}
	
	public void endStream(NetworkTransport metaData) {
		throw new IllegalArgumentException("Streams aren't used with UDP networks");
	}
	
	@Override
	public void writeBytes(NetworkRoute network, byte[] writeBytes) throws IOException {
		acceptors.get(network);
	}

	@Override
	public void close() {
		for (Acceptor acceptor : acceptors.values()) {
			acceptor.close();
		}
	}
}

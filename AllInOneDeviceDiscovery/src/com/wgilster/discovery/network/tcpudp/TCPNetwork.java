package com.wgilster.discovery.network.tcpudp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.wgilster.discovery.Network;
import com.wgilster.discovery.NetworkListener;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.NetworkRoute;

public class TCPNetwork implements Network {
	private Map<NetworkRoute, Acceptor> acceptors = new Hashtable<NetworkRoute, Acceptor>();
	private Map<NetworkTransport, Socket> clientSockets = new Hashtable<NetworkTransport, Socket>();
	
	private static class Acceptor implements Runnable, Closeable {
		private ServerSocket server;
		private List<NetworkListener> listeners = new ArrayList<NetworkListener>();
		private boolean alive = true;
		
		public Acceptor(NetworkRoute route) throws UnknownHostException, IOException {
			this.server = new ServerSocket(route.getSourcePort(), 10, InetAddress.getByName(route.getSourceAddress()));
		}

		public void run() {
			while (alive) {
				try {
					Socket client = server.accept();
					NetworkRoute route = new NetworkRoute();
					route.setDestinationAddress(client.getLocalAddress().getHostName());
					route.setDestinationPort(client.getLocalPort());
					route.setSourceAddress(((InetSocketAddress)client.getRemoteSocketAddress()).getHostName());
					route.setSourcePort(((InetSocketAddress)client.getRemoteSocketAddress()).getPort());
					
					NetworkTransport meta = new NetworkTransport();
					meta.setInputStream(client.getInputStream());
					meta.setOutputStream(client.getOutputStream());
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
			try {
				alive = false;
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void registerRoute(NetworkRoute network, NetworkListener listener) throws IOException {
		Acceptor acceptor = acceptors.get(network);
		if (acceptor == null) {
			acceptor = new Acceptor(network);
			Thread acceptorThread = new Thread(acceptor, "Acceptor for:" + network);
			acceptorThread.setDaemon(true);
			acceptorThread.start();
		}
		acceptor.listeners.add(listener);
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
		Socket socket = null;
		if (network.getSourceAddress() != null) {
			if (network.getSourcePort() != null) {
				socket = new Socket(network.getDestinationAddress(), network.getDestinationPort(), InetAddress.getByName(network.getSourceAddress()), network.getSourcePort());
			} else {
				socket = new Socket(network.getDestinationAddress(), network.getDestinationPort(), InetAddress.getByName(network.getSourceAddress()), 0);
			}
		} else {
			socket = new Socket(network.getDestinationAddress(), network.getDestinationPort());
		}
		NetworkTransport meta = new NetworkTransport();
		clientSockets.put(meta, socket);
		meta.setInputStream(socket.getInputStream());
		meta.setOutputStream(socket.getOutputStream());
		meta.setRoute(network);
		return meta;
	}
	
	public void endStream(NetworkTransport metaData) {
		Socket socket = clientSockets.get(metaData);
		if (socket == null) {
			return;
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeBytes(NetworkRoute network, byte[] writeBytes) throws IOException {
		NetworkTransport meta = startStream(network);
		try {
			meta.getOutputStream().write(writeBytes);
		} finally {
			endStream(meta);
		}
	}

	@Override
	public void close() {
		for (Acceptor acceptor : acceptors.values()) {
			acceptor.close();
		}
		for (Socket client : clientSockets.values()) {
			try {
				client.close();
			} catch (IOException e) {
			}
		}
	}
}

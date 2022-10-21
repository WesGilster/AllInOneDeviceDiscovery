package com.wgilster.discovery;

import java.io.IOException;

public interface Network {
	public void registerRoute(NetworkRoute network, NetworkListener listener) throws IOException;
	public void unRegisterRoute(NetworkRoute network, NetworkListener listener);
	public NetworkTransport startStream(NetworkRoute network) throws IOException;
	public void endStream(NetworkTransport metaData);
	public void writeBytes(NetworkRoute network, byte[] writeBytes) throws IOException;
	public void close();
}

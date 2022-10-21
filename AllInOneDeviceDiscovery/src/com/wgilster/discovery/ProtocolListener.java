package com.wgilster.discovery;

public interface ProtocolListener {
	public void protocolEvent(NetworkTransport transport, Thing thing);
}

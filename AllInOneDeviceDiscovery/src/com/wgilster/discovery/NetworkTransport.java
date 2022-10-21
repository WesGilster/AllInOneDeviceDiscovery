package com.wgilster.discovery;

import java.io.InputStream;
import java.io.OutputStream;

public class NetworkTransport {
	private NetworkRoute route;
	private InputStream inputStream;
	private OutputStream outputStream;
	private byte[] bytes;
	
	public NetworkRoute getRoute() {
		return route;
	}
	public void setRoute(NetworkRoute route) {
		this.route = route;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
}

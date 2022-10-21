package com.wgilster.discovery;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface Protocol {
	public void bindToNetwork(Network network) throws IOException;
	public ScheduledFuture writeThings(List<Thing> things, NetworkRoute route, TimeUnit timeUnit, long length, int iterations) throws IOException;
	public List<Thing> readThings() throws IOException;
	public void addProtocolListener(ProtocolListener listener);
	public void removeProtocolListener(ProtocolListener listener);
}
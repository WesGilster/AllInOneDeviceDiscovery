package com.wgilster.discovery.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.Protocol;
import com.wgilster.discovery.ProtocolListener;
import com.wgilster.discovery.Thing;

public abstract class AbstractProtocol implements Protocol, ThingWriter {
	private List<ProtocolListener> listeners = new ArrayList<ProtocolListener>();

	protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(50, new ThreadFactory(){
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread("BackgroundProtocolThread");
			thread.setDaemon(true);
			return thread;
		}
	});
	
	public List<ProtocolListener> getProtocolListeners() {
		return listeners;
	}
	
	@Override
	public void addProtocolListener(ProtocolListener listener) {
		listeners.add(listener);
	}
	@Override
	public void removeProtocolListener(ProtocolListener listener) {
		listeners.remove(listener);
	}

	public abstract void writeThing(int iteration, NetworkRoute route, Thing thing) throws IOException;
	
	protected ScheduledFuture writeThings(ThingWriter writer, List<Thing> things, NetworkRoute route, TimeUnit timeUnit, long delay, final int iterations) throws IOException {
		final ScheduledFuture future[] = new ScheduledFuture[1];
		future[0] = executor.scheduleWithFixedDelay(new Runnable() {
			private int iter = iterations;
			
			@Override
			public void run() {
				NetworkRoute networkRoute = route;
				for (Thing thing : things) {
					try {
						writer.writeThing(iter, route, thing);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (iter-- <= 0) {
					future[0].cancel(true);
				}
			}
		}, 0, delay, timeUnit);
		return future[0];
	}
	
	@Override
	public ScheduledFuture writeThings(List<Thing> things, NetworkRoute route, TimeUnit timeUnit, long delay, final int iterations) throws IOException {
		return writeThings(this, things, route, timeUnit, delay, iterations);
	}
}

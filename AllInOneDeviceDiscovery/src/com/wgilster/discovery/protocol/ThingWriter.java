package com.wgilster.discovery.protocol;

import java.io.IOException;

import com.wgilster.discovery.NetworkRoute;
import com.wgilster.discovery.Thing;

public interface ThingWriter {
	public void writeThing(int iteration, NetworkRoute route, Thing thing) throws IOException;
}

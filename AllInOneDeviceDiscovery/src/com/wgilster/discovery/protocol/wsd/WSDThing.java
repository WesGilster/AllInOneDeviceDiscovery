package com.wgilster.discovery.protocol.wsd;

import java.util.UUID;

import com.wgilster.discovery.Thing;

public abstract class WSDThing implements Thing {
	private UUID uuid = UUID.randomUUID();

	@Override
	public String getId() {
		return uuid.toString();
	}
}

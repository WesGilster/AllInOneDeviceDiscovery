package com.wgilster.discovery;

import java.util.Map;

public class NetworkRoute {
	private String sourceAddress;
	private String destinationAddress;
	private Integer sourcePort;
	private Integer destinationPort;
	private Map<String, ?> properties;
	
	public String getSourceAddress() {
		return sourceAddress;
	}
	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	public String getDestinationAddress() {
		return destinationAddress;
	}
	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}
	public Integer getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(Integer sourcePort) {
		this.sourcePort = sourcePort;
	}
	public Integer getDestinationPort() {
		return destinationPort;
	}
	public void setDestinationPort(Integer destinationPort) {
		this.destinationPort = destinationPort;
	}
	public Map<String, ?> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, ?> properties) {
		this.properties = properties;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationAddress == null) ? 0 : destinationAddress.hashCode());
		result = prime * result + ((destinationPort == null) ? 0 : destinationPort.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((sourceAddress == null) ? 0 : sourceAddress.hashCode());
		result = prime * result + ((sourcePort == null) ? 0 : sourcePort.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkRoute other = (NetworkRoute) obj;
		if (destinationAddress == null) {
			if (other.destinationAddress != null)
				return false;
		} else if (!destinationAddress.equals(other.destinationAddress))
			return false;
		if (destinationPort == null) {
			if (other.destinationPort != null)
				return false;
		} else if (!destinationPort.equals(other.destinationPort))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (sourceAddress == null) {
			if (other.sourceAddress != null)
				return false;
		} else if (!sourceAddress.equals(other.sourceAddress))
			return false;
		if (sourcePort == null) {
			if (other.sourcePort != null)
				return false;
		} else if (!sourcePort.equals(other.sourcePort))
			return false;
		return true;
	}
}

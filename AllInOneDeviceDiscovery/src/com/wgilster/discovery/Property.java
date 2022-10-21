package com.wgilster.discovery;

public enum Property {
	TTL,				//int
	ServiceName,		//String
	DomainName,			//String
	//LocationName,		//String
	Port,				//int
	IPV4Address,		//Inet4Address
	IPV6Address,		//Inet6Address
	TextData,			//long string separated by Strings with line separators
	ByteData,			//byte[]
}

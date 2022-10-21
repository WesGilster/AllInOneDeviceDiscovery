package com.wgilster.discovery.protocol.mdns;
public enum MDNSType {
	HostAddress(1, "A", false),
	AuthoritativeNameServer(2, "NS", false),
	MailDestination(3, "MD", true),
	MailForwarder(4, "MF", true),
	CanonicalNameForAlias(5, "CNAME", false),
	StartOfZoneAuthority(6, "SOA", false),
	MailboxDomainName(7, "MB", true),
	MailGroupMember(8, "MG", true),
	MailRenameDomainName(9, "MR", true),
	EmptyResourceData(10, "NULL", true),
	WellKnownService(11, "WKS", true),
	DomainNamePointer(12, "PTR", false),
	HostInfo(13, "HINFO", true),
	MailListInfo(14, "MINFO", true),
	MailExchange(15, "MX", false),
	Text(16, "TXT", false),
	ResponsiblePerson(17, "RP", true),
	AFSRecord(18, "AFSDB", false),
	X25(19, "X25", true),
	ISDN(20, "ISDN", true),
	RT(21, "RT", true),
	NSAP(22, "NSAP", true),
	NSAP_PTR(23, "NSAP-PTR", true),
	Signature(24, "SIG", false),
	Key(25, "KEY", false),
	PX(26, "PX", true),
	GPOS(27, "GPOS", true),
	IPV6Address(28, "AAAA", false),
	Location(29, "LOC", false),
	NXT(30, "NXT", true),
	EID(31, "EID", true),
	NB(32, "NB", true),
	SerivceLocator(33, "SRV", false),
	ATMA(34, "ATMA", true),
	NamingAuthorityPointer(35, "NAPTR", false),
	KeyExchanger(36, "KX", false),
	Certificate(37, "CERT", false),
	A6(38, "A6", true),
	DelegationName(39, "DNAME", false),
	SINK(40, "SINK", true),
	Option(41, "OPT", false),	
	AppressPrefixList(42, "APL", false),
	DelegationSigner(43, "DS", false),
	SSHPublicKeyFingerprint(44, "SSHFP", false),
	IPSecKey(45, "IPSECKEY", false),
	DNSSecuritySignature(46, "RRSIG", false),
	NextSecure(47, "NSEC", false),
	DNSKey(48, "DNSKEY", false),
	DHCPIdentifier(49, "DHCID", false),
	NextSecure3(50, "NSEC3", false),
	NextSecure3Parameters(51, "NSEC3PARAM", false),
	TLSACertificantAssociation(52, "TLSA", false),
	SMIMECertificateAssociation(53, "SMIMEA", false),
	HostIdentityProtocol(55, "HIP", false),
	ChildDS(59, "CDS", false),
	ChildDNSKey(60, "CDNSKEY", false),
	OpenPGPPublicKey(61, "OPENPGPKEY", false),
	ChildToParentSync(62, "CSYNC", false),
	MessageDigestsDNSZones(63, "ZONEMD", false),
	ServiceBinding(64, "SVCB", false),
	HTTPSBinding(65, "HTTPS", false),
	UINFO(100, "UINFO", true),
	UID(101, "UID", true),
	GID(102, "GID", true),
	UNSPEC(103, "UNSPEC", true),
	NID(104, "NID", true),
	L32(105, "L32", true),
	L64(106, "L64", true),
	LP(107, "LP", true),
	MACAddressEUI48(108, "EUI48", false),
	MACAddressEUI64(109, "EUI64", false),
	TransactionKey(249, "TKEY", false),
	TransactionSignature(250, "TSIG", false),
	IncrementalZoneTransfer(251, "IXFR", false),	
	RequestZoneTransfer(252, "AXFR", false),
	RequestMailbox(253, "MAILB", true),
	RequestMailAgent(254, "MAILA", true),
	RequestAllRecords(255, "*", false), 
	URI(256, "URI", true),
	CertificationAuthorityAuthorization(257, "CAA", false),
	DOA(259, "DOA", true);

	private int value;
	private String abbreviation;
	private boolean obsolete;
	
	private MDNSType(int value, String abbreviation, boolean obsolete) {
		this.value = value;
		this.abbreviation = abbreviation;
		this.obsolete = obsolete;
	}
	
	public int getCode() {
		return value;
	}
	
	public static MDNSType fromCode(int classValue) {
		for (MDNSType v : MDNSType.values()) {
			if (v.value == classValue) {
				return v;
			}
		}
		
		return null;
	}
}

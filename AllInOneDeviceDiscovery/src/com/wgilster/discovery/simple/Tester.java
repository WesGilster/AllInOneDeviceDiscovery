package com.wgilster.discovery.simple;

import java.io.IOException;
import java.util.Map;

import com.wgilster.discovery.Consumption;
import com.wgilster.discovery.NetworkTransport;
import com.wgilster.discovery.Property;
import com.wgilster.discovery.ProtocolListener;
import com.wgilster.discovery.Thing;
import com.wgilster.discovery.network.tcpudp.TCPNetwork;
import com.wgilster.discovery.network.tcpudp.UDPNetwork;
import com.wgilster.discovery.protocol.ipp.IPPProtocol;
import com.wgilster.discovery.protocol.mdns.MDNSProtocol;

public class Tester {
	public static class QuickPrinter implements ProtocolListener {
		private MDNSProtocol protocol;
		
		public QuickPrinter(MDNSProtocol protocol) {
			this.protocol = protocol;
		}
		
		@Override
		public void protocolEvent(NetworkTransport transport, Thing thing) {
			if (thing.getConsumption() == Consumption.Seeker) {
				/*try {
					List<Thing> things = Collections.singletonList(new PrinterThing("Wes Printer", "_ipps-3d", "_tcp"));
					protocol.writeThings(things, null, TimeUnit.MILLISECONDS, 500, 2);
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
			
			System.out.println("====");
			System.out.println(thing.getId() + " " + thing.getConsumption() + " " + thing.getType());
			for (Thing childThing : thing.getChildren()) {
				System.out.println("   " + childThing.getId() + " " + childThing.getConsumption() + " " + childThing.getType());
				for (Property property : childThing.getType().getProperties()) {
					if (childThing.getProperty(property) != null) {
						System.out.println("      " + property + "="+ childThing.getProperty(property));
					}
				}
			}
		}
	}
	
	public static class PropertyPrinter implements ProtocolListener {
		@Override
		public void protocolEvent(NetworkTransport transport, Thing thing) {
			System.out.println("====");
			System.out.println("Source:" + transport.getRoute().getSourceAddress() +  ":" + transport.getRoute().getSourcePort());
			System.out.println("Destination:" + transport.getRoute().getDestinationAddress() +  ":" + transport.getRoute().getDestinationPort());
			Map<String, Object> fields = thing.getProperties();
			for (Map.Entry<String, Object> field : fields.entrySet()) {
				System.out.println("   " + field.getKey() + " = " + field.getValue());
			}
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		MDNSProtocol mdns = new MDNSProtocol();
		mdns.bindToNetwork(new UDPNetwork(MDNSProtocol.MESSAGE_SIZE));
		mdns.addProtocolListener(new QuickPrinter(mdns));
		
		IPPProtocol ipp = new IPPProtocol();
		ipp.bindToNetwork(new TCPNetwork());
		
		//protocol.addProtocolListener(new PropertyPrinter());
		
		/*DHCPProtocol dhcp = new DHCPProtocol();
		dhcp.bindToNetwork(new UDPNetwork());
		dhcp.addProtocolListener(new SeekerPrinter());
		
		NetBios protocol = new NetBios();
		protocol.bindToNetwork(new UDPNetwork());
		
		UPNPProtocol protocol = new UPNPProtocol();
		protocol.bindToNetwork(new UDPNetwork());
		*/
		
		Thread.sleep(10000000);
	}
}

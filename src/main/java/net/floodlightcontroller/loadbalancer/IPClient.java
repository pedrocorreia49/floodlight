package net.floodlightcontroller.loadbalancer;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.TransportPort;

// data structure for storing connected
public class IPClient {
    IPv4Address ipAddress;
    IpProtocol nw_proto;
    TransportPort srcPort; // tcp/udp src port. icmp type (OFMatch convention)
    TransportPort targetPort; // tcp/udp dst port, icmp code (OFMatch convention)

    public IPClient() {
        ipAddress = IPv4Address.NONE;
        nw_proto = IpProtocol.NONE;
        srcPort = TransportPort.NONE;
        targetPort = TransportPort.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPClient ipClient = (IPClient) o;

        if (!ipAddress.equals(ipClient.ipAddress)) return false;
        if (!nw_proto.equals(ipClient.nw_proto)) return false;
        if (!srcPort.equals(ipClient.srcPort)) return false;
        return targetPort.equals(ipClient.targetPort);
    }

    @Override
    public int hashCode() {
        int result = ipAddress.hashCode();
        result = 31 * result + nw_proto.hashCode();
        result = 31 * result + srcPort.hashCode();
        result = 31 * result + targetPort.hashCode();
        return result;
    }
}

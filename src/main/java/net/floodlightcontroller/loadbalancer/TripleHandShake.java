package net.floodlightcontroller.loadbalancer;

public class TripleHandShake {
    int memberIp;
    short connectionState;
    String memberId;
    public TripleHandShake(int ipv4,String memberId,short s) {
        memberIp = ipv4;
        connectionState = s;
        this.memberId = memberId;
    }

    public short getState() {
        return connectionState;
    }

    public void setState(short s){
        connectionState=s;
    }

    public String getMemberId(){
        return memberId;
    }
}

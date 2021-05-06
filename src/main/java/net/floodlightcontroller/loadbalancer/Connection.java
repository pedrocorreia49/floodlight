package net.floodlightcontroller.loadbalancer;

import net.floodlightcontroller.routing.Path;

import java.util.ArrayList;

public class Connection {
    String memberId;
    Path routeIn;
    Path routeOut;
    long lastConnected; //timestamp of the last time certain client was connected

    public Connection(String memberId, Path routeIn, Path routeOut, long lastConnected) {
        this.memberId = memberId;
        this.routeIn = routeIn;
        this.routeOut = routeOut;
        this.lastConnected = lastConnected;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public Path getRouteIn() {
        return routeIn;
    }

    public void setRouteIn(Path routeIn) {
        this.routeIn = routeIn;
    }

    public Path getRouteOut() {
        return routeOut;
    }

    public void setRouteOut(Path routeOut) {
        this.routeOut = routeOut;
    }

    public long getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(long lastConnected) {
        this.lastConnected = lastConnected;
    }
}

#!/usr/bin/python

"""

Script to create topology, with remote controller, and hosts with internet connectivity

"""

from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.topo import Topo
from mininet.node import RemoteController, OVSSwitch
from mininet.link import TCLink

class MyTopo( Topo ):
    "Minimal topology with a single switch and two hosts"

    def build( self ):
        # Create hosts.
        h1 = self.addHost('h1', ip='10.0.0.1', defaultRoute=None)
        h2 = self.addHost('h2', ip='10.0.0.2', defaultRoute=None)
        h3 = self.addHost('h3', ip='10.0.0.3', defaultRoute=None)
        h4 = self.addHost('h4', ip='10.0.0.4', defaultRoute=None)
        h5 = self.addHost('h5', ip='10.0.0.5', defaultRoute=None)
        h6 = self.addHost('h6', ip='10.0.0.6', defaultRoute=None)
        h7 = self.addHost('h7', ip='10.0.0.7', defaultRoute=None)

        # Create switches
        s1 = self.addSwitch('s1')
        s2 = self.addSwitch('s2')
        s3 = self.addSwitch('s3')
        s4 = self.addSwitch('s4')
        s5 = self.addSwitch('s5')

        s100 = {'bw':100}
        s20 = {'bw':20}
        s300 = {'bw':300}

        # Add links between the switch and each host
        self.addLink(s1, s2, cls=TCLink)
        self.addLink(s2, s3, cls=TCLink)
        self.addLink(s3, s4, cls=TCLink)
        self.addLink(s4, s5, cls=TCLink)
        self.addLink(s5, h2, cls=TCLink)
        self.addLink(s5, h3, cls=TCLink)
        self.addLink(s1, h1, cls=TCLink)
        self.addLink(s2, h4, cls=TCLink)
        self.addLink(s4, h5, cls=TCLink)
        self.addLink(h6, s1, cls=TCLink, **s20)
        self.addLink(h7, s5, cls=TCLink, **s300)

def runMyTopo():
    "Bootstrap a Mininet network using the Minimal Topology"

    # Create an instance of our topology
    topo = MyTopo()

    # Create a network based on the topology using OVS and controlled by
    # a remote controller.
    net = Mininet(
        topo=topo,
        switch=OVSSwitch,
        controller=lambda name: RemoteController(name, ip='192.168.56.1', port=6653),
        waitConnected=True,
        ipBase='10.0.0.0/8',
        autoSetMacs=True )

    # Add NAT connectivity
    net.addNAT().configDefault()

    # Actually start the network
    net.start()

    # Drop the user in to a CLI so user can run commands.
    CLI( net )

    # After the user exits the CLI, shutdown the network.
    net.stop()

if __name__ == '__main__':
    # This runs if this file is executed directly
    setLogLevel( 'info' )
    runMyTopo()

# Allows the file to be imported using `mn --custom <filename> --topo minimal`
topos = {
    'mytopo': MyTopo
}
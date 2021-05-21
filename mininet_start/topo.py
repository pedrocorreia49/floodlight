#!/usr/bin/env python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                      controller=RemoteController,
                      ip='192.168.1.10',
                      protocol='tcp',

                      port=6653)

    info( '*** Add switches\n')
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch, dpid='0000000000000004', protocols=["OpenFlow13"])
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch, dpid='0000000000000003', protocols=["OpenFlow13"])
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch, dpid='0000000000000002', protocols=["OpenFlow13"])
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch, dpid='0000000000000001', protocols=["OpenFlow13"])
    s5 = net.addSwitch('s5', cls=OVSKernelSwitch, dpid='0000000000000005', protocols=["OpenFlow13"])

    info( '*** Add hosts\n')
    h4 = net.addHost('h4', cls=Host, ip='10.0.0.4', defaultRoute=None)
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', defaultRoute=None)
    h5 = net.addHost('h5', cls=Host, ip='10.0.0.5', defaultRoute=None)
    h6 = net.addHost('h6', cls=Host, ip='10.0.0.6', defaultRoute=None)
    h3 = net.addHost('h3', cls=Host, ip='10.0.0.3', defaultRoute=None)
    h7 = net.addHost('h7', cls=Host, ip='10.0.0.7', defaultRoute=None)
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', defaultRoute=None)

    info( '*** Add links\n')
    s2s3 = {'bw':100}
    net.addLink(s2, s3, cls=TCLink , **s2s3)
    s1s2 = {'bw':100}
    net.addLink(s1, s2, cls=TCLink , **s1s2)
    s3s4 = {'bw':100}
    net.addLink(s3, s4, cls=TCLink , **s3s4)
    s4s5 = {'bw':300}
    net.addLink(s4, s5, cls=TCLink , **s4s5)
    net.addLink(s5, h2)
    net.addLink(s5, h3)
    net.addLink(s1, h1)
    net.addLink(s2, h4)
    net.addLink(s4, h5)
    net.addLink(h7, s5)
    net.addLink(h6, s1)
    net.addNAT().configDefault()
    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s4').start([c0])
    net.get('s3').start([c0])
    net.get('s2').start([c0])
    net.get('s1').start([c0])
    net.get('s5').start([c0])

    info( '*** Post configure switches and hosts\n')

    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()


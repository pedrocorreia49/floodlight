/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.loadbalancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.python.antlr.PythonParser.else_clause_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.loadbalancer.LoadBalancer.IPClient;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.util.OFMessageUtils;

/**
 * Data structure for Load Balancer based on Quantum proposal
 * http://wiki.openstack.org/LBaaS/CoreResourceModel/proposal
 * 
 * @author KC Wang
 */

@JsonSerialize(using = LBPoolSerializer.class)
public class LBPool {
	protected static Logger log = LoggerFactory.getLogger(LBPool.class);
	protected String id;
	protected String name;
	protected String tenantId;
	protected String netId;
	protected short lbMethod;
	protected byte protocol;
	protected ArrayList<String> members;
	protected ArrayList<String> monitors;
	protected ArrayList<String> prevPicked;
	protected short adminState;
	protected short status;
	protected final static short ROUND_ROBIN = 1;
	protected final static short STATISTICS = 2;
	protected final static short WEIGHTED_RR = 3;
	protected final static short SPL = 4;

	protected String vipId;

	protected int previousMemberIndex;

	protected LBStats poolStats;
	protected ITopologyService its;

	public LBPool() {
		id = String.valueOf((int) (Math.random() * 10000));
		name = null;
		tenantId = null;
		netId = null;
		lbMethod = 0;
		protocol = 0;
		members = new ArrayList<String>();
		prevPicked = new ArrayList<String>();
		monitors = new ArrayList<String>();
		adminState = 0;
		status = 0;
		previousMemberIndex = -1;
		its = null;
		poolStats = new LBStats();
	}

	public Comparator<SwitchPort> clusterIdComparator = new Comparator<SwitchPort>() {
		@Override
		public int compare(SwitchPort d1, SwitchPort d2) {
			DatapathId d1ClusterId = its.getClusterId(d1.getNodeId());
			DatapathId d2ClusterId = its.getClusterId(d2.getNodeId());
			return d1ClusterId.compareTo(d2ClusterId);
		}
	};

	public String lbMethodToString(short lbMethod) {
		if (lbMethod == 1) {
			return "Round-Robin";
		} else if (lbMethod == 2) {
			return "Statistics";
		} else if (lbMethod == 3) {
			return "Weighted Round-Robin";
		} else if (lbMethod == 4) {
			return "SPL";
		}
		return "Invalid Method";
	}

	public void setPoolStatistics(ArrayList<Long> bytesIn, ArrayList<Long> bytesOut, int activeFlows) {
		if (!bytesIn.isEmpty() && !bytesOut.isEmpty()) {
			long sumIn = 0;
			long sumOut = 0;

			for (Long bytes : bytesIn) {
				sumIn += bytes;
			}
			poolStats.bytesIn = sumIn;

			for (Long bytes : bytesOut) {
				sumOut += bytes;
			}
			poolStats.bytesOut = sumOut;
			poolStats.activeFlows = activeFlows;
		}
	}

	public String pickMember(IDeviceService ids, ITopologyService its, IPClient client,
			HashMap<String, LBMember> totalMembers, IOFSwitch sw, OFPacketIn pi, IRoutingService routingEngineService) {
		Collection<? extends IDevice> allDevices = ids.getAllDevices();
		IDevice srcDevice = null;
		IDevice dstDevice = null;
		this.its = its;
		String picked = null;
		Integer latency = null;
		for (String s : members) {
			LBMember member = totalMembers.get(s);
			dstDevice = null;
			int auxLatency = 0;
			for (IDevice d : allDevices) {
				for (int j = 0; j < d.getIPv4Addresses().length; j++) {
					if (srcDevice == null && client.ipAddress.equals(d.getIPv4Addresses()[j]))
						srcDevice = d;
					if (dstDevice == null && member.address == d.getIPv4Addresses()[j].getInt()) {
						dstDevice = d;
						member.macString = d.getMACAddressString();
					}
					if (srcDevice != null && dstDevice != null)
						break;
				}
			}
			if (srcDevice == null || dstDevice == null)
				return null;

			DatapathId srcIsland = its.getClusterId(sw.getId());

			if (srcIsland == null) {
				log.debug("No openflow island found for source {}/{}", sw.getId().toString(), pi.getInPort());
				return null;
			}

			// Validate that we have a destination known on the same island
			// Validate that the source and destination are not on the same switchport
			boolean on_same_island = false;
			boolean on_same_if = false;
			// Switch
			for (SwitchPort dstDap : dstDevice.getAttachmentPoints()) {
				DatapathId dstSwDpid = dstDap.getNodeId();
				DatapathId dstIsland = its.getClusterId(dstSwDpid);
				if ((dstIsland != null) && dstIsland.equals(srcIsland)) {
					on_same_island = true;
					if ((sw.getId().equals(dstSwDpid)) && OFMessageUtils.getInPort(pi).equals(dstDap.getPortId())) {
						on_same_if = true;
					}
					break;
				}
			}

			if (!on_same_island) {
				// Flood since we don't know the dst device
				if (log.isTraceEnabled()) {
					log.trace("No first hop island found for destination " + "device {}, Action = flooding", dstDevice);
				}
				break;
			}

			if (on_same_if) {
				if (log.isTraceEnabled()) {
					log.trace("Both source and destination are on the same " + "switch/port {}/{}, Action = NOP",
							sw.toString(), pi.getInPort());
				}
				break;
			}

			// Destination address of client's request to set in the outbound actions

			// Install all the routes where both src and dst have attachment
			// points. Since the lists are stored in sorted order we can
			// traverse the attachment points in O(m+n) time
			SwitchPort[] srcDaps = srcDevice.getAttachmentPoints();
			Arrays.sort(srcDaps, clusterIdComparator);
			SwitchPort[] dstDaps = dstDevice.getAttachmentPoints();
			Arrays.sort(dstDaps, clusterIdComparator);

			int iSrcDaps = 0, iDstDaps = 0;

			// following Forwarding's same routing routine, retrieve both in-bound and
			// out-bound routes for
			// all clusters.
			while ((iSrcDaps < srcDaps.length) && (iDstDaps < dstDaps.length)) {
				SwitchPort srcDap = srcDaps[iSrcDaps];
				SwitchPort dstDap = dstDaps[iDstDaps];
				DatapathId srcCluster = its.getClusterId(srcDap.getNodeId());
				DatapathId dstCluster = its.getClusterId(dstDap.getNodeId());

				int srcVsDest = srcCluster.compareTo(dstCluster);
				if (srcVsDest == 0) {
					if (!srcDap.equals(dstDap) && (srcCluster != null) && (dstCluster != null)) {
						Path routeIn = routingEngineService.getPath(srcDap.getNodeId(), srcDap.getPortId(),
								dstDap.getNodeId(), dstDap.getPortId());
						Path routeOut = routingEngineService.getPath(dstDap.getNodeId(), dstDap.getPortId(),
								srcDap.getNodeId(), srcDap.getPortId());

						if (!routeIn.getPath().isEmpty()) {
							setPathCosts(routeIn, its);
							auxLatency += (int) routeIn.getLatency().getValue();
						}
						if (!routeOut.getPath().isEmpty()) {
							setPathCosts(routeOut, its);
							auxLatency += (int) routeOut.getLatency().getValue();
						}
					}
					iSrcDaps++;
					iDstDaps++;
				} else if (srcVsDest < 0) {
					iSrcDaps++;
				} else {
					iDstDaps++;
				}
			}

			if (latency == null || latency >= auxLatency) {
				latency = auxLatency;
				picked = s;
				log.info("Member {} picked with latency of {}",s,latency);
			}
		}

		return picked;
	}

	public String pickMember(IPClient client, HashMap<String, U64> membersBandwidth,
			HashMap<String, Short> membersWeight, HashMap<String, Short> memberStatus) {
		if (members.size() > 0) {
			if (lbMethod == STATISTICS && !membersBandwidth.isEmpty() && membersBandwidth.values() != null) {
				ArrayList<String> poolMembersId = new ArrayList<String>();
				// Get the members that belong to this pool and the statistics for them
				for (String memberId : membersBandwidth.keySet()) {
					for (int i = 0; i < members.size(); i++) {
						if (LoadBalancer.isMonitoringEnabled && !monitors.isEmpty() && !memberStatus.isEmpty()) { // if
																													// health
																													// monitors
																													// active
							if (members.get(i).equals(memberId) && memberStatus.get(memberId) != -1) {
								poolMembersId.add(memberId);
							}
						} else { // no health monitors active
							if (members.get(i).equals(memberId)) {
								poolMembersId.add(memberId);
							}
						}
					}
				}
				// return the member which has the minimum bandwidth usage, out of this pool
				// members
				if (!poolMembersId.isEmpty()) {
					ArrayList<U64> bandwidthValues = new ArrayList<U64>();
					ArrayList<String> membersWithMin = new ArrayList<String>();
					Collections.sort(poolMembersId);

					for (int j = 0; j < poolMembersId.size(); j++) {
						bandwidthValues.add(membersBandwidth.get(poolMembersId.get(j)));
					}
					U64 minBW = Collections.min(bandwidthValues);
					String memberToPick = poolMembersId.get(bandwidthValues.indexOf(minBW));

					for (Integer i = 0; i < bandwidthValues.size(); i++) {
						if (bandwidthValues.get(i).equals(minBW)) {
							membersWithMin.add(poolMembersId.get(i));
						}
					}

					// size of the prev list is half of the number of available members
					int sizeOfPrevPicked = bandwidthValues.size() / 2;

					// Remove previously picked members from being eligible for being picked now
					for (Iterator<String> it = membersWithMin.iterator(); it.hasNext();) {
						String memberMin = it.next();
						if (prevPicked.contains(memberMin)) {
							it.remove();
						}
					}
					// Keep the previously picked list to a size based on the members of the pool
					if (prevPicked.size() > sizeOfPrevPicked) {
						prevPicked.remove(prevPicked.size() - 1);
					}

					// If there is only one member with min BW value and membersWithMin is empty
					if (membersWithMin.isEmpty()) {
						memberToPick = prevPicked.get(prevPicked.size() - 1); // means that the min member has been
																				// prevs picked
					} else
						memberToPick = membersWithMin.get(0);

					prevPicked.add(0, memberToPick); // set the first memberId of prevPicked to be the last member
														// picked

					log.debug("Member {} picked using Statistics", memberToPick);
					return memberToPick;
				}
				return null;
			} else if (lbMethod == WEIGHTED_RR && !membersWeight.isEmpty()) {

				HashMap<String, Short> activeMembers = new HashMap<String, Short>();

				if (LoadBalancer.isMonitoringEnabled && !monitors.isEmpty() && !memberStatus.isEmpty()) { // if health
																											// monitors
																											// active
					for (String memberId : membersWeight.keySet()) {
						if (memberStatus.get(memberId) != -1) {
							activeMembers.put(memberId, membersWeight.get(memberId));
						}
					}
					return weightsToMember(activeMembers); // only members with status != -1

				} else
					return weightsToMember(membersWeight); // all members in membersWeight are considered
			} else if (lbMethod == SPL) {

			} else if (LoadBalancer.isMonitoringEnabled && !monitors.isEmpty() && !memberStatus.isEmpty()) { // if
																												// health
																												// monitors
																												// active
				for (int i = 0; i < members.size();) {
					previousMemberIndex = (previousMemberIndex + 1) % members.size();
					if (memberStatus.get(members.get(previousMemberIndex)) != -1)
						return members.get((previousMemberIndex));
				}
				return null;
			} else {
				// simple round robin
				previousMemberIndex = (previousMemberIndex + 1) % members.size();
				return members.get(previousMemberIndex);
			}
		}

		return null;
	}

	/**
	 * helper function to pick a member
	 * 
	 * @param weights - hashmap with memberId and weight of the member
	 * @return member picked by Weighted Round Robin
	 */
	private String weightsToMember(HashMap<String, Short> weights) {
		Random randomNumb = new Random();
		short totalWeight = 0;

		for (Short weight : weights.values()) {
			totalWeight += weight;
		}

		int rand = randomNumb.nextInt(totalWeight);
		short val = 0;
		for (String memberId : weights.keySet()) {
			val += weights.get(memberId);
			if (val > rand) {
				log.debug("Member {} picked using WRR", memberId);
				return memberId;
			}
		}
		return null;
	}

	public void setPathCosts(Path p, ITopologyService its) {
		U64 cost = U64.ZERO;

		Map<DatapathId, Set<Link>> links = its.getAllLinks();
		for (int i = 0; i < p.getPath().size() - 1; i++) {
			DatapathId src = p.getPath().get(i).getNodeId();
			DatapathId dst = p.getPath().get(i + 1).getNodeId();
			OFPort srcPort = p.getPath().get(i).getPortId();
			OFPort dstPort = p.getPath().get(i + 1).getPortId();
			for (Link l : links.get(src)) {
				if (l.getSrc().equals(src) && l.getDst().equals(dst) && l.getSrcPort().equals(srcPort)
						&& l.getDstPort().equals(dstPort)) {
					// log.info("Matching link found: {}", l);
					cost = cost.add(l.getLatency());
				}
			}
		}
		p.setLatency(cost);
		// log.info("LOAD BALANCING Total cost is {}", p.getLatency());
		// log.info(p.toString());
	}
}

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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;

/**
 * Data structure for Load Balancer based on
 * Quantum proposal http://wiki.openstack.org/LBaaS/CoreResourceModel/proposal 
 * 
 * @author KC Wang
 */

@JsonSerialize(using=LBMemberSerializer.class)
public class LBMember {
    protected String id;
    protected int address;
    protected short port;
    protected String macString;
    protected LBStats memberStats;
    protected int connectionLimit;
    protected short adminState;
    protected short status;
    protected String poolId;
    protected String memberId;
    protected String vipId;
    protected short weight;
    
    public LBMember() {
        id = String.valueOf((int) (Math.random()*10000));
        address = -1;
        macString = null;
        port = -1;
        memberStats =  new LBStats();
        connectionLimit = -1;
        adminState = -1;
        status = -1;
        poolId=null;
        memberId = null;
        vipId = null;
        weight = -1;
    }

    public void setMemberStatistics(ArrayList<Long> bytesIn, ArrayList<Long> bytesOut, int activeFlows) {
		if (!bytesIn.isEmpty() && !bytesOut.isEmpty()) {
			long sumIn = 0;
			long sumOut = 0;

			for (Long bytes : bytesIn) {
				sumIn += bytes;
			}
            memberStats.bytesInDiff = sumIn - memberStats.bytesIn;
			memberStats.bytesIn = sumIn;

			for (Long bytes : bytesOut) {
				sumOut += bytes;
			}
            memberStats.bytesOutDiff = sumOut-memberStats.bytesOut;
			memberStats.bytesOut = sumOut;
			memberStats.activeFlows = activeFlows;
		}
	}

}

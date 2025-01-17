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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import net.floodlightcontroller.packet.IPv4;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembersResource extends ServerResource {

	protected static Logger log = LoggerFactory.getLogger(MembersResource.class);
	private static final int BAD_REQUEST = 400;

	@Get("json")
	public Collection <LBMember> retrieve() {
		ILoadBalancerService lbs =
				(ILoadBalancerService)getContext().getAttributes().
				get(ILoadBalancerService.class.getCanonicalName());

		String memberId = (String) getRequestAttributes().get("member");
		if (memberId!=null)
			return lbs.listMember(memberId);
		else        
			return lbs.listMembers();               
	}

	@Put
	public LBMember updateMember(String postData){
		String memberId = (String) getRequestAttributes().get("member");
		if(memberId!=null){
			ILoadBalancerService lbs =
				(ILoadBalancerService)getContext().getAttributes().
				get(ILoadBalancerService.class.getCanonicalName());
				
			if(!lbs.listMember(memberId).isEmpty()){
				Iterator<LBMember> it = lbs.listMember(memberId).iterator();  
				LBMember lb = it.next();
				try {
					lb = jsonToMember(postData, lb);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				lbs.updateMember(lb);
				return lb;
			}else{
				return null;
			}
		}
		return null;
	}
	@Post
	public LBMember createMember(String postData) {        

		LBMember member= new LBMember();
		try {
			member=jsonToMember(postData,member);
		} catch (IOException e) {
			log.error("Could not parse JSON {}", e.getMessage());
			throw new ResourceException(BAD_REQUEST); // Sends HTTP error message with code 400 (Bad Request).
		}

		ILoadBalancerService lbs =
				(ILoadBalancerService)getContext().getAttributes().
				get(ILoadBalancerService.class.getCanonicalName());

		String memberId = (String) getRequestAttributes().get("member");
		if (memberId != null)
			return lbs.updateMember(member);
		else        
			return lbs.createMember(member);
	}

	@Delete
	public String removeMember() {

		String memberId = (String) getRequestAttributes().get("member");

		ILoadBalancerService lbs =
				(ILoadBalancerService)getContext().getAttributes().
				get(ILoadBalancerService.class.getCanonicalName());

		int status = lbs.removeMember(memberId);
		if(status == -1){
			return "{\"status\" : \"Error: Member cannot be deleted!\"}";
		} else{
			return "{\"status\" : \"200 OK!\"}";
		}
	}

	protected LBMember jsonToMember(String json,LBMember member) throws IOException {
		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		

		try {
			jp = f.createParser(json);
		} catch (JsonParseException e) {
			throw new IOException(e);
		}

		jp.nextToken();
		if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected START_OBJECT");
		}

		while (jp.nextToken() != JsonToken.END_OBJECT) {
			if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
				throw new IOException("Expected FIELD_NAME");
			}

			String n = jp.getCurrentName();
			jp.nextToken();
			if (jp.getText().equals("")) 
				continue;
			if (n.equals("id")) {
				member.id = jp.getText();
				continue;
			} else
				if (n.equals("address")) {
					member.address = IPv4.toIPv4Address(jp.getText());
					continue;
				} else
					if (n.equals("port")) {
						member.port = Short.parseShort(jp.getText());
						continue;
					} else
						if (n.equals("connection_limit")) {
							member.connectionLimit = Integer.parseInt(jp.getText());
							continue;
						} else
							if (n.equals("admin_state")) {
								member.adminState = Short.parseShort(jp.getText());
								continue;
							} else
								if (n.equals("status")) {
									member.status = Short.parseShort(jp.getText());
									continue;
								} else
									if (n.equals("pool_id")) {
										member.poolId = jp.getText();
										continue;
									} else
										if (n.equals("throughput")) {
											member.throughput = Double.parseDouble(jp.getText());
											continue;
										}
			if(n.equals("weight")){
				member.weight = Short.parseShort(jp.getText());
				continue;
			}

			log.warn("Unrecognized field {} in " +
					"parsing Members", 
					jp.getText());
		}
		jp.close();

		return member;
	}
}

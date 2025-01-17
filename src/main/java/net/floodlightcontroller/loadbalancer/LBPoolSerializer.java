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
import org.projectfloodlight.openflow.types.IpProtocol;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LBPoolSerializer extends JsonSerializer<LBPool>{

    @Override
    public void serialize(LBPool pool, JsonGenerator jGen,
                          SerializerProvider serializer) throws IOException,
                                                  JsonProcessingException {
        jGen.writeStartObject();
        
        jGen.writeStringField("id", pool.id);
        jGen.writeStringField("name", pool.name);
        jGen.writeStringField("vipId", pool.vipId);
        jGen.writeStringField("lbMethod", pool.lbMethodToString(pool.lbMethod));
        jGen.writeStringField("protocol", IpProtocol.of(pool.protocol).toString());
        jGen.writeStringField("Timeout", Integer.toString(pool.timeout));                                       
        //for (int i=0; i<pool.members.size(); i++)
            jGen.writeStringField("poolMembers", String.valueOf(pool.members.size()));

        jGen.writeEndObject();
    }

}

#!/bin/sh
 
curl -X POST -d '{"id":"1","name":"vip1","protocol":"tcp","address":"10.0.0.100","port":"80"}' http://localhost:8080/quantum/v1.0/vips/
curl -X POST -d '{"id":"1","name":"pool1","lb_method":"LLM","protocol":"tcp","vip_id":"1","timeout":30}' http://localhost:8080/quantum/v1.0/pools/
curl -X POST -d '{"id":"1","address":"10.0.0.2","port":"80","pool_id":"1"}' http://localhost:8080/quantum/v1.0/members/
curl -X POST -d '{"id":"2","address":"10.0.0.5","port":"80","pool_id":"1"}' http://localhost:8080/quantum/v1.0/members/
curl -X POST -d '{"id":"3","address":"10.0.0.3","port":"80","pool_id":"1"}' http://localhost:8080/quantum/v1.0/members/

curl -X POST -d '{"id":"2","name":"vip2","protocol":"tcp","address":"10.0.0.101","port":"80"}' http://localhost:8080/quantum/v1.0/vips/
curl -X POST -d '{"id":"2","name":"pool2","lb_method":"LTP","protocol":"tcp","vip_id":"2","timeout":30}' http://localhost:8080/quantum/v1.0/pools/
curl -X POST -d '{"id":"4","address":"10.0.0.4","port":"80","pool_id":"2"}' http://localhost:8080/quantum/v1.0/members/
curl -X POST -d '{"id":"5","address":"10.0.0.9","port":"80","pool_id":"2"}' http://localhost:8080/quantum/v1.0/members/
curl -X POST -d '{"id":"6","address":"10.0.0.10","port":"80","pool_id":"2"}' http://localhost:8080/quantum/v1.0/members/

# curl -X POST -d '{"id":"3","name":"vip3","protocol":"tcp","address":"10.0.0.102","port":"300"}' http://localhost:8080/quantum/v1.0/vips/
# curl -X POST -d '{"id":"3","name":"pool3","lb_method":"SLP","protocol":"tcp","vip_id":"3","timeout":40}' http://localhost:8080/quantum/v1.0/pools/
# curl -X POST -d '{"id":"7","address":"10.0.0.7","port":"80","pool_id":"3"}' http://localhost:8080/quantum/v1.0/members/
# curl -X POST -d '{"id":"8","address":"10.0.0.8","port":"80","pool_id":"3"}' http://localhost:8080/quantum/v1.0/members/
# curl -X POST -d '{"id":"9","address":"10.0.0.9","port":"80","pool_id":"3"}' http://localhost:8080/quantum/v1.0/members/

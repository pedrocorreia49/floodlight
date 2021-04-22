#!/bin/sh
 
curl -X POST -d '{"id":"1","name":"vip1","protocol":"icmp","address":"10.0.0.100","port":"8"}' http://localhost:8080/quantum/v1.0/vips/
curl -X POST -d '{"id":"1","name":"pool1","lb_method":"SPL","protocol":"icmp","vip_id":"1"}' http://localhost:8080/quantum/v1.0/pools/
curl -X POST -d '{"id":"1","address":"10.0.0.1","port":"8","pool_id":"1"}' http://localhost:8080/quantum/v1.0/members/
curl -X POST -d '{"id":"2","address":"10.0.0.3","port":"8","pool_id":"1"}' http://localhost:8080/quantum/v1.0/members/

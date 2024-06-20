This Load Balancer project has the following 2 options:
1. Round robin load balancer.
   Given seven servers, named a-g, it will output the name of the next server in the round robin sequence.
2. Least connected load balancer.
   Each server is acquired for a random period of time between 1 and 10 seconds, before being released. Each server may accept multiple simultaneous incoming
connections but the load balancer should always serve on request the least connected server from the pool.

# Loadbalancer application

#### This project was coded in the context of SwissRe technical assignment & aims at defining some basic software components to describe the functionality of a load balancer.

## Load balancing

The main class `LoadBalancer.java` is responsible for invoking the nodes' cluster & receiving the response from one of them depending on the load balancing algorithm. The load balancing algorithm is injected upon creation of this class using the strategy pattern (1 interface implemented byt 2 different concrete strategies, 1 for random & 1 for round robin). This is a singleton component, making sure that it is only created once throughout our application.

## Heart-beat check

The main class `LoadBalancer.java` is also responsible for checking the health of the nodes' cluster. Again this behavior is injected upon creation using 2 different strategies, 1 for simple check & 1 for advanced. In this case, `AdvancedHeartBeatChecker.java` is also a decorator around the `SimpleHeartBeatChecker.java` component. The advanced component encapsulates the simple component's behavior adding some extra functionality of its own. The main component, upon creation, registers a scheduled task in order to perform the heart-beat check periodically.

## Nodes' Cluster

The cluster of provider instance nodes is managed by the `ProviderClusterService.java` component. This is responsible for managing nodes inside the cluster in an efficient & thread-safe way. The main `LoadBalancer.java` component will make all its requests to the cluster through this service which acts like a proxy. This is also a sinlgleton component.
 
 ## Rate limiter
 //todo...
 
## Instantiating the load balancer app

This is an example of how one can instantiate the load balancing & nodes' cluster components

```

        // New load-balancer with random algorithm and simple heart-beat check
        LoadBalancer balancer = LoadBalancer.getInstance(new RandomImplementation(), new SimpleHeartBeatChecker());

        // New cluster with 10 provider nodes
        ProviderClusterService.getInstance(10);

```


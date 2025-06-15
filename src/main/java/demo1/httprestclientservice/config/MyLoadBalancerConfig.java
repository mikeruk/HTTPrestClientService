package demo1.httprestclientservice.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClient(name = "backend-service", configuration = MyLoadBalancerConfig.class)
public class MyLoadBalancerConfig
{
    // the bean above goes here

    @Bean
    public ServiceInstanceListSupplier zonePreferenceServiceInstanceListSupplier(
            ConfigurableApplicationContext context
    ) {
        // this builder will:
        //  1. ask your DiscoveryClient for instances,
        //  2. extract the "zone" (from eureka.instance.metadata-map.zone or spring.cloud.loadbalancer.zone),
        //  3. filter to only same-zone instances (falling back to all if none match)
        return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withZonePreference()
                .build(context);
    }
}


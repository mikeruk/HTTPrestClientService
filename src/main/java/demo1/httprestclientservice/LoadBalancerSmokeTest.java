package demo1.httprestclientservice;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoadBalancerSmokeTest {
    private final HttpClientInterface client;

    public LoadBalancerSmokeTest(HttpClientInterface client) {
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnce() {
        System.out.println("→ Performing LB call at startup…");
        Map<String,String> body = client.ping();
        System.out.println("→ Got: " + body.get("message"));
    }
}



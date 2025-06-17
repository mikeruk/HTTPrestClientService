


In the process of learning about REST Clients, this docs were used:
         https://docs.spring.io/spring-framework/reference/integration/rest-clients.html


So far we have experimented a lot with OpenFeign client. What is its alternative?


Below are a few common alternatives to Spring Cloud OpenFeign, along with brief descriptions of their strengths and typical use cases.

1. Spring WebClient
   Spring’s reactive, non-blocking HTTP client—part of Spring WebFlux—can be used in place of Feign for both synchronous and asynchronous calls.

Key Points:

Supports reactive streams (Mono/Flux) and non-blocking I/O when running on a Reactor-backed server like Netty, but can also be used in a blocking manner if desired.

Provides a fluent, builder-style API (you construct requests via WebClient.builder()), rather than interface-based annotations like @FeignClient.

Ideal if you want to leverage reactive back-pressure or compose multiple asynchronous calls into a pipeline.

When to Use:

You need full control over headers, filters, or connection settings at runtime.

You’re already using Spring WebFlux (or plan to migrate to it) and want to stay entirely non-blocking all the way through.

You need more flexibility than Feign’s declarative interfaces—for example, dynamic URLs or streaming responses.

Citations:

“WebClient (RestTemplate — deprecated to support WebClient)… Supports reactive call [and] underlying HTTP client libraries such as Reactor Netty… Comes in 2 flavour[s] — Annotation and functional way.”
Stack Overflow
Medium

“In the Spring ecosystem, three popular options are RestTemplate, WebClient, and FeignClient… WebClient is non-blocking, supports both sync and async programming models.”
DEV Community

2. Spring HTTP Interface (HttpInterface)
   Introduced in Spring Framework 6 as a first-class, declarative HTTP-client alternative to Feign, based on Java interfaces and the new HttpClient.

Key Points:

You define a Java interface annotated with @HttpExchange (or other HTTP-method annotations), and Spring generates a client proxy at runtime—very similar in feel to @FeignClient, but without pulling in the entire Spring Cloud OpenFeign stack.

Built on top of the JDK 11+ java.net.http.HttpClient by default (though you can switch to Reactor Netty or Apache HttpClient under the hood).

Allows you to inject the interface directly via Spring’s @Bean registration, and Spring Boot auto-configures the HttpInterfaceClient behind it.

When to Use:

You want a minimal, first-party solution for declarative HTTP calls without depending on Spring Cloud or third-party Feign.

You’re on Spring 6 / Spring Boot 3+ and can take advantage of the modern HttpClient stack.

You need finer control over the request encoding/decoding (e.g. reactive codecs, custom Jackson configurations) while still using interface proxies.

Citations:

“Spring has now its own declarative REST client – HttpExchange. Use HttpExchange with RestClient (Spring MVC) or WebClient (Spring WebFlux).”
Reddit

“Feign Client is a great option for APIs as it usually requires less boilerplate… Spring has an official library supporting Open Feign, it is called Spring Cloud OpenFeign. When to use Open Feign and when not? … Spring Interface Clients instead of Open Feign.”
GitHub
Reddit

3. RestTemplate (Legacy / Blocking)
   Although officially in maintenance mode (no new features planned), RestTemplate remains a perfectly valid blocking HTTP client for many Spring MVC applications.

Key Points:

Uses a straightforward, imperative API (getForObject(), postForEntity(), etc.).

Integrates well with Spring MVC applications that don’t require reactive behavior.

Supported until Spring 6 (but actively marked “feature-complete” rather than “deprecated”); Spring recommends migrating to WebClient or HttpInterface for new development.

When to Use:

You have an existing codebase that already heavily relies on RestTemplate, and there’s no immediate need to switch.

Your application is purely servlet-based (Spring MVC) and you don’t expect high concurrency or streaming requirements.

Citations:

“Feign client should be used when Spring Cloud Gateway needs to communicate to other microservices, whereas WebClient/RestTemplate should be used for back-to-back communication… RestTemplate is now deprecated (feature-complete) to support WebClient.”
Stack Overflow
Medium

“RestTemplate is a synchronous HTTP client that has been the standard in Spring for a long time. It’s simple to use and integrates well with Spring Boot.”
DEV Community

4. Third-Party Clients (Retrofit, OkHttp, Apache HttpClient, etc.)
   If you’re not strictly tied to a Spring-first solution, you might consider one of these libraries:

Retrofit (Square):

Very similar to Feign in that you define a Java interface with annotations (e.g. @GET, @POST), and Retrofit generates a client implementation.

Uses OkHttp under the hood; has excellent support for JSON (via converters) and easy integration with Kotlin coroutines.

OkHttp / Apache HttpClient (Low-Level):

Provides full control over connection pooling, timeouts, interceptors, and other HTTP settings.

Often used as a base library behind higher-level wrappers (e.g. WebClient can be configured to use Reactor Netty instead of OkHttp, but you can also wire in OkHttp directly).

When to Use:

You need a highly optimized, low-level HTTP pipeline for fine-grained control over threading, SSL certificates, or custom connection pooling.

You’re writing a client that will run outside of a Spring context or in a non-Java/Kotlin ecosystem (e.g. Android), where Retrofit or OkHttp are already the de facto standards.

Citations:

“What I personally use if possible is… RestClient that uses a low level client… Generate HttpInterface interface and model classes using the openapi generator… Create the final client using Spring’s infra by wiring them all together.”
Reddit

(Implied by the widespread use of Retrofit/OkHttp as third-party HTTP clients in modern microservice architectures.)


| Feature/Aspect              | OpenFeign (`@FeignClient`)                                                                | WebClient (`WebClient.builder()`)                   | HttpInterface (`@HttpExchange`)                                      | RestTemplate (`RestTemplate`)                         | Retrofit / OkHttp / Apache HttpClient                           |
| --------------------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------------- | -------------------------------------------------------------------- | ----------------------------------------------------- | --------------------------------------------------------------- |
| **Programming Style**       | Declarative interface (annotations)                                                       | Fluent, builder-style API                           | Declarative interface (annotations)                                  | Imperative, method calls                              | Declarative (Retrofit) or imperative (OkHttp)                   |
| **I/O Model**               | Blocking (per servlet thread)                                                             | Non-blocking/reactive or blocking                   | Blocking by default (over `HttpClient` or Reactor Netty)             | Blocking (per servlet thread)                         | Blocking (unless combined with async wrappers)                  |
| **Reactive Support**        | None built-in (would need 3rd-party)                                                      | First-class reactive (Mono/Flux)                    | Can be plugged into Reactor Netty (but no built-in reactive streams) | None (blocking only)                                  | Only via OkHttp’s async callbacks or custom integration         |
| **Spring Version**          | Requires Spring Cloud OpenFeign                                                           | Part of Spring WebFlux (Spring 5+)                  | Part of Spring 6 / Spring Boot 3+                                    | Core Spring (Spring 2.x / 3.x / 4.x / 5.x)            | Independent of Spring (can be used anywhere)                    |
| **Annotation Dependencies** | `spring-cloud-starter-openfeign`                                                          | `spring-boot-starter-webflux`                       | `spring-boot-starter-http-interface`                                 | `spring-boot-starter-web`                             | Varies by library (e.g. `retrofit2`, `okhttp3`)                 |
| **Ideal For…**              | Microservice-to-microservice calls with minimal boilerplate and Eureka/Ribbon integration | Reactive services, high-concurrency, streaming APIs | Lightweight, first-party declarative HTTP clients; modern Spring     | Existing Spring MVC apps that don’t need reactiveness | Non-Spring applications or when you need very low-level control |

Which One Should You Choose?
If you want a drop-in replacement for Feign’s declarative style and you’re on Spring 6 / Boot 3+: try HttpInterface (@HttpExchange).

If you need reactive, non-blocking I/O and you’re building on Spring WebFlux: use WebClient.

If you have a legacy Spring MVC application and want the simplest path with minimal changes: stick with RestTemplate, but plan to migrate eventually.

If you need fine-grained, low-level HTTP control or want to use a non-Spring ecosystem: look at OkHttp, Apache HttpClient, or Retrofit.

All of these options can be easily wired into a Spring Boot application; you just choose the dependency that best fits your concurrency model and programming style.




Below is a general comparison of Spring’s built-in HTTP clients (RestClient, WebClient, RestTemplate, and HTTP Interface) versus Spring Cloud OpenFeign. For each, I’ll highlight key differences, typical use cases, and whether—and why—you might choose one over Feign.

1. Programming Model: Declarative vs. Programmatic
   OpenFeign

Declarative, interface-driven: You create a Java interface annotated with @FeignClient and method-level annotations (@GetMapping, @PostMapping, etc.). Feign generates a runtime proxy that implements the interface and does the HTTP calls behind the scenes.

Opinionated decoding/encoding: Feign integrates tightly with Spring’s Encoder/Decoder abstractions (Jackson by default), and with Ribbon/Eureka (if you include those dependencies) for client-side load balancing and service discovery.

Boilerplate reduction: Because you just define an interface, there is almost no imperative “request building” code.

Dependency: Requires Spring Cloud OpenFeign on the classpath (and often Spring Cloud dependencies like spring-cloud-netflix for service discovery).

RestTemplate / RestClient / WebClient / HTTP Interface

Programmatic (fluent) or interface-driven (HTTP Interface):

RestTemplate: Imperative “template” style (restTemplate.getForObject(…), postForEntity(…)). You write code to assemble URIs, headers, bodies, etc.

RestClient (Spring 6+): Also imperative/fluent, but with a builder-style API (RestClient.builder().baseUrl(…).build().get()…) instead of the older “template” calls.

WebClient: Fluent, reactive style. You chain methods (WebClient.create(baseUrl).get().uri(…).retrieve().bodyToMono(…)).

HTTP Interface: Declarative like Feign—you define an interface annotated with @HttpExchange (or method-level mappings), and Spring generates a proxy. But it lives in Spring Framework 6 (no Spring Cloud).

Less “magic” than Feign: With RestTemplate/RestClient/WebClient, you are explicitly invoking methods, supplying URLs or URIs, and getting back responses. That extra control can be beneficial when you need fine-grained handling of headers, timeouts, reactive pipelines, etc.

→ What this means

If you want zero boilerplate and don’t mind adding a Spring Cloud dependency, Feign’s interface style is convenient.

If you prefer to see exactly how the request is built (or need dynamic URIs based on runtime data), a programmatic approach (RestTemplate/RestClient/WebClient) can be easier to debug and customize.

2. Blocking vs. Non-Blocking (Reactive) I/O

   OpenFeign

Blocking by default. Every Feign client call happens on the calling thread (which, in a servlet environment, is a servlet thread). There is no built-in support for reactive back-pressure or non-blocking I/O.

If you want to incorporate reactive streams, you’d have to wrap Feign calls manually in a Mono.fromCallable() (and you still block internally), or use a third-party Feign adapter for Reactor (not common).

RestTemplate

Blocking. Each call (e.g. getForObject()) blocks until the response arrives. Suitable for traditional Spring MVC apps.

RestClient (Spring 6)

Blocking by default (it wraps JDK 11’s java.net.http.HttpClient in a synchronous manner). There is no reactor/Flux/Mono support at the RestClient API level—under the hood it can use a non-blocking client, but your code remains imperative.
Because RestClient is simply a higher-level abstraction over reactive I/O, it supports both use cases - blocking and non-blocking. Whether it behaves “synchronously” or “asynchronously” is determined by your method signatures.

WebClient

Non-blocking/reactive. Uses Reactor Netty (or another reactive HTTP engine) under the hood. You build a Mono<T> or Flux<T> pipeline, which means threads are not tied up during I/O.
Preferred when: you have a reactive application (Spring WebFlux) or you need extreme scalability (polling many services concurrently) or “streaming” semantics (e.g. SSE, chunked responses).

HTTP Interface

Blocking by default, because it delegates to RestTemplate or JDK HttpClient (configured as blocking). 
Spring 6 does not yet provide a Reactor-based HttpInterface implementation out of the box—although you can manually wire a Reactor Netty ClientHttpConnector if you want a reactive path, it isn’t the “default.”
Conclusion: The HTTP Interface is not intrinsically “only blocking.” It supports both modes. You decide by choosing a synchronous return type (blocks) or a Mono/Flux return type (non-blocking).

→ What this means
If you have a traditional, servlet-based application and you’re fine with blocking I/O, Feign, RestTemplate, RestClient, or HTTP Interface all “feel” fine (they’ll each tie up a thread while waiting).
If you want to embrace reactive end-to-end (e.g. WebFlux controllers + non-blocking HTTP client), then WebClient is strongly preferred over Feign (because Feign cannot participate in a Reactor chain natively).

3. Dependency Footprint & Autoconfiguration
   OpenFeign

Requires adding spring-cloud-starter-openfeign (which pulls in a chunk of Spring Cloud). If you are already using Spring Cloud Netflix (Eureka, Ribbon), Feign integrates seamlessly with those (service discovery, load balancing).

Feign auto-configures @FeignClient proxies when you annotate your main application class with @EnableFeignClients.

RestTemplate

Built into Spring Boot’s Web starter (spring-boot-starter-web). No additional “starters” needed.

Deprecated as of Spring 5 (in favor of WebClient or RestClient) but still available.

RestClient

Introduced in Spring Framework 6 / Spring Boot 3. If you depend on spring-boot-starter-web (or even just spring-web), you get RestClient (no separate starter).
Very lightweight—just a builder around JDK’s HttpClient.
It is imperative first (blocking first) client in comparison to WebClient which is non-blocking first! This small difference is important, because it causes limitations 
in the RestClient, e.g. cannot directly apply RETRY logic.

WebClient

Part of Spring WebFlux (spring-boot-starter-webflux). If you’re already using WebFlux, you have it. If you’re only on Spring MVC, you can still pull in spring-boot-starter-webflux for the client, though the application remains blocking on the server side.

HTTP Interface

Comes with Spring 6 (no need for Spring Cloud). If you’re on Spring Boot 3 (which includes Spring 6), you just add the spring-http-interface-client dependency (often transitively included).

Very lightweight—just the annotation processor and proxies. No Ribbon/Eureka by default (you’d need to wire in @LoadBalancerClient and such if you want load balancing).

→ What this means

If you want zero extra dependencies beyond Spring Boot Core, use RestTemplate (for MVC) or RestClient/HTTP Interface (for Spring 6).

If you already have Spring Cloud on the classpath and want built-in Ribbon/Eureka integration (or a declarative, pluggable ecosystem), Feign might be the quickest path.

If you want a first-party, future-proof interface-based client, HTTP Interface is preferred to Feign (because it doesn’t require Spring Cloud and will evolve with Spring Framework releases).

4. Ease of Use & Boilerplate
   OpenFeign

Pros: If you have many microservices and you prefer a uniform interface-based approach (@FeignClient(name = "inventory-service") interface InventoryClient { @GetMapping("/items") List<Item> getAll(); }), Feign eliminates almost all boilerplate.

Cons: Because it’s more “magical,” debugging is sometimes less straightforward. You have to understand Feign’s encoder/decoder, client‐side load balancer, and how fallback/Hystrix (if used) ties in.

RestTemplate / RestClient

Pros: Very explicit. You call restClient.get().uri("/items").retrieve().bodyToMono(...) or restTemplate.getForObject("/items", Item[].class). You see exactly what’s happening, and you can customize every bit (headers, interceptors, timeouts).

Cons: More “boilerplate” per call. If you have ten endpoints, you end up writing ten methods that build the request manually.

WebClient

Pros: If you need reactive streams, WebClient’s fluent API is easy to compose (e.g. webClient.get().uri("/items").retrieve().bodyToFlux(Item.class).filter(…)).

Cons: Steeper learning curve if you aren’t already familiar with Reactor. For simple, one‐off calls, you might over-engineer.

HTTP Interface

Pros: Almost identical developer experience to Feign (interface + annotations), but it’s part of Spring Framework itself. You don’t need to pull in Spring Cloud.

Cons: As of Spring 6, it’s fairly new—some edge-case features (custom interceptors, pluggable load balancer hints) may not be as mature as Feign + Ribbon/Eureka combination.

→ What this means

If your project values minimal boilerplate and you like @FeignClient style, Feign is still a solid choice—especially if you already use Spring Cloud.

If you prefer fully explicit request construction and fewer hidden abstractions, RestClient/WebClient/RestTemplate give you that transparency.

5. Integration with Service Discovery / Load Balancing
   OpenFeign

Built-in support (via Spring Cloud LoadBalancer or Netflix Ribbon, depending on your version). You simply specify @FeignClient(name="users") and Feign will pick a healthy instance of users from Eureka/Consul / Zookeeper.

You can also configure retries, interceptors, and Hystrix (circuit breaker) at the Feign level.

RestTemplate

You can wire in a @LoadBalanced RestTemplate bean (Spring Cloud LoadBalancer) so that logical service names are resolved at runtime. But you lose the interface-based simplicity.

If you don’t configure a load balancer, RestTemplate will treat whatever URL you supply as fixed—no discovery.

RestClient / WebClient

Both can be used with Spring Cloud LoadBalancer by registering a ReactorLoadBalancerExchangeFilterFunction (for WebClient) or customizing the underlying HttpClient for RestClient.
Slightly more setup than Feign, but more flexible: you can dynamically adjust URLs, add filter functions, etc.
RestClient is imperative first (blocking first) client in comparison to WebClient which is non-blocking first! This small difference is important, because it causes limitations
in the RestClient, e.g. cannot directly apply RETRY logic.

HTTP Interface

Can participate in Spring Cloud LoadBalancer if you annotate with @LoadBalancerClient (so a service name in the @HttpExchange URI is resolved).

Doesn’t have built-in Ribbon fallback support out of the box, but Spring LoadBalancer (the newer project) can slot in easily.

→ What this means

If automatic discovery + load balancing is a requirement, Feign provides the least friction (just annotate, configure the registry).

All of the other clients can be made to work with discovery, but they require a bit more boilerplate (register a bean, add a filter/interceptor). If you only call a handful of services, that extra setup may be negligible.

6. Error Handling & Resilience Patterns
   OpenFeign

You can plug in IErrorDecoder implementations to map 4xx/5xx responses to exceptions.

Combined with Spring Cloud Circuit Breaker / Resilience4j / Hystrix (depending on version), you can declare fallbacks at the interface level.

Feign also supports request interceptors (for auth headers, tracing).

RestTemplate / RestClient

Both throw a RestClientException (e.g. HttpClientErrorException) on 4xx/5xx; you can register a ResponseErrorHandler to customize behavior.

For circuit breaking, you typically use Resilience4j or Spring Retry wrappers around the call (e.g. @CircuitBreaker on your service method that calls restTemplate).

You write exception handling logic imperatively (or annotate your service methods), rather than associating it directly with the HTTP interface.

WebClient

You get a reactive onStatus(...) operator to inspect status codes and decide how to map them (e.g. onStatus(HttpStatus::is4xxClientError, resp -> Mono.error(new MyClientException(...)))).

Integrates well with Resilience4j Reactor modules for circuit breakers, retry, bulkhead, etc.

HTTP Interface

Error handling is either default (throws HttpClientErrorException) or you can register a custom HttpInterfaceClientCustomizer/ErrorDecoder (Spring 6 is still evolving this API).

For resilience, you again wrap the generated interface proxy in a Resilience4j or Spring Retry annotation.

→ What this means

If you want declarative fallbacks (e.g., “if this endpoint fails, call this method instead”), Feign’s built-in support can be easier to configure.

With the other clients, you tend to centralize resilience logic in service classes (e.g., annotate the service method with @CircuitBreaker) rather than “on the HTTP client” directly.

7. Summary:         When Is Each Preferred Over Feign?
   Client       	When It’s Preferred over Feign

RestClient	
• You’re on Spring 6+ and want a minimal, blocking, fluent client with no Spring Cloud dependencies.
• You don’t need a declarative interface.
• It can also work as great non-blocking client.
• RestClient is imperative first (blocking first) client in comparison to WebClient which is non-blocking first! This small difference is important, because it causes limitations
in the RestClient, e.g. cannot directly apply RETRY logic.

   
WebClient	
• You need reactive, non-blocking calls.
• You’re using Spring WebFlux end-to-end, and you want to leverage back-pressure.

RestTemplate	
• You have a legacy Spring MVC codebase and don’t want to introduce Spring Cloud just for Feign.
• You’re comfortable writing imperative calls.

HTTP Interface
• You like Feign’s “interface + annotation” style but don’t want to pull in Spring Cloud/OpenFeign.
• You’re on Spring 6/Spring Boot 3 and want a first-party solution that evolves with Spring.
• It can also work as great non-blocking client.

Quick Summary Table
| Client Type                          | Blocking by Design?               | Built-in Reactive API?                       | Notes                                                                 |
| ------------------------------------ | --------------------------------- | -------------------------------------------- | --------------------------------------------------------------------- |
| **RestTemplate**                     | Yes (always blocks)               | No                                           | Must switch to WebClient or HTTP Interface for non-blocking behavior. |
| **WebClient**                        | No (reactive/non-blocking)        | Yes (Mono/Flux everywhere)                   | You only block if you explicitly call `.block()`.                     |
| **OpenFeign (Spring)**               | Yes (synchronous)                 | No (reactive requires extra libs)            | Standard Spring Cloud Feign is blocking.                              |
| **HTTP Interface**                   | Depends on return type:           |                                              |                                                                       |
| • `T`/`ResponseEntity<T>` → blocks   |                                   |                                              |                                                                       |
| • `Mono<T>`/`Flux<T>` → non-blocking | Yes (if you return `Mono`/`Flux`) | Under the hood is WebClient / Reactor Netty. |                                                                       |



By contrast, Feign remains a solid choice when:

You already use Spring Cloud (Eureka, Ribbon, Sleuth, etc.). Feign plugs into all of those seamlessly.

You want zero boilerplate—just define an interface and call methods.

You prefer declarative fallbacks and in-client resilience patterns (e.g., Hystrix).

8. Key Takeaways
   No “one size fits all.”

If you need reactive pipelines or streaming, use WebClient rather than Feign.

If you want a lightweight, first-party declarative client, use HTTP Interface instead of pulling in Spring Cloud.

If you already have a legacy MVC app with many RestTemplate calls, keep using RestTemplate (or migrate to RestClient gradually).

Use Feign if you want to leverage Spring Cloud’s discovery/load-balancing/resilience ecosystem with minimal code.

Dependency vs. Control trade-off.

Feign (and HTTP Interface) hide a lot of HTTP plumbing, which is convenient but can make debugging harder.

RestClient/WebClient/RestTemplate require you to explicitly assemble each request, but you gain finer control over headers, timeouts, codecs, and resilience patterns.

Future-proofing.

Spring Framework is moving toward RestClient (imperative) and WebClient (reactive) for its HTTP-client story.

Spring 6’s HTTP Interface is clearly intended as a modern, first-party alternative to third-party tools like Feign.

In short, none of the Spring-built clients are universally “better” than Feign—it depends on your application’s requirements (blocking vs. non-blocking, dependency constraints, declarative vs. imperative style, and integration with Spring Cloud). Choose the client that best aligns with your architectural goals and team’s familiarity.





                START of experiment with HTTP Interface + RestClient.



NOW let's dive into writing the HTTP Interface + RestClient.
The HTTP Interface in the end uses one of the HTTP clients such as RestClient or WebClient



Below is description that walks through each part of your minimal HTTP‐REST‐Client demo. 
It’s concise but explains why each dependency and class exists, how they fit together, and how to run the app.

```markdown
# HTTP Rest Client Service Demo

A minimal Spring Boot 3 / Java 17 demo showing how to use Spring’s **@HttpExchange** (HTTP Interface) to create a thin client that forwards incoming requests to another backend (e.g. a user‐service). This demo also registers itself with Eureka so you could swap in a load‐balanced URL later.

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [How to Run](#how-to-run)
4. [Configuration Files](#configuration-files)
    - [build.gradle](#buildgradle)
    - [application.yml](#applicationyml)
5. [Code Structure & Explanation](#code-structure--explanation)
    - [1. Main Application Class](#1-main-application-class)
    - [2. HttpClientInterface (HTTP Interface)](#2-HttpClientInterface-http-interface)
    - [3. ApplicationBeanConfiguration (Proxy Bean)](#3-applicationbeanconfiguration-proxy-bean)
    - [4. UserProxyController (REST Endpoint)](#4-userproxycontroller-rest-endpoint)
6. [Next Steps](#next-steps)

---

## Overview

This small demo shows:

- How to define a Spring **HTTP Interface** (`@HttpExchange`) that mirrors a remote controller’s endpoints.
- How to register a bean of that interface at startup using `HttpServiceProxyFactory`.
- How to expose your own “proxy” controller (`/proxy/**`) that simply forwards to the remote service.
- How to register this client‐service with Eureka (so you can later replace a hard‐coded URL with a service‐ID).

All you do “in the client” is declare an interface, wire it up in a `@Configuration` class, and expose your own controller that calls it.

---

## Prerequisites

- **Java 17** (build‐tool toolchain is set to 17).
- **Spring Boot 3.4.6** (which brings Spring Framework 6.4.x).
- A running **Eureka Server** on `http://localhost:8761` (for service registration).
- A backend REST service on `http://localhost:8081` (for testing the proxy). This demo assumes that remote service offers endpoints under `/api/v1/...`.

If you don’t already have a user‐service running on port 8081, you can point the base URL to any other service, or skip Eureka entirely by hard‐coding a different URL.

---

## How to Run

1. Start your **Eureka Server** on port 8761.
2. Ensure your backend “user‐service” is up on port 8081 (exposes `/api/v1/create-new-user`, `/api/v1/user/{id}`, and `/api/v1/user-with-data/{id}`).
3. Run this project (e.g. `./gradlew bootRun`).
4. Verify in Eureka UI (http://localhost:8761) that **HTTPrestClientService** is registered.
5. Use Postman (or `curl`) against the proxy endpoints:

    - **Create a new user**
      ```
      POST http://localhost:8080/proxy/create-new-user
      Content-Type: application/json
 
      {
        "registrationDataDbDTO": { … },
        "profileDataDbDTO": { … },
        "descriptionDataDbDTO": { … }
      }
      ```
    - **Fetch by ID**
      ```
      GET http://localhost:8080/proxy/user/123
      X-API-Version: v1
      ```
    - **Fetch user with data**
      ```
      GET http://localhost:8080/proxy/user-with-data/123
      Custom-Header-1: foo
      Custom-Header-2: bar
      ```

Behind the scenes, each `/proxy/...` call is simply forwarded to the backend at `http://localhost:8081/api/v1/...`.

---

## Configuration Files

### build.gradle

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.6'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'demo1'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Core Spring Web (includes RestClient + HTTP Interface support)
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Validation API (optional, if your DTOs use @Valid)
    implementation group: 'jakarta.validation', name: 'jakarta.validation-api', version: '3.1.1'

    // Spring Cloud BOM (Moorgate SR1) to align versions
    implementation platform("org.springframework.cloud:spring-cloud-dependencies:2024.0.1")

    // Eureka Client so this service can register itself
    implementation "org.springframework.cloud:spring-cloud-starter-netflix-eureka-client"
}

tasks.named('test') {
    useJUnitPlatform()
}

```

Explanation

Spring Boot 3.4.6 pulls in Spring Framework 6.x, which introduces the new RestClient and @HttpExchange HTTP Interface.
We include spring-boot-starter-web so that both the embedded Tomcat server and RestClient classes are on the classpath.
The Spring Cloud BOM (2024.0.1) ensures a compatible set of Eureka + Load Balancer libraries.
We add spring-cloud-starter-netflix-eureka-client so this app can register itself with a running Eureka server.

application.yml
```yml
spring:
  application:
    name: HTTPrestClientService    # this service’s registration name in Eureka

eureka:
  client:
    service-url:
      defaultZone: "http://localhost:8761/eureka"
    register-with-eureka: true     # register as HTTPrestClientService
    fetch-registry: true           # fetch registry to enable load-balancing

# ────────────────────────────────────────────────────────────────────────────
# This is your custom namespace. You could name it anything (e.g. "client:",
# "remote-api:", "user-service:", etc.). Here we chose "dservice:" to remind us
# that this is the base URL for our “dservice” backend.
dservice:
  base-url: "http://localhost:8081"

#  dservice: is not special to Spring. It’s simply a grouping key (a map) under which you put your settings.
#  Under dservice:, you create a property called base-url whose value is http://localhost:8081.
#  Later, your code will read user.base-url instead of having builder.baseUrl("http://localhost:8081") hard-coded.

```
Explanation

spring.application.name is what Eureka will display as this instance’s service-ID.
eureka.client.service-url.defaultZone points at your Eureka server.
register-with-eureka: true tells the app to register itself at startup.
fetch-registry: true allows this client to look up other services (if we ever swap in a load-balanced URL).

Code Structure & Explanation
Below is a file-by-file breakdown. The goal is to show how a very small amount of code wires an @HttpExchange interface into a real Spring bean, then exposes a “proxy” controller that simply forwards to the remote service.

1. Main Application Class
```java
@SpringBootApplication
public class HttPrestClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttPrestClientServiceApplication.class, args);
    }
}

```

Annotated with @SpringBootApplication (includes @Configuration, @EnableAutoConfiguration, and @ComponentScan).
No custom logic here—just boots the Spring context so all other beans get registered.

2. HttpClientInterface (HTTP Interface)

```java
@HttpExchange(
    url    = "/api/v1",
    accept = MediaType.APPLICATION_JSON_VALUE
)
public interface HttpClientInterface {

    @PostExchange("/create-new-user")
    ResponseEntity<UserDbDTO> create(@RequestBody UserDbDTO body);

    @GetExchange("/user/{id}")
    ResponseEntity<UserDTO> getById(
        @PathVariable Long id,
        @RequestHeader(name = "X-API-Version", required = false) String apiVersion
    );

    @GetExchange("/user-with-data/{id}")
    ResponseEntity<UserDbDTO> getWithData(
        @PathVariable Long id,
        @RequestHeader Map<String,String> dynamicHeaders
    );
}

```

What this does:

@HttpExchange(url = "/api/v1", …) declares that all methods in this interface are calls into a remote service whose base path is /api/v1.

Each method has a Spring‐style annotation (@PostExchange or @GetExchange) that matches exactly the endpoint on the backend.

create(...) → POST /api/v1/create-new-user

getById(...) → GET /api/v1/user/{id} with an optional X-API-Version header

getWithData(...) → GET /api/v1/user-with-data/{id} with a map of arbitrary headers

At runtime, Spring will generate a dynamic proxy that implements this interface — invoking each method triggers a real 
RestClient call to the target URL (with path variables, headers, and request body bound automatically).

We return ResponseEntity<…> so we can inspect status codes, headers, etc. if needed. You could also 
return UserDbDTO or Mono<User> in a reactive scenario.


3. ApplicationBeanConfiguration (Proxy Bean)
```java
@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;

    // Constructor injection of our properties holder
    public ApplicationBeanConfiguration(DserviceClientProperties props) {
        this.props = props;
    }

    @Bean
    HttpClientInterface userHttpInterface(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl(props.getBaseUrl())   // reads dservice.base-url from the application.yml
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(HttpClientInterface.class);
    }
}

```

By annotating DserviceClientProperties with @ConfigurationProperties(prefix = "dservice"), Spring will automatically bind 
any dservice.* properties from your YAML (or application.properties) into that bean.
In this example, props.getBaseUrl() returns exactly what you set under user.base-url in application.yml.

Why ApplicationBeanConfiguration is needed:

By default, Spring does not know how to turn HttpClientInterface into a bean. We must explicitly create one.

RestClient.Builder is auto‐configured by spring-boot-starter-web (because Spring Framework 6.1+ includes RestClient). We inject that builder.

We call builder.baseUrl("http://localhost:8081") OR builder.baseUrl(props.getBaseUrl()) so that every call to @HttpExchange(url="/api/v1") is 
resolved against http://localhost:8081/api/v1.

RestClientAdapter.create(restClient) wraps the blocking RestClient into an adapter compatible with Spring’s HTTP Interface machinery.

HttpServiceProxyFactory.builderFor(...) creates a factory that can build proxies for any interface annotated with @HttpExchange.

Finally we call .createClient(HttpClientInterface.class) to produce a Spring bean of type HttpClientInterface. 
Once this method returns, Spring sees it as a managed bean, so you can @Autowired (or constructor‐inject) it elsewhere.

Note: If you wanted to use Eureka (load-balancing) instead of a fixed URL, you would add @LoadBalanced to the RestClient.Builder bean and 
change baseUrl("http://<SERVICE-ID>"). We’ve hard-coded localhost:8081 here for simplicity.

```java
@Component
@ConfigurationProperties(prefix = "dservice")
public class DserviceClientProperties
{
   /**
    * Binds to the 'user.base-url' property in application.yml.
    */
   private String baseUrl; // takes its value from base-url: http://localhost:8081 in application.yml

   private String serviceId; // takes its value from service-id: backend-service in application.yml

   private boolean isUseEureka; // takes its value from use-eureka: true in application.yml

   // (Optional) If you later want more settings, you can add them here:
   // private int timeoutMs;
   // private String apiKey;
   // …with matching getters & setters.

   public String getBaseUrl() {
      return baseUrl;
   }
   public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
   }

   public String getServiceId() {
      return serviceId;
   }

   public void setServiceId(String serviceId) {
      this.serviceId = serviceId;
   }

   public boolean isUseEureka() {
      return isUseEureka;
   }

   public void setUseEureka(boolean useEureka) {
      isUseEureka = useEureka;
   }
}
```

As we understand at this point, we are do not ask Eureka for an instance list and do not do any name-based resolution in Eureka.
We are only reading a literal URL (http://localhost:8081) from dservice.base-url and passing that straight into 
RestClient.builder().baseUrl(…). That means Spring is treating it as a fixed host:port.

To switch over so that Eureka resolves your backend, do two things:

Change the bean that provides RestClient.Builder to be @LoadBalanced.
In other words, instead of letting Spring auto-configure a plain RestClient.Builder, 
explicitly define one as a bean with @LoadBalanced. For example:


```java
@Configuration
public class ApplicationBeanConfiguration {

   private final DserviceClientProperties props;

   // Constructor injection of our properties holder
   public ApplicationBeanConfiguration(DserviceClientProperties props) {
      this.props = props;
   }

   /**
    * Tell Spring: “When anyone injects RestClient.Builder, give them a
    * load-balanced version that uses Eureka/LoadBalancer under the hood.”
    *
    *  ---------- THIS BEAN IS CRITICAL! ----------
    *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
    */
   @Bean
   @LoadBalanced
   RestClient.Builder restClientBuilder() {
      return RestClient.builder();
   }

   @Bean
   HttpClientInterface userHttpInterface(RestClient.Builder builder) {

      String target;

      target = "http://" + props.getServiceId();

      // Now, if we call builder.baseUrl("http://backend-service"),
      // Spring Cloud’s LoadBalancer/Eureka machinery will intercept “backend-service”
      // and ask Eureka for a real instance address.
      RestClient restClient = builder
              .baseUrl(target)   // <-- note “http://dservice” (the service-ID)
              .build();

      return HttpServiceProxyFactory
              .builderFor(RestClientAdapter.create(restClient))
              .build()
              .createClient(HttpClientInterface.class);
   }
}

```

The updated application.yml looks so:
```yml
spring:
   application:
      name: "HTTPrestClientService"

      # the name THIS app registers under
eureka:
   client:
      service-url:
         defaultZone: "http://localhost:8761/eureka"
      register-with-eureka: true
      fetch-registry: true


# ────────────────────────────────────────────────────────────────────────────
# This is your custom namespace. You could name it anything (e.g. "client:",
# "remote-api:", "user-service:", etc.). Here we chose "dservice:" to remind us
# that this is the base URL for our “dservice” backend, but we dont user it any more.
# Now we added also service-id:backend-service , because we will use Eureka to resolve by service-id, which is 'backend-service'.
dservice:
   #base-url: http://localhost:8081 - hard coded URL is no longer needed, because we will use Eureka and resolve by service-id:
   service-id: backend-service
   use-eureka: true
```

Am I implementing any load balancing? How does it work with HTTP‐Interface/RestClient/WebClient?

Short answer: Out of the box, neither RestClient.builder() nor WebClient.builder() is load-balanced. 

You have to explicitly annotate your builder‐bean with @LoadBalanced so that Spring Cloud LoadBalancer will wrap it.

Each request to http://<service-id>/… goes to LoadBalancer → Eureka → one of the instances.

Internally, whenever you do .baseUrl("http://backend-service"), behind the scenes Spring Cloud’s LoadBalancer will intercept calls to 

any host called “backend-service”, look up all registered backend-service_X instances from Eureka, and choose one (round-robin by default).

But ss long as you pass a fixed URL, Spring Cloud LoadBalancer is never invoked, because you point to one particular running instance. You are

not pointing to a pool of instances, among which the load can be balanced.

Next, lets show the Controllers:

4. UserProxyController (REST Endpoint)
```java
@RestController
@RequestMapping("/proxy")
public class UserProxyController {

    private final HttpClientInterface users;

    public UserProxyController(HttpClientInterface users) {
        this.users = users;
    }

    @PostMapping("/create-new-user")
    public ResponseEntity<UserDbDTO> create(@RequestBody UserDbDTO body) {
        return users.create(body);   // simply forward to remote
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-API-Version", required = false) String ver) {
        System.out.println("printing getById");
        return users.getById(id, ver);
    }

    @GetMapping("/user-with-data/{id}")
    public ResponseEntity<UserDbDTO> getWithData(
            @PathVariable Long id,
            @RequestHeader Map<String,String> headers) {
        System.out.println("printing getWithData");
        return users.getWithData(id, headers);
    }
}

```

How it works:

This controller is exposed on http://<this-app>:8080/proxy/....

Each endpoint method simply calls the corresponding method on HttpClientInterface—the generated proxy will perform 
a real HTTP call to http://localhost:8081/api/v1/....

We log to the console (System.out.println(...)) so you can see in the terminal whenever the controller is invoked.

By returning ResponseEntity<...> directly, any status code or headers from the remote service pass through unchanged.

Flow Summary
Client (e.g. Postman) → POST http://localhost:8080/proxy/create-new-user

UserProxyController.create(...) is invoked.

The controller calls users.create(body) (the HTTP Interface proxy).

The proxy issues POST http://localhost:8081/api/v1/create-new-user with the JSON body.

The backend responds (for example, HTTP 200 + JSON payload).

That ResponseEntity<UserDbDTO> travels back through the proxy → controller → Postman.


Next Steps

Move baseUrl into application.yml

```yml
user:
  base-url: http://localhost:8081

```
Then bind it with @ConfigurationProperties in ApplicationBeanConfiguration instead of hard-coding.

Eureka + Load Balancer

Add spring-cloud-starter-loadbalancer.

Change the builder bean to:
```java

@Bean
@LoadBalanced
RestClient.Builder restClientBuilder() {
  return RestClient.builder();
}

```
Use .baseUrl("http://user-service") so RestClient resolves the service ID via Eureka.





                END of experiment with HTTP Interface + RestClient.





               


               START of experiment to customize the RestClient -  1. Adjusting Timeouts (Connect, Read, Write) .


We already have a minimal configuration to run the app using a RestClient. Now we will build upon this basic minimal config in order
to add additional functionality;

NB! IN THIS DEMO we use synchronous, blocking RestClient! !!!!!

ADDING TIMEOUTS
First, add this gradle dependency:
```gradle
//for implementing 1. Adjusting Timeouts (Connect, Read, Write)
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

Next, your ApplicationBeanConfiguration class must look so:
```java
package demo1.httprestclientservice.config;

import demo1.httprestclientservice.HttpClientInterface;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;                // RestClient itself
import org.springframework.web.client.support.RestClientAdapter;  // ← correct import
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;

    public ApplicationBeanConfiguration(DserviceClientProperties props) {
        this.props = props;
    }



    /**
     *
     *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
     */
    /**
     * Tell Spring: “When anyone injects RestClient.Builder, give them a
     * load-balanced version that uses Eureka/LoadBalancer under the hood.”
     *
     *  ---------- THIS BEAN IS CRITICAL! ----------
     *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
     */
    /**
     * A load-balanced RestClient.Builder that uses Reactor Netty
     * with custom connect/read/write timeouts.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        // 1) Build your Reactor Netty HttpClient
        HttpClient reactorClient = HttpClient.create()
                // 2 seconds to establish TCP connection
                /**
                 * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
                 * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
                 */
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                // 5 seconds max for the full HTTP response
                /**
                 * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
                 * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
                 * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
                 */
                .responseTimeout(Duration.ofSeconds(5))
                // 5 seconds read or write inactivity timeout
                /**
                 *          Read timeout (new ReadTimeoutHandler(5)):
                 * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
                 * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
                 *      -------------------
                 *          Write timeout (new WriteTimeoutHandler(5))                 *
                 * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
                 * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
                 */
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5))
                );

        // 2) Wrap that in the Reactor-Netty RequestFactory
        ReactorClientHttpRequestFactory factory =
                new ReactorClientHttpRequestFactory(reactorClient);

        // 3) Tell RestClient to use it
        return RestClient.builder()
                .requestFactory(factory);
    }

    /**
     * No changes here — this builds your HTTP-interface proxy
     * on top of the RestClient.
     */
    @Bean
    HttpClientInterface userHttpInterface(RestClient.Builder builder) {
        String target = "http://" + props.getServiceId();

        RestClient restClient = builder
                .baseUrl(target)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(HttpClientInterface.class);
    }
}
```

And that's it! It's working successfully!



               END of experiment to customize the RestClient -  1. Adjusting Timeouts (Connect, Read, Write) 










               START of experiment to customize the RestClient -  2. Adding a Retry/Backoff Strategy



The RESULT of the experiment showed that you cannot directly apply RETRY logic in the RestClient, because RestClient is imperative first
(blocking first) client in comparison to WebClient which is non-blocking first! This small difference is important, because it causes
limitations in the RestClient, e.g. cannot directly apply RETRY logic.
The suggested approach is to use WebClient just for the part of applying RETRY Logic. The following code is just example how to 
implement "2. Adding a Retry/Backoff Strategy" , but it requires to MIX RestClient with WebClient. The following code is not implemented
in the project app. This code below is just for example how it should be like:

```java
package demo1.httprestclientservice.config;

import demo1.httprestclientservice.HttpClientInterface;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;                // synchronous RestClient
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.client.support.RestClientAdapter;

import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;

    public ApplicationBeanConfiguration(DserviceClientProperties props) {
        this.props = props;
    }

    /**
     * NB! THIS IS YOUR SYNCHRONOUS, BLOCKING RestClient.
     * It still uses Reactor Netty under the covers with your custom timeouts,
     * but all your interface methods returning plain T / ResponseEntity<T>
     * will block() on the Reactor pipeline.
     */
    @Bean
    @LoadBalanced  // <-- makes RestClient.Builder load-balanced via Eureka
    RestClient.Builder restClientBuilder() {
        // 1) Build your Reactor Netty HttpClient with timeouts
        HttpClient reactorClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5))
                    .addHandlerLast(new WriteTimeoutHandler(5))
            );

        // 2) Wrap it for use by RestClient
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(reactorClient);

        // 3) Tell RestClient to use Reactor Netty for all HTTP I/O
        return RestClient.builder()
                .requestFactory(connector);
    }

    /**
     * A load-balanced WebClient.Builder, so that reactive clients
     * will also go through Eureka. We'll use this below for retry/backoff.
     */
    @Bean
    @LoadBalanced    // <-- makes WebClient.Builder load-balanced via Eureka
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * YOUR HTTP-INTERFACE PROXY, built on a reactive WebClient with retry/backoff.
     * Methods returning Mono<T> or Flux<T> would be fully non-blocking.
     * Even methods returning T/ResponseEntity<T> will use block() under the covers,
     * but now with a retry strategy applied.
     */
    @Bean
    public HttpClientInterface userHttpInterface(WebClient.Builder webClientBuilder) {
        // 1) Same Reactor Netty client (with timeouts) as above
        HttpClient reactorClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5))
                    .addHandlerLast(new WriteTimeoutHandler(5))
            );

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(reactorClient);

        // 2) Build a reactive WebClient with retry/backoff filter
        WebClient webClient = webClientBuilder
            .clientConnector(connector)
            .baseUrl("http://" + props.getServiceId() + "/api/v1")
            .filter((request, next) ->
                next.exchange(request)
                    // retry up to 3 times, exponential backoff starting at 500 ms
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        // only retry on 5xx server errors
                        .filter(ex ->
                            ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException &&
                            ((org.springframework.web.reactive.function.client.WebClientResponseException) ex)
                                .getStatusCode().is5xxServerError()
                        )
                    )
            )  // :contentReference[oaicite:0]{index=0}
            .build();

        // 3) Create a reactive‐backed proxy for your interface
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(
                WebClientAdapter.create(webClient) // :contentReference[oaicite:1]{index=1}
            )
            .build();

        return factory.createClient(HttpClientInterface.class);
    }
}

```
What’s going on?
Two separate beans

restClientBuilder() stays exactly as you had it (synchronous RestClient with your timeouts).
webClientBuilder() gives you a load-balanced reactive WebClient.Builder.
Reactive proxy
We build a WebClient that reuses the same Reactor Netty HttpClient (with connect/read/write timeouts) but also installs a retry/backoff filter.
We then hand that WebClient to WebClientAdapter.create(...) and feed it into HttpServiceProxyFactory.builderFor(...).
Retry/backoff

Uses Reactor’s Retry.backoff(3, Duration.ofMillis(500)) to retry up to 3 times on any 5xx server error, waiting 500 ms, then 1 s, then 2 s between attempts.
digma.ai
stackoverflow.com

Method return types
If your HttpClientInterface methods return Mono<…> or Flux<…>, everything stays non-blocking end-to-end.
If they return ResponseEntity<…> or T, Spring will internally call .block() on that Mono/Flux, but now with retry logic applied.
With this in place, your second customization (retry/backoff) is fully wired in, and your first customization (timeouts) remains untouched. Let me know if you’d like to tweak the retry rules or add jitter!


Key points
RestClient.Builder is “imperative-first”
It gives you .requestFactory(...) to swap the I/O implementation (e.g. Reactor Netty vs the default), but it has no concept of reactor operators or ExchangeFilterFunction.
Anything you build with it will, under the covers, do a .block() when your interface method returns a plain T or ResponseEntity<T>.
WebClient is “reactive-first”

It has .clientConnector(...) (so you can reuse your Reactor Netty client with timeouts) and .filter(...) for Reactor operators like retries, backoff, circuit breakers, etc.
You get to choose your return type (Mono<T>, Flux<T>) or allow Spring to block for you when your interface returns T.
HttpServiceProxyFactory can work with either
If you call HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)), you get a blocking proxy with whatever connector/timeouts you gave it—but no retry filters.
If you call HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)), you get a reactive proxy that honors all your WebClient filters, including retry/backoff.


So: do we “lose” the RestClient?
Not really—think of it as two different underlying engines:

Blocking path:
```java
RestClient restClient = RestClient.builder()
    .requestFactory(new ReactorClientHttpRequestFactory(reactorClientWithTimeouts))
    .build();
RestClientAdapter adapter = RestClientAdapter.create(restClient);
HttpServiceProxyFactory.builderFor(adapter)…

```

→ gives you a synchronous client with timeouts but no reactive retry.

Reactive path:

```java
WebClient webClient = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(reactorClientWithTimeouts))
    .filter(retryFilter)
    .build();
WebClientAdapter adapter = WebClientAdapter.create(webClient);
HttpServiceProxyFactory.builderFor(adapter)…

```

→ gives you an asynchronous (or synchronous-if-blocked) client with timeouts andRetry/Backoff.

You choose which adapter to plug into HttpServiceProxyFactory. If you need HTTP-level retry and backoff, you must use the WebClientAdapter (i.e. the reactive path). If you’re okay handling retries yourself (e.g. via Spring Retry or Resilience4j around each blocking call), you can stick with the blocking RestClientAdapter.

In practice
Stay with RestClientAdapter when:
You really want plain synchronous calls everywhere.
You only need connector‐level tweaks (timeouts, SSL, proxy), but no reactive filters.

Switch to WebClientAdapter when:
You want to tap into the full reactive filter API (retry, backoff, circuit breaker, metrics, logging, etc.).
You don’t mind your interface methods returning Mono<T>/Flux<T> or being blocked on those publishers.
That’s why, to wire in retry/backoff, we built and injected a load-balanced WebClient and then created your HttpClientInterface against WebClientAdapter. You haven’t “lost” the RestClient; you’ve simply chosen the reactive engine that lets you express the retry strategy at the HTTP layer.





               END of experiment to customize the RestClient -  2. Adding a Retry/Backoff Strategy







               START of experiment to customize the RestClient -  3. Inserting Custom Headers (e.g., Correlation ID, Auth Token)



First, add some secretToken to application.yml so:
```yml
dservice:
  authToken: "superSecretToken"
...
```

Next, import its value to the java class
```java
@Component
@ConfigurationProperties(prefix = "dservice")
public class DserviceClientProperties { 
    private String authToken;      // takes its value from authToken: "superSecretToken" in application.yml

   public String getAuthToken() {
      return authToken;
   }
   public void setAuthToken(String authToken) {
      this.authToken = authToken;
   }
}

```

Next, add the new headers to the RestClient builder like so:

```java
@Configuration
public class ApplicationBeanConfiguration {

   private final DserviceClientProperties props;

   public ApplicationBeanConfiguration(DserviceClientProperties props) {
      this.props = props;
   }

   /**
    *
    *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
    */
   /**
    * Tell Spring: “When anyone injects RestClient.Builder, give them a
    * load-balanced version that uses Eureka/LoadBalancer under the hood.”
    *
    *  ---------- THIS BEAN IS CRITICAL! ----------
    *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
    */
   /**
    * A load-balanced RestClient.Builder that uses Reactor Netty
    * with custom connect/read/write timeouts.
    */
   @Bean
   @LoadBalanced
   RestClient.Builder restClientBuilder() {
      // 1) Build your Reactor Netty HttpClient
      HttpClient reactorClient = HttpClient.create()
              // 2 seconds to establish TCP connection
              /**
               * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
               * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
               */
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
              // 5 seconds max for the full HTTP response
              /**
               * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
               * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
               * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
               */
              .responseTimeout(Duration.ofSeconds(5))
              // 5 seconds read or write inactivity timeout
              /**
               *          Read timeout (new ReadTimeoutHandler(5)):
               * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
               * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
               *      -------------------
               *          Write timeout (new WriteTimeoutHandler(5))                 *
               * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
               * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
               */
              .doOnConnected(conn ->
                      conn.addHandlerLast(new ReadTimeoutHandler(5))
                              .addHandlerLast(new WriteTimeoutHandler(5))
              );

      // 2) Wrap that in the Reactor-Netty RequestFactory
      ReactorClientHttpRequestFactory factory =
              new ReactorClientHttpRequestFactory(reactorClient);

      // 3) Tell RestClient to use it, and register a defaultRequest
      //    hook that adds headers before every request.
      return RestClient.builder()
              .requestFactory(factory)
              // Add an X-Correlation-ID (new UUID each time) and an Authorization header
              .defaultRequest(spec -> {
                 // Dynamic Correlation ID per request
                 String correlationId = UUID.randomUUID().toString();
                 spec.header("X-Correlation-ID", correlationId);

                 // Static or fetched Auth token (replace with real retrieval)
                 String token = props.getAuthToken();  // assume you have this in props
                 spec.header("Authorization", "Bearer " + token);
              });
   }

   /**
    * No changes here — this builds your HTTP-interface proxy
    * on top of the RestClient.
    */
   @Bean
   HttpClientInterface userHttpInterface(RestClient.Builder builder) {
      String target = "http://" + props.getServiceId();

      RestClient restClient = builder
              .baseUrl(target)
              .build();

      return HttpServiceProxyFactory
              .builderFor(RestClientAdapter.create(restClient))
              .build()
              .createClient(HttpClientInterface.class);
   }
}
```
What changed?
We added a .defaultRequest(Consumer<RestClient.RequestHeadersSpec<?>>) to the RestClient.Builder.
Inside that lambda, we generate a fresh UUID for X-Correlation-ID on each call and inject an Authorization header (you could pull the token from your props, a security context, etc.).
All your existing timeout comments and reactor‐netty wiring remain untouched.
Now every request sent via all HttpClientInterface methods will carry those two headers automatically.



               END of experiment to customize the RestClient -  3. Inserting Custom Headers (e.g., Correlation ID, Auth Token)






               START of experiment to customize the RestClient -  4. Custom Error Decoding & Mapping to Exceptions


First, for improved debugging, you can add this logging in application.yml
   'Optional'
```yml
logging:
  level:
    org.springframework.web.service.invoker: DEBUG
    org.springframework.web.reactive.function.client.ExchangeFunctions: DEBUG
```

What is expected to happen? Well, the backend-service may sometimes respond with http error codes like: 401, 404, or others. 
To simulate such functionality create a demo controller in the "dservice" project, like so:

```java

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1")
public class UserRestController {
   ... all other controllers here ...
   
   @GetMapping("/http-status/{code}")
   public ResponseEntity<String> getCustomErrorResponse(@PathVariable int code)
   {
      try {
         HttpStatus status = HttpStatus.valueOf(code);

         ResponseEntity<String> responseEntity = ResponseEntity
                 .status(status)
                 .body("Custom error with status: " + code);

         return responseEntity;

      } catch (IllegalArgumentException ex)
      {
         return ResponseEntity
                 .status(HttpStatus.BAD_REQUEST)
                 .body("Invalid HTTP status code: " + code);
      }
   }
}
```
Thus we can simulate all kinds of error codes as response.

Upon receiving such error response by default the RestClient - all such non‐2xx responses are turned into a generic RestClientResponseException.
You might want to make those responses unfriendly for debugging and to map, say, a 404 to a UserNotFoundException or a 401 to UnauthorizedException.
Therefore, now create your custom error responses, like so:

```java
/**
 * Thrown when the RestClient receives a 404 Not Found from the backend.
 *
 * NB! Do not annotate it with: @ResponseStatus(CustomHttpStatus.NOT_FOUND), because if you do,
 * then the custom error message defined in the RestClient.builder() will be overwritten by the annotation;
 * The end client Postman will not get anything about the 404,
 * instead postman client will receive generic 500 "internal error"
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super();
    }
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
}

/**
 * Thrown when the RestClient receives a 401 Not Found from the backend.
 *
 * NB! Do not annotate it with: @ResponseStatus(CustomHttpStatus.NOT_FOUND), because if you do,
 * then the custom error message defined in the RestClient.builder() will be overwritten by the annotation;
 * The end client Postman will not get anything about the 401,
 * instead postman client will receive generic 500 "internal error"
 */
public class UnauthorizedException extends RuntimeException {
   public UnauthorizedException() {
      super();
   }
   public UnauthorizedException(String message) {
      super(message);
   }
   public UnauthorizedException(String message, Throwable cause) {
      super(message, cause);
   }
   public UnauthorizedException(Throwable cause) {
      super(cause);
   }
}

/**
 * Thrown for any 5xx (server) HTTP status.
 */
public class DownstreamServiceException extends RuntimeException {
   public DownstreamServiceException() {
      super();
   }
   public DownstreamServiceException(String message) {
      super(message);
   }
   public DownstreamServiceException(String message, Throwable cause) {
      super(message, cause);
   }
   public DownstreamServiceException(Throwable cause) {
      super(cause);
   }
}

/**
 * Thrown for any other 4xx (client) HTTP status.
 */
public class ClientErrorException extends RuntimeException {
   public ClientErrorException() {
      super();
   }
   public ClientErrorException(String message) {
      super(message);
   }
   public ClientErrorException(String message, Throwable cause) {
      super(message, cause);
   }
   public ClientErrorException(Throwable cause) {
      super(cause);
   }
}
```

Next, apply these custom error responses in the RestClient builder in the ApplicationBeanConfiguration like so:
```java

import demo1.httprestclientservice.HttpClientInterface;
import demo1.httprestclientservice.exceptions.ClientErrorException;
import demo1.httprestclientservice.exceptions.DownstreamServiceException;
import demo1.httprestclientservice.exceptions.UnauthorizedException;
import demo1.httprestclientservice.exceptions.UserNotFoundException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;                // RestClient itself
import org.springframework.web.client.support.RestClientAdapter;  // ← correct import
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ApplicationBeanConfiguration {

   private final DserviceClientProperties props;

   public ApplicationBeanConfiguration(DserviceClientProperties props) {
      this.props = props;
   }



   /**
    *
    *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
    */
   /**
    * Tell Spring: “When anyone injects RestClient.Builder, give them a
    * load-balanced version that uses Eureka/LoadBalancer under the hood.”
    *
    *  ---------- THIS BEAN IS CRITICAL! ----------
    *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
    */
   /**
    * A load-balanced RestClient.Builder that uses Reactor Netty
    * with custom connect/read/write timeouts.
    */
   @Bean
   @LoadBalanced
   RestClient.Builder restClientBuilder() {
      // 1) Build your Reactor Netty HttpClient
      HttpClient reactorClient = HttpClient.create()
              // 2 seconds to establish TCP connection
              /**
               * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
               * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
               */
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
              // 5 seconds max for the full HTTP response
              /**
               * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
               * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
               * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
               */
              .responseTimeout(Duration.ofSeconds(5))
              // 5 seconds read or write inactivity timeout
              /**
               *          Read timeout (new ReadTimeoutHandler(5)):
               * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
               * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
               *      -------------------
               *          Write timeout (new WriteTimeoutHandler(5))                 *
               * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
               * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
               */
              .doOnConnected(conn ->
                      conn.addHandlerLast(new ReadTimeoutHandler(5))
                              .addHandlerLast(new WriteTimeoutHandler(5))
              );

      // 2) Wrap that in the Reactor-Netty RequestFactory
      ReactorClientHttpRequestFactory factory =
              new ReactorClientHttpRequestFactory(reactorClient);

      // 3) Tell RestClient to use it, and register a defaultRequest
      //    hook that adds headers before every request.
      return RestClient.builder()
              .requestFactory(factory)
              // Add an X-Correlation-ID (new UUID each time) and an Authorization header
              .defaultRequest(spec -> {
                 // Dynamic Correlation ID per request
                 String correlationId = UUID.randomUUID().toString();
                 spec.header("X-Correlation-ID", correlationId);

                 // Static or fetched Auth token (replace with real retrieval)
                 String token = props.getAuthToken();  // assume you have this in props
                 spec.header("Authorization", "Bearer " + token);
              })
              // b) map 4xx & 5xx status to custom exceptions
              // b) map 4xx & 5xx status to custom exceptions
              .defaultStatusHandler(
                      HttpStatusCode::isError,     // test any non-2xx
                      (request, response) -> {
                         HttpStatusCode status = response.getStatusCode();

                         if (status.is4xxClientError()) {
                            // 404 → custom not-found
                            if (status == HttpStatusCode.valueOf(404)) {
                               throw new UserNotFoundException("My Custom Error Response: User not found (404)");
                            }
                            // 401 → unauthorized
                            if (status == HttpStatusCode.valueOf(401)) {
                               throw new UnauthorizedException("My Custom Error Response: Unauthorized (401)");
                            }
                            // other 4xx → generic client error
                            throw new ClientErrorException("My Custom Error Response: Client error: " + status.value());
                         }

                         if (status.is5xxServerError()) {
                            // any 5xx → downstream failure
                            throw new DownstreamServiceException("My Custom Error Response: Server error: " + status.value());
                         }

                         // fallback (should never happen if predicate matches only errors)
                      }
              );

   }

   /**
    * No changes here — this builds your HTTP-interface proxy
    * on top of the RestClient.
    */
   @Bean
   HttpClientInterface userHttpInterface(RestClient.Builder builder) {
      String target = "http://" + props.getServiceId();

      RestClient restClient = builder
              .baseUrl(target)
              .build();

      return HttpServiceProxyFactory
              .builderFor(RestClientAdapter.create(restClient))
              .build()
              .createClient(HttpClientInterface.class);
   }
}

```

As a result, when the backend-service responds with http 401, or 404, the RestClient will not throw (print the exception on the console and the app will continue running)
its own generic RestClientResponseException,
but instead will throw (print the exception on the console and the app will continue running) my custom error exceptions! And me as admin
will be able to easily recognize this in the application log (printed on the console); That is the benefit of using such custom mapping.

And as final result: the postman client sending to URL (http://localhost:8080/proxy/proxy-http-status/401) will receive generic 500 "internal error" and will not know anything about the thrown 404, or 401 error





                  END of experiment to customize the RestClient -  4. Custom Error Decoding & Mapping to Exceptions






                  START of experiment to customize the RestClient -  5. Custom JSON (Jackson) Configuration


We continue with the next customization of the RestClient - we want to use a custom ObjectMapper—for example, for 
enabling a special date format, ignoring unknown fields.

From what we have already learned we know that defining a new @Bean for ObjectMapper will apply our few new custom features, but will disable 
in general all other default features of the object mapper. We don't want this! What is the alternative then? - Well, logically we could suggest
to implement the interface WebMvcConfigurer, but wait, stop here! It won't help us in that case either!! - Why you don’t need 
a WebMvcConfigurer ?? - A WebMvcConfigurer gives you callbacks, hooks into how Spring MVC sets up its 
HttpMessageConverters for controllers of the server-side. In the project we do have such Controllers and modifying via 
WebMvcConfigurer will change the ObjectMapper for them!!!
```java
@RestController
@RequestMapping("/proxy")       // <— choose any prefix you like
public class UserProxyController {

    private final HttpClientInterface users;

    public UserProxyController(HttpClientInterface users) {
        this.users = users;
    }

    @PostMapping("/create-new-user")
    public ResponseEntity<UserDbDTO> create(@RequestBody UserDbDTO body) {
        return users.create(body);   // simply forward
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id,
                                           @RequestHeader(value = "X-API-Version", required = false) String ver) {
        System.out.println("printing getById");
        return users.getById(id, ver);
    }

    @GetMapping("/user-with-data/{id}")
    public ResponseEntity<UserDbDTO> getWithData(@PathVariable Long id,
                                                 @RequestHeader Map<String,String> headers) {
        System.out.println("printing getWithData");
        return users.getWithData(id, headers);
    }

    @GetMapping("/proxy-http-status/{code}")
    public ResponseEntity<String> getCustomErrorResponse(@PathVariable int code) {
        System.out.println("getting getCustomErrorResponse");
        ResponseEntity<String> responseEntity = users.proxyGetCustomErrorResponse(code);
        return responseEntity;
    }
}
```
, but modifying via WebMvcConfigurer WILL NOT MODIFY the ObjectMapper for the RestClient or for the HTTP Client Interface !!! 
WebMvcConfigurer will have zero effect on the explicit RestClient.builder().messageConverters(converters -> { … })...

In order to tweak, change the functionality of the ObjectMapper for the RestClient and for the HTTP Client Interface, there are these options:

1. Use a Jackson2ObjectMapperBuilderCustomizer
Spring Boot’s auto-configuration registers a Jackson2ObjectMapperBuilder that you can customize—it will still apply all of Spring Boot’s 
“default” settings (Hibernate MODULE, JSR-310, JavaTimeModule, Hibernate5Module, Kotlin support, any spring.jackson.* properties, etc.), 
and then apply your tweaks:
```java
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return builder -> {
            // ignore unknown JSON fields
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            // write dates as ISO-8601, not timestamps
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            // custom date format for legacy java.util.Date
            builder.dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            builder.timeZone(TimeZone.getTimeZone("UTC"));
            // support java.time.*
            builder.modulesToInstall(new JavaTimeModule());
            // snake_case <-> camelCase
            builder.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        };
    }
}
```
That single bean will piggy-back on all of Spring Boot’s defaults, then apply yours.

OR - the second option is:

2. Autowire and tweak the existing ObjectMapper
If you absolutely must adjust the one-and-only mapper bean, you can simply @Autowired it and mutate it in a configuration class (or in a 
@PostConstruct):
```java
@Configuration
public class JacksonConfig {
  
  public JacksonConfig(ObjectMapper objectMapper) {
    // ignore extra fields in incoming JSON
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // use ISO dates
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // snake_case
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    // etc...
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
    objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
  }
}

```
Because Spring Boot runs its auto-configuration before calling your config class, you’ll start with the fully-fleshed mapper and simply 
layer on extra functionality. In other words - the injected ObjectMapper comes from the IoC of Spring, which means - it is 'spring-aware'! And you
can further modify it!

Which of the two options to choose from? I would prefer the option, which reaches the lower level of abstraction of that feature. 
Which one is it then?
Because Spring Boot runs its auto-configuration before calling your config class, you’ll start with the fully-fleshed mapper and simply 
layer on extra functionality.

| Technique                             | Level of abstraction       | Pro                                                                  |
| ------------------------------------- | -------------------------- | -------------------------------------------------------------------- |
| Jackson2ObjectMapperBuilderCustomizer | Higher-level (declarative) | Keeps you aligned with Spring Boot’s auto-configuration and defaults |
| Autowire & mutate raw ObjectMapper    | Lower-level (imperative)   | Full direct control over the live `ObjectMapper` instance            |

In most cases you’ll reach for the builder customizer (higher-level) because it gives you all the Spring Boot defaults plus just your extras. 
Directly tweaking the ObjectMapper is available when you need fine-grained, immediate control at the Jackson core level.

NB!!! Very important! Those two options above will modify the ObjectMapper not only for the RestClient and HTTP Rest Client interface, but 
also for the Server-side @RestControllers !!! Why? - Because both approaches modify directly the objectMapper which is already inside the IoC 
container and is globally available. And you just modify this existing and globally available bean.
By mutating the one auto-configured ObjectMapper bean, you’re changing the Jackson instance that Spring Boot uses everywhere:
Server-side
Spring MVC’s MappingJackson2HttpMessageConverter (used by your @RestController methods) is built from that same bean. So your 
FAIL_ON_UNKNOWN_PROPERTIES, ISO-8601 dates, snake_case, etc. will apply to every @RequestBody and @ResponseBody.

Client-side
When you inject that mutated ObjectMapper into your RestClient.builder().messageConverters(...), you’re explicitly wiring the very same 
mapper into your HTTP client proxy. All of your downstream calls will use those exact settings.

Anywhere else
Any other component that @Autowired-injects ObjectMapper (or that uses Spring’s Jackson2ObjectMapperBuilder) will see the same configuration.

Here is example code, which achieves exactly that:

```java
@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;

    public ApplicationBeanConfiguration(
            DserviceClientProperties props,
            ObjectMapper objectMapper   // the auto-configured one, injected here! Its globally available inside the IoC container.
    ) {
        this.props = props;

        // now just layer on your tweaks
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        objectMapper.registerModule(new JavaTimeModule());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(df);
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder(ObjectMapper objectMapper) {
        // … your reactor client factory, status handlers, defaults, etc …
        return RestClient.builder()
                .requestFactory(factory)
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                });
    }

    // … your HttpClientInterface bean, etc …
}

```
Why this works
You never override Spring Boot’s default bean definition, so all of its auto-configured Jackson modules (Hibernate, JSR-310, Kotlin, any 
spring.jackson.* props, etc.) still get applied. Meaning - you are working, modifying the bean, which is 'spring-aware'! (What means 'spring-aware' - explanation is below)
You mutate that single shared ObjectMapper instance, so every part of your app—MVC controllers and your hand-built RestClient—will see the 
exact same, tweaked mapper.



CONCLUSION: I should ask myself - do I really want to mutate, tweak the objectMapper for the whole application? Or do I prefer to tweak the
objectMapper only for the restClient, without affecting the server-side @RestControllers?
Well - I prefer to tweak, mutate the objectMapper only for the restClient !!! So, how to achieve this?



If you want your controllers to keep using the stock Spring Boot mapper, but give your RestClient a different one, you simply have to stop
mutating the global bean and instead build a fresh mapper just for your client. But how to get a brand new ObjectMapper instance? To explain this,
we first need to explain the bigger picture - so:

First, there is this independent Java class: ObjectMapper objectMapper = new ObjectMapper(); - you can use it outside of Spring in any other apps.
Such new ObjectMapper(); has its settings by default - the very basic and typical for java settings !! Ok!! 

But Spring decided to use that 'new ObjectMapper();' for their project as well and when you install your gradle dependencies and run the very
basic 'Hello World' App, the IoC container in Spring already has there an instance of such 'new ObjectMapper();' !!! That very first instance
is really very basic, BUT still it slightly differs from the  new ObjectMapper(); outside a Spring project. Why? - Because Spring still needed
to add some settings which are needed by the Spring Project. Thus, you can say that such objectMapper instance is Spring-aware, because it contains
setting, which are typical for any Spring project!

So, when you run the very basic 'Hello World' Spring App there are two ways to call out that global objectMapper instance:
- either via @Autowired ObjectMapper objectMapper; in any Java (Spring) class or component, so:
```java
@Component
public class SomeJavaClass {
    @Autowired
    private ObjectMapper objectMapper;
    // … use objectMapper …
}
```

OR

- via direct dependency injection into the constructor of any Java (Spring) class or component:
```java
@Configuration
public class ApplicationBeanConfiguration {
    private final ObjectMapper objectMapper;    
    public ApplicationBeanConfiguration(ObjectMapper objectMapper) 
    {
        this.objectMapper = objectMapper;
    }
    // … other @Bean methods …
}
```

Both of those ways will give you objectMapper instance, which is 'spring-aware', meaning it already contains the settings given by the Spring project.
Basically both approaches give you the objectMapper from the IoC container - the very same one, which is also 'spring-aware';

And from that level - usually when the Spring project grows, comes the need to add more functionality, features to that 'spring-aware' objectMapper.
And you do so - you modify some of its settings, but keeping all rest (we already know how to achieve that). And again, and again, and again! But at some
point we need to use a brand new instance of the ObjectMapper, but the one we have in the IoC is already changed a lot! Exactly this is the
case with our RestClient.builder() - we want to build it using objectMapper instance which is separate from the global one, which is already changed.
How do we get us a brand new objectMapper instance ??? Well, there are few approaches:

1. Directly create new, raw mapper:
```java
ObjectMapper objectMapper = new ObjectMapper();
```
But this objectMapper IS NOT 'spring-aware', meaning you’ll miss out on all the typical Spring auto-configurations like the JSR-310, Hibernate, 
and other modules that Spring Boot usually wires in.

2. Directly reaching out to the IoC container and getting the 'objectMapper' instance from there via @Autowired OR via the injection in the constructor, as
we described just above - BUT this is bad practice, because if we have modified that 'objectMapper' instance, we will take those modifications along! And
we want a clean objectMapper, but still to be 'spring-aware'.

3. Use the class: 
private Jackson2ObjectMapperBuilder jacksonBuilder = new Jackson2ObjectMapperBuilder(); - this is very FRESH approach, because
it gives you, first - a brand new jacksonBuilder instance. And with it you can create a brand new instance of ObjectMapper! Great!
But at this point those instances of Jackson2ObjectMapperBuilder and ObjectMapper ARE NOT 'spring-aware', because we created them manually using
the 'new' key word! How to get them to be 'spring-aware'? Very simple - call them, get them from the IoC container or viw @Autowired, so:
```java
@Configuration
public class ApplicationBeanConfiguration {
   private final Jackson2ObjectMapperBuilder jacksonBuilder;

   public ApplicationBeanConfiguration(
           Jackson2ObjectMapperBuilder jacksonBuilder
   ) {
      this.jacksonBuilder = jacksonBuilder;      // This is injected from the IoC and therefore it is already 'spring-aware' 
   }

   ObjectMapper clientMapper = jacksonBuilder.build(); // Since the jacksonBuilder is 'spring-aware', the clientMapper coming from it is also 'spring-aware'

   // …
   
}
```
OR

```java
@Configuration
public class ApplicationBeanConfiguration {
    @Autowired
    private final Jackson2ObjectMapperBuilder jacksonBuilder;  // With @Autowired this is injected from the IoC and therefore it is already 'spring-aware'

   ObjectMapper clientMapper = jacksonBuilder.build(); // Since the autowired jacksonBuilder is 'spring-aware', the clientMapper coming from it is also 'spring-aware'
   
```

Exactly that 3th approach (see 3. above) - injecting Jackson2ObjectMapperBuilder jacksonBuilder in the constructor and then using it to create
new instance of object mapper is used  in the public class ApplicationBeanConfiguration in our project. The benefits of that approach is we keep
the global object mapper (in the IoC) separate and untouched from this one ObjectMapper clientMapper = jacksonBuilder.build();, which is created and used 
inside the public class ApplicationBeanConfiguration, so:

```java
@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;
    private final Jackson2ObjectMapperBuilder jacksonBuilder;

    public ApplicationBeanConfiguration(DserviceClientProperties props,
                                        Jackson2ObjectMapperBuilder jacksonBuilder)
    {
        this.props           = props;
        this.jacksonBuilder  = jacksonBuilder;
    }

    /**
     *
     *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
     */
    /**
     * Tell Spring: “When anyone injects RestClient.Builder, give them a
     * load-balanced version that uses Eureka/LoadBalancer under the hood.”
     *
     *  ---------- THIS BEAN IS CRITICAL! ----------
     *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
     */
    /**
     * A load-balanced RestClient.Builder that uses Reactor Netty
     * with custom connect/read/write timeouts.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        // 1) Build your Reactor Netty HttpClient
        HttpClient reactorClient = HttpClient.create()
                // 2 seconds to establish TCP connection
                /**
                 * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
                 * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
                 */
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                // 5 seconds max for the full HTTP response
                /**
                 * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
                 * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
                 * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
                 */
                .responseTimeout(Duration.ofSeconds(5))
                // 5 seconds read or write inactivity timeout
                /**
                 *          Read timeout (new ReadTimeoutHandler(5)):
                 * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
                 * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
                 *      -------------------
                 *          Write timeout (new WriteTimeoutHandler(5))                 *
                 * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
                 * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
                 */
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5))
                );

        // 2) Wrap that in the Reactor-Netty RequestFactory
        ReactorClientHttpRequestFactory factory =
                new ReactorClientHttpRequestFactory(reactorClient);

        // 3) Create a *new* ObjectMapper from the Boot‐configured builder
        ObjectMapper clientMapper = jacksonBuilder.build(); // this returns us a brand new ObjectMapper, untouched from other modifications, but its also 'spring-aware'!!!
        // 4) Tweak *only* this mapper—controllers remain unaffected
        clientMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        clientMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        clientMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        clientMapper.registerModule(new JavaTimeModule());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        clientMapper.setDateFormat(df);


        // 5) Tell RestClient to use it, and register a defaultRequest
        //    hook that adds headers before every request.
        //  Tell RestClient to use the *new* ObjectMapper, and register your default headers and error‐handling
        return RestClient.builder()
                .requestFactory(factory)
                // Add an X-Correlation-ID (new UUID each time) and an Authorization header
                .defaultRequest(spec -> {
                    // Dynamic Correlation ID per request
                    String correlationId = UUID.randomUUID().toString();
                    spec.header("X-Correlation-ID", correlationId);

                    // Static or fetched Auth token (replace with real retrieval)
                    String token = props.getAuthToken();  // assume you have this in props
                    spec.header("Authorization", "Bearer " + token);
                })
                // b) map 4xx & 5xx status to custom exceptions
                // b) map 4xx & 5xx status to custom exceptions
                .defaultStatusHandler(
                        HttpStatusCode::isError,     // test any non-2xx
                        (request, response) -> {
                            HttpStatusCode status = response.getStatusCode();

                            if (status.is4xxClientError()) {
                                // 404 → custom not-found
                                if (status == HttpStatusCode.valueOf(404)) {
                                    throw new UserNotFoundException("My Custom Error Response: User not found (404)");
                                }
                                // 401 → unauthorized
                                if (status == HttpStatusCode.valueOf(401)) {
                                    throw new UnauthorizedException("My Custom Error Response: Unauthorized (401)");
                                }
                                // other 4xx → generic client error
                                throw new ClientErrorException("My Custom Error Response: Client error: " + status.value());
                            }

                            if (status.is5xxServerError()) {
                                // any 5xx → downstream failure
                                throw new DownstreamServiceException("My Custom Error Response: Server error: " + status.value());
                            }

                            // fallback (should never happen if predicate matches only errors)
                        }
                )
                // Replace JSON converters with one using your private mapper
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
                })
                ;
    }



    /**
     * No changes here — this builds your HTTP-interface proxy
     * on top of the RestClient.
     */
    @Bean
    HttpClientInterface userHttpInterface(RestClient.Builder builder) {
        String target = "http://" + props.getServiceId();

        RestClient restClient = builder
                .baseUrl(target)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(HttpClientInterface.class);
    }
}
```
Yes. When you pass that clientMapper into your MappingJackson2HttpMessageConverter, it becomes the Jackson core used by that converter for 
both directions:

Deserialization (reading HTTP responses → your Java DTOs)
   - DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
   - PropertyNamingStrategy for field-name matching
   - any modules you’ve registered (e.g. JavaTimeModule)

Serialization (writing request bodies → JSON)
   - SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
   - the same PropertyNamingStrategy
   - your custom DateFormat, etc.

Jackson’s view of “how to turn JSON into objects” and “how to turn objects into JSON” both live on the same ObjectMapper instance—so every 
tweak you make (dates, naming, modules, features) will apply whenever that mapper is used, regardless of direction.
And most importantly, that clientMapper is not applied to the server-side controllers!





            END of experiment to customize the RestClient -  5. Custom JSON (Jackson) Configuration




            START of experiment to customize the RestClient -  6. Metrics & Instrumentation 


In production, you’ll want to track how many calls you’re making, response times, error rates, etc. You can hook in Micrometer or 
Spring Boot’s MeterRegistry and record metrics around every request.

First, add this dependency:
```gradle
    // for implementing MeterRegistry for 6. Metrics & Instrumentation
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator
    implementation group: 'org.springframework.boot', name: 'spring-boot-starter-actuator', version: '3.5.0'
```

Next, add this configs to the application.yml
Expose the Actuator endpoints -    In your application.yml (or application.properties), ensure you expose the metrics 
(and optionally prometheus) endpoints. It also enables percentiles or a histogram you can enable them in your application.yml
```yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    metrics:
      enabled: true
    prometheus:
      enabled: true
  distribution:
    # turn on histograms (so backends like Prometheus can compute quantiles)
    percentiles-histogram:
      http.server.requests: true
      http.client.requests: true
    # also compute specific percentiles
    percentiles:
      http.server.requests: [ 0.5, 0.95, 0.99 ]
      http.client.requests: [ 0.5, 0.95, 0.99 ]
```

Next, modify, customize the  HttpClient reactorClient = HttpClient.create() so:
```java
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import demo1.httprestclientservice.HttpClientInterface;
import demo1.httprestclientservice.exceptions.ClientErrorException;
import demo1.httprestclientservice.exceptions.DownstreamServiceException;
import demo1.httprestclientservice.exceptions.UnauthorizedException;
import demo1.httprestclientservice.exceptions.UserNotFoundException;
import io.micrometer.core.instrument.Metrics;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;                // RestClient itself
import org.springframework.web.client.support.RestClientAdapter;  // ← correct import
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

import io.micrometer.core.instrument.MeterRegistry;


@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;
    private final Jackson2ObjectMapperBuilder jacksonBuilder;

    public ApplicationBeanConfiguration(DserviceClientProperties props,
                                        Jackson2ObjectMapperBuilder jacksonBuilder)
    {
        this.props           = props;
        this.jacksonBuilder  = jacksonBuilder;
    }



    /**
     *
     *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
     */
    /**
     * Tell Spring: “When anyone injects RestClient.Builder, give them a
     * load-balanced version that uses Eureka/LoadBalancer under the hood.”
     *
     *  ---------- THIS BEAN IS CRITICAL! ----------
     *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
     */
    /**
     * A load-balanced RestClient.Builder that uses Reactor Netty
     * with custom connect/read/write timeouts.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder(MeterRegistry registry) {
        // Register Reactor Netty's metrics with Micrometer
        Metrics.addRegistry(registry);

        // 1) Build your Reactor Netty HttpClient
        //  Also add metrics enabled
        HttpClient reactorClient = HttpClient.create()
                // Enable Micrometer metrics for HTTP client (record connection/request metrics)
                .metrics(true, conn ->
                        props.getServiceId() + "-" + Function.identity().apply(conn))  // record metrics tagged by service ID
                // 2 seconds to establish TCP connection
                /**
                 * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
                 * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
                 */
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                // 5 seconds max for the full HTTP response
                /**
                 * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
                 * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
                 * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
                 */
                .responseTimeout(Duration.ofSeconds(5))
                // 5 seconds read or write inactivity timeout
                /**
                 *          Read timeout (new ReadTimeoutHandler(5)):
                 * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
                 * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
                 *      -------------------
                 *          Write timeout (new WriteTimeoutHandler(5))                 *
                 * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
                 * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
                 */
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5))
                );

        // 2) Wrap that in the Reactor-Netty RequestFactory
        ReactorClientHttpRequestFactory factory =
                new ReactorClientHttpRequestFactory(reactorClient);

        // 3) Create a *new* ObjectMapper from the Boot‐configured builder
        ObjectMapper clientMapper = jacksonBuilder.build();
        // 4) Tweak *only* this mapper—controllers remain unaffected
        clientMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        clientMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        clientMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        clientMapper.registerModule(new JavaTimeModule());
        DateFormat df = new SimpleDateFormat("MM|dd|yyyy HH~mm~ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        clientMapper.setDateFormat(df);


        // 5) Tell RestClient to use it, and register a defaultRequest
        //    hook that adds headers before every request.
        //  Tell RestClient to use the *new* ObjectMapper, and register your default headers and error‐handling
        return RestClient.builder()
                .requestFactory(factory)
                // Add an X-Correlation-ID (new UUID each time) and an Authorization header
                .defaultRequest(spec -> {
                    // Dynamic Correlation ID per request
                    String correlationId = UUID.randomUUID().toString();
                    spec.header("X-Correlation-ID", correlationId);

                    // Static or fetched Auth token (replace with real retrieval)
                    String token = props.getAuthToken();  // assume you have this in props
                    spec.header("Authorization", "Bearer " + token);
                })
                // b) map 4xx & 5xx status to custom exceptions
                // b) map 4xx & 5xx status to custom exceptions
                .defaultStatusHandler(
                        HttpStatusCode::isError,     // test any non-2xx
                        (request, response) -> {
                            HttpStatusCode status = response.getStatusCode();

                            if (status.is4xxClientError()) {
                                // 404 → custom not-found
                                if (status == HttpStatusCode.valueOf(404)) {
                                    throw new UserNotFoundException("My Custom Error Response: User not found (404)");
                                }
                                // 401 → unauthorized
                                if (status == HttpStatusCode.valueOf(401)) {
                                    throw new UnauthorizedException("My Custom Error Response: Unauthorized (401)");
                                }
                                // other 4xx → generic client error
                                throw new ClientErrorException("My Custom Error Response: Client error: " + status.value());
                            }

                            if (status.is5xxServerError()) {
                                // any 5xx → downstream failure
                                throw new DownstreamServiceException("My Custom Error Response: Server error: " + status.value());
                            }

                            // fallback (should never happen if predicate matches only errors)
                        }
                )
                // Replace JSON converters with one using your private mapper
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
                })
                ;
    }



    /**
     * No changes here — this builds your HTTP-interface proxy
     * on top of the RestClient.
     */
    @Bean
    HttpClientInterface userHttpInterface(RestClient.Builder builder) {
        String target = "http://" + props.getServiceId();

        RestClient restClient = builder
                .baseUrl(target)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(HttpClientInterface.class);
    }
}
```


In postman client sent these requests:
Very first - send some normal requests to your controller endpoint, otherwise there will no metrics to collect for.
After you have executed several request towards the controller, the metric service has collected their metrisc and now you can quesry and 
view them so:


- GET available metrics: http://localhost:8080/actuator/metrics
Based on the result of the first request, you can fine tune the next request so:
- GET http server response times: http://localhost:8080/actuator/metrics/http.server.requests
- GET http client response times: http://localhost:8080/actuator/metrics/http.client.requests
- GET - filtered metric for tag: /proxy/user/{id}   :  http://localhost:8080/actuator/metrics/http.server.requests?tag=method:GET&tag=uri:%2Fproxy%2Fuser%2F%7Bid%7D
- GET - filtered metric for tag: /proxy/user-with-data/{id}   :  http://localhost:8080/actuator/metrics/http.server.requests?tag=method:GET&tag=uri:%2Fproxy%2Fuser-with-data%2F%7Bid%7D




            END of experiment to customize the RestClient -  6. Metrics & Instrumentation 






            START of experiment to customize the RestClient -  7. Circuit Breaker / Bulkhead (Resilience4j Integration) 

docs: https://docs.spring.io/projectreactor/reactor-netty/docs/1.1.1/api/index.html?reactor%2Fnetty%2FMetrics.html=&utm_source=chatgpt.com

Why?
Repeated failures to your backend (e.g. DB down) should not cascade into your entire system.


How?
Spring Cloud for Resilience4j provides io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator. 
You can wrap your RestClient call with Circuit Breaker logic. For instance, define a bean for a Resilience4j 
circuit breaker and then apply it in a filter:


First, add this dependency:
```gradle
// for implementing resilience4j for 7. Circuit Breaker / Bulkhead (Resilience4j Integration)
    implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.3.0'
```

Second, define a CircuitBreaker bean in your configuration, then in your service methods call the breaker’s executeSupplier(...) 
around the blocking call:
```java
@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;
    private final Jackson2ObjectMapperBuilder jacksonBuilder;

    public ApplicationBeanConfiguration(DserviceClientProperties props,
                                        Jackson2ObjectMapperBuilder jacksonBuilder)
    {
        this.props           = props;
        this.jacksonBuilder  = jacksonBuilder;
    }
    
    
   ...

   
   /**
    * Resilience4j CircuitBreakerConfig:
    * trips if >50% failures in last 20 calls, marks slow calls >2s,
    * permits 5 calls in half-open, waits 30s when open.
    */
   @Bean
   public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig backendServiceCircuitBreakerConfig()
   {
      return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
              .failureRateThreshold(50)
              .slidingWindowSize(20)
              .slowCallRateThreshold(50)
              .slowCallDurationThreshold(Duration.ofSeconds(2))
              .permittedNumberOfCallsInHalfOpenState(5)
              .waitDurationInOpenState(Duration.ofSeconds(30))
              .build();
   }

   /**
    * Create a CircuitBreaker named "backendService" using the above config.
    */
   @Bean
   public io.github.resilience4j.circuitbreaker.CircuitBreaker backendServiceCircuitBreaker(
           io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config) {
      return io.github.resilience4j.circuitbreaker.CircuitBreaker.of("backendService", config); // where backendService is just a user-friendly name
   }

```


backendServiceCircuitBreakerConfig(): configures Resilience4j for a 50 % failure threshold over a sliding window of 20 calls, 
a slow-call threshold of 2 s, permits 5 calls in half-open, and waits 30 s when open.

backendServiceCircuitBreaker(...): creates the actual CircuitBreaker instance named "backendService" from that config.


Next, you can wrap your getById call in the controller:
```java
@RestController
@RequestMapping("/proxy")       // <— choose any prefix you like
public class UserProxyController {

   private final HttpClientInterface users;
   private io.github.resilience4j.circuitbreaker.CircuitBreaker breaker;



   public UserProxyController(HttpClientInterface users,
                              io.github.resilience4j.circuitbreaker.CircuitBreaker breaker) {
      this.users = users;
      this.breaker = breaker;
   }
   
   
   ...


   @GetMapping("/user/{id}")
   public ResponseEntity<UserDTO> getById(
           @PathVariable Long id,
           @RequestHeader(value = "X-API-Version", required = false) String ver) {

      System.out.println("printing getById, id=" + id + ", apiVersion=" + ver);

      // Decorate the supplier with the circuit breaker
      Supplier<ResponseEntity<UserDTO>> decorated =
              CircuitBreaker.decorateSupplier(breaker, () -> users.getById(id, ver));

      try {
         // Execute the call (or immediately throw CallNotPermittedException if open)
         return decorated.get();
      } catch (CallNotPermittedException ex) {
         // Circuit is open – return a 503 Service Unavailable as a simple fallback
         return ResponseEntity
                 .status(HttpStatus.SERVICE_UNAVAILABLE)
                 .body(null);
      }
   }


```
This way, only your getById is guarded by the circuit breaker, and the load balancer still handles routing between Eureka instances
as before. Let me know if you need a fallback or error handling around CallNotPermittedException!

What happens?
On each call to getWithData, Resilience4j monitors failures or slow calls.
If >50% of the last 20 calls fail or exceed 2 s, the breaker opens for 30 s, and further calls immediately 
throw CallNotPermittedException. You can catch that exception and return a fallback if you like.

With this setup:
The first few calls to /proxy/user/{id} go through to your backend via users.getById(...).
If too many failures or slow calls occur (per your config), the breaker opens and subsequent calls immediately 
throw CallNotPermittedException, which you catch and convert into a 503.
When the breaker’s “waitDurationInOpenState” elapses, Resilience4j will transition to half-open and let a few calls through 
to re-test the backend. If they succeed, the circuit closes again.



            END of experiment to customize the RestClient -  7. Circuit Breaker / Bulkhead (Resilience4j Integration)









            START of experiment to customize the RestClient -  8. Custom Load-Balancing Rules (Zone/Affinity, Metadata-based Routing)



First, in the backend-service app, make sure to have this zone added in athe application.yml:
```yml
eureka:
  client:
    service-url:
      defaultZone: "http://localhost:8761/eureka/"
    register-with-eureka: true # without it still works, but this ensures this instance is registered with Eureka as a service.
    fetch-registry: true  # without it still works, but this ensures it fetches the list of other services from Eureka registry.
  instance:
    metadata-map:
      zone: "eu-west-1a"
```
Again in the backend-service app make sure you have this new Controller endpoint:
```java

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1")
public class UserRestController {
   ...

    @Value("${eureka.instance.metadata-map.zone}")
    private String zone;
    @GetMapping("/ping")
    public Map<String,String> ping()
    {
        return Collections.singletonMap("message", "pong from zone=" + zone);
    }

```

Next, in the ApplicationBeanConfiguration class , in the return RestClient.builder() add these two converter - maybe we dont need them
but later in case we get different types of errors.
```java

// Replace JSON converters with one using your private mapper
                .messageConverters(converters -> {
        converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
        // 1) text/plain and text/*+json as raw Strings
        converters.add(new StringHttpMessageConverter());
        // 2) JSON → POJOs
        converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
        })
        ;
```

Next, add a zone, preferred zone for the loadbalancer in the application.yml in the HttpClientService app here I give you the complete
file content:

```yml
spring:
  application:
    name: "HTTPrestClientService"
  cloud:
    loadbalancer:
      zone: "eu-west-1a"

           # the name THIS app registers under
eureka:
  client:
    service-url:
      defaultZone: "http://localhost:8761/eureka"
    register-with-eureka: true
    fetch-registry: true


# ────────────────────────────────────────────────────────────────────────────
# This is your custom namespace. You could name it anything (e.g. "client:",
# "remote-api:", "user-service:", etc.). Here we chose "dservice:" to remind us
# that this is the base URL for our “dservice” backend, but we dont user it any more.
# Now we added also service-id:backend-service , because we will use Eureka to resolve by service-id, which is 'backend-service'.
dservice:
  #base-url: http://localhost:8081 - hard coded URL is no longer needed, because we will use Eureka and resolve by service-id:
  service-id: "backend-service"
  use-eureka: true
  authToken: "superSecretToken"



logging:
  level:
    org.springframework.web.service.invoker: DEBUG
    org.springframework.web.reactive.function.client.ExchangeFunctions: DEBUG
    org.springframework.cloud.loadbalancer: DEBUG
    org.springframework.cloud.loadbalancer.core: DEBUG
    org.springframework.cloud.loadbalancer.client: DEBUG
    org.springframework.cloud.loadbalancer.core.ZonePreferenceServiceInstanceListSupplier: DEBUG
    org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,beans
  endpoint:
    metrics:
      enabled: true
    prometheus:
      enabled: true
  distribution:
    # turn on histograms (so backends like Prometheus can compute quantiles)
    percentiles-histogram:
      http.server.requests: true
      http.client.requests: true
    # also compute specific percentiles
    percentiles:
      http.server.requests: [ 0.5, 0.95, 0.99 ]
      http.client.requests: [ 0.5, 0.95, 0.99 ]

```

Next, add the custom configration for the load balancer:

```java
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
```
At this point we have configured the custom load balancer for the http client service. And when making calls we will specifically ask
for the preferred zone defined here: eureka.instance.metadata-map.zone in application.yml

But how to make sure that the remote backend-service really replies only from that zone?
Create this smoke test class:
```java
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
```
Every time you start the app, it will perform a call to the backend-service and will print the response on the console.
If you get error that the remote backend-service is not available, then start the second instance. Maybe it expects at least two
instances of the backend-service:
```text
→ Performing LB call at startup…
...
→ Got: pong from zone=eu-west-1a
```

If you want to make calls via Postman client, open the client and send a GET to: http://localhost:8080/proxy/ping
Replies are:
```json
{
    "message": "pong from zone=eu-west-1a"
}
```
 


               END of experiment to customize the RestClient -  8. Custom Load-Balancing Rules (Zone/Affinity, Metadata-based Routing)







               START of experiment to customize the RestClient -  10. Uploading Large Files: Tune Buffer Size / Memory Limits


I set the condition to be able to upload files of size 2TB.

First, in the ApplicationBeanConfiguration add new modification so:
```java
.doOnConnected(conn -> {
        // 1) Let Reactor Netty install its default HTTP codec BEFORE you add yours.
        //    (By default, no need to remove anything—it’s there already.)

        // 2) Add your custom chunked writer for large uploads
        conn.addHandlerLast("chunkedWriter", new ChunkedWriteHandler());

        // 3) Re‐install your read/write timeouts
        conn.addHandlerLast(new ReadTimeoutHandler(5));
        conn.addHandlerLast(new WriteTimeoutHandler(5));
        });
```
   so that the final class ApplicationBeanConfiguration looks so:

```java

@Configuration
public class ApplicationBeanConfiguration {

    private final DserviceClientProperties props;
    private final Jackson2ObjectMapperBuilder jacksonBuilder;

    public ApplicationBeanConfiguration(DserviceClientProperties props,
                                        Jackson2ObjectMapperBuilder jacksonBuilder)
    {
        this.props           = props;
        this.jacksonBuilder  = jacksonBuilder;
    }



    /**
     *
     *  NB! THIS IS  SYNCHRONOUS, BLOCKING RestClient !!!
     */
    /**
     * Tell Spring: “When anyone injects RestClient.Builder, give them a
     * load-balanced version that uses Eureka/LoadBalancer under the hood.”
     *
     *  ---------- THIS BEAN IS CRITICAL! ----------
     *  A load-balanced RestClient.Builder that knows how to ask Eureka for "backend-service"
     */
    /**
     * A load-balanced RestClient.Builder that uses Reactor Netty
     * with custom connect/read/write timeouts.
     */
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder(MeterRegistry registry) {
        // Register Reactor Netty's metrics with Micrometer
        Metrics.addRegistry(registry);

        // 1) Build your Reactor Netty HttpClient
        //  Also add metrics enabled
        HttpClient reactorClient = HttpClient.create()
                // Enable Micrometer metrics for HTTP client (record connection/request metrics)
                .metrics(true, conn ->
                        props.getServiceId() + "-" + Function.identity().apply(conn))  // record metrics tagged by service ID
                // 2 seconds to establish TCP connection
                /**
                 * This is the maximum time (in milliseconds) allowed for the TCP handshake to complete when opening a socket to the remote server.
                 * If the TCP connection isn’t established within 2 000 ms, Netty aborts the connect attempt and you get a connection timeout error.
                 */
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                // 5 seconds max for the full HTTP response
                /**
                 * This sets a default maximum idle interval between network‐level read operations while processing the HTTP response.
                 * Concretely, once the request is sent, if more than 5 s elapse without receiving any bytes at the TCP level, the client will raise a ReadTimeoutException.
                 * Internally, Reactor Netty installs its own ReadTimeoutHandler for you when you call responseTimeout(...), and removes it when the full response is received.
                 */
                .responseTimeout(Duration.ofSeconds(5))
                // 5 seconds read or write inactivity timeout
                /**
                 *          Read timeout (new ReadTimeoutHandler(5)):
                 * This is a Netty handler that watches for read inactivity: if no data is read from the socket for 5 s at any point (even mid-response), it fires a ReadTimeoutException.
                 * Unlike the higher-level responseTimeout(), this low-level handler applies exactly 5 s of zero‐read tolerance on the channel.
                 *      -------------------
                 *          Write timeout (new WriteTimeoutHandler(5))                 *
                 * Symmetrically, this Netty handler watches for write inactivity: if HttpClient is writing a request body (or other data) and no bytes are written for 5 s, it throws a WriteTimeoutException.                 *
                 * This guards against a stalled or very slow outbound stream (e.g. uploading a large payload that stalls).
                 */
                .doOnConnected(conn -> {
                    // 1) Let Reactor Netty install its default HTTP codec BEFORE you add yours.
                    //    (By default, no need to remove anything—it’s there already.)

                    // 2) Add your custom chunked writer for large uploads
                    conn.addHandlerLast("chunkedWriter", new ChunkedWriteHandler());

                    // 3) Re‐install your read/write timeouts
                    conn.addHandlerLast(new ReadTimeoutHandler(5));
                    conn.addHandlerLast(new WriteTimeoutHandler(5));
                });

        // 2) Wrap that in the Reactor-Netty RequestFactory
        ReactorClientHttpRequestFactory factory =
                new ReactorClientHttpRequestFactory(reactorClient);

        // 3) Create a *new* ObjectMapper from the Boot‐configured builder
        ObjectMapper clientMapper = jacksonBuilder.build();
        // 4) Tweak *only* this mapper—controllers remain unaffected
        clientMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        clientMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        clientMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        clientMapper.registerModule(new JavaTimeModule());
        DateFormat df = new SimpleDateFormat("MM|dd|yyyy HH~mm~ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        clientMapper.setDateFormat(df);


        // 5) Tell RestClient to use it, and register a defaultRequest
        //    hook that adds headers before every request.
        //  Tell RestClient to use the *new* ObjectMapper, and register your default headers and error‐handling
        return RestClient.builder()
                .requestFactory(factory)
                // Add an X-Correlation-ID (new UUID each time) and an Authorization header
                .defaultRequest(spec -> {
                    // Dynamic Correlation ID per request
                    String correlationId = UUID.randomUUID().toString();
                    spec.header("X-Correlation-ID", correlationId);

                    // Static or fetched Auth token (replace with real retrieval)
                    String token = props.getAuthToken();  // assume you have this in props
                    spec.header("Authorization", "Bearer " + token);
                })
                // b) map 4xx & 5xx status to custom exceptions
                // b) map 4xx & 5xx status to custom exceptions
                .defaultStatusHandler(
                        HttpStatusCode::isError,     // test any non-2xx
                        (request, response) -> {
                            HttpStatusCode status = response.getStatusCode();

                            if (status.is4xxClientError()) {
                                // 404 → custom not-found
                                if (status == HttpStatusCode.valueOf(404)) {
                                    throw new UserNotFoundException("My Custom Error Response: User not found (404)");
                                }
                                // 401 → unauthorized
                                if (status == HttpStatusCode.valueOf(401)) {
                                    throw new UnauthorizedException("My Custom Error Response: Unauthorized (401)");
                                }
                                // other 4xx → generic client error
                                throw new ClientErrorException("My Custom Error Response: Client error: " + status.value());
                            }

                            if (status.is5xxServerError()) {
                                // any 5xx → downstream failure
                                throw new DownstreamServiceException("My Custom Error Response: Server error: " + status.value());
                            }

                            // fallback (should never happen if predicate matches only errors)
                        }
                )
                // Replace JSON converters with one using your private mapper
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
                    // 1) text/plain and text/*+json as raw Strings
                    converters.add(new StringHttpMessageConverter());
                    // 2) JSON → POJOs
                    converters.add(new MappingJackson2HttpMessageConverter(clientMapper));
                    // File streaming
                    converters.add(new ResourceHttpMessageConverter());
                    // Form data / multipart
                    converters.add(new FormHttpMessageConverter());;
                })
                ;
    }



    /**
     * No changes here — this builds your HTTP-interface proxy
     * on top of the RestClient.
     */
    @Bean
    HttpClientInterface userHttpInterface(RestClient.Builder builder) {
        String target = "http://" + props.getServiceId();

        RestClient restClient = builder
                .baseUrl(target)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(HttpClientInterface.class);
    }


    /**
     * Resilience4j CircuitBreakerConfig:
     * trips if >50% failures in last 20 calls, marks slow calls >2s,
     * permits 5 calls in half-open, waits 30s when open.
     */
    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig backendServiceCircuitBreakerConfig()
    {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(20)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Create a CircuitBreaker named "backendService" using the above config.
     */
    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreaker backendServiceCircuitBreaker(
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config) {
        return io.github.resilience4j.circuitbreaker.CircuitBreaker.of("backendService", config); // where backendService is just a user-friendly name
    }



}
```

in application.yml is this:

```yml
spring:
  application:
    name: "HTTPrestClientService"
  cloud:
    loadbalancer:
      zone: "eu-west-1a"

           # the name THIS app registers under
eureka:
  client:
    service-url:
      defaultZone: "http://localhost:8761/eureka"
    register-with-eureka: true
    fetch-registry: true


# ────────────────────────────────────────────────────────────────────────────
# This is your custom namespace. You could name it anything (e.g. "client:",
# "remote-api:", "user-service:", etc.). Here we chose "dservice:" to remind us
# that this is the base URL for our “dservice” backend, but we dont user it any more.
# Now we added also service-id:backend-service , because we will use Eureka to resolve by service-id, which is 'backend-service'.
dservice:
  #base-url: http://localhost:8081 - hard coded URL is no longer needed, because we will use Eureka and resolve by service-id:
  service-id: "backend-service"
  use-eureka: true
  authToken: "superSecretToken"



logging:
  level:
    org.springframework.web.service.invoker: DEBUG
    org.springframework.web.reactive.function.client.ExchangeFunctions: DEBUG
    org.springframework.cloud.loadbalancer: DEBUG
    org.springframework.cloud.loadbalancer.core: DEBUG
    org.springframework.cloud.loadbalancer.client: DEBUG
    org.springframework.cloud.loadbalancer.core.ZonePreferenceServiceInstanceListSupplier: DEBUG
    org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,beans
  endpoint:
    metrics:
      enabled: true
    prometheus:
      enabled: true
  distribution:
    # turn on histograms (so backends like Prometheus can compute quantiles)
    percentiles-histogram:
      http.server.requests: true
      http.client.requests: true
    # also compute specific percentiles
    percentiles:
      http.server.requests: [ 0.5, 0.95, 0.99 ]
      http.client.requests: [ 0.5, 0.95, 0.99 ]
```

The public class UserProxyController receives new endpoint:

```java
@PostMapping("/upload")
    public ResponseEntity<Void> upload() {
        // Simulate a large file by streaming a never-ending or generated payload.
        // Here’s one way to simulate 10 GB of zero-bytes without a real file:
        InputStream infiniteStream = new InputStream() {
            private long remaining = 10L * 1024 * 1024 * 1024;
            @Override
            public int read() throws IOException {
                if (remaining-- <= 0) return -1;
                return 0;
            }
        };
        Resource resource = new InputStreamResource(infiniteStream) {
            @Override
            public long contentLength() {
                return 10L * 1024 * 1024 * 1024; // pretend 10 GB
            }
        };

        return users.uploadFile(resource);
    }
```

The http client also receives new request:
```java
    @PostExchange(
            url         = "/upload",
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            accept      = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<Void> uploadFile(@RequestBody Resource file);
```

And finally the backend-service app also receives some new code:
the application.yml now defines sizes to: max-in-memory-size: 100MB, max-file-size: 2TB,  max-request-size: 2TB

```yml
server:
  port: 8081  # Make sure backend-service runs on 8081

spring:
  application:
    name: "backend-service" # must match @FeignClient(name=...)
  datasource:
    driverClassName: org.postgresql.Driver
    url: "jdbc:postgresql://localhost:5444/date_platform2"
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-timeout: 12000
  liquibase:
    #    database-change-log-table: "LIQUIBASECHANGELOG"
    #    database-change-log-lock-table: "LIQUIBASECHANGELOGLOCK"
    #    url: "jdbc:postgresql://localhost:5444/liquibase-test1"
    #    driver-class-name: "org.postgresql.Driver"
    #    user: ${DB_USERNAME}
    #    password: ${DB_PASSWORD}
    #    change-log: "classpath:db/changelog/changelog-root.xml"
    #    show-summary: summary
    #    show-summary-output: all
    #    ui-service: console
    enabled: false # disable it! Otherwise, it will create duplicate LIQUIBASECHANGELOG and LIQUIBASECHANGELOGLOCK files, which are created via the command line tool
  codec:
    max-in-memory-size: 100MB
  servlet:
    multipart:
      max-file-size: 2TB
      max-request-size: 2TB


mybatis:
  mapper-locations:
    - classpath:code_generator/mybatis/generatedClasses/mappers/*.xml
    - classpath:mappers/*.xml
  type-aliases-package: code_generator.mybatis.generatedClasses.models
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# Swagger (Springdoc OpenAPI) Configuration
springdoc:
  api-docs:
    enabled: true  # Enables OpenAPI JSON documentation
    path: /v3/api-docs # Default path for API documentation
  swagger-ui:
    enabled: true  # Enables Swagger UI
    path: /swagger-ui.html # Custom path for Swagger UI
    operationsSorter: method # Sort API methods alphabetically
    tagsSorter: alpha # Sort tags alphabetically
    displayRequestDuration: true # Show request duration in Swagger UI
  show-actuator: false # Set to true if using actuator endpoints

eureka:
  client:
    service-url:
      defaultZone: "http://localhost:8761/eureka/"
    register-with-eureka: true # without it still works, but this ensures this instance is registered with Eureka as a service.
    fetch-registry: true  # without it still works, but this ensures it fetches the list of other services from Eureka registry.
  instance:
    metadata-map:
      zone: "eu-west-1a"
```

The public class UserRestController in the backend-service also receives new endpoint, but it does not write the file in the file system, 
instead it writes it in the buffer space... nowhere! For Demo!

```java

@RestController
    @RequestMapping("/api/v1")
    public class FileUploadController {

        /**
         * A streaming endpoint that consumes the request body as Flux<DataBuffer>.
         * It never loads the whole file into memory, and simply discards (or writes)
         * each chunk as it arrives.
         */
        @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public Mono<Void> upload(Flux<DataBuffer> dataBuffers) {
            return dataBuffers
                    // e.g. write to disk, S3, etc. For demo, just release buffers:
                    .map(DataBufferUtils::release)
                    .then();
        }
    }
```


Next, 1) Create a dummy big file on disk

On Windows (PowerShell)
# Create a 2 TB sparse file
$f = New-Object System.IO.FileStream("dummy-2TB.bin",
        [System.IO.FileMode]::Create,
        [System.IO.FileAccess]::Write,
        [System.IO.FileShare]::None)
$f.SetLength(2TB)
$f.Close()

OR use the tool fsutil, so:

Windows’s fsutil file createnew always allocates the full file size on disk. What you’ve made is a “real” 1 GB file of zeroes, 
not a sparse placeholder. To get a truly sparse file (so it “appears” as 1 GB in Properties but only consumes a few bytes of 
actual space), you need to:
Create an empty file (size = 0).
Mark it sparse.
Set its valid data length to 1 GB.

You can do this from an administrator PowerShell or CMD prompt:
# 1) Create an empty file
fsutil file createnew dummy‐1GB.bin 0

# 2) Mark it as sparse
fsutil sparse setflag dummy‐1GB.bin

# 3) Tell NTFS “this file logically has 1 GB of data”
fsutil sparse setrange dummy‐1GB.bin 0 1073741824
After step 2, NTFS will treat the file as sparse.

After step 3, the file’s “Size” (logical length) is 1 073 741 824 bytes (1 GB), but its “Size on disk” (actual allocated clusters) 
will be very small—just enough to store the sparse‐file metadata.
Now when you point Postman at dummy-1GB.bin, it will stream a 1 GB upload without you actually consuming 1 GB of disk space.


Finally send a POST request via postman: http://localhost:8080/proxy/upload
In the body choose binary and point the FileUpload from this computer.

RESULT: it starts well with 1GB file upload, but at some point in the the HTTP client with the RestClient throws OOM error:

```cmd
java.lang.OutOfMemoryError: Java heap space
```

That error is not resolved, because it requires too many, many changes of the ApplicationBeanConfiguration file and I want to avoid
this. But theoretically, if you need to change it, you have two options:

1. Disable Request-Body Buffering via Apache HTTP Components
   By swapping to HttpComponentsClientHttpRequestFactory (which uses Apache HttpClient) and turning off its request-buffering,
you’ll stream straight from disk (or your simulated stream) to the socket:
NB! Very strange ! I had no idea that using websockets will solve it so easily!
```java
// 1) Create a low-level HttpClient
CloseableHttpClient httpClient = HttpClientBuilder.create()
    .disableCookieManagement()
    .disableAutomaticRetries()
    .build();

// 2) Use it in the Spring factory, disabling the in-memory buffer
HttpComponentsClientHttpRequestFactory factory =
    new HttpComponentsClientHttpRequestFactory(httpClient);
factory.setBufferRequestBody(false);  // <— avoids FastByteArrayOutputStream

// 3) Plug that into your RestClient builder
@Bean @LoadBalanced
RestClient.Builder restClientBuilder(DserviceClientProperties props, Jackson2ObjectMapperBuilder jacksonBuilder) {
    return RestClient.builder()
        .requestFactory(factory)
        // … your other customizations …
        ;
}


```
With bufferRequestBody=false, Apache HttpClient will employ HTTP/1.1 chunked-streaming or fixed-length streaming directly from 
the InputStreamResource, never copying everything into a single huge byte array.


2. Switch to Spring’s Reactive WebClient (Stream-Friendly by Default)
   Spring’s non-blocking WebClient is designed for streaming large payloads without ever buffering the full body in the heap.
You’d wire it up like this:
```java
@Bean
WebClient webClient(ReactorNettyHttpClientConnector connector, DserviceClientProperties props) {
    return WebClient.builder()
        .baseUrl("http://" + props.getServiceId())
        .clientConnector(connector)
        .defaultHeader("Authorization", "Bearer " + props.getAuthToken())
        .build();
}

// …

// When you need to upload:
webClient.post()
    .uri("/api/v1/upload")
    .contentType(MediaType.APPLICATION_OCTET_STREAM)
    .body(BodyInserters.fromResource(resource))   // streams the resource
    .retrieve()
    .toBodilessEntity()
    .block();


```
Behind the scenes, WebClient uses Reactor Netty’s streaming APIs to push each chunk as it’s read, never collecting gigabytes
in a single heap buffer docs.spring.io.


Bonus Tip: Force Chunked Transfer by Hiding Content Length
If you really want to stick with RestClient + ReactorClientHttpRequestFactory, you can trick it into chunked mode 
(which often avoids full buffering) by returning -1 from your InputStreamResource.contentLength(). That way the client won’t 
set a Content-Length header and should default to chunked transfer. In practice, though, the two approaches above are more robust.


That's it with this point - 10. Uploading Large Files: Tune Buffer Size / Memory Limits. 

Let's move on to the next one!




               END of experiment to customize the RestClient -  10. Uploading Large Files: Tune Buffer Size / Memory Limits





I have a list of features to customize in RestClient, like these below, but I implemented only up to 10th, because from the
gained experience it seems that some features are much better to be implemented with WebClient. Therefore, I now stop the experiments with
the RestClient and will open a new project for Http Client with WebClient.

1. Adjusting Timeouts (Connect, Read, Write)
By default, the underlying HTTP client (used by Spring’s RestClient) may have timeouts that are too long (or too short) for your 
environment. If you expect slow endpoints or want to fail fast, customizing connect/read/write timeouts is critical.

2. Adding a Retry/Backoff Strategy
Networks can be flaky. If your backend occasionally returns 5xx or times out, you might want to automatically retry a few times 
before giving up (with exponential backoff).

3. Inserting Custom Headers (e.g., Correlation ID, Auth Token)
In a microservice world, you often want to propagate a “correlation ID” (for tracing across services) or inject an 
Authorization: Bearer <token> header automatically on every request.

4. Custom Error Decoding & Mapping to Exceptions
By default, non‐2xx responses are turned into a generic RestClientResponseException. You might want to map, say, a 404 to a 
UserNotFoundException or a 401 to UnauthorizedException.

5. Custom JSON (Jackson) Configuration
Suppose you want to use a custom ObjectMapper—for example, enabling a special date format, ignoring unknown fields, 
or registering a module (e.g. JSR310, Kotlin, Protobuf). You need to tell the RestClient to use your ObjectMapper when 
serializing/deserializing request and response bodies.

6. Metrics & Instrumentation
In production, you’ll want to track how many calls you’re making, response times, error rates, etc. You can hook in Micrometer or 
Spring Boot’s MeterRegistry and record metrics around every request.

7. Circuit Breaker / Bulkhead (Resilience4j Integration)
Repeated failures to your backend (e.g. DB down) should not cascade into your entire system. A circuit breaker lets you “trip” 
after N failures and avoid hammering a bad endpoint.

8. Custom Load-Balancing Rules (Zone/Affinity, Metadata-based Routing)
By default, Spring Cloud LoadBalancer uses a simple round-robin. Sometimes you want:
Zone Affinity: Prefer instances in the same zone/region as the client.
Metadata Filtering: Only use instances that have a specific metadata label (e.g. version=v2).
Weighting: Give some instances higher “weight” if they’re more powerful.

9. Circuit-Breaker with Fallback to a Local Stub
Sometimes, instead of throwing an exception when the breaker is open, you want to return a default “fallback” response 
(e.g. cached data, empty user, placeholder).

10. Uploading Large Files: Tune Buffer Size / Memory Limits
If you need to send or receive large payloads (e.g. >10 MB), the default in-memory buffering may not suffice. You might want to raise
the max in-memory size or switch to streaming chunks.

11. Proxy or Custom SSL (TrustStore) Configuration
In corporate environments, you sometimes have to route outgoing HTTP calls through an HTTP proxy (say, corporate-proxy:8080).

12. Request/Response Logging (Full Body + Headers)
While debugging, you often want to log every outgoing request (method, URI, headers, body) and every incoming response 
(status, headers, body). Spring’s ExchangeFilterFunction can do this, without you sprinkling logs in every controller.

13. Dynamic Base URL Resolution (Non-Eureka Fallback)
You currently use Eureka (serviceId = "backend-service") in your HttpClientInterface. But you might want a fallback to a fixed URL 
if Eureka is down (or if the user configures some base-url in a properties file for testing).

14. Custom Cookie Management
If your backend sets a session cookie (e.g. Set-Cookie: SESSION=abc123; Path=/; HttpOnly), you may need to send that cookie 
automatically on subsequent calls (sticky session).

15. Defining Custom Error Handling Strategies by Status Family
Maybe you want to treat all 4xx as “client failures” but still parse the body, while all 5xx should throw an exception immediately 
(and never convert into a DTO).

16. Custom DNS Resolution / Hostname Verification
If you need to bypass DNS resolution (e.g. to hardcode an IP → hostname mapping for testing), or if you need to skip hostname 
verification (for internal certs).

17. Conditional Logic Based on Request Path or Headers
Suppose you want different behavior when calling /user/{id} vs /user-with-data/{id}. For instance, maybe calls to /user-with-data/… 
must carry an extra header like X-Internal-Auth: secret, whereas /user/… should not.

18. Capturing Response Cookies and Propagating Them
If your backend returns Set-Cookie: SESSION=xyz on one call, you may want to store and reuse that in subsequent calls 
(similar to “sticky sessions” in #11 but here perhaps for a different domain).

19. Bulkhead (Thread Pool) Isolation
If your HTTP calls are expensive (e.g. large payload, slow DB), you may not want them to exhaust your main reactive event loops. 
You can isolate them in a dedicated thread pool (“bulkhead”) so that a spike in these calls doesn’t starve CPU for other traffic.

20. Custom SSL Pinning (Pin a Specific Certificate Fingerprint)
For maximum security, you might want to verify that the server’s certificate matches a known fingerprint (public-key pinning), 
not just that it’s signed by the CA in your trust store.

21. Customizing HTTP/2 or HTTP/1.1 Features
You might want to force HTTP/2 (for multiplexing) or explicitly disable HTTP/2 if your server doesn’t support it (and your client 
negotiates it automatically). You can also tweak “keep-alive” settings.

22. Custom Request Throttling (Rate Limiting)
To avoid overwhelming your backend (or to respect the third-party’s rate limits), you might want to throttle outgoing 
requests to, say, 10 QPS.

23. Custom Connection Pool Settings
By default, Reactor Netty’s connection pool size might be too small for high concurrency. You can tune max connections, pending 
acquisition, idle time, etc.

24. Custom Codec for XML, YAML, or Protobuf
Maybe you’re talking to a legacy service that uses XML or a partner that uses Protobuf. You need to register an additional codec 
so RestClient can automatically marshal/unmarshal.

25. Conditional Circuit Breaker Per Endpoint
Perhaps you trust /user/{id} to be quick, but /user-with-data/{id} is slow (joins multiple tables). You might want a tighter 
circuit breaker on the slow path (e.g., trip after 3 failures), but leave the simple GET alone.

26. Dynamic Connection Pool Adjustment at Runtime
Maybe you want to throttle performance during off-peak hours (e.g. only 10 connections at night) and allow more during business 
hours (e.g. 100 connections). You could expose an actuator endpoint to tweak connection pool sizes on the fly.

27. Per-Client Logging Level (Wiretap)
If you want to log TCP-level details (headers, wire bytes), Reactor Netty’s “wiretap” can dump low-level frames. Useful only 
when debugging SSL handshakes or subtle protocol issues.

28. Implementing a Custom “Fallback to Cache” on 404
If your user data is sometimes stale, you want to first check a local cache. If the remote call returns 404, then you serve from 
the cache. Otherwise, you return the remote data and repopulate cache.

29. Request Batching (Combining Multiple Calls into One)
If you have to fetch user A, B, and C in quick succession, it’s often more efficient to call /api/v1/users?ids=A,B,C once rather 
than 3 separate /user/{id} calls. You can implement a small “batcher” layer on top of your RestClient.

30. Custom Authorization Flow (OAuth2 Client Credentials)
If your backend is secured by OAuth2, you need to fetch an access token from an auth server (e.g. Keycloak) and attach it as 
Authorization: Bearer <token> on every request. The token needs automatic refresh before expiry.















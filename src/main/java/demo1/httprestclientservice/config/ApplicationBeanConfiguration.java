package demo1.httprestclientservice.config;

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
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;                // RestClient itself
import org.springframework.web.client.support.RestClientAdapter;  // ← correct import
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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



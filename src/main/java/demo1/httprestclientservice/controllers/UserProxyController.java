package demo1.httprestclientservice.controllers;

import demo1.httprestclientservice.DTOs.db.UserDTO;
import demo1.httprestclientservice.DTOs.db.UserDbDTO;
import demo1.httprestclientservice.HttpClientInterface;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

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

    @PostMapping("/create-new-user")
    public ResponseEntity<UserDbDTO> create(@RequestBody UserDbDTO body) {
        return users.create(body);   // simply forward
    }

//    @GetMapping("/user/{id}")
//    public ResponseEntity<UserDTO> getById(@PathVariable Long id,
//                                           @RequestHeader(value = "X-API-Version", required = false) String ver) {
//        System.out.println("printing getById");
//        ResponseEntity<UserDTO> user = users.getById(id, ver);
//        return user;
//    }

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



    @GetMapping("/user-with-data/{id}")
    public ResponseEntity<UserDbDTO> getWithData(@PathVariable Long id,
                                                 @RequestHeader Map<String,String> headers) {
        System.out.println("printing getWithData");
        ResponseEntity<UserDbDTO> userWithData = users.getWithData(id, headers);
        return userWithData;
    }

    @GetMapping("/proxy-http-status/{code}")
    public ResponseEntity<String> getCustomErrorResponse(@PathVariable int code) {
        System.out.println("getting getCustomErrorResponse");
        ResponseEntity<String> responseEntity = users.proxyGetCustomErrorResponse(code);
        return responseEntity;
    }

    @GetMapping("/ping")
    public Map<String, String> getPing() {
        System.out.println("pinging");
        Map<String, String> ping = users.ping();
        return ping;
    }


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
}

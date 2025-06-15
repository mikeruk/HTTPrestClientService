package demo1.httprestclientservice;


import demo1.httprestclientservice.DTOs.db.UserDTO;
import demo1.httprestclientservice.DTOs.db.UserDbDTO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.*;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Map;

@HttpExchange(url = "/api/v1", accept = MediaType.APPLICATION_JSON_VALUE)
public interface HttpClientInterface {

    @PostExchange("/create-new-user")
    ResponseEntity<UserDbDTO> create(@RequestBody UserDbDTO body);

    @GetExchange("/user/{id}")
    ResponseEntity<UserDTO> getById(@PathVariable Long id,
                    @RequestHeader(name = "X-API-Version", required = false) String apiVersion);

    @GetExchange("/user-with-data/{id}")
    ResponseEntity<UserDbDTO> getWithData(@PathVariable Long id,
                                          @RequestHeader Map<String,String> dynamicHeaders);

    @GetExchange("/http-status/{code}")
    ResponseEntity<String> proxyGetCustomErrorResponse(@PathVariable int code);

    @HttpExchange(method = "GET", url = "/ping", accept = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> ping();

    @PostExchange(
            url         = "/upload",
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            accept      = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<Void> uploadFile(@RequestBody Resource file);
}


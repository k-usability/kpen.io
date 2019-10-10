package io.kpen.web;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.kpen.util.Tx.run;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3010", "https://kpen.io"})
@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class APIController {

    @PostMapping(value = "/public")
    public Message publicEndpoint() {
        return run(ctx ->
                new Message("All good. You DO NOT need to be authenticated to call /api/public."));
    }

    @Data
    public class Message {
        private final String message;
    }
}

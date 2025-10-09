package com.example.flserver;

import jdk.jfr.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FLController {
    FLService service;

    public FLController(FLService service) {
        this.service = service;
    }

    @GetMapping("/init")
    public String init(){
        service.initializeAndSendToEdge();
        return "initialized";
    }
}

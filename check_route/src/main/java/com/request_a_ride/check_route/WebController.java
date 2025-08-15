package com.request_a_ride.check_route;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "check_route.html"; // Matches index.html in templates
    }
}
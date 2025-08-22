package com.request_a_ride.driver_route;

import com.request_a_ride.check_route.WebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Controller
@RequestMapping("/driver")
public class DriverRouteController extends WebController {

    @GetMapping("/route")
    public String driverRoute(Model model) {
        try {
            // Read driver coordinates from file
            DriverCoordinates coords = readDriverCoordinates();
            
            // Add coordinates to model for the template
            model.addAttribute("driverLat", coords.driverLat);
            model.addAttribute("driverLng", coords.driverLng);
            model.addAttribute("userLat", coords.userLat);
            model.addAttribute("userLng", coords.userLng);
            model.addAttribute("distance", coords.distance);
            model.addAttribute("driverId", coords.driverId);
            
            return "driver_route.html"; // New template for driver route
            
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to parent's functionality
            return super.home();
        }
    }
    
    private DriverCoordinates readDriverCoordinates() throws IOException {
        // Read all driver route info from the file created by DriverRouteApplication
        BufferedReader reader = new BufferedReader(new FileReader("driver_route_info.txt"));
        
        double distance = Double.parseDouble(reader.readLine());
        int driverId = Integer.parseInt(reader.readLine());
        double driverLat = Double.parseDouble(reader.readLine());
        double driverLng = Double.parseDouble(reader.readLine());
        double userLat = Double.parseDouble(reader.readLine());
        double userLng = Double.parseDouble(reader.readLine());
        
        reader.close();
        
        return new DriverCoordinates(distance, driverId, driverLat, driverLng, userLat, userLng);
    }
    
    // Inner class to hold coordinate data
    private static class DriverCoordinates {
        double distance;
        int driverId;
        double driverLat;
        double driverLng;
        double userLat;
        double userLng;
        
        DriverCoordinates(double distance, int driverId, double driverLat, double driverLng, 
                         double userLat, double userLng) {
            this.distance = distance;
            this.driverId = driverId;
            this.driverLat = driverLat;
            this.driverLng = driverLng;
            this.userLat = userLat;
            this.userLng = userLng;
        }
    }
}
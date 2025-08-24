package com.request_a_ride.driver_route;

import com.request_a_ride.check_route.CheckRouteApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Scanner;

@SpringBootApplication
public class DriverRouteApplication extends CheckRouteApplication {

    public static void main(String[] args) throws Exception {
        Scanner scn = new Scanner(System.in);
        
        // Read driver coordinates from nearest_driver.txt (created by your ride request code)
        BufferedReader driverReader = new BufferedReader(new FileReader("nearest_driver.txt"));
        
        double distance = Double.parseDouble(driverReader.readLine());
        int driverId = Integer.parseInt(driverReader.readLine());
        double driverLat = Double.parseDouble(driverReader.readLine());
        double driverLng = Double.parseDouble(driverReader.readLine());
        driverReader.close();

        Thread.sleep(1000);
        System.out.println("Driver found!");
        Thread.sleep(1000);
        System.out.println("Driver ID: " + driverId);
        Thread.sleep(1000);
        System.out.println("Driver Location: " + driverLat + ", " + driverLng);
        Thread.sleep(1000);
        System.out.println("Distance to driver: " + distance + " km");
        Thread.sleep(1000);
        
        // Read user coordinates from existing pick_up_coordinates.txt
        BufferedReader userCoordReader = new BufferedReader(new FileReader("pick_up_coordinates.txt"));
        String userCoordString = userCoordReader.readLine();
        userCoordReader.close();
        
        // The format is "18.941%2C72.8351" so we need to decode it
        String decodedUserCoords = userCoordString.replace("%2C", ",");
        System.out.println("User coordinates: " + decodedUserCoords);
        
        // Create driver coordinate string in same format  
        String driverCoordString = driverLat + "," + driverLng;
        
        System.out.println("Getting route from driver to user using Ola Maps API...");
        
        // Get directions from driver to user using Ola Maps Directions API
        Process driver_to_user_directions = new ProcessBuilder(
                "curl",
                "https://api.olamaps.io/routing/v1/directions?origin=" + driverCoordString + "&destination=" + decodedUserCoords + "&mode=driving&api_key=Uer9nnXtI726LogTusu4W3ebgiCXGyumxqwCaXhn",
                "--request", "POST"
        ).start();
        
        // Write directions JSON to file
        BufferedReader directionsJsonInput = new BufferedReader(new InputStreamReader(driver_to_user_directions.getInputStream()));
        BufferedWriter directionsJsonOutput = new BufferedWriter(new FileWriter("driver_to_user_directions_raw.txt"));
        
        String directionsJsonLine = directionsJsonInput.readLine();
        while(directionsJsonLine != null) {
            directionsJsonOutput.write(directionsJsonLine);
            directionsJsonOutput.newLine();
            directionsJsonLine = directionsJsonInput.readLine();
        }
        directionsJsonOutput.close();

        Thread.sleep(1000);
        System.out.println("Processing route data...");
        
        // Extract route coordinates and create JavaScript file (exactly like your original)
        new File("driver_route_coordinates.js").delete();
        Process driver_route_formatted = new ProcessBuilder(
                "bash",
                "-c",
                "jq -r '[.. | .start_location? | select(.lat and .lng) | [.lng, .lat]] \n" +
                        "| \"const driverRouteCoordinates = \\(.);\"' driver_to_user_directions_raw.txt > /Users/vansh/Desktop/UBER/check_route/src/main/resources/static/driver_route_coordinates.js\n"
        ).start();
        
        driver_route_formatted.waitFor();
        
        // Create info file for the web controller
        BufferedWriter driverInfoWriter = new BufferedWriter(new FileWriter("driver_route_info.txt"));
        driverInfoWriter.write(String.valueOf(distance)); // Distance
        driverInfoWriter.newLine();
        driverInfoWriter.write(String.valueOf(driverId)); // Driver ID
        driverInfoWriter.newLine();
        driverInfoWriter.write(String.valueOf(driverLat)); // Driver Lat
        driverInfoWriter.newLine();
        driverInfoWriter.write(String.valueOf(driverLng)); // Driver Lng
        driverInfoWriter.newLine();
        
        // Parse and write user coordinates
        String[] userCoords = decodedUserCoords.split(",");
        driverInfoWriter.write(userCoords[0]); // User Lat
        driverInfoWriter.newLine();
        driverInfoWriter.write(userCoords[1]); // User Lng
        driverInfoWriter.close();

        Thread.sleep(1500);
        System.out.println("All files generated successfully!");
        Thread.sleep(1500);
        System.out.println("Starting Spring Boot server...");
        
        // Start Spring Boot application
        SpringApplication.run(DriverRouteApplication.class, args);
        Thread.sleep(1500);
        
        // Open the driver route page
        System.out.println("Opening driver route in browser...");
        Process process = new ProcessBuilder("open", "http://localhost:8080/driver/route").start();
        scn.nextLine();
    }
}
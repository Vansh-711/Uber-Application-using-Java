import com.request_a_ride.check_route.CheckRouteApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

import ch.hsr.geohash.GeoHash;

public class request_a_ride extends Thread{
    request_a_ride() throws Exception{
        Scanner scn = new Scanner(System.in);

        //1. just checking the path on the map
        //call check_route spring boot

        Thread check_route_thread = new Thread(() -> {
            try {
                CheckRouteApplication.main(new String[]{});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        check_route_thread.setDaemon(true);//ensures that this new thread also ends with the end of main thread.
        check_route_thread.start();

        Thread.sleep(4000);

        System.out.println("Route service starting... Check your path now");
        System.out.println("Press enter when you have checked the path : ");
        scn.nextLine();

        System.out.println("\nMap Checked");

        //this will clear the port
        Process killing_on_8080 = new ProcessBuilder("curl",
                "-X",
                "POST",
                "http://localhost:8080/actuator/shutdown").start();
        Thread.sleep(2000);
        System.out.println("Proceeding ahead");


        //2. driver search starts
        BufferedReader user_location_br = new BufferedReader(new FileReader("pick_up_coordinates.txt"));
        String user_location = user_location_br.readLine();
        user_location.replace("%2C" , ",");
        String[] user_location_arr = user_location.split(",");


        //now we will convert user location into geohash code
        Double user_location_lat = Double.parseDouble(user_location_arr[0]);
        Double user_location_lng = Double.parseDouble(user_location_arr[1]);

        String user_location_geohash = GeoHash.geoHashStringWithCharacterPrecision(user_location_lat,user_location_lng,6);
        System.out.println("Geohash: " + user_location_geohash);

















    }
}

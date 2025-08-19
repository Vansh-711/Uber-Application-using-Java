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

        //by the we wait here
        //we can start the new thread that will create a bst from cities name.

        //this will create the bst in background


        //may have to make a change here
        check_route_thread.join();

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
    }
}

import com.request_a_ride.check_route.CheckRouteApplication;

import java.io.*;
import java.nio.Buffer;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;

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
        //we will start a thread that will create a hash map of the h3 indexes of the city
        hash_map_h3_index hash_map_1 = new hash_map_h3_index();
        hash_map_1.start();
        //this will create the hashmap in background


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


        //2. driver search starts

        //now we will use H3 first to find user city and then to find the nearest drivers
        hash_map_1.join();
        System.out.println("Loaded cities: " + hash_map_1.hash_map_city.size());

        //1.Finding user city
        //the hashmap of every city hexcode is stored already
        BufferedReader user_curr_br = new BufferedReader(new FileReader("pick_up_coordinates.txt"));

        String[] user_curr_line_arr = user_curr_br.readLine().split("%2C");

        Double user_curr_lat = Double.parseDouble(user_curr_line_arr[0]);
        Double user_curr_lng = Double.parseDouble(user_curr_line_arr[1]);

        //making h3 core
        H3Core h3 = H3Core.newInstance();

        //making hexcode of user
        long user_h3_index = h3.latLngToCell(user_curr_lat , user_curr_lng , 6);

        //we will search till 3rd ring

        BufferedWriter city_name_bw = new BufferedWriter(new FileWriter("/Users/vansh/Desktop/UBER/user_current_city.txt"));

        city_name_bw.write(find_city(h3 , user_h3_index , hash_map_1));
        city_name_bw.flush();

        //now we have a file named user_current_city.txt which contains the user current city

    }

    String find_city(H3Core h3, long user_h3_index, hash_map_h3_index hash_map_1) throws IOException {
        int k = 0;
        while(k <= 8){
            List<Long> hexes = h3.gridDisk(user_h3_index, k);
            for(Long hex : hexes){
                if(hash_map_1.hash_map_city.containsKey(hex)){
                    return hash_map_1.hash_map_city.get(hex);
                }
            }
            k++;
        }
        return "City not found";
    }
}

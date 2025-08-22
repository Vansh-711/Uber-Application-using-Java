import com.request_a_ride.check_route.CheckRouteApplication;

import java.io.*;
import java.nio.Buffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

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
        long user_h3_index_res6 = h3.latLngToCell(user_curr_lat , user_curr_lng , 6);//edge length : 3.229482772 km

        //we will search till 3rd ring

        BufferedWriter city_name_bw = new BufferedWriter(new FileWriter("/Users/vansh/Desktop/UBER/user_current_city.txt"));

        city_name_bw.write(find_city(h3 , user_h3_index_res6 , hash_map_1));
        city_name_bw.flush();

        //now we have a file named user_current_city.txt which contains the user current city


        //2.Finding drivers near user

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");
        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("something else in connection ");


        BufferedReader user_city_br = new BufferedReader(new FileReader("user_current_city.txt"));
        String user_city = user_city_br.readLine();

        String driver_in_user_city_query = "select id , latitude , longitude from drivers where city = ?";
        PreparedStatement driver_in_user_city_ps = con.prepareStatement(driver_in_user_city_query);

        driver_in_user_city_ps.setString(1 , user_city);

        ResultSet driver_in_user_city_result_set = driver_in_user_city_ps.executeQuery();

        //now we have a result set of all the drivers in the city

        //we will go through them all and give random position to driver in their city

        ProcessBuilder driver_in_user_city_pb = new ProcessBuilder("bash", "-c",
                "curl -s \"https://nominatim.openstreetmap.org/search?city=" + user_city + "&format=json\" | jq -r '.[0].boundingbox | join(\",\")'");

        Process driver_in_user_city_p = driver_in_user_city_pb.start();

        BufferedReader driver_in_user_city_boundary_br = new BufferedReader(new InputStreamReader(driver_in_user_city_p.getInputStream()));
        String driver_in_user_city_boundary = driver_in_user_city_boundary_br.readLine();
        //now we have city boundary stored in this string

        // Check if API returned valid data
        if (driver_in_user_city_boundary == null || driver_in_user_city_boundary.trim().isEmpty() ||
                driver_in_user_city_boundary.equals("null")) {
            System.out.println("Error : No boundary for city : " + user_city);
        }
        else {

            int exitCode = driver_in_user_city_p.waitFor();
            if (exitCode != 0) {
                System.out.println("Error in calling api with exit code : " + exitCode);
            }

            //transforming string into arr
            String[] driver_in_user_city_boundary_arr = driver_in_user_city_boundary.split(",");

            //saving the boundary of the city
            double min_lat_user_city = Double.parseDouble(driver_in_user_city_boundary_arr[0]);
            double max_lat_user_city = Double.parseDouble(driver_in_user_city_boundary_arr[1]);
            double min_lng_user_city = Double.parseDouble(driver_in_user_city_boundary_arr[2]);
            double max_lng_user_city = Double.parseDouble(driver_in_user_city_boundary_arr[3]);


            //we will store all the drivers in the city with their random location inside linked list
            //we will now store this data in our own linked list with complex nodes
            ll_drivers drivers_in_user_city_ll = new ll_drivers();

            Random rand = new Random();
            while (driver_in_user_city_result_set.next()) {
                //generating random location in city
                double lat = min_lat_user_city + (max_lat_user_city - min_lat_user_city) * rand.nextDouble();
                double lng = min_lng_user_city + (max_lng_user_city - min_lng_user_city) * rand.nextDouble();

                int driver_in_user_city_id = driver_in_user_city_result_set.getInt(1);
                drivers_in_user_city_ll.push(driver_in_user_city_id, lat, lng);
            }

            //now we have a linked that contains all the drivers in the city and there random location in the city
            //we will now use that info and use h3 to find nearby drivers

            //we will have to create new hexcode of resolution 7 for finding neraby drivers of user
            long user_h3_index_res7 = h3.latLngToCell(user_curr_lat, user_curr_lng, 7);// edge length : 1.22062975 km

            //looping to save all drivers hexcode into a hashmap of lists for instant searching
            HashMap<Long, List<ll_nodes>> drivers_by_hex_hashmap = new HashMap<>();

            ll_nodes shifter = drivers_in_user_city_ll.head;

            while (shifter != null) {
                long driver_h3 = h3.latLngToCell(shifter.random_lat, shifter.random_lng, 7);

                drivers_by_hex_hashmap.computeIfAbsent(driver_h3, k -> new ArrayList<>()).add(shifter);
                shifter = shifter.next;
            }
            //we have saved the hexcode of all drivers in hashmap

            //searching with gridDisk
            int k = 0;

            double shortest_distance_to_user = Double.MAX_VALUE;
            ll_nodes nearest_driver = null;

            while (k <= 12) {
                // Get all hexes within distance k from user's hex
                List<Long> hexes = h3.gridDisk(user_h3_index_res7, k);

                boolean found_in_this_k = false; // flag for this k-ring

                for (Long hex : hexes) {
                    // Check if there are drivers in this hex
                    if (drivers_by_hex_hashmap.containsKey(hex)) {
                        List<ll_nodes> nearby_drivers = drivers_by_hex_hashmap.get(hex);

                        for (ll_nodes driver : nearby_drivers) {
                            // Calculate Haversine distance between user and driver
                            double distance = haversine(user_curr_lat, user_curr_lng, driver.random_lat, driver.random_lng);

                            if (distance < shortest_distance_to_user) {
                                shortest_distance_to_user = distance;
                                nearest_driver = driver;
                            }
                        }

                        // Mark that we found drivers in this k-ring
                        found_in_this_k = true;
                    }
                }

                // If we found drivers in this ring, stop loop
                if (found_in_this_k) {
                    break;
                }

                k++;
            }
            if (k > 12) {
                System.out.println("No drivers found in k-rings, searching all drivers...");

                // Fallback: search all drivers regardless of hex
                ll_nodes shifter_2 = drivers_in_user_city_ll.head;
                while (shifter_2 != null) {
                    double distance = haversine(user_curr_lat, user_curr_lng,
                            shifter_2.random_lat, shifter_2.random_lng);

                    if (distance < shortest_distance_to_user) {
                        shortest_distance_to_user = distance;
                        nearest_driver = shifter_2;
                    }
                    shifter_2 = shifter_2.next;
                }
            }

            if(nearest_driver != null) {
                BufferedWriter nearest_driver_br = new BufferedWriter(new FileWriter("nearest_driver.txt"));

                nearest_driver_br.write(Double.toString(shortest_distance_to_user));
                nearest_driver_br.newLine();

                nearest_driver_br.write(Integer.toString(nearest_driver.id));
                nearest_driver_br.newLine();

                nearest_driver_br.write(Double.toString(nearest_driver.random_lat));
                nearest_driver_br.newLine();

                nearest_driver_br.write(Double.toString(nearest_driver.random_lng));

                nearest_driver_br.flush();
            }
            else{
                System.out.println("No drivers available in the city");
            }
            //now we have the shortest distance to nearest driver and also the details of that driver in nearest_driver.txt
            //we will use that to draw a line from that driver to user in spring boot


        }
    }

    double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c; // distance in km
    }

    String find_city(H3Core h3, long user_h3_index, hash_map_h3_index hash_map_1) throws IOException {
        int k = 0;
        while(k <= 4){
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
class ll_drivers{
    ll_nodes head;
    void push(int id , double lat , double lng){
        if(head == null){
            ll_nodes temp = new ll_nodes(id , lat , lng);
            head = temp;
        }
        else{
            ll_nodes temp = head;
            while(temp.next != null){
                temp = temp.next;
            }
            ll_nodes neww = new ll_nodes(id , lat , lng);
            temp.next = neww;
        }
    }
}
class ll_nodes{
    ll_nodes next;
    int id;
    double random_lat;
    double random_lng;
    ll_nodes(){}

    ll_nodes(int id , double lat , double lng){
        this.id = id;
        this.random_lat = lat;
        this.random_lng = lng;
    }
}

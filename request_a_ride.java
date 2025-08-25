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

        killing_on_8080.waitFor();
        Thread.sleep(3000);

        System.out.println("\nProceeding ahead..");


        //2. driver search starts
        Thread.sleep(1000);
        System.out.print("Driver search starts ..");
        Thread.sleep(3000);

        //now we will use H3 first to find user city and then to find the nearest drivers
        hash_map_1.join();
        System.out.println("Loaded cities: " + hash_map_1.hash_map_city.size());

        //2.1 Finding user city
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


        //2.2 Finding drivers near user

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");
        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("error in connection ");


        BufferedReader user_city_br = new BufferedReader(new FileReader("user_current_city.txt"));
        String user_city = user_city_br.readLine();

        // query to take data from database about all drivers in city

        String driver_in_user_city_query = "select id , latitude , longitude , vehicle from drivers where city = ?";
        PreparedStatement driver_in_user_city_ps = con.prepareStatement(driver_in_user_city_query);

        driver_in_user_city_ps.setString(1 , user_city);

        ResultSet driver_in_user_city_result_set = driver_in_user_city_ps.executeQuery();

        //now we have a result set of all the drivers in the city

        //we will go through them all and give random position to driver in their city

        //mapping the city boundaries
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
                String vehicle_type = driver_in_user_city_result_set.getString(4);

                drivers_in_user_city_ll.push(driver_in_user_city_id, lat, lng, vehicle_type);
            }

            //now we have a linked that contains all the drivers in the city and there random location in the city
            //we will now use that info and use h3 to find nearby drivers

            //we will have to create new hexcode of resolution 7 for finding neraby drivers of user
            long user_h3_index_res7 = h3.latLngToCell(user_curr_lat, user_curr_lng, 7);// edge length : 1.22062975 km

            // Storage for nearest driver of each vehicle type
            HashMap<String, ll_nodes> nearest_drivers_by_type = new HashMap<>();
            HashMap<String, Double> shortest_distances_by_type = new HashMap<>();

            String[] vehicle_types = {"Two-wheeler", "Rickshaw", "Normal cab", "Cab XL", "Premium Cab"};
            for(String type : vehicle_types) {
                shortest_distances_by_type.put(type, Double.MAX_VALUE);
            }

            //looping to save all drivers by hex into hexcode into a hashmap of lists for instant searching
            HashMap<Long, List<ll_nodes>> drivers_by_hex_hashmap = new HashMap<>();

            //saving ghe hexcode of all drivers in user_city into hashmap for rapid searching
            ll_nodes shifter = drivers_in_user_city_ll.head;

            while (shifter != null) {
                long driver_h3 = h3.latLngToCell(shifter.random_lat, shifter.random_lng, 7);

                //now we will check if in the hashmap, in the index of driver_h3, if there a arraylist list of nodes exists or not, if it exists then add into arraylist and if not then create a new arraylist.
                //either we can do this
                List<ll_nodes> list = drivers_by_hex_hashmap.get(driver_h3);
                if(list == null){
                    list = new ArrayList<>();
                    drivers_by_hex_hashmap.put(driver_h3 , list);
                }
                list.add(shifter);

                //or the below commented line, below one is more efficient
                //drivers_by_hex_hashmap.computeIfAbsent(driver_h3, k -> new ArrayList<>()).add(shifter);
                shifter = shifter.next;
            }
            //we have saved the hexcode of all drivers in hashmap

            //searching with gridDisk for all 5 vehicle types
            int k = 0;

            while (k <= 12) {
                // Get all hexes within distance k from user's hex
                List<Long> hexes = h3.gridDisk(user_h3_index_res7, k);

                boolean found_in_this_k = false; // checking if we found driver in the k-ring

                for (Long hex : hexes) {
                    // Check if there are drivers in this hex
                    if (drivers_by_hex_hashmap.containsKey(hex)) {
                        List<ll_nodes> nearby_drivers = drivers_by_hex_hashmap.get(hex);

                        for (ll_nodes driver : nearby_drivers) {
                            // Calculate Haversine distance between user and driver
                            double distance = haversine(user_curr_lat, user_curr_lng, driver.random_lat, driver.random_lng);

                            // Check if this is the nearest driver for this vehicle type
                            if (distance < shortest_distances_by_type.get(driver.vehicle_type)) {
                                shortest_distances_by_type.put(driver.vehicle_type, distance);
                                nearest_drivers_by_type.put(driver.vehicle_type, driver);
                            }
                        }

                        // Mark that we found drivers in this k-ring
                        found_in_this_k = true;
                    }
                }

                // Check if we found at least one driver of each type, if found then stop
                if (nearest_drivers_by_type.size() == 5) {
                    break;
                }

                if (found_in_this_k) {
                    // Continue searching if we haven't found all 5 types yet
                }
                k++;
            }

            if (k > 12) {
                System.out.println("searching all drivers...");

                // Fallback: search all drivers regardless of hex
                ll_nodes shifter_2 = drivers_in_user_city_ll.head;
                while (shifter_2 != null) {
                    double distance = haversine(user_curr_lat, user_curr_lng,
                            shifter_2.random_lat, shifter_2.random_lng);

                    // Check if this is the nearest driver for this vehicle type
                    if (distance < shortest_distances_by_type.get(shifter_2.vehicle_type)) {
                        shortest_distances_by_type.put(shifter_2.vehicle_type, distance);
                        nearest_drivers_by_type.put(shifter_2.vehicle_type, shifter_2);
                    }
                    shifter_2 = shifter_2.next;
                }
            }


            if(nearest_drivers_by_type.size() > 0) {
                BufferedWriter nearest_drivers_bw = new BufferedWriter(new FileWriter("nearest_drivers_5.txt"));

                for(String vehicle_type : vehicle_types) {
                    if(nearest_drivers_by_type.containsKey(vehicle_type)) {
                        ll_nodes driver = nearest_drivers_by_type.get(vehicle_type);
                        double distance = shortest_distances_by_type.get(vehicle_type);

                        nearest_drivers_bw.write(vehicle_type);
                        nearest_drivers_bw.newLine();
                        nearest_drivers_bw.write(Double.toString(distance));
                        nearest_drivers_bw.newLine();
                        nearest_drivers_bw.write(Integer.toString(driver.id));
                        nearest_drivers_bw.newLine();
                        nearest_drivers_bw.write(Double.toString(driver.random_lat));
                        nearest_drivers_bw.newLine();
                        nearest_drivers_bw.write(Double.toString(driver.random_lng));
                        nearest_drivers_bw.newLine();
                        nearest_drivers_bw.write("---"); // separator
                        nearest_drivers_bw.newLine();
                    }
                }
                nearest_drivers_bw.flush();
            }
            else{
                System.out.println("No drivers available in the city");
                //we will never reach this part
            }
            //now we have the shortest distance to nearest driver and also the details of that driver in nearest_driver.txt
            //we will use that to draw a line from that driver to user in spring boot

            BufferedReader nearest_drivers_5_br = new BufferedReader(new FileReader("nearest_drivers_5.txt"));

            // Arrays for vehicle info
            String[] vehicle_names = {"Two-wheeler", "Rickshaw", "Normal Cab", "Cab XL", "Premium Cab"};
            double[] distances = new double[5];
            int[] driver_ids = new int[5];
            double[] latitudes = new double[5];
            double[] longitudes = new double[5];

            // Read the file and parse data
            String line;
            int vehicle_index = 0;
            int line_count = 0;

            while ((line = nearest_drivers_5_br.readLine()) != null && vehicle_index < 5) {
                if (line.equals("---")) {
                    vehicle_index++;
                    line_count = 0;
                    continue;
                }

                switch (line_count % 6) {
                    case 0: // vehicle type (we already know the order)
                        break;
//                    case 1: // distance
//                        distances[vehicle_index] = Double.parseDouble(line);
//                        break;
                    case 2: // driver id
                        driver_ids[vehicle_index] = Integer.parseInt(line);
                        break;
                    case 3: // latitude
                        latitudes[vehicle_index] = Double.parseDouble(line);
                        break;
                    case 4: // longitude
                        longitudes[vehicle_index] = Double.parseDouble(line);
                        break;
                }
                line_count++;
            }

            nearest_drivers_5_br.close();

            //finding distance between user and location to reach
            double user_dest_distance_double = 0;

            Process user_dest_distance = new ProcessBuilder(
                    "bash",
                    "-c",
                    "jq -r '(.routes[0].legs[0].distance / 1000)' directions_raw.txt > user_dest_dist.txt"
            ).start();

            // Wait for the process to complete
            int exitCode_2 = user_dest_distance.waitFor();
            if (exitCode_2 != 0) {
                System.out.println("Error creating user_dest_dist.txt with exit code: " + exitCode);
            }
            Thread.sleep(1000);

            //changing the distance in nearest_driver to more accurate distance

            // Read the distance from user_dest_dist.txt
            BufferedReader dist_reader = new BufferedReader(new FileReader("user_dest_dist.txt"));
            user_dest_distance_double = Double.parseDouble(dist_reader.readLine());
            dist_reader.close();

            System.out.println("Accurate distance stored");


            // finding and printing prices
            int[] basePrices = {30, 50, 80, 130, 140};
            int pricePerKm = 10;

            System.out.println("Which vehicle would you like to select : ");
            for (int i = 0; i < 5; i++) {
                double totalPrice = basePrices[i] + (pricePerKm * user_dest_distance_double);
                System.out.println((i + 1) + " : " + vehicle_names[i] + " | Price : ₹" + totalPrice);
            }

            int user_vehicle_choice = 0;
            boolean repeat_vehicle_choice = true;

            while(repeat_vehicle_choice) {
                System.out.print("\nEnter your choice : ");
                String user_vehicle_selection_input = scn.nextLine();

                switch (user_vehicle_selection_input) {
                    case "1": {
                        user_vehicle_choice = 1;
                        repeat_vehicle_choice = false;
                        break;
                    }
                    case "2": {
                        user_vehicle_choice = 2;
                        repeat_vehicle_choice = false;
                        break;
                    }
                    case "3": {
                        user_vehicle_choice = 3;
                        repeat_vehicle_choice = false;
                        break;
                    }
                    case "4": {
                        user_vehicle_choice = 4;
                        repeat_vehicle_choice = false;
                        break;
                    }
                    case "5": {
                        user_vehicle_choice = 5;
                        repeat_vehicle_choice = false;
                        break;
                    }
                    default: {
                        System.out.println("Enter only number 1 to 5");
                    }
                }
            }

            // Create nearest_driver.txt with selected vehicle's details
            BufferedWriter selected_driver_bw = new BufferedWriter(new FileWriter("nearest_driver.txt"));

            // array start from 0, so we do -1
            int selected_index = user_vehicle_choice - 1;

            selected_driver_bw.write(Double.toString(distances[selected_index]));
            selected_driver_bw.newLine();

            selected_driver_bw.write(Integer.toString(driver_ids[selected_index]));
            selected_driver_bw.newLine();

            selected_driver_bw.write(Double.toString(latitudes[selected_index]));
            selected_driver_bw.newLine();

            selected_driver_bw.write(Double.toString(longitudes[selected_index]));

            selected_driver_bw.flush();

            System.out.println("Selected driver details saved to nearest_driver.txt");

            //we will call now the checkRouteApplication.java class main method
            Thread driver_route_thread = new Thread(() -> {
                try {
                    com.request_a_ride.driver_route.DriverRouteApplication.main(new String[]{});
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            driver_route_thread.start();
            driver_route_thread.join();

            //now we got to price details

            System.out.print("Press enter when you are done");
            scn.nextLine();
//            System.out.println("now we will see the price details");
//
//            int[] basePrices = {30, 50, 80, 130, 140};
//
//            // Price per km
//            int pricePerKm = 10;
//
//            // Example: calculate for 20 km
//            BufferedReader reading_dist = new BufferedReader(new FileReader("nearest_driver.txt"));
//            double dist = Double.parseDouble(reading_dist.readLine());
//            Double distance = dist;
//
//            // Calculate total price for each vehicle
//            Double totalPrice = basePrices[user_vehicle_choice - 1] + (pricePerKm * distance);
//            System.out.print("Total Fare : "+totalPrice + "\n");
//
//            switch (user_vehicle_choice){
//                case 1 : {
//                    System.out.println("Vehicle: Bike \nPrice for " + distance + " km is " + totalPrice);
//                    break;
//                }
//                case 2 : {
//                    System.out.println("Vehicle: Rickshaw \nPrice for " + distance + " km is " + totalPrice);
//                    break;
//                }
//                case 3 : {
//                    System.out.println("Vehicle: Normal cab \nPrice for " + distance + " km is " + totalPrice);
//                    break;
//                }
//                case 4 : {
//                    System.out.println("Vehicle: Cab XL \nPrice for " + distance + " km is " + totalPrice);
//                    break;
//                }
//                case 5 : {
//                    System.out.println("Vehicle: Premium Cab \nPrice for " + distance + " km is " + totalPrice);
//                    break;
//                }
//                default:{
//                    System.out.println("Enter only number 1 to 5");
//                }
//            }

            System.out.println("Proceeding ahead....");
            Thread.sleep(2000);
            scn.nextLine();
            System.out.println("");

            //now we will send a email with all the details

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
                    String user_city =  hash_map_1.hash_map_city.get(hex);

                    return user_city;
                }
            }
            k++;
        }
        return "City not found";
    }
}
class ll_drivers{
    ll_nodes head;
    void push(int id , double lat , double lng, String vehicle_type){
        if(head == null){
            ll_nodes temp = new ll_nodes(id , lat , lng, vehicle_type);
            head = temp;
        }
        else{
            ll_nodes temp = head;
            while(temp.next != null){
                temp = temp.next;
            }
            ll_nodes neww = new ll_nodes(id , lat , lng, vehicle_type);
            temp.next = neww;
        }
    }
}
class ll_nodes{
    ll_nodes next;
    int id;
    double random_lat;
    double random_lng;
    String vehicle_type;
    ll_nodes(){}

    ll_nodes(int id , double lat , double lng , String vehicle){
        this.id = id;
        this.random_lat = lat;
        this.random_lng = lng;
        this.vehicle_type = vehicle;
    }
}

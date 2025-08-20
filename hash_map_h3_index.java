import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;

public class hash_map_h3_index extends Thread{
    HashMap<Long, String> hash_map_city = new HashMap<>();
    public void run(){
        try {
            making_hash_map();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void making_hash_map() throws Exception{

        BufferedReader india_cities_br = new BufferedReader(new FileReader("india_cities_clean2.csv"));
        String india_cities_line = india_cities_br.readLine();//doing this extra to skip the line city,lat,lng,population
        india_cities_line = india_cities_br.readLine();

        //making h3 core
        H3Core h3 = H3Core.newInstance();

        while(india_cities_line != null){
            String[] india_cities_line_arr = india_cities_line.split(",");

            String city_name = india_cities_line_arr[0];
            Double city_lat = Double.parseDouble(india_cities_line_arr[1]);
            Double city_lng = Double.parseDouble(india_cities_line_arr[2]);

            long city_h3_index = h3.latLngToCell(city_lat , city_lng , 6);

            hash_map_city.put(city_h3_index , city_name);
            india_cities_line = india_cities_br.readLine();
        }
        //this will how create a hashmap of cities index
    }
}

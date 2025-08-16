package com.request_a_ride.check_route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Scanner;

@SpringBootApplication
public class CheckRouteApplication{

	public static void main(String[] args) throws Exception{
        Scanner scn = new Scanner(System.in);
        System.out.print("Enter your current location in full form : ");
        String pick_up  = scn.nextLine();
        System.out.print("Enter your destination : ");
        String drop_down = scn.nextLine();

        //command to geocode the location given by the user
        String pick_up_name = pick_up.replaceAll("\\s+" , "%20");
        String drop_down_name = drop_down.replaceAll("\\s+" , "%20");

        ProcessBuilder pb_pick_up = new ProcessBuilder(
                "curl" , "https://api.olamaps.io/places/v1/geocode?address=" + pick_up_name + "&api_key=___api_key___"
        );
        ProcessBuilder pb_drop_down = new ProcessBuilder(
                "curl" , "https://api.olamaps.io/places/v1/geocode?address=" + drop_down_name + "&api_key=___api_key___"
        );

        Process get_pick_up_coor = pb_pick_up.start();
        Process get_drop_down_coor = pb_drop_down.start();

        //writing the json output into text file raw

        BufferedWriter jsonOutput_pick_up_bw = new BufferedWriter(new FileWriter("pick_up_raw_json.txt"));
        BufferedWriter jsonOutput_drop_down_bw = new BufferedWriter(new FileWriter("drop_down_raw_json.txt"));

        BufferedReader jsonOutput_pick_up_br= new BufferedReader(new InputStreamReader(get_pick_up_coor.getInputStream()));
        BufferedReader jsonOutput_drop_down_br = new BufferedReader(new InputStreamReader(get_drop_down_coor.getInputStream()));

        String pick_up_raw_line = jsonOutput_pick_up_br.readLine();
        String drop_down_raw_line = jsonOutput_drop_down_br.readLine();

        while(pick_up_raw_line != null){
            jsonOutput_pick_up_bw.write(pick_up_raw_line);
            jsonOutput_pick_up_bw.newLine();
            pick_up_raw_line = jsonOutput_pick_up_br.readLine();
        }
        jsonOutput_pick_up_bw.close();

        while(drop_down_raw_line != null){
            jsonOutput_drop_down_bw.write(drop_down_raw_line);
            jsonOutput_drop_down_bw.newLine();
            drop_down_raw_line = jsonOutput_drop_down_br.readLine();
        }
        jsonOutput_drop_down_bw.close();

        //running the terminal command that will extract the needed coordinates from the whole json file and store it into another text file
        Process pick_up_coordinates = new ProcessBuilder(
                "bash",
                "-c",
                "jq -r '.geocodingResults[0].geometry.location | \"\\(.lat)%2C\\(.lng)\"' pick_up_raw_json.txt > pick_up_coordinates.txt"
        ).start();

        Process drop_down_coordinates = new ProcessBuilder(
                "bash",
                "-c",
                "jq -r '.geocodingResults[0].geometry.location | \"\\(.lat)%2C\\(.lng)\"' drop_down_raw_json.txt > drop_down_coordinates.txt"
        ).start();

        Thread.sleep(1500);

        //now we have the files named pick_up_coordinates and drop_down_coordinates. Now we will pass this coordinates through routing directions api and get the raw json of path
        BufferedReader pick_up_coordinates_br = new BufferedReader(new FileReader("pick_up_coordinates.txt"));
        BufferedReader drop_down_coordinates_br = new BufferedReader(new FileReader("drop_down_coordinates.txt"));

        String pick_up_coordinate_string = pick_up_coordinates_br.readLine();
        String drop_down_coordinate_string = drop_down_coordinates_br.readLine();

        Process directions_api_curl = new ProcessBuilder(
                "curl",
                "https://api.olamaps.io/routing/v1/directions?origin=" + pick_up_coordinate_string + "&destination=" + drop_down_coordinate_string + "&mode=driving&api_key=___api_key___",
                "--request", "POST"
        ).start();

        //writing the raw json into txt file

        BufferedReader jsonOutput_directions_br = new BufferedReader(new InputStreamReader(directions_api_curl.getInputStream()));
        BufferedWriter jsonOutput_direction_bw = new BufferedWriter(new FileWriter("directions_raw.txt"));

        String direction_raw_line = jsonOutput_directions_br.readLine();
        while(direction_raw_line != null){
            jsonOutput_direction_bw.write(direction_raw_line);
            jsonOutput_direction_bw.newLine();
            direction_raw_line = jsonOutput_directions_br.readLine();
        }
        jsonOutput_direction_bw.close();

        //running the terminal command that will turn the jsonOutput_direction raw into just lat,lng format
        Process direction_raw_to_formatted = new ProcessBuilder(
                "bash",
                "-c",
                "jq -r '.. | .start_location? | select(.lat and .lng) | \"\\(.lat),\\(.lng)\"' directions_raw.txt > direction_coordinates.txt"
        ).start();




		SpringApplication.run(CheckRouteApplication.class, args);

        Process process = new ProcessBuilder("open" , "http://localhost:8080/").start();
	}
}

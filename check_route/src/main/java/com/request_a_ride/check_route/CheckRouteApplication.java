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
                "curl" , "https://api.olamaps.io/places/v1/geocode?address=" + pick_up_name + "&api_key=Uer9nnXtI726LogTusu4W3ebgiCXGyumxqwCaXhn"
        );
        ProcessBuilder pb_drop_down = new ProcessBuilder(
                "curl" , "https://api.olamaps.io/places/v1/geocode?address=" + drop_down_name + "&api_key=Uer9nnXtI726LogTusu4W3ebgiCXGyumxqwCaXhn"
        );

        Process get_pick_up_coor = pb_pick_up.start();
        Process get_drop_down_coor = pb_drop_down.start();

        //writing the json output into text file raw

        BufferedWriter jsonOutput_pick_up_bw = new BufferedWriter(new FileWriter("pick_up_raw_json.txt"));
        BufferedWriter jsonOutput_drop_down_bw = new BufferedWriter(new FileWriter("drop_down_raw_json.txt"));

        BufferedReader jsonOutput_pick_up_br= new BufferedReader(new InputStreamReader(get_pick_up_coor.getInputStream()));
        BufferedReader jsonOutput_drop_down_br = new BufferedReader(new InputStreamReader(get_drop_down_coor.getInputStream()));

        String pick_up_raw_line = jsonOutput_pick_up_br.readLine();
        while(pick_up_raw_line != null){
            jsonOutput_pick_up_bw.write(pick_up_raw_line);
            jsonOutput_pick_up_bw.newLine();
            pick_up_raw_line = jsonOutput_pick_up_br.readLine();
        }
        jsonOutput_pick_up_bw.flush();

        //running the terminal command that will extract the needed coordinates from the whole json file and store it into another text file
        Process pick_up_coordinates = new ProcessBuilder(
                "bash",
                "-c",
                "jq -r '.geocodingResults[0].geometry.location | \"[\\(.lng), \\(.lat)],\"' pick_up_raw_json.txt > pick_up_coordinates.txt"
        ).start();









		SpringApplication.run(CheckRouteApplication.class, args);

        Process process = new ProcessBuilder("open" , "http://localhost:8080/").start();
	}
}

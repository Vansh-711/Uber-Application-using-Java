import com.request_a_ride.check_route.CheckRouteApplication;
import java.util.Scanner;

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

        Thread.sleep(40000);

        System.out.println("Route service starting... Check your path now");
        System.out.println("Press enter when you have checked the path : ");
        scn.nextLine();


        System.out.println("\nMap Checked");
        System.out.println("Proceeding ahead");

        //2. driver search starts










    }
}

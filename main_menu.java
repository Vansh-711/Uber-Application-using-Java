import java.util.Scanner;
public class main_menu{
    public static void main(String[] args) throws Exception{
        Scanner scn = new Scanner(System.in);
        boolean main_menu_repeat = true;
        while(main_menu_repeat) {
            System.out.println("1 : Request a ride");
            System.out.println("2 : Exit");
            System.out.print("Enter choice : ");
            String main_menu_input = scn.nextLine();
            switch (main_menu_input) {
                case "1": {
                    request_a_ride user_1 = new request_a_ride();
                    user_1.start();
                    user_1.join();
                    break;
                }
                case "2": {
                    main_menu_repeat = false;
                    break;
                }
                default: {
                    System.out.println("Enter only number between ");
                    break;
                }
            }
        }
    }
}

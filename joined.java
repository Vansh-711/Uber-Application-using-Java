import java.sql.*;
import java.util.Scanner;

class UberSystem
{
    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", ""))
        {
            if (con != null) {
                System.out.println(" Database connected successfully");
            } else {
                System.out.println(" Failed to connect");
                return;
            }

            System.out.print("Enter choice (signup/login): ");
            String choice = scn.nextLine().trim().toLowerCase();

            if (choice.equals("signup"))
            {
                sine_up.main(args);
                scn.nextLine();
                log_in.main(args);
            }
            else if (choice.equals("login"))
            {
                log_in.main(args);
            }
            else
            {
                System.out.println("Invalid choice!");
            }

            main_menu.main(args);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

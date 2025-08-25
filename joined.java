import java.sql.*;
import java.util.Scanner;

class UberSystem
{
    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);

        try
        {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");
            if (con != null)
            {
                System.out.println(" Database connected successfully");

                while(true)
                {
                    System.out.print("Enter choice (signup/login/exit): ");
                    String choice = scn.nextLine().trim().toLowerCase();

                    if (choice.equals("signup"))
                    {
                        new_sine_up.main(args);
                    }
                    else if (choice.equals("login"))
                    {
                        log_in.main(args);
                        break;
                    }
                    else if (choice.equals("exit"))
                    {
                        break;
                    }
                    else
                    {
                        System.out.println("Invalid choice!");
                    }
                }
                main_menu.main(args);
            }
            else
            {
                System.out.println(" Failed to connect");
            }

        }
        catch (Exception e)
        {
            System.out.println("something to else ");
        }
    }
}

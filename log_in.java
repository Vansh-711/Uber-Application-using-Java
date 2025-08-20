import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class log_in
{
    public static void main(String[] args) throws Exception
    {
        Scanner scn = new Scanner(System.in);
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");

        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("something else in connection ");

        System.out.println("\nWelcome to login menu\n");
        System.out.println("1. phone no \n2. email id ");
        System.out.print("Enter choice : ");
        String x = scn.nextLine();
        scn.nextLine();
        boolean b = false;

        ResultSet rs = null;
        switch (x)
        {
            case "1":
            {
                System.out.print("Enter phone number for login: ");
                String inputPhone = scn.nextLine();

                System.out.println("enter your password ");
                String inputPassword = scn.nextLine();

                String loginQuery = "SELECT * FROM user_info WHERE phone = ? and password = ? ";

                try
                {
                    PreparedStatement pst = con.prepareStatement(loginQuery);
                    pst.setString(1, inputPhone);
                    pst.setString(2, inputPassword);

                    rs = pst.executeQuery();

                    if (rs.next()) {
                        System.out.println("Login successful!");
                        System.out.println("User Details:");
                        System.out.println("ID: " + rs.getInt("id"));
                        System.out.println("Name: "
                                + rs.getString("first_name") + " "
                                + rs.getString("middle_name") + " "
                                + rs.getString("last_name"));
                        System.out.println("Phone: " + rs.getString("phone"));
                        System.out.println("Email: " + rs.getString("email"));
                        System.out.println("Gender: " + rs.getString("gender"));
                        System.out.println("Age: " + rs.getInt("age"));
                        b = !b;
                        break;
                    }
                    else
                    {
                        System.out.println("No user found with this phone number!");
                    }

                }
                catch (Exception e)
                {
                    System.out.println("Error during login: " + e.getMessage());
                }
                break;
            }

            case "2":
            {
                System.out.print("Enter email for login: ");
                String inputEmail = scn.nextLine();

                System.out.println("enter your password ");
                String inputPassword = scn.nextLine();

                String loginQuery = "SELECT * FROM user_info WHERE email = ? and password = ?";


                try
                {
                    PreparedStatement pst = con.prepareStatement(loginQuery);
                    pst.setString(1, inputEmail);
                    pst.setString(2, inputPassword);

                    rs = pst.executeQuery();

                    if (rs.next())
                    {
                        System.out.println("Login successful!");
                        System.out.println("User Details:");
                        System.out.println("ID: " + rs.getInt("id"));
                        System.out.println("Name: "
                                + rs.getString("first_name") + " "
                                + rs.getString("middle_name") + " "
                                + rs.getString("last_name"));
                        System.out.println("Phone: " + rs.getString("phone"));
                        System.out.println("Email: " + rs.getString("email"));
                        System.out.println("Gender: " + rs.getString("gender"));
                        System.out.println("Age: " + rs.getInt("age"));
                        b = !b;
                        break;
                    }
                    else
                    {
                        System.out.println("No user found with this email!");
                    }

                }
                catch (Exception e)
                {
                    System.out.println("Error during login: " + e.getMessage());
                }
                break;
            }
            default:
            {
                System.out.println("enter valid input");
            }
        }


            if (con == null)
            {
                throw new SQLException("Connection is null!");
            }

        String userName = null;
        int rows = 0;
        if (b) {
            String fullKey = rs.getString("first_name") + "_"
                    + rs.getString("middle_name") + "_"
                    + rs.getString("last_name") + "__"
                    + rs.getInt("id");

            LocalDate date = LocalDate.now();
            LocalTime time = LocalTime.now();

            String que = "CREATE TABLE IF NOT EXISTS `" + fullKey + "` ("
                    + "serial_no INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_name VARCHAR(100), "
                    + "user_last_login_date DATE, "
                    + "user_last_login_time TIME)";

            System.out.println("full key is: " + fullKey);

            PreparedStatement pst = con.prepareStatement(que);
            pst.executeUpdate();

            System.out.println("Table '" + fullKey + "' created (if not exists).");

            String ique = "INSERT INTO `" + fullKey + "` "
                    + "(user_name, user_last_login_date, user_last_login_time) "
                    + "VALUES (?, ?, ?)";

            PreparedStatement ipst = con.prepareStatement(ique);

            userName = rs.getString("first_name") + " "
                    + rs.getString("middle_name") + " "
                    + rs.getString("last_name");

            ipst.setString(1, userName);
            ipst.setDate(2, Date.valueOf(date));
            ipst.setTime(3, Time.valueOf(time));

            rows = ipst.executeUpdate();
        }

        if (rows > 0)
            {
                System.out.println("Inserted login record for: " + userName);
            }
    }
}

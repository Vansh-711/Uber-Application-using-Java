import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class log_in
{
    static Connection con;

    static {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        Scanner scn = new Scanner(System.in);

        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("something else in connection ");

        System.out.println("\nWelcome to Login Menu\n");

        String email_for_otp = "";

        ResultSet rs = null;
        while(true)
        {
            System.out.println("1. phone no \n2. email id ");
            System.out.print("Enter your choice : ");
            int x = scn.nextInt();
            scn.nextLine();
            boolean b = false;


            switch (x)
            {
                case 1:
                {
                    System.out.print("Enter phone number for login: ");
                    String inputPhone = scn.nextLine();

                    System.out.println("enter your password ");
                    String inputPassword = scn.nextLine();

                    String loginQuery = "SELECT * FROM user_info WHERE phone = ?";
                    String loginQuery1 = "SELECT password FROM user_info where phone = ? ";
                    ResultSet rs1 = null;

                    try
                    {
                        PreparedStatement pst = con.prepareCall(loginQuery1);
                        pst.setString(1 ,inputPhone);
                        rs1 = pst.executeQuery();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error during 1 login: " + e.getMessage());
                    }

                    try
                    {
                        PreparedStatement pst = con.prepareStatement(loginQuery);
                        pst.setString(1, inputPhone);

                        rs = pst.executeQuery();

                        if (rs.next() && rs1.next() && rs1.getString(1).equals(inputPassword))
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
                            System.out.println("No user found with this phone number!");
                        }

                    }
                    catch (Exception e)
                    {
                        System.out.println("Error during login: " + e.getMessage());
                    }
                    break;
                }

                case 2:
                {
                    System.out.print("Enter email for login: ");
                    String inputEmail = scn.nextLine();

                    System.out.println("enter your password ");
                    String inputPassword = scn.nextLine();

                    String loginQuery = "SELECT * FROM user_info WHERE email = ?";
                    String loginQuery1 = "SELECT password FROM user_info where email = ? ";
                    ResultSet rs1 = null;

                    try
                    {
                        PreparedStatement pst = con.prepareCall(loginQuery1);
                        pst.setString(1 ,inputEmail);
                        rs1 = pst.executeQuery();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error during 1 login: " + e.getMessage());
                    }


                    try
                    {
                        PreparedStatement pst = con.prepareStatement(loginQuery);
                        pst.setString(1, inputEmail);
                        rs = pst.executeQuery();

                        if (rs.next() && rs1.next() && rs1.getString(1).equals(inputPassword))
                        {
                            System.out.println("Login successful!");
                            System.out.println("User Details:");
                            System.out.println("    ID: " + rs.getInt("id"));
                            System.out.println("    Name: "
                                    + rs.getString("first_name") + " "
                                    + rs.getString("middle_name") + " "
                                    + rs.getString("last_name"));
                            System.out.println("    Phone: " + rs.getString("phone"));
                            System.out.println("    Email: " + rs.getString("email"));
                            System.out.println("    Gender: " + rs.getString("gender"));
                            System.out.println("    Age: " + rs.getInt("age"));
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

                PreparedStatement pst = con.prepareStatement(que);
                pst.executeUpdate();

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
                System.out.println("Inserted login record for: " + userName +"\n");
            }
            if(b)
                break;
        }
        email_for_otp = rs.getString("email");
        OTP_verify(email_for_otp);
    }
    static String OTP_verify(String email_for_otp)
    {
        boolean repeat_verify = true;
        while(repeat_verify) {
            try {
                Scanner scn = new Scanner(System.in);
                int otp = (int) (Math.random() * 900000) + 100000;

                ProcessBuilder otp_verify_pb = new ProcessBuilder(
                        "bash", "-c",
                        "echo -e \"Subject: One-Time Password (OTP) for Verification\\n\\nYour OTP for verification is: " + otp + "\\nDo not share it with anyone.\" | msmtp " + email_for_otp
                );
                Process otp_verify_process = otp_verify_pb.start();

                System.out.println("Sending Email .....");
                otp_verify_process.waitFor();
                System.out.println("Check your email");

                while (true) {
                    System.out.print("enter OTP : ");
                    int enter_otp = scn.nextInt();
                    if (otp == enter_otp) {
                        System.out.println("OTP Matched successfully.. ");
                        Thread.sleep(1000);
                        System.out.println("Proceeding ahead\n");
                        repeat_verify = false;
                        return null;
                    } else {
                        System.out.println("wrong try again ");
                    }
                }
            } catch (Exception e) {
                System.out.println("Enter only numbers");
            }
        }
        return  null;
    }
}
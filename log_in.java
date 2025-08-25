import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class log_in
{
    static Connection con;

    static
    {
        try
        {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");
        }
        catch (SQLException e)
        {
            System.out.println("error in connection");
        }
    }

    public static void main(String[] args) throws Exception
    {
        Scanner scn = new Scanner(System.in);

        if (con != null)
        {
            System.out.println("connection is done ");

            String email_for_otp = "";

            ResultSet rs = null;
            while (true)
            {
                System.out.println("1. phone no \n2. email id ");
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
                            pst.setString(1, inputPhone);
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
                            pst.setString(1, inputEmail);
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

                if (b)
                {
                    break;
                }
            }
            email_for_otp = rs.getString("email");
            OTP_verify(email_for_otp);

            LocalDate currentDate = LocalDate.now();
            LocalTime currentTime = LocalTime.now();
            try
            {
                // Use proper SQL syntax with placeholders

                String fnm = rs.getString("first_name") + "_" +
                        rs.getString("middle_name") + "_" +
                        rs.getString("last_name")+"_"+
                        rs.getInt("id");

                String qq = "INSERT INTO "+fnm+" (user_name, user_last_login_date, user_last_login_time) VALUES (?, ?, ?)";
                PreparedStatement spt = con.prepareStatement(qq);

                String nm = rs.getString("first_name") + " " +
                        rs.getString("middle_name") + " " +
                        rs.getString("last_name");

                spt.setString(1, nm);
                spt.setDate(2, Date.valueOf(currentDate));
                spt.setTime(3, Time.valueOf(currentTime));

                if(spt.executeUpdate() > 0)
                {
                    System.out.println("Time and date are saved successfully");
                }
            }
            catch (Exception e)
            {
                System.out.println("Error in saving time and date: " + e.getMessage());
                e.printStackTrace(); // This will help you see the exact error
            }


        }
        else
        {
            System.out.println("something else in connection ");
        }
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

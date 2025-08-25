import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

class new_sine_up
{
    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);
        Connection con = null;

        try
        {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");

            if (con != null)
            {
                System.out.println("Connection established successfully");

                // Get user information
                System.out.print("Enter first Name: ");
                String first = scn.nextLine();

                System.out.print("Enter middle Name: ");
                String middle = scn.nextLine();

                System.out.print("Enter last Name: ");
                String last = scn.nextLine();

                // Phone number validation with duplicate check
                String phoneNumber;
                while (true)
                {
                    System.out.print("Enter Phone Number: ");
                    phoneNumber = scn.nextLine().trim();

                    if (phoneNumber.matches("\\d{10}"))  //  \\d(10) check -> It checks if it contains an empty number and it is the number 10??
                    {
                        char firstDigit = phoneNumber.charAt(0);
                        if (firstDigit == '6' || firstDigit == '7' || firstDigit == '8' || firstDigit == '9')
                        {
                            // Check if phone already exists
                            if (isPhoneExists(con, phoneNumber))
                            {
                                System.out.println("This phone number is already registered. Please use a different number.");
                                continue;
                            }
                            System.out.println("Valid Phone Number: " + phoneNumber);
                            break;
                        }
                        else
                        {
                            System.out.println("Phone number must start with 6, 7, 8, or 9");
                        }
                    }
                    else
                    {
                        System.out.println("Phone number must be exactly 10 digits!");
                    }
                }

                // Email validation with duplicate check
                String email;
                while (true)
                {
                    System.out.print("Enter Email: ");
                    email = scn.nextLine().trim();

                    // Basic validation
                    if (email.length() < 5 || !email.contains("@") || !email.endsWith(".com"))
                    {
                        System.out.println("Email must be valid and end with '.com'");
                        continue;
                    }

                    // Check if email already exists
                    if (isEmailExists(con, email))
                    {
                        System.out.println("This email is already registered. Please use a different email.");
                        continue;
                    }

                    System.out.println("Valid Email: " + email);
                    break;
                }

                // Password validation
                System.out.println("********** PASSWORD RULES **********");
                System.out.println("1. First character must be CAPITAL letter");
                System.out.println("2. Use at least one special character (@, #, $, %, ^, &)");
                System.out.println("3. Last character must be a NUMBER");
                System.out.println("************************************");

                String password;
                while (true)
                {
                    System.out.print("Enter password: ");
                    password = scn.nextLine();

                    if (password.length() < 3)
                    {
                        System.out.println("Password too short! Try again.");
                        continue;
                    }

                    char firstChar = password.charAt(0);
                    if (!Character.isUpperCase(firstChar))
                    {
                        System.out.println("First character must be a CAPITAL letter!");
                        continue;
                    }

                    boolean hasSpecial = false;
                    String specialCharacters = "@#$%^&";
                    for (int i = 0; i < password.length(); i++)
                    {
                        if (specialCharacters.contains(String.valueOf(password.charAt(i))))
                        {
                            hasSpecial = true;
                            break;
                        }
                    }

                    if (!hasSpecial)
                    {
                        System.out.println("Password must contain at least one special character (@, #, $, %, ^, &).");
                        continue;
                    }

                    char lastChar = password.charAt(password.length() - 1);
                    if (!Character.isDigit(lastChar))
                    {
                        System.out.println("Last character must be a NUMBER!");
                        continue;
                    }

                    System.out.println("Password is valid!");
                    break;
                }

                // Gender selection
                System.out.println("********** GENDER SELECTION **********");
                System.out.println("1. Enter M or Male");
                System.out.println("2. Enter F or Female");
                System.out.println("3. Enter O or Other");
                System.out.println("**************************************");

                String gender = "";
                while (true)
                {
                    System.out.print("Enter gender (M/F/O or Male/Female/Other): ");
                    String input = scn.nextLine().trim().toLowerCase();

                    if (input.equalsIgnoreCase("m") || input.equalsIgnoreCase("male"))
                    {
                        gender = "Male";
                        break;
                    }
                    else if (input.equalsIgnoreCase("f") || input.equalsIgnoreCase("female"))
                    {
                        gender = "Female";
                        break;
                    }
                    else if (input.equalsIgnoreCase("o") || input.equalsIgnoreCase("other"))
                    {
                        gender = "Other";
                        break;
                    }
                    else
                    {
                        System.out.println("Invalid input. Please enter M, F, O or Male, Female, Other.");
                    }
                }
                System.out.println("Selected gender: " + gender);

                // Age validation
                int age = 0;
                while (true)
                {
                    System.out.print("Enter age: ");
                    String input = scn.nextLine();
                    try
                    {
                        age = Integer.parseInt(input);
                        if (age >= 7 && age <= 120)
                        {
                            break;
                        }
                        System.out.println("Invalid age. Must be between 7 and 120.");
                    }
                    catch (NumberFormatException e)
                    {
                        System.out.println("Invalid input. Please enter numbers only.");
                    }
                }
                System.out.println("Entered age: " + age);

                //**************************************************************************************************************
                // details are complicated
                //***************************************************************************************************************

                // Insert user into database
                int userId = insertUser(con, first, middle, last, phoneNumber, email, password, gender, age);

                //--------------------------------------------------------------
                // call procedure in my sql
                String callProcedure = "{call create_user_table(? , ? , ? , ?)}";
                CallableStatement stmt = con.prepareCall(callProcedure);
                stmt.setString(1 , first);
                stmt.setString(2 , middle);
                stmt.setString(3 , last);
                stmt.setInt(4 , userId);
                stmt.execute();
                //-----------------------------------------------------

                if (userId > 0)
                {
                    System.out.println("User registered successfully with ID: " + userId);

                    // Create individual table for user
//                    createUserTable(con, first, middle, last, userId);
                }
                else
                {
                    System.out.println("Failed to register user. Please try again.");
                }
            }
        }
        catch (SQLException e)
        {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Check if phone number already exists
    private static boolean isPhoneExists(Connection con, String phoneNumber) throws SQLException
    {
        String query = "SELECT COUNT(*) FROM user_info WHERE phone = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query))  //try-with-resources not need to catch  --> AutoCloseable Java 7 introduce this
        {
            pstmt.setString(1, phoneNumber);
            try (ResultSet rs = pstmt.executeQuery())
            {
                if (rs.next())
                {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Check if email already exists
    private static boolean isEmailExists(Connection con, String email) throws SQLException
    {
        String query = "SELECT COUNT(*) FROM user_info WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query))
        {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) //try-with-resources not need to catch
            {
                if (rs.next())
                {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Insert user into database and return user ID
    private static int insertUser(Connection con, String first, String middle, String last,
                                  String phone, String email, String password, String gender, int age) throws SQLException
    {
        String query = "INSERT INTO user_info (first_name, middle_name, last_name, phone, email, password, gender, age) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))  //try-with-resources not need to catch
        {
            pstmt.setString(1, first);
            pstmt.setString(2, middle);
            pstmt.setString(3, last);
            pstmt.setString(4, phone);
            pstmt.setString(5, email);
            pstmt.setString(6, password);
            pstmt.setString(7, gender);
            pstmt.setInt(8, age);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0)
            {
                try (ResultSet generatedKeys =  pstmt.getGeneratedKeys())
                {
                    if (generatedKeys.next())
                    {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

//    // Create individual table for user
//    private static void createUserTable(Connection con, String first, String middle, String last, int userId) throws SQLException
//    {
//        String tableName = first + "_" + middle + "_" + last + "__" + userId;
//        LocalDate date = LocalDate.now();
//        LocalTime time = LocalTime.now();
//
//        // Create table
//        String createTableQuery = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
//                "serial_no INT AUTO_INCREMENT PRIMARY KEY, " +
//                "user_name VARCHAR(100), " +
//                "user_last_login_date DATE, " +
//                "user_last_login_time TIME)";
//
//        try
//        {
//            PreparedStatement pstmt = con.prepareStatement(createTableQuery);
//            pstmt.executeUpdate();
//            System.out.println("User table created successfully: " + tableName);
//        }
//        catch (Exception e)
//        {
//            System.out.println("error in create new  table");
//        }
//    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Scanner;


public class sign_up
{
    public static void main(String[] args) throws Exception
    {
        Scanner scn = new Scanner(System.in);
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");

        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("something else in connection ");

        Statement st = con.createStatement();

        System.out.print("Enter first Name: ");
        String first = scn.nextLine();

        System.out.print("Enter middle Name: ");
        String middle = scn.nextLine();

        System.out.print("Enter last Name: ");
        String last = scn.nextLine();

        long phoneNumber;
        while (true)
        {
            System.out.print("Enter Phone Number: ");
            phoneNumber = scn.nextLong();
            String phoneStr = phoneNumber + "";

            if (phoneStr.length() == 10)
            {
                boolean isValid = true;
                for (int i = 0; i < phoneStr.length(); i++)
                {
                    char ch = phoneStr.charAt(i);
                    if (ch < 48 || ch > 57)
                    {
                        isValid = false;
                        break;
                    }
                }
                if (isValid)
                {
                    System.out.println(" Valid Phone Number: " + phoneStr);
                    break;
                }
                else
                {
                    System.out.println(" Phone number must contain only digits!");
                }
            }
            else
            {
                System.out.println(" Phone number must be exactly 10 digits!");
            }
        }


        String email;
        while (true)
        {
            System.out.print("Enter Email: ");
            email = scn.nextLine().trim();
            boolean hasAt = false;
            boolean hasDigit = false;
            boolean hasUpper = false;
            boolean hasSpace = false;
            int atIndex = email.indexOf('@');
            if (atIndex > 0 && atIndex < email.length() - 1)
            {
                hasAt = true;
            }
            for (int i = 0; i < email.length(); i++)
            {
                char ch = email.charAt(i);
                if (Character.isDigit(ch))
                {
                    hasDigit = true;
                }
                if (Character.isUpperCase(ch))
                {
                    hasUpper = true;
                }
                if (Character.isSpaceChar(ch))
                {
                    hasSpace = true;
                }
            }
            if (!hasAt)
            {
                System.out.println(" Email must contain '@' and it cannot be the first or last character.");
            }
            else if (!hasDigit)
            {
                System.out.println(" Email must contain at least one digit (0-9).");
            }
            else if (hasUpper)
            {
                System.out.println(" Email must NOT contain uppercase letters.");
            }
            else if (hasSpace)
            {
                System.out.println(" Email must not contain spaces.");
            }
            else
            {
                System.out.println(" Valid Email: " + email);
                break;
            }
        }




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

            if (!(firstChar >= 'A' && firstChar <= 'Z'))
            {
                System.out.println("First character must be a CAPITAL letter!");
                continue;
            }

            boolean hasSpecial = false;
            String specials = "@#$%^&";

            for (int i = 0; i < password.length(); i++)
            {
                if (specials.contains(password.charAt(i) + ""))
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

            if (!(lastChar >= '0' && lastChar <= '9'))
            {
                System.out.println("Last character must be a NUMBER!");
                continue;
            }

            System.out.println("Password is valid!");
            break;
        }




        System.out.println("********** GENDER SELECTION **********");
        System.out.println("1. Enter M or Male");
        System.out.println("2. Enter F or Female");
        System.out.println("3. Enter O or Other");
        System.out.println("**************************************");

        String gender = "";

        while (true)
        {
            System.out.print("Enter gender (M/F/O or Male/Female/Other): ");
            String in = scn.nextLine();
            String s = in.trim().toLowerCase();

            if (s.equals("m") || s.equals("male"))
            {
                gender = "Male";
                break;
            }


            if (s.equals("f") || s.equals("female"))
            {
                gender = "Female";
                break;
            }


            if (s.equals("o") || s.equals("other"))
            {
                gender = "Other";
                break;
            }


            System.out.println("Invalid input. Please enter M, F, O or Male, Female, Other.");
        }
        System.out.println("Selected gender: " + gender);

        int age = 0;
        while (true)
        {
            System.out.print("Enter age: ");
            String in = scn.nextLine();
            try
            {
                age = Integer.parseInt(in);
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
        System.out.println("Minimum allowed age: 7");

        try
        {
            String insertQuery = "INSERT INTO user_info (first_name, middle_name, last_name, phone, email, password, gender, age) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = con.prepareStatement(insertQuery);
            pstmt.setString(1, first);
            pstmt.setString(2, middle);
            pstmt.setString(3, last);
            pstmt.setString(4, String.valueOf(phoneNumber));
            pstmt.setString(5, email);
            pstmt.setString(6, password);
            pstmt.setString(7, gender);
            pstmt.setInt(8, age);

            int rows = pstmt.executeUpdate();

            if (rows > 0)
            {
                System.out.println(" User inserted successfully!");
            }
            else
            {
                System.out.println(" Insert failed!");
            }

        }
        catch (Exception e)
        {
            System.out.println(" Error while inserting user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
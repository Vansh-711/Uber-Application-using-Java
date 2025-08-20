import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class drivers_info
{
    public static void main(String[] args) throws SQLException {


        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/uber_application", "root", "");

        if (con != null)
            System.out.println("connection is done ");
        else
            System.out.println("something else in connection ");

        Statement ST = con.createStatement();


        String s = "CREATE TABLE user_info (\n" +
                "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    first_name VARCHAR(50) NOT NULL,\n" +
                "    middle_name VARCHAR(50),\n" +
                "    last_name VARCHAR(50) NOT NULL,\n" +
                "    phone VARCHAR(15) UNIQUE NOT NULL,\n" +
                "    email VARCHAR(100) UNIQUE NOT NULL,\n" +
                "    password VARCHAR(255) NOT NULL,\n" +
                "    gender ENUM('Male', 'Female', 'Other') NOT NULL,\n" +
                "    age INT CHECK (age >= 7 AND age <= 120),\n" +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n" +
                ");\n";


        ST.execute(s);
    }
}

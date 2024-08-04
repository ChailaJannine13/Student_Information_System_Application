package com.example.ndtc_sis;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    // JDBC driver class name
    String classes = "net.sourceforge.jtds.jdbc.Driver";

    // Database connection details
    protected static String ip = "120.28.214.77"; // IP address of the database server
    protected static String port = "1433"; // Port number of the database service
    protected static String db = "DB_PROPOSE_SCHEMA"; // Database name
    protected static String un = "mobilesis"; // Database username
    protected static String pw = "mobilesis@123"; // Database password

    // Method to est ablish a database connection
    public Connection CONN() {
        // Setting up a policy to allow network operations on the main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll() // Allows all thread policy violations (including network access)
                .build();
        StrictMode.setThreadPolicy(policy);

        Connection conn = null; // Initialize the connection object to null
        try {
            // Load the JDBC driver class
            Class.forName(classes);

            // Construct the connection URL
            String conUrl = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";" +
                    "databaseName=" + db + ";user=" + un + ";password=" + pw + ";";
            Log.d("DB Connection", "Connecting to: " + conUrl); // Log the connection URL

            // Establish the connection
            conn = DriverManager.getConnection(conUrl);
            Log.d("DB Connection", "Connection successful"); // Log successful connection
        } catch (ClassNotFoundException e) {
            // Handle exception if JDBC driver class is not found
            Log.e("DB Connection", "Driver class not found", e);
        } catch (SQLException e) {
            // Handle SQL exceptions during connection establishment
            Log.e("DB Connection", "SQL exception", e);
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            Log.e("DB Connection", "Unexpected exception", e);
        }

        // Return the established connection (or null if connection failed)
        return conn;
    }
}

package com.example.ndtc_sis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Typeface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Profile extends AppCompatActivity {
    private static final int PADDING_DP = 15;

    TextView studentIdTextView;
    private TableLayout tableLayout;
    private String studentUID;
    TextView studentIdFnameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        studentIdTextView = findViewById(R.id.student_id);
        tableLayout = findViewById(R.id.table_layout);


        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Profile.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
            PopupMenu popupMenu = new PopupMenu(wrapper, hamburgerMenu, 0, 0, R.style.PopupMenuStyle); // Create the PopupMenu with the specified style
            popupMenu.inflate(R.menu.popup_menu); // Inflate the menu

            // Set a menu item click listener for the PopupMenu
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId(); // Get the ID of the clicked menu item
                Intent intent;
                // Get the studentUID from the current intent
                String studentUID = getIntent().getStringExtra("studentUID");

                // Determine which menu item was clicked and start the corresponding activity
                if (itemId == R.id.home) {
                    intent = new Intent(Profile.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Profile.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Profile.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Profile.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Profile.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Profile.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Profile.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Profile.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    return false; // Return false if none of the menu items match
                }

                // Pass the studentUID to the new intent and start the activity
                intent.putExtra("studentUID", studentUID);
                startActivity(intent);
                return true;
            });

            popupMenu.show(); // Show the PopupMenu
        });

        studentUID = getIntent().getStringExtra("studentUID");
        if (studentUID != null) {
            studentIdTextView.setText(studentUID);
        }
        handlePersonalInfoClick();
        fetchAndDisplayStudent_ID_FName();
        setOnClickListeners();
        hideStatusBar();
    }

    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
                getWindow().setDecorFitsSystemWindows(false);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar();
        }
    }

    private void setOnClickListeners() {
        findViewById(R.id.personal_info).setOnClickListener(view -> handlePersonalInfoClick());
        findViewById(R.id.wifi_acc).setOnClickListener(view -> handleWifiAccClick());
        findViewById(R.id.contact_info).setOnClickListener(view -> handleContactInfoClick());
        findViewById(R.id.parents_guardian).setOnClickListener(view -> handleParentsGuardianClick());
    }

    private void handlePersonalInfoClick() {
        tableLayout.removeAllViews(); // Clear existing views

        // Find and update the personal information button text
        TextView personalInfoButton = findViewById(R.id.button_info);
        personalInfoButton.setText(R.string.personal_information);

        // Query the database for personal information
        ConnectionClass connectionClass = new ConnectionClass();
        String query = "SELECT studentLName, studentFName, studentMName, studentNameExt, studentGender, studentBDay, studentCivilStatus, studentNationality, religionName, tribeName, permanentAddress FROM view_STUDENT_DEMOGRAPHIC_COMPLETE WHERE STUDENT_UID = ?";

        try (Connection conn = connectionClass.CONN();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "NDTC-" + studentUID); // Use the correct student UID

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Retrieve student information from the result set
                    String studentLName = rs.getString("studentLName");
                    String studentFName = rs.getString("studentFName");
                    String studentMName = rs.getString("studentMName");
                    String studentNameExt = rs.getString("studentNameExt");
                    String studentGender = rs.getString("studentGender");
                    String studentBDay = rs.getString("studentBDay");

                    // Only get the first 10 characters of the studentBDay
                    if (studentBDay != null && studentBDay.length() > 10) {
                        studentBDay = studentBDay.substring(0, 10);
                    }

                    String studentCivilStatus = rs.getString("studentCivilStatus");
                    String studentNationality = rs.getString("studentNationality");
                    String religionName = rs.getString("religionName");
                    String tribeName = rs.getString("tribeName");
                    String permanentAddress = rs.getString("permanentAddress");

                    // Labels and values for displaying student information
                    String[] labels = {"Last Name", "First Name", "Middle Name", "Name Extensions", "Gender", "Birthdate", "Civil Status", "Nationality", "Religion", "Ethnicity", "Address"};
                    String[] values = {studentLName, studentFName, studentMName, studentNameExt, studentGender, studentBDay, studentCivilStatus, studentNationality, religionName, tribeName, permanentAddress};

                    // Loop through labels and values to create table rows
                    for (int i = 0; i < labels.length; i++) {
                        TableRow tableRow = new TableRow(this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));

                        // Create and set label view
                        TextView labelView = new TextView(this);
                        labelView.setText(labels[i]);
                        labelView.setTextColor(Color.BLACK);
                        labelView.setGravity(android.view.Gravity.START);
                        setPadding(labelView); // Add padding to the label

                        // Create and set value view
                        TextView valueView = new TextView(this);
                        valueView.setText(values[i]);
                        valueView.setTextColor(Color.BLACK);
                        valueView.setGravity(android.view.Gravity.START);
                        valueView.setTypeface(null, Typeface.BOLD);
                        setPadding(valueView); // Add padding to the value

                        // Set layout parameters and margins for label and value views
                        TableRow.LayoutParams labelParams = new TableRow.LayoutParams(
                                20,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1f
                        );
                        TableRow.LayoutParams valueParams = new TableRow.LayoutParams(
                                20,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1f
                        );
                        valueParams.setMargins(90, 0, 0, 0); // Add left margin to the valueView

                        labelView.setLayoutParams(labelParams);
                        valueView.setLayoutParams(valueParams);

                        // Add label and value views to the table row
                        tableRow.addView(labelView);
                        tableRow.addView(valueView);
                        // Add table row to the table layout
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e);
        }
    }


    private void handleWifiAccClick() {
        // Clear existing views from the TableLayout
        tableLayout.removeAllViews();

        // Find the TextView for the personal info button and set its text to "wifi_account"
        TextView personalInfoButton = findViewById(R.id.button_info);
        personalInfoButton.setText(R.string.wifi_account);

        // Query the database for student details
        ConnectionClass connectionClass = new ConnectionClass();
        String query = "SELECT studentFName, studentMName FROM view_STUDENT_DEMOGRAPHIC_COMPLETE WHERE STUDENT_UID = ? ";

        try (Connection conn = connectionClass.CONN();  // Establish the database connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {  // Prepare the SQL query

            // Set the student UID parameter in the query
            pstmt.setString(1, "NDTC-" + studentUID);

            // Execute the query and get the result set
            try (ResultSet rs = pstmt.executeQuery()) {
                // If a result is found, retrieve the username and password
                if (rs.next()) {
                    String username = rs.getString("studentFName");
                    String password = rs.getString("studentMName");

                    // Define labels and values for the table rows
                    String[] labels = {"Username", "Password"};
                    String[] values = {username, password};

                    // Loop through labels and values to create table rows
                    for (int i = 0; i < labels.length; i++) {
                        TableRow tableRow = new TableRow(this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));

                        // Create and set properties for the label TextView
                        TextView labelView = new TextView(this);
                        labelView.setText(labels[i]);
                        labelView.setTextColor(Color.BLACK);
                        labelView.setGravity(android.view.Gravity.START);
                        setPadding(labelView);

                        // Create and set properties for the value TextView
                        TextView valueView = new TextView(this);
                        valueView.setText(values[i]);
                        valueView.setTextColor(Color.BLACK);
                        valueView.setGravity(android.view.Gravity.START);
                        valueView.setTypeface(null, Typeface.BOLD);
                        setPadding(valueView);

                        // Add the label and value TextViews to the TableRow
                        tableRow.addView(labelView);
                        tableRow.addView(valueView);
                        // Add the TableRow to the TableLayout
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } catch (SQLException e) {
            // Log any SQL exceptions that occur
            Log.e("DB Query", "Query error", e);
        }
    }



    private void handleContactInfoClick() {
        // Clear existing views in the table layout
        tableLayout.removeAllViews();
        // Get the TextView for the personal info button and set its text to "Contact Information"
        TextView personalInfoButton = findViewById(R.id.button_info);
        personalInfoButton.setText(R.string.contact_information);

        // SQL query to retrieve contact information from the database
        String query = "SELECT studentContactNo, studentLName FROM view_STUDENT_DEMOGRAPHIC_COMPLETE WHERE STUDENT_UID = ?";

        // Create a new connection to the database
        ConnectionClass connectionClass = new ConnectionClass();
        try (Connection conn = connectionClass.CONN();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the student UID in the query
            pstmt.setString(1, "NDTC-" + studentUID);

            // Execute the query and process the result set
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Retrieve contact number and email address from the result set
                    String contactNum = rs.getString("studentContactNo");
                    String EmailAdd = rs.getString("studentLName");

                    // Define labels and corresponding values for display
                    String[] labels = {"Contact Number", "Email Address"};
                    String[] values = {contactNum, EmailAdd};

                    // Loop through the labels and values to create table rows
                    for (int i = 0; i < labels.length; i++) {
                        // Create a new table row
                        TableRow tableRow = new TableRow(this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));

                        // Create and configure the label view
                        TextView labelView = new TextView(this);
                        labelView.setText(labels[i]);
                        labelView.setTextColor(Color.BLACK);
                        labelView.setGravity(android.view.Gravity.START);
                        setPadding(labelView);

                        // Create and configure the value view
                        TextView valueView = new TextView(this);
                        valueView.setText(values[i]);
                        valueView.setTextColor(Color.BLACK);
                        valueView.setGravity(android.view.Gravity.START);
                        valueView.setTypeface(null, Typeface.BOLD);
                        setPadding(valueView);

                        // Add the label and value views to the table row
                        tableRow.addView(labelView);
                        tableRow.addView(valueView);
                        // Add the table row to the table layout
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } catch (SQLException e) {
            // Log any SQL exceptions that occur
            Log.e("DB Query", "Query error", e);
        }
    }


    private void handleParentsGuardianClick() {
        tableLayout.removeAllViews(); // Clear existing views

        // Update the button text to "Parents/Guardian"
        TextView personalInfoButton = findViewById(R.id.button_info);
        personalInfoButton.setText(R.string.parents_guardian);

        // Labels for the table rows
        String[] labels = {
                "Father's Name", "Father's Contact Number", "Father's Occupation",
                "Father's Educational Attainment", "Mother's Name", "Mother's Contact Number",
                "Mother's Occupation", "Guardian's Name", "Guardian's Contact Number",
                "Guardian's Occupation", "Guardian's Address"
        };

        // Query the database for student details
        ConnectionClass connectionClass = new ConnectionClass();
        String query = "SELECT fathers_Name, studentBDay, fathers_Occupation, programCode, mothers_Name, studentFName, mothers_Occupation, guardianName, guardianContactNo, yearLevel, guardianAddress FROM view_STUDENT_DEMOGRAPHIC_COMPLETE WHERE STUDENT_UID = ?";

        try (Connection conn = connectionClass.CONN(); // Establish a database connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "NDTC-" + studentUID); // Use the correct enrollment UID

            try (ResultSet rs = pstmt.executeQuery()) { // Execute the query
                if (rs.next()) { // Check if there are results

                    // Retrieve data from the result set
                    String studentFatherName = rs.getString("fathers_Name");
                    String fatherContactNum = rs.getString("studentBDay");
                    String fathersOccupation = rs.getString("fathers_Occupation");
                    String fatherEducationalAtt = rs.getString("programCode");
                    String mothersName = rs.getString("mothers_Name");
                    String mothersContactNo = rs.getString("studentFName");
                    String mothersOccupation = rs.getString("mothers_Occupation");
                    String guardianName = rs.getString("guardianName");
                    String guardianContactNo = rs.getString("guardianContactNo");
                    String guardianOccupation = rs.getString("yearLevel");
                    String guardianAddress = rs.getString("guardianAddress");

                    // Values corresponding to the labels
                    String[] values = {studentFatherName, "", fathersOccupation, "", mothersName, "", mothersOccupation, guardianName, guardianContactNo, "", guardianAddress};

                    // Create table rows dynamically
                    for (int i = 0; i < labels.length; i++) {
                        TableRow tableRow = new TableRow(this);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                        ));

                        // Create and configure the label view
                        TextView labelView = new TextView(this);
                        labelView.setText(labels[i]);
                        labelView.setTextColor(Color.BLACK);
                        labelView.setGravity(android.view.Gravity.START);
                        setPadding(labelView); // Set padding for the label view

                        // Create and configure the value view
                        TextView valueView = new TextView(this);
                        valueView.setText(values[i]);
                        valueView.setTextColor(Color.BLACK);
                        valueView.setGravity(android.view.Gravity.START);
                        valueView.setTypeface(null, Typeface.BOLD);
                        setPadding(valueView); // Set padding for the value view

                        // Add the label and value views to the table row
                        tableRow.addView(labelView);
                        tableRow.addView(valueView);

                        // Add the table row to the table layout
                        tableLayout.addView(tableRow);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e); // Log any SQL exceptions
        }
    }

    private void fetchAndDisplayStudent_ID_FName() {
        new Thread(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection conn = connectionClass.CONN(); // Establish database connection
            if (conn != null) {
                try {
                    // Prepare SQL query to fetch student UID and full name based on a given student UID
                    String query = "SELECT student_UID, studentFullName FROM view_STUDENT_DEMOGRAPHIC_COMPLETE WHERE STUDENT_UID = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, "NDTC-" + studentUID); // Set the parameter for the query
                    ResultSet rs = pstmt.executeQuery(); // Execute the query

                    if (rs.next()) {
                        // Retrieve student UID and full name from the result set
                        final String studentUid = rs.getString("student_UID");
                        final String studentFullName = rs.getString("studentFullName");

                        // Log the retrieved data
                        Log.d("DB Data", "Student UID: " + studentUid);
                        Log.d("DB Data", "Student Full Name: " + studentFullName);

                        // Format student UID by removing the first five characters
                        final String formattedStudentUid = studentUid.length() > 5 ? studentUid.substring(5) : studentUid;

                        // Update the UI with the retrieved and formatted data
                        runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                studentIdFnameTextView = findViewById(R.id.student_ID_Fname); // Re-initialize in case of scope issues
                                studentIdFnameTextView.setText(studentFullName + "\n" + formattedStudentUid);
                                Log.d("UI Update", "TextView updated with: " + studentFullName + " - " + formattedStudentUid);
                            }
                        });
                    } else {
                        // Log if no data is found for the given student UID
                        Log.d("DB Data", "No data found for student UID: " + studentUID);
                    }

                    // Close the result set, prepared statement, and connection
                    rs.close();
                    pstmt.close();
                    conn.close();
                } catch (SQLException e) {
                    // Log any SQL exceptions that occur
                    Log.e("DB Connection", "Error fetching data", e);
                }
            } else {
                // Log if the connection is null
                Log.e("DB Connection", "Connection is null");
            }
        }).start();
    }


    private void setPadding(TextView textView) {
        // Get the screen density scale factor
        float scale = getResources().getDisplayMetrics().density;

        // Convert padding from dp to pixels, with rounding
        int leftPx = (int) (Profile.PADDING_DP * scale + 0.5f);
        int topPx = (int) (Profile.PADDING_DP * scale + 0.5f);
        int rightPx = (int) (Profile.PADDING_DP * scale + 1.0f); // Slightly different value for right padding
        int bottomPx = (int) (Profile.PADDING_DP * scale + 0.5f);

        // Set padding for the TextView
        textView.setPadding(leftPx, topPx, rightPx, bottomPx);
    }


}

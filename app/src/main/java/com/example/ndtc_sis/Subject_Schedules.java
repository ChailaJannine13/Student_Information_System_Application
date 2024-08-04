package com.example.ndtc_sis;

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
import java.util.ArrayList;
import java.util.List;

public class Subject_Schedules extends AppCompatActivity {
    private static final int PADDING_DP = 15; // Constant for padding in dp

    TextView studentIdTextView;
    private TableLayout tableLayouts;
    private String studentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        setContentView(R.layout.activity_subjects_schedules); // Set the layout for the activity

        studentIdTextView = findViewById(R.id.student_id); // Initialize the TextView for student ID

        tableLayouts = findViewById(R.id.subject_table); // Initialize the TableLayout for subjects

        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Subject_Schedules.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(Subject_Schedules.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Subject_Schedules.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Subject_Schedules.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Subject_Schedules.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Subject_Schedules.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Subject_Schedules.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Subject_Schedules.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Subject_Schedules.this, MainActivity.class);
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

        studentUID = getIntent().getStringExtra("studentUID"); // Get the student UID from the intent
        if (studentUID != null) {
            studentIdTextView.setText(studentUID); // Set the student UID in the TextView
        }

        getStudentGradesData(); // Fetch and display student grades data
        hideStatusBar(); // Hide the status bar
    }

    // Method to hide the status bar
    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android R and above
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars());
                getWindow().setDecorFitsSystemWindows(false);
            }
        } else {
            // For older versions
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
        hideStatusBar(); // Hide the status bar when the activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar(); // Hide the status bar when the window gains focus
        }
    }

    // Method to fetch student grades data from the database
    private void getStudentGradesData() {
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = connectionClass.CONN();
            if (conn != null) {
                // Query to get the latest enrollment UID for the student
                String enrollmentUidQuery = "SELECT DISTINCT TOP 1 ENROLLMENT_UID FROM view_STUDENT_GRADES WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
                pstmt = conn.prepareStatement(enrollmentUidQuery);
                pstmt.setString(1, "NDTC-" + studentUID);
                rs = pstmt.executeQuery();

                List<String> enrollmentUids = new ArrayList<>();
                while (rs.next()) {
                    enrollmentUids.add(rs.getString("ENROLLMENT_UID"));
                }

                rs.close();
                pstmt.close();

                if (!enrollmentUids.isEmpty()) {
                    for (int i = 0; i < enrollmentUids.size(); i++) {
                        // Query to get the subjects enrolled for each enrollment UID
                        String sql = "SELECT subjectCode, subjectDescription,Schedule,TeacherName, roomName, unit, ContactHours FROM view_SUBJECT_ENROLLED WHERE ENROLLMENT_UID = ?";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, enrollmentUids.get(i));
                        rs = pstmt.executeQuery();

                        processResultSet(rs); // Process the result set and display the data
                    }
                } else {
                    Log.e("DB Query", "No ENROLLMENT_UIDs found for student: " + studentUID);
                }
            } else {
                Log.e("DB Connection", "Connection is null");
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                Log.e("DB Cleanup", "Cleanup error", se);
            }
        }
    }

    // Method to add a header row to the table layout
    private void addHeaderRow(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Array of headers for the table
        String[] headers = {"Subject Code", "Subject Description", "Time/Days", "Teacher", "Room", "Unit", "Contact Hrs"};

        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setTextColor(Color.BLACK);
            textView.setAllCaps(true);
            textView.setTypeface(null, Typeface.BOLD);
            setPadding(textView);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow); // Add the header row to the table layout
    }

    // Method to process the result set and add data rows to the table layout
    private void processResultSet(ResultSet rs) throws SQLException {
        // Add header row to the table layout if it's empty
        if (tableLayouts.getChildCount() == 0) {
            addHeaderRow(tableLayouts);
        }

        while (rs.next()) {
            String subjectCode = rs.getString("subjectCode");
            String subjectDescription = rs.getString("subjectDescription");
            String schedule = rs.getString("Schedule");
            String teacherName = rs.getString("TeacherName");
            String roomName = rs.getString("roomName");
            String unit = rs.getString("unit");
            String contactHours = rs.getString("ContactHours");

            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            // Create TextViews for each column and set their properties
            TextView subjectCodeTV = createTextView(subjectCode);
            tableRow.addView(subjectCodeTV);

            TextView subjectDescriptionTV = createTextView(subjectDescription);
            tableRow.addView(subjectDescriptionTV);

            TextView scheduleTV = createTextView(schedule);
            tableRow.addView(scheduleTV);

            TextView teacherNameTV = createTextView(teacherName);
            tableRow.addView(teacherNameTV);

            TextView roomNameTV = createTextView(roomName);
            tableRow.addView(roomNameTV);

            TextView subjectUnitTV = createTextView(unit);
            tableRow.addView(subjectUnitTV);

            TextView contactHoursTV = createTextView(contactHours);
            tableRow.addView(contactHoursTV);

            // Add the row to the appropriate TableLayout
            tableLayouts.addView(tableRow);

            Log.d("DB Query", "Added row: " + subjectCode + ", " + subjectDescription + ", " + schedule + ", " + teacherName + ", " + roomName + ", " + unit + ", " + contactHours);
        }
    }

    // Method to create a TextView for table rows
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(Color.BLACK); // Ensure the text color is visible
        setPadding(textView);
        return textView;
    }

    // Method to set padding for TextViews
    private void setPadding(TextView textView) {
        float scale = getResources().getDisplayMetrics().density;
        int leftPx = (int) (Subject_Schedules.PADDING_DP * scale + 0.5f);
        int topPx = (int) (Subject_Schedules.PADDING_DP * scale + 0.5f);
        int rightPx = (int) (Subject_Schedules.PADDING_DP * scale + 0.5f);
        int bottomPx = (int) (Subject_Schedules.PADDING_DP * scale + 0.5f);
        textView.setPadding(leftPx, topPx, rightPx, bottomPx);
    }
}

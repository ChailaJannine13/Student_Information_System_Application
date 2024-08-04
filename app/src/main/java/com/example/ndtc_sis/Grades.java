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

public class Grades extends AppCompatActivity {
    private static final int PADDING_DP = 15;

    TextView studentIdTextView;
    private TableLayout tableLayout;
    private String enrollmentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_grades);

        studentIdTextView = findViewById(R.id.student_id);
        tableLayout = findViewById(R.id.grade_table);

        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Grades.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(Grades.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Grades.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Grades.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Grades.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Grades.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Grades.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Grades.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Grades.this, MainActivity.class);
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

        String studentUID = getIntent().getStringExtra("studentUID");
        enrollmentUID = getIntent().getStringExtra("ENROLLMENT_UID");
        if (studentUID != null) {
            studentIdTextView.setText(studentUID);
        }

        // Fetch and display the student's grades data
        getStudentGradesData();
        hideStatusBar();
    }

    private void hideStatusBar() {
        // Hide the status bar for immersive full-screen experience
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
        hideStatusBar(); // Ensure the status bar is hidden when the activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar(); // Ensure the status bar is hidden when the window gains focus
        }
    }

    // Fetch the student's grades data from the database
    private void getStudentGradesData() {
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = connectionClass.CONN(); // Establish the database connection
            if (conn != null) {
                // Query to fetch college grades
                String sql1 = "SELECT subjectCode, subjectDescription, unit, CGFinalGrade FROM view_STUDENT_GRADES WHERE ENROLLMENT_UID = ?";
                pstmt = conn.prepareStatement(sql1);
                pstmt.setString(1, enrollmentUID);
                rs = pstmt.executeQuery();

                boolean dataFound = processResultSet(rs, true);

                if (!dataFound) {
                    // Query to fetch basic education grades if no college grades are found
                    String sql2 = "SELECT subjectCode, subjectDescription, schoolYear, Semester FROM view_BASIC_ED_GRADES_GS_JHS_SHS WHERE ENROLLMENT_UID = ?";
                    pstmt = conn.prepareStatement(sql2);
                    pstmt.setString(1, enrollmentUID);
                    rs = pstmt.executeQuery();
                    processResultSet(rs, false);
                }
            } else {
                Log.e("DB Connection", "Connection is null");
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e);
        } finally {
            try {
                // Close the database resources
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                Log.e("DB Cleanup", "Cleanup error", se);
            }
        }
    }

    // Process the result set and populate the table with grades data
    private boolean processResultSet(ResultSet rs, boolean isCollege) throws SQLException {
        boolean dataFound = false;

        if (tableLayout.getChildCount() == 0) {
            addHeaderRow(tableLayout, isCollege); // Add the header row if the table is empty
        }

        while (rs.next()) {
            String subjectCode = rs.getString("subjectCode");
            String subjectDescription = rs.getString("subjectDescription");
            String subjectUnitOrSchoolYear = isCollege ? rs.getString("unit") : rs.getString("schoolYear");
            String finalGradeOrSemester = isCollege ? rs.getString("CGFinalGrade") : rs.getString("Semester");

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

            TextView subjectUnitOrSchoolYearTV = createTextView(subjectUnitOrSchoolYear);
            tableRow.addView(subjectUnitOrSchoolYearTV);

            TextView finalGradeOrSemesterTV = createTextView(finalGradeOrSemester);
            tableRow.addView(finalGradeOrSemesterTV);

            // Add the row to the TableLayout
            tableLayout.addView(tableRow);

            Log.d("DB Query", "Added row: " + subjectCode + ", " + subjectDescription + ", " + subjectUnitOrSchoolYear + ", " + finalGradeOrSemester);
            dataFound = true;
        }

        return dataFound;
    }

    // Create a TextView for a table cell
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(Color.BLACK); // Ensure the text color is visible
        setPadding(textView);
        return textView;
    }

    // Set padding for a TextView with specified DP values
    private void setPadding(TextView textView) {
        float scale = getResources().getDisplayMetrics().density;
        int leftPx = (int) (Grades.PADDING_DP * scale + 0.5f);
        int topPx = (int) (Grades.PADDING_DP * scale + 0.5f);
        int rightPx = (int) (Grades.PADDING_DP * scale + 0.5f);
        int bottomPx = (int) (Grades.PADDING_DP * scale + 0.5f);
        textView.setPadding(leftPx, topPx, rightPx, bottomPx);
    }

    // Add a header row to the table
    private void addHeaderRow(TableLayout tableLayout, boolean isCollege) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        String[] headers = isCollege ?
                new String[]{"Subject Code", "Subject Description", "Subject Unit", "Final Grade"} :
                new String[]{"Subject Code", "Subject Description", "School Year", "Semester"};

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

        tableLayout.addView(headerRow);
    }
}

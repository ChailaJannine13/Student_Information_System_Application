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

public class Enrollment extends AppCompatActivity {
    private static final int PADDING_DP = 15;

    TextView studentIdTextView;
    private TableLayout tableLayout, tableLayout2;
    private String studentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enrollment);

        // Initialize UI components
        studentIdTextView = findViewById(R.id.student_id);
        tableLayout = findViewById(R.id.enrollment_table);
        tableLayout2 = findViewById(R.id.enrollment_table_2);

        // Setup hamburger menu click listener
        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Enrollment.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(Enrollment.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Enrollment.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Enrollment.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Enrollment.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Enrollment.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Enrollment.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Enrollment.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Enrollment.this, MainActivity.class);
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

        // Get the studentUID from the intent and set it in the TextView
        studentUID = getIntent().getStringExtra("studentUID");
        if (studentUID != null) {
            studentIdTextView.setText(studentUID);
        }

        // Add header rows to the tables
        addHeaderRow(tableLayout);
        addHeaderRow(tableLayout2);

        // Retrieve and display student enrollment data
        getStudentEnrollmentData();
        hideStatusBar();
    }

    // Hide the status bar for a fullscreen experience
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

    // Fetch the student's enrollment data from the database
    private void getStudentEnrollmentData() {
        ConnectionClass connectionClass = new ConnectionClass();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = connectionClass.CONN();
            if (conn != null) {
                List<EnrollmentData> enrollmentDataList = new ArrayList<>();
                EnrollmentData currentEnrollmentData = null;

                // Check and retrieve from view_STUDENT_GRADES
                String query1 = "SELECT DISTINCT ENROLLMENT_UID, schoolYear, Semester, yearLevel, programMajor FROM view_STUDENT_GRADES WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
                pstmt = conn.prepareStatement(query1);
                pstmt.setString(1, "NDTC-" + studentUID);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    EnrollmentData data = new EnrollmentData(
                            rs.getString("ENROLLMENT_UID"),
                            rs.getString("schoolYear"),
                            rs.getString("Semester"),
                            rs.getString("yearLevel"),
                            rs.getString("programMajor")
                    );
                    enrollmentDataList.add(data);
                    if (currentEnrollmentData == null) {
                        currentEnrollmentData = data;
                    }
                }
                rs.close();
                pstmt.close();

                // Check and retrieve from view_ENROLLMENT if not found in view_STUDENT_GRADES
                if (enrollmentDataList.isEmpty()) {
                    String query2 = "SELECT DISTINCT ENROLLMENT_UID, schoolYear, Semester, yearLevel, Dept_Name FROM view_BASIC_ED_GRADES_GS_JHS_SHS WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
                    pstmt = conn.prepareStatement(query2);
                    pstmt.setString(1, "NDTC-" + studentUID);
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        EnrollmentData data = new EnrollmentData(
                                rs.getString("ENROLLMENT_UID"),
                                rs.getString("schoolYear"),
                                rs.getString("Semester"),
                                rs.getString("yearLevel"),
                                rs.getString("Dept_Name")
                        );
                        enrollmentDataList.add(data);
                        if (currentEnrollmentData == null) {
                            currentEnrollmentData = data;
                        }
                    }
                    rs.close();
                    pstmt.close();
                }

                // Check and retrieve from view_BASIC_ED_GRADES_GS_JHS_SHS if not found in previous views
                if (enrollmentDataList.isEmpty()) {
                    String query3 = "SELECT ENROLLMENT_UID, schoolYear, Semester, yearLevel, Dept_Name as programMajor FROM view_BASIC_ED_GRADES_GS_JHS_SHS WHERE STUDENT_UID = ?";
                    pstmt = conn.prepareStatement(query3);
                    pstmt.setString(1, "NDTC-" + studentUID);
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        EnrollmentData data = new EnrollmentData(
                                rs.getString("ENROLLMENT_UID"),
                                rs.getString("schoolYear"),
                                rs.getString("Semester"),
                                rs.getString("yearLevel"),
                                rs.getString("programMajor")
                        );
                        enrollmentDataList.add(data);
                        if (currentEnrollmentData == null) {
                            currentEnrollmentData = data;
                        }
                    }
                    rs.close();
                    pstmt.close();
                }

                // Display enrollment data in tables
                if (!enrollmentDataList.isEmpty()) {
                    displayEnrollmentData(currentEnrollmentData, tableLayout);
                    for (EnrollmentData data : enrollmentDataList) {
                        if (!data.equals(currentEnrollmentData)) {
                            displayEnrollmentData(data, tableLayout2);
                        }
                    }
                } else {
                    // Handle case where no enrollment data is found
                    TableRow noDataRow = new TableRow(this);
                    TextView noDataText = new TextView(this);
                    noDataText.setText(R.string.empty_data);
                    noDataRow.addView(noDataText);
                    tableLayout.addView(noDataRow);
                }
            } else {
                Log.e("Enrollment", "Connection is null");
            }
        } catch (SQLException e) {
            Log.e("Enrollment", "SQL Exception: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                Log.e("Enrollment", "SQL Exception in finally: " + e.getMessage());
            }
        }
    }

    // Display a row of enrollment data in the specified table
    private void displayEnrollmentData(EnrollmentData data, TableLayout targetTableLayout) {
        TableRow tableRow = new TableRow(this);

        addCellToRow(tableRow, data.getSchoolYear());
        addCellToRow(tableRow, data.getSemester());
        addCellToRow(tableRow, data.getYearLevel());
        addCellToRow(tableRow, data.getProgramMajor());

        // Add OnClickListener to the TableRow
        tableRow.setOnClickListener(v -> {
            Intent intent = new Intent(Enrollment.this, Grades.class);
            intent.putExtra("ENROLLMENT_UID", data.getEnrollmentUID());
            String studentUID = getIntent().getStringExtra("studentUID");
            intent.putExtra("studentUID", studentUID);
            startActivity(intent);
        });

        targetTableLayout.addView(tableRow);
    }

    // Add a cell to the given row
    private void addCellToRow(TableRow row, String cellText) {
        TextView textView = new TextView(this);
        textView.setText(cellText);
        textView.setPadding(dpToPx(), dpToPx(), dpToPx(), dpToPx());
        textView.setTextColor(Color.BLACK);
        row.addView(textView);
    }

    // Convert dp to pixels
    private int dpToPx() {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(Enrollment.PADDING_DP * density);
    }

    // Add header row to the given table layout
    private void addHeaderRow(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        String[] headers = {"School Year", "Semester", "Year Level", "Program Major"};

        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setTextColor(Color.BLACK);
            textView.setAllCaps(true);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(dpToPx(), dpToPx(), dpToPx(), dpToPx());
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    // Class to hold enrollment data
    private static class EnrollmentData {
        private final String enrollmentUID;
        private final String schoolYear;
        private final String semester;
        private final String yearLevel;
        private final String programMajor;

        public EnrollmentData(String enrollmentUID, String schoolYear, String semester, String yearLevel, String programMajor) {
            this.enrollmentUID = enrollmentUID;
            this.schoolYear = schoolYear;
            this.semester = semester;
            this.yearLevel = yearLevel;
            this.programMajor = programMajor;
        }

        public String getEnrollmentUID() {
            return enrollmentUID;
        }

        public String getSchoolYear() {
            return schoolYear;
        }

        public String getSemester() {
            return semester;
        }

        public String getYearLevel() {
            return yearLevel;
        }

        public String getProgramMajor() {
            return programMajor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EnrollmentData that = (EnrollmentData) o;

            return enrollmentUID.equals(that.enrollmentUID);
        }

        @Override
        public int hashCode() {
            return enrollmentUID.hashCode();
        }
    }
}

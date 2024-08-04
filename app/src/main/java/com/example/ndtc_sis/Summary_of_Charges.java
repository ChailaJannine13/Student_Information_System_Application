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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Summary_of_Charges extends AppCompatActivity {
    private static final int PADDING_DP = 15;

    TextView studentIdTextView;
    private TableLayout tableLayout;
    private String studentUID;

    // Variables to store total values
    private double totalAccountCharges = 0;
    private double totalPayments = 0;
    private double totalDiscounts = 0;

    private AutoCompleteTextView schoolYearSemesterDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge experience
        setContentView(R.layout.activity_summary_of_charges);

        studentIdTextView = findViewById(R.id.student_id);

        tableLayout = findViewById(R.id.summary_of_charges_table);
        schoolYearSemesterDropdown = findViewById(R.id.schoolYearSemesterDropdown);

        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Summary_of_Charges.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(Summary_of_Charges.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Summary_of_Charges.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Summary_of_Charges.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Summary_of_Charges.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Summary_of_Charges.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Summary_of_Charges.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Summary_of_Charges.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Summary_of_Charges.this, MainActivity.class);
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

        getEnrollmentData(); // Fetch and display enrollment data
        hideStatusBar(); // Hide the status bar
    }

    // Method to hide the status bar for an immersive experience
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
        hideStatusBar(); // Hide the status bar when the activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar(); // Hide the status bar when the window gains focus
        }
    }

    // Method to fetch enrollment data from the database
    private void getEnrollmentData() {
        ConnectionClass connectionClass = new ConnectionClass();
        String deptNameQuery = "SELECT DISTINCT TOP 1 ENROLLMENT_UID, Dept_Name FROM view_ENROLLMENT WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
        String enrollmentQueryCollege = "SELECT DISTINCT TOP 4 ENROLLMENT_UID, schoolYear, Semester, programCode, yearLevel FROM view_ENROLLMENT WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
        String enrollmentQueryBasicEd = "SELECT DISTINCT TOP 4 ENROLLMENT_UID, schoolYear, Semester, programCode FROM view_BASIC_ED_GRADES_GS_JHS_SHS WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";

        try (Connection conn = connectionClass.CONN();
             PreparedStatement pstmt = conn.prepareStatement(deptNameQuery)) {

            pstmt.setString(1, "NDTC-" + studentUID);
            try (ResultSet rs = pstmt.executeQuery()) {
                String enrollmentQuery;

                if (rs.next()) {
                    String deptName = rs.getString("Dept_Name");

                    // Determine the query based on Dept_Name
                    if ("COLLEGE".equalsIgnoreCase(deptName)) {
                        enrollmentQuery = enrollmentQueryCollege;
                    } else {
                        enrollmentQuery = enrollmentQueryBasicEd;
                    }

                    try (PreparedStatement pstmt2 = conn.prepareStatement(enrollmentQuery)) {
                        pstmt2.setString(1, "NDTC-" + studentUID);
                        try (ResultSet rs2 = pstmt2.executeQuery()) {
                            List<String> enrollmentData = new ArrayList<>();
                            Map<String, String> enrollmentMap = new HashMap<>();

                            while (rs2.next()) {
                                String enrollmentUid = rs2.getString("ENROLLMENT_UID");
                                String schoolYear = rs2.getString("schoolYear");
                                String semester = rs2.getString("Semester");

                                String displayText;
                                if (enrollmentQuery.equals(enrollmentQueryCollege)) {
                                    // Student is from college
                                    String programCode = rs2.getString("programCode");
                                    String yearLevel = rs2.getString("yearLevel").substring(0, 1); // Get the first character of yearLevel
                                    displayText = programCode + " - " + yearLevel + " - " + schoolYear;
                                    if (!"N/A".equalsIgnoreCase(semester)) {
                                        displayText += " - " + semester;
                                    }
                                } else {
                                    // Student is from grade school, junior high school, or senior high school
                                    String gradeLevel = rs2.getString("programCode");
                                    displayText = gradeLevel + " - " + schoolYear;
                                    if (!"N/A".equalsIgnoreCase(semester)) {
                                        displayText += " - " + semester;
                                    }
                                }

                                enrollmentData.add(displayText);
                                enrollmentMap.put(displayText, enrollmentUid); // Store ENROLLMENT_UID with display text
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line, enrollmentData);
                            schoolYearSemesterDropdown.setAdapter(adapter);
                            if (!enrollmentData.isEmpty()) {
                                String latestEnrollment = enrollmentData.get(0);
                                schoolYearSemesterDropdown.setText(latestEnrollment, false);
                                getStudentSummary_of_ChargesData(enrollmentMap.get(latestEnrollment)); // Load data for the latest enrollment
                            }
                            schoolYearSemesterDropdown.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedText = (String) parent.getItemAtPosition(position);
                                String selectedEnrollmentUid = enrollmentMap.get(selectedText);
                                getStudentSummary_of_ChargesData(selectedEnrollmentUid); // Update data based on selected enrollment UID
                            });
                        }
                    }
                } else {
                    Log.w("DB Query", "No department name found for the given student UID.");
                }
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e);
        }
    }

    // Method to fetch and display summary of charges data
    private void getStudentSummary_of_ChargesData(String enrollmentUid) {
        ConnectionClass connectionClass = new ConnectionClass();
        String sql;

        if (enrollmentUid == null) {
            sql = "SELECT acctName, AccountCharges, TotalPayments, TotalDiscount, Balances FROM view_ACCOUNT_SUMMARY WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
        } else {
            sql = "SELECT acctName, AccountCharges, TotalPayments, TotalDiscount, Balances FROM view_ACCOUNT_SUMMARY WHERE ENROLLMENT_UID = ?";
        }

        try (Connection conn = connectionClass.CONN();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (enrollmentUid == null) {
                pstmt.setString(1, "NDTC-" + studentUID);
            } else {
                pstmt.setString(1, enrollmentUid);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                // Clear previous data
                tableLayout.removeAllViews();
                totalAccountCharges = 0;
                totalPayments = 0;
                totalDiscounts = 0;

                processResultSet(rs); // Process the result set and display data
            }
        } catch (SQLException e) {
            Log.e("DB Query", "Query error", e);
        }
    }

    // Method to add header row to the table
    private void addHeaderRow(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        String[] headers = {"Account Name", "Account Charges", "Payments", "Discounts", "Balances"};

        for (int i = 0; i < headers.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(headers[i]);
            if (i == 0) {
                textView.setGravity(android.view.Gravity.START); // Align first header to the left
            } else {
                textView.setGravity(android.view.Gravity.CENTER); // Center align other headers
            }
            textView.setTextColor(Color.BLACK);
            textView.setAllCaps(true);
            textView.setTypeface(null, Typeface.BOLD);
            setPadding(textView);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    // Method to process the result set and display data in the table
    private void processResultSet(ResultSet rs) throws SQLException {
        // Add header row to the table layout if it's empty
        if (tableLayout.getChildCount() == 0) {
            addHeaderRow(tableLayout);
        }

        while (rs.next()) {
            String Account_Name = rs.getString("acctName");
            double Account_Charges = rs.getDouble("AccountCharges");
            double Payments = rs.getDouble("TotalPayments");
            double Discount = rs.getDouble("TotalDiscount");
            double Balances = rs.getDouble("Balances");

            // Update total values
            totalAccountCharges += Account_Charges;
            totalPayments += Payments;
            totalDiscounts += Discount;

            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            // Create TextViews for each column and set their properties
            TextView accountNameTV = createTextView(Account_Name, true);
            tableRow.addView(accountNameTV);

            TextView accountChargesTV = createTextView(String.valueOf(Account_Charges), false);
            tableRow.addView(accountChargesTV);

            TextView totalPaymentsTV = createTextView(String.valueOf(Payments), false);
            tableRow.addView(totalPaymentsTV);

            TextView discountTV = createTextView(String.valueOf(Discount), false);
            tableRow.addView(discountTV);

            TextView balancesTV = createTextView(String.valueOf(Balances), false);
            tableRow.addView(balancesTV);

            // Add the row to the TableLayout
            tableLayout.addView(tableRow);

            Log.d("DB Query", "Added row: " + Account_Name + ", " + Account_Charges + ", " + Payments + ", " + Discount + ", " + Balances);
        }

        // Add the TOTAL row after processing all rows
        addTotalRow();
    }

    // Method to add a total row to the table
    private void addTotalRow() {
        TableRow totalRow = new TableRow(this);
        totalRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Calculate the total balance after deductions
        double totalBalanceAfterDeductions = totalAccountCharges - totalPayments - totalDiscounts;

        String[] totals = {"TOTAL", String.valueOf(totalAccountCharges), String.valueOf(totalPayments), String.valueOf(totalDiscounts), String.valueOf(totalBalanceAfterDeductions)};

        for (int i = 0; i < totals.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(totals[i]);
            if (i == 0) {
                textView.setGravity(android.view.Gravity.START); // Align first column to the left
            } else {
                textView.setGravity(android.view.Gravity.CENTER); // Center align other columns
            }
            textView.setTextColor(Color.BLACK);
            textView.setTypeface(null, Typeface.BOLD); // Make the text bold
            setPadding(textView);
            totalRow.addView(textView);
        }

        tableLayout.addView(totalRow);
    }

    // Helper method to create a TextView with specific properties
    private TextView createTextView(String text, boolean isFirstColumn) {
        TextView textView = new TextView(this);
        textView.setText(text);
        if (isFirstColumn) {
            textView.setGravity(android.view.Gravity.START); // Align first column to the left
        } else {
            textView.setGravity(android.view.Gravity.CENTER); // Center align other columns
        }
        textView.setTextColor(Color.BLACK); // Ensure the text color is visible
        setPadding(textView);
        return textView;
    }

    // Helper method to set padding for a TextView
    private void setPadding(TextView textView) {
        float scale = getResources().getDisplayMetrics().density;
        int leftPx = (int) (Summary_of_Charges.PADDING_DP * scale + 0.5f);
        int topPx = (int) (Summary_of_Charges.PADDING_DP * scale + 0.5f);
        int rightPx = (int) (Summary_of_Charges.PADDING_DP * scale + 0.5f);
        int bottomPx = (int) (Summary_of_Charges.PADDING_DP * scale + 0.5f);
        textView.setPadding(leftPx, topPx, rightPx, bottomPx);
    }
}

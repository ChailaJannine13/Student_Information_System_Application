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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Summary_of_Payments extends AppCompatActivity {
    private static final int PADDING_DP = 15;

    TextView studentIdTextView;
    private TableLayout tableLayout;
    private String studentUID;

    private AutoCompleteTextView schoolYearSemesterDropdown;
    private ExecutorService executorService;
    private TextView loadingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge display
        setContentView(R.layout.activity_summary_of_payments);

        // Initialize UI elements
        studentIdTextView = findViewById(R.id.student_id);
        tableLayout = findViewById(R.id.summary_of_payments_table);
        schoolYearSemesterDropdown = findViewById(R.id.schoolYearSemesterDropdown);
        executorService = Executors.newSingleThreadExecutor();

        // Setup hamburger menu button
        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(Summary_of_Payments.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(Summary_of_Payments.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Summary_of_Payments.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Summary_of_Payments.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Summary_of_Payments.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Summary_of_Payments.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Summary_of_Payments.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Summary_of_Payments.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Summary_of_Payments.this, MainActivity.class);
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

        // Get studentUID from intent and set it to TextView
        studentUID = getIntent().getStringExtra("studentUID");
        if (studentUID != null) {
            studentIdTextView.setText(studentUID);
        }

        // Fetch enrollment data
        getEnrollmentData();
        hideStatusBar(); // Hide status bar for full-screen display
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
        hideStatusBar(); // Ensure status bar is hidden when activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar(); // Ensure status bar is hidden when window gains focus
        }
    }

    private void getEnrollmentData() {
        runOnUiThread(this::showLoadingIndicator); // Show loading indicator on UI thread
        executorService.execute(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            String deptNameQuery = "SELECT DISTINCT TOP 1 ENROLLMENT_UID, Dept_Name FROM view_ENROLLMENT WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
            String enrollmentQueryCollege = "SELECT DISTINCT TOP 4 ENROLLMENT_UID, schoolYear, Semester, programCode, yearLevel FROM view_ENROLLMENT WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
            String enrollmentQueryBasicEd = "SELECT DISTINCT TOP 4 ENROLLMENT_UID, schoolYear, Semester, programCode FROM view_BASIC_ED_GRADES_GS_JHS_SHS WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";

            try (Connection conn = connectionClass.CONN();
                 PreparedStatement pstmt = conn.prepareStatement(deptNameQuery)) {

                pstmt.setString(1, "NDTC-" + studentUID); // Set student UID parameter
                try (ResultSet rs = pstmt.executeQuery()) {
                    String enrollmentQuery;

                    if (rs.next()) {
                        String deptName = rs.getString("Dept_Name");

                        // Determine which query to use based on department name
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

                                // Process enrollment data
                                while (rs2.next()) {
                                    String enrollmentUid = rs2.getString("ENROLLMENT_UID");
                                    String schoolYear = rs2.getString("schoolYear");
                                    String semester = rs2.getString("Semester");

                                    String displayText;
                                    if (enrollmentQuery.equals(enrollmentQueryCollege)) {
                                        String programCode = rs2.getString("programCode");
                                        String yearLevel = rs2.getString("yearLevel").substring(0, 1);
                                        displayText = programCode + " - " + yearLevel + " - " + schoolYear;
                                        if (!"N/A".equalsIgnoreCase(semester)) {
                                            displayText += " - " + semester;
                                        }
                                    } else {
                                        String gradeLevel = rs2.getString("programCode");
                                        displayText = gradeLevel + " - " + schoolYear;
                                        if (!"N/A".equalsIgnoreCase(semester)) {
                                            displayText += " - " + semester;
                                        }
                                    }

                                    enrollmentData.add(displayText);
                                    enrollmentMap.put(displayText, enrollmentUid);
                                }

                                // Update UI with enrollment data
                                runOnUiThread(() -> {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                            android.R.layout.simple_dropdown_item_1line, enrollmentData);
                                    schoolYearSemesterDropdown.setAdapter(adapter);
                                    if (!enrollmentData.isEmpty()) {
                                        String latestEnrollment = enrollmentData.get(0);
                                        schoolYearSemesterDropdown.setText(latestEnrollment, false);
                                        getStudentSummary_of_PaymentsData(enrollmentMap.get(latestEnrollment));
                                    }
                                    schoolYearSemesterDropdown.setOnItemClickListener((parent, view, position, id) -> {
                                        String selectedText = (String) parent.getItemAtPosition(position);
                                        String selectedEnrollmentUid = enrollmentMap.get(selectedText);
                                        getStudentSummary_of_PaymentsData(selectedEnrollmentUid);
                                    });
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
        });
    }

    private void getStudentSummary_of_PaymentsData(String enrollmentUid) {
        runOnUiThread(this::showLoadingIndicator); // Show loading indicator on UI thread
        executorService.execute(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            String sql;

            if (enrollmentUid == null) {
                sql = "SELECT transactionDate, ORNo, TotalPayments FROM view_TRANSACTION_PAYMENT WHERE STUDENT_UID = ? ORDER BY ENROLLMENT_UID DESC";
            } else {
                sql = "SELECT transactionDate, ORNo, TotalPayments FROM view_TRANSACTION_PAYMENT WHERE ENROLLMENT_UID = ?";
            }

            try (Connection conn = connectionClass.CONN();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (enrollmentUid == null) {
                    pstmt.setString(1, "NDTC-" + studentUID); // Set student UID parameter
                } else {
                    pstmt.setString(1, enrollmentUid); // Set enrollment UID parameter
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    List<Map<String, String>> data = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, String> row = new HashMap<>();
                        row.put("transactionDate", rs.getString("transactionDate"));
                        row.put("ORNo", String.valueOf(rs.getDouble("ORNo")));
                        row.put("Payments", String.valueOf(rs.getDouble("TotalPayments")));
                        data.add(row);
                    }

                    // Update UI with payment data
                    runOnUiThread(() -> {
                        tableLayout.removeAllViews();
                        addHeaderRow(tableLayout);
                        for (Map<String, String> row : data) {
                            TableRow tableRow = new TableRow(Summary_of_Payments.this);
                            tableRow.setLayoutParams(new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            ));

                            TextView transactionDateTV = createTextView(row.get("transactionDate"), true);
                            tableRow.addView(transactionDateTV);

                            TextView orNoTV = createTextView(row.get("ORNo"), false);
                            tableRow.addView(orNoTV);

                            TextView paymentsTV = createTextView(row.get("Payments"), false);
                            tableRow.addView(paymentsTV);

                            tableLayout.addView(tableRow);
                        }
                    });
                }
            } catch (SQLException e) {
                Log.e("DB Query", "Query error", e);
            } finally {
                runOnUiThread(this::removeLoadingIndicator); // Remove loading indicator on UI thread
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showLoadingIndicator() {
        if (loadingTextView == null) {
            loadingTextView = new TextView(this);
            loadingTextView.setText("Loading...");
            loadingTextView.setTextColor(Color.BLACK);
            loadingTextView.setGravity(android.view.Gravity.CENTER);
            loadingTextView.setTypeface(null, Typeface.BOLD);
            loadingTextView.setTextSize(18);
            loadingTextView.setAlpha(0.50f);
            setPadding(loadingTextView);
        }
        tableLayout.removeAllViews();
        tableLayout.addView(loadingTextView);
    }

    private void removeLoadingIndicator() {
        tableLayout.removeView(loadingTextView);
    }

    private void addHeaderRow(TableLayout tableLayout) {
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        String[] headers = {"Date", "OR #", "Amount(₱)"};

        for (int i = 0; i < headers.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(headers[i]);
            if (i == 0) {
                textView.setGravity(android.view.Gravity.START);
            } else {
                textView.setGravity(android.view.Gravity.CENTER);
            }
            textView.setTextColor(Color.BLACK);
            textView.setAllCaps(true);
            textView.setTypeface(null, Typeface.BOLD);
            setPadding(textView);
            headerRow.addView(textView);
        }

        tableLayout.addView(headerRow);
    }

    private TextView createTextView(String text, boolean isFirstColumn) {
        TextView textView = new TextView(this);
        textView.setText(text);
        if (isFirstColumn) {
            textView.setGravity(android.view.Gravity.START);
        } else {
            textView.setGravity(android.view.Gravity.CENTER);
        }
        textView.setTextColor(Color.BLACK);
        setPadding(textView);
        return textView;
    }

    private void setPadding(TextView textView) {
        float scale = getResources().getDisplayMetrics().density;
        int leftPx = (int) (Summary_of_Payments.PADDING_DP * scale + 0.5f);
        int topPx = (int) (Summary_of_Payments.PADDING_DP * scale + 0.5f);
        int rightPx = (int) (Summary_of_Payments.PADDING_DP * scale + 0.5f);
        int bottomPx = (int) (Summary_of_Payments.PADDING_DP * scale + 0.5f);
        textView.setPadding(leftPx, topPx, rightPx, bottomPx);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown(); // Shutdown executor service to release resources
        }
    }
}

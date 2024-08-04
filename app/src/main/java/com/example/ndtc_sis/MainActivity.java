package com.example.ndtc_sis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;

    private Set<String> validStudentUIDs;

    private TextView loginErrorTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display
        setContentView(R.layout.activity_main);

        TextInputLayout usernameLayout = findViewById(R.id.username_layout);
        usernameEditText = findViewById(R.id.username);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        passwordEditText = findViewById(R.id.password);
        Button login = findViewById(R.id.login);
        loginErrorTextView = findViewById(R.id.login_error);  // Initialize the TextView for displaying login errors

        // Set hint color to black
        usernameEditText.setHintTextColor(Color.BLACK);
        passwordEditText.setHintTextColor(Color.BLACK);

        // Add TextWatcher to handle hint visibility
        addTextWatcher(usernameLayout, usernameEditText);
        addTextWatcher(passwordLayout, passwordEditText);

        loadValidStudentUIDs(); // Load valid student UIDs from the database

        // Add OnFocusChangeListener to hide the error message when the user clicks on EditText
        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                loginErrorTextView.setVisibility(View.GONE); // Hide error message when username field is focused
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                loginErrorTextView.setVisibility(View.GONE); // Hide error message when password field is focused
            }
        });

        // Set OnClickListener for the login button
        login.setOnClickListener(v -> {
            String studentUID = Objects.requireNonNull(usernameEditText.getText()).toString();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString();

            // Check if the UID is valid and password is correct
            if (isValidStudentUID(studentUID) && password.equals("1234")) {
                // If valid, start the dashboard activity and pass the student UID
                Intent intent = new Intent(MainActivity.this, dashboard.class);
                intent.putExtra("studentUID", studentUID);
                startActivity(intent);
            } else {
                // Show error message if login credentials are incorrect
                loginErrorTextView.setText("The username and password that you've entered is incorrect.");
                loginErrorTextView.setVisibility(View.VISIBLE);
            }
        });

        hideStatusBar(); // Hide the status bar for a full-screen experience
    }

    @Override
    public void onBackPressed() {
        // Disable back button after logout
        moveTaskToBack(true);
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
        hideStatusBar(); // Ensure the status bar is hidden when the activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar(); // Hide the status bar when the window gains focus
        }
    }

    private void addTextWatcher(TextInputLayout layout, TextInputEditText editText) {
        // Add TextWatcher to handle hint visibility
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    layout.setHint(null); // Hide hint when text is entered
                } else {
                    layout.setHint(editText.getTag().toString()); // Show hint when text is cleared
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadValidStudentUIDs() {
        validStudentUIDs = new HashSet<>(); // Initialize the set to store valid student UIDs
        ConnectionClass connectionClass = new ConnectionClass();
        Connection connection = connectionClass.CONN();

        if (connection != null) {
            try {
                // Add queries for all views
                String[] queries = {
                        "SELECT STUDENT_UID FROM view_STUDENT_GRADES",
                        "SELECT STUDENT_UID FROM view_ENROLLMENT",
                        "SELECT STUDENT_UID FROM view_BASIC_ED_GRADES_GS_JHS_SHS"
                };

                for (String query : queries) {
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()) {
                        String studentUID = resultSet.getString("STUDENT_UID");
                        if (studentUID.length() > 5) {
                            validStudentUIDs.add(studentUID.substring(5)); // Add valid UIDs to the set
                        }
                    }

                    resultSet.close();
                    statement.close();
                }
                connection.close();
            } catch (Exception e) {
                Log.e("DB Error", "Error loading STUDENT_UIDs", e); // Log any errors that occur
            }
        } else {
            Log.e("DB Error", "Connection failed"); // Log if connection to the database fails
        }
    }

    private boolean isValidStudentUID(String studentUID) {
        return validStudentUIDs.contains(studentUID); // Check if the given UID is valid
    }
}

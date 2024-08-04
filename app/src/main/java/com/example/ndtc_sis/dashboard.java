package com.example.ndtc_sis;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class dashboard extends AppCompatActivity {

    // Define the items to be displayed in the AutoCompleteTextView dropdown
    String[] item =  {"Enrollment History","Statement of Account","Summary of Charges"};

    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    TextView studentIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Enable edge-to-edge display
        setContentView(R.layout.activity_dashboard);

        // Initialize the ArrayAdapter with the item array
        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, item);
        autoCompleteTextView = findViewById(R.id.auto_complete_text);
        studentIdTextView = findViewById(R.id.student_id);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, id) -> {
            String item = adapterView.getItemAtPosition(i).toString();
            switch (item) {
                case "Enrollment History": {
                    Intent intent = new Intent(dashboard.this, Enrollment.class);
                    String studentUID = getIntent().getStringExtra("studentUID");
                    // Pass the studentUID to the new intent
                    intent.putExtra("studentUID", studentUID);
                    startActivity(intent);
                    break;
                }
                case "Statement of Account": {
                    Intent intent = new Intent(dashboard.this, Statement_of_Account.class);
                    String studentUID = getIntent().getStringExtra("studentUID");
                    // Pass the studentUID to the new intent
                    intent.putExtra("studentUID", studentUID);
                    startActivity(intent);
                    break;
                }
                case "Summary of Charges": {
                    Intent intent = new Intent(dashboard.this, Summary_of_Charges.class);
                    String studentUID = getIntent().getStringExtra("studentUID");
                    // Pass the studentUID to the new intent
                    intent.putExtra("studentUID", studentUID);
                    startActivity(intent);
                    break;
                }
            }
        });

        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu); // Initialize the ImageView for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            Context wrapper = new ContextThemeWrapper(dashboard.this, R.style.PopupMenuItemStyle); // Create a themed context for the PopupMenu
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
                    intent = new Intent(dashboard.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(dashboard.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(dashboard.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(dashboard.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(dashboard.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(dashboard.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(dashboard.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(dashboard.this, MainActivity.class);
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

        // Get studentUID from the intent
        Intent intent = getIntent();
        String studentUID = intent.getStringExtra("studentUID");
        if (studentUID != null) {
            studentIdTextView.setText(studentUID); // Set the student ID text view
        }

        hideStatusBar(); // Hide the status bar
    }

    // Method to hide the status bar
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
}

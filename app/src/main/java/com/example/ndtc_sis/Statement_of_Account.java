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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Statement_of_Account extends AppCompatActivity {

    // Array of items for the AutoCompleteTextView
    String[] item =  {"1ST SEMESTER","2ND SEMESTER","3RD SEMESTER","4TH SEMESTER","SUMMER"};

    // Declaration of AutoCompleteTextView
    AutoCompleteTextView autoCompleteTextView;

    // Declaration of ArrayAdapter for the AutoCompleteTextView
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);  // Enable edge-to-edge display
        setContentView(R.layout.activity_statement_of_account);  // Set the layout for the activity

        // Initialize the AutoCompleteTextView and set the adapter
        autoCompleteTextView = findViewById(R.id.auto_complete_text);
        adapterItems = new ArrayAdapter<>(this, R.layout.list_item, item);

        // Set the adapter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapterItems);

        // Set an item click listener for the AutoCompleteTextView
        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, id) -> {
            String item = adapterView.getItemAtPosition(i).toString();
            Toast.makeText(Statement_of_Account.this,"item" + item, Toast.LENGTH_SHORT).show();  // Display a toast message when an item is clicked
        });

        // Initialize the ImageView for the hamburger menu
        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu);

        // Set a click listener for the hamburger menu
        hamburgerMenu.setOnClickListener(view -> {
            // Create a themed context for the PopupMenu
            Context wrapper = new ContextThemeWrapper(Statement_of_Account.this, R.style.PopupMenuItemStyle);
            // Create the PopupMenu with the specified style
            PopupMenu popupMenu = new PopupMenu(wrapper, hamburgerMenu, 0, 0, R.style.PopupMenuStyle);
            popupMenu.inflate(R.menu.popup_menu);  // Inflate the menu

            // Set a menu item click listener for the PopupMenu
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();  // Get the ID of the clicked menu item
                Intent intent;
                // Get the studentUID from the current intent
                String studentUID = getIntent().getStringExtra("studentUID");

                // Determine which menu item was clicked and start the corresponding activity
                if (itemId == R.id.home) {
                    intent = new Intent(Statement_of_Account.this, dashboard.class);
                } else if (itemId == R.id.profile) {
                    intent = new Intent(Statement_of_Account.this, Profile.class);
                } else if (itemId == R.id.enrollment) {
                    intent = new Intent(Statement_of_Account.this, Enrollment.class);
                } else if (itemId == R.id.subject) {
                    intent = new Intent(Statement_of_Account.this, Subject_Schedules.class);
                } else if (itemId == R.id.statement_of_account) {
                    intent = new Intent(Statement_of_Account.this, Statement_of_Account.class);
                } else if (itemId == R.id.summary_of_payments) {
                    intent = new Intent(Statement_of_Account.this, Summary_of_Payments.class);
                } else if (itemId == R.id.summary_of_charges) {
                    intent = new Intent(Statement_of_Account.this, Summary_of_Charges.class);
                } else if (itemId == R.id.sign_out) {
                    intent = new Intent(Statement_of_Account.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    return false;  // Return false if none of the menu items match
                }

                // Pass the studentUID to the new intent and start the activity
                intent.putExtra("studentUID", studentUID);
                startActivity(intent);
                return true;
            });

            popupMenu.show();  // Show the PopupMenu
        });

        hideStatusBar();  // Hide the status bar
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
        hideStatusBar();  // Hide the status bar when the activity resumes
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideStatusBar();  // Hide the status bar when the window gains focus
        }
    }
}

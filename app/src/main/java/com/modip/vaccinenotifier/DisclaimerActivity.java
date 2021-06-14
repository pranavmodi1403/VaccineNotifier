package com.modip.vaccinenotifier;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DisclaimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.disclaimer_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tv_disclaimer = findViewById(R.id.disclaimer_text);
        String data = "By this app, We are just helping people to check the available slots by district and state or by pincode by giving them realtime data from API Setu. \n\nWe are using API Setu/Co-WIN API to fetch the data. \n\nPeople can book appointment through Co-WIN Web Portal. \n\nWe do not track any data of end users";
        tv_disclaimer.setText(data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

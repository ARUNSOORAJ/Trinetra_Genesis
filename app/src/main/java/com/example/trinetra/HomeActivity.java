package com.example.trinetra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);


        // Wire up the new violation button

        Button btn = findViewById(R.id.button_new_violation);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(HomeActivity.this, NewViolationActivity.class);
                startActivity(homeIntent);
            }
        });

        // Wire up the violation reports screen (user details)

        TextView txt = findViewById(R.id.text_violation_number);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userDetails = new Intent(HomeActivity.this, UserDetailsActivity.class);
                startActivity(userDetails);
            }
        });
    }
}

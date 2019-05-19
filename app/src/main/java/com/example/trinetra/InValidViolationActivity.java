package com.example.trinetra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class InValidViolationActivity extends AppCompatActivity {

    private NewViolationActivity violation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_valid_violation);

        ImageView imageView = (ImageView) findViewById(R.id.imageView4);
        Bitmap image = violation.image2;
        image = Bitmap.createScaledBitmap(image, 500, 500, false);
        imageView.setImageBitmap(image);

        Button btn = findViewById(R.id.buton_home_from_invalid_violation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnhome = new Intent(InValidViolationActivity.this, HomeActivity.class);
                startActivity(returnhome);
            }
        });
    }
}

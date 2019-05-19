package com.example.trinetra;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ValidViolationActivity extends AppCompatActivity {

    private NewViolationActivity violation;
    private String TAG="ValidViolationActivity";
    private static Integer counter = 12555;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valid_violation);

        ImageView imageView = (ImageView) findViewById(R.id.imageView3);
        Bitmap image = violation.image2;
        image = Bitmap.createScaledBitmap(image, 500, 500, false);
        imageView.setImageBitmap(image);

        Button btn = findViewById(R.id.buton_home_from_valid_violation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnhome = new Intent(ValidViolationActivity.this, HomeActivity.class);
                startActivity(returnhome);
            }
        });

        Button btn_block = findViewById(R.id.button_log_blockchain);
        btn_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams rp = new RequestParams();
                rp.add("$class", "composer.violations.CreateViolation");

                rp.add("violation_id",  Integer.toString(counter));
                counter += 1;
                rp.add("violation_type", "3 rider");
                rp.add("number_plate", "string");
                rp.add("gps_location", "string");
                rp.add("civilian_id", "string");
                Log.i(TAG, "counter : " + counter);
                HttpUtils.post("CreateViolation", rp, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d("asd", "---------------- this is response : " + response);
                        try {
                            JSONObject serverResp = new JSONObject(response.toString());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            Log.i(TAG, "erro : " + e);
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                        // Pull out the first event on the public timeline

                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.i("Error----> ", ""+statusCode+" ------ "+ errorResponse);
                    }

                });

                Intent blockchain_log = new Intent(ValidViolationActivity.this, BlockchainLoggingActivity.class);
                startActivity(blockchain_log);
            }
        });

    }
}

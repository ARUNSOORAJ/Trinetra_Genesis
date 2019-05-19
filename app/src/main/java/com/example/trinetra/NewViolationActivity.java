package com.example.trinetra;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class NewViolationActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1 ;
    private static Bitmap image ;
    public static Bitmap image2;
    private static String TAG = "NewViolation";
    private static Detector detector;
    private Interpreter tflite;
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/traffic.txt";
    private static final String TF_OD_API_MODEL_FILE = "mobilenetv1_detect.tflite";
    private static Boolean valid = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_violation);

        Button btn = findViewById(R.id.button_validate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String processed_data = processImage();
                Log.i(TAG, " Processed data : " + processed_data);
                if (valid){
                    Intent validViolation = new Intent(NewViolationActivity.this, ValidViolationActivity.class);
                    startActivity(validViolation);
                } else{
                    Intent invalidViolation = new Intent(NewViolationActivity.this, InValidViolationActivity.class);
                    startActivity(invalidViolation);
                }

            }
        });

        Button btn_validate = findViewById(R.id.button_upload);
        btn_validate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "upload button is clicked");
                    Intent i = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.imageView2);
            image = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(image);

        }
    }

    private static MappedByteBuffer loadModelFile(AssetManager assets) throws IOException {
        Log.i("Detector", "file is : " + assets.openFd(TF_OD_API_MODEL_FILE));
        AssetFileDescriptor fileDescriptor = assets.openFd(TF_OD_API_MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String processImage(){
        try{
            tflite = new Interpreter(loadModelFile(this.getAssets()));
        }catch (IOException e){
            Log.i(TAG, "problem with tflite: " + e);
        }
        Log.i(TAG, "value of tflite : " + tflite);

        //tflite.setNumThreads(4);
        AssetManager mngr = this.getAssets();
        BufferedReader reader = null;
        String mLine;
        String value = "";

        try{
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("box_priors.txt")));

            // do reading, usually loop until end of file reading
            while ((mLine = reader.readLine()) != null) {
                //process line
                value += mLine;

            }
        }catch ( IOException e){
            Log.i(TAG, "problem in open asset folder : " + e);
        }
        Log.i(TAG, "no problem in reading asset folder");
        Log.i(TAG, "value of box : " + value);


        try{
            Log.i(TAG, "asset is : " + this.getAssets());
            detector = Detector.getInstance(this.getAssets());
        } catch (IOException e){
            Log.i(TAG, "there is a problem in getting instance of detector");
        }

        //sending frame for detection
        List<DetectedObject> list = detector.processImage(Bitmap.createScaledBitmap(image, 300, 300, false));

        Log.i(TAG, "length of detection: " + list.size());
        Log.i(TAG, " detection List: " + list);
        image2 = image.copy(Bitmap.Config.ARGB_8888, true);
        image2 = Bitmap.createScaledBitmap(image2, 300, 300, false);

        // Rectangle Objects
        Paint red = new Paint();
        Paint blue = new Paint();
        red.setAntiAlias(true);
        blue.setAntiAlias(true);
        red.setAlpha(200);
        blue.setAlpha(200);;
        // Fill with color
        red.setStyle(Paint.Style.STROKE);
        blue.setStyle(Paint.Style.STROKE);
        // Set fill color
        red.setColor(Color.RED);
        blue.setColor(Color.BLUE);

        Canvas canvas = new Canvas(image2);
        canvas.drawBitmap(image2, 0, 0, null);

        Integer bike = 0;
        Integer rider = 0;
        valid = Boolean.FALSE;
        final List<DetectedObject> mappedRecognitions =
                new LinkedList<DetectedObject>();
        for (final DetectedObject result : list) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= 0.4 ) {
                Log.i(TAG, "Length of detections : " + list.size() + " this item's location : " + result + " location : " + location + "Title :" + result.getTitle());
                mappedRecognitions.add(result);
                if (result.getTitle().equals("bike")){
                    canvas.drawRect(location, red);
                    bike += 1;
                }
                if (result.getTitle().equals("rider") || result.getTitle().equals("pedestrian")){
                    canvas.drawRect(location, blue);
                    rider += 1;
                }
            }
        }
        if (bike >= 1 && rider >= 3){
            valid = Boolean.TRUE;
        }

        return ("test");
    }
}

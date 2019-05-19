package com.example.trinetra;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;
import android.util.Size;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Detector {

    private static Detector instance;
    private String TAG = "Detector";

    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/traffic.txt";
    private static final String TF_OD_API_MODEL_FILE = "mobilenetv1_detect.tflite";
    private Vector<String> labels = new Vector<String>();

    private static final Size TF_LITE_INPUT_DIM = new Size(300, 300);
    //private static final Size ORIGINAL_FRAME_DIM = new Size(1920, 1080);
    //private static final Size FRAME_DIM = new Size(640, 480);

    private static final float THRESHOLD_CONFIDENCE = 0.6f;

    private static final int MAX_NUM_DETECTIONS = 100;
    private static final int NUM_THREADS = 4;

    private Interpreter tflite;

    private int[] intValues;

    private float[][][] outputLocations;
    private float[][] outputClasses;
    private float[][] outputScores;
    private float[] numDetections;

    private ByteBuffer imgData;
    private int[] pixelValues;

    private static MappedByteBuffer loadModelFile(AssetManager assets) throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(TF_OD_API_MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public Detector(AssetManager assetManager) {
        Log.i(TAG, "going to initialize tflite");
        try{
            InputStream labelsInput = assetManager.open(TF_OD_API_LABELS_FILE.split("file:///android_asset/")[1]);
            BufferedReader br = null;
            br = new BufferedReader(new InputStreamReader(labelsInput));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
        } catch (IOException e){
            Log.i(TAG, "error in reading" + e);
        }

        try {
            tflite = new Interpreter(loadModelFile(assetManager));
            Log.i(TAG, "inside the try : " + tflite);
        }catch (Exception e){
            Log.i(TAG, "Null pointer Exception in detector" + e);
        }

        pixelValues = new int[TF_LITE_INPUT_DIM.getWidth() * TF_LITE_INPUT_DIM.getHeight()];
        imgData = ByteBuffer.allocateDirect(TF_LITE_INPUT_DIM.getWidth() * TF_LITE_INPUT_DIM.getHeight() * 3);
        imgData.order(ByteOrder.nativeOrder());
        //intValues = new int[TF_LITE_INPUT_DIM.getWidth() * TF_LITE_INPUT_DIM.getHeight()];
        intValues = new int[TF_LITE_INPUT_DIM.getHeight() * TF_LITE_INPUT_DIM.getWidth()];

        tflite.setNumThreads(NUM_THREADS);
        outputLocations = new float[1][MAX_NUM_DETECTIONS][4];
        outputClasses = new float[1][MAX_NUM_DETECTIONS];
        outputScores = new float[1][MAX_NUM_DETECTIONS];
        numDetections = new float[1];
    }


    public static Detector getInstance(final AssetManager asset) throws IOException {

        if (instance == null)
            instance = new Detector(asset);
        return instance;
    }
    //public List<DetectedObject>processImage(final byte[] frame) {
    public List<DetectedObject>processImage(final Bitmap frame){
        //int tt = 0;
        //Log.i(TAG, "frame width :" + frame.getWidth());
       // Log.i(TAG, "frame width :" + frame.getHeight());
        Log.i("Detector", "width of frame : " + frame.getWidth());
        frame.getPixels(intValues, 0, frame.getWidth(), 0, 0, frame.getWidth(), frame.getHeight());
       // int yy = 0;

        imgData.rewind();

        for (int i=0; i<TF_LITE_INPUT_DIM.getHeight(); i++) {
            for (int j=0; j<TF_LITE_INPUT_DIM.getWidth(); j++) {
                //int pixel = pixelValues[i*TF_LITE_INPUT_DIM.getHeight() + j];
                //int pixel = ((int) frame[i*TF_LITE_INPUT_DIM.getHeight() + j]) + 128;
                int pixel = intValues[i*TF_LITE_INPUT_DIM.getHeight() + j];
                imgData.put((byte) ((pixel >> 16) & 0xFF));
                imgData.put((byte) ((pixel >> 8) & 0xFF));
                imgData.put((byte) (pixel & 0xFF));
            }
        }
        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        Trace.endSection();

        tflite.runForMultipleInputsOutputs(inputArray, outputMap);

        final ArrayList<DetectedObject> detections = new ArrayList<>(MAX_NUM_DETECTIONS);

        for (int i = 0; i < MAX_NUM_DETECTIONS; ++i) {
            final RectF detection = new RectF(
                    outputLocations[0][i][1] * TF_LITE_INPUT_DIM.getWidth(),
                    outputLocations[0][i][0] * TF_LITE_INPUT_DIM.getHeight(),
                    outputLocations[0][i][3] * TF_LITE_INPUT_DIM.getWidth(),
                    outputLocations[0][i][2] * TF_LITE_INPUT_DIM.getHeight());
           /* final RectF detection = new RectF(
                    outputLocations[0][i][1] * 640,
                    outputLocations[0][i][0] * 480,
                    outputLocations[0][i][3] * 640,
                    outputLocations[0][i][2] * 480);*/
            //Log.i(TAG, "Rectangle obtained : " + detection);

            int labelOffset = 1;
            detections.add(new DetectedObject(
                    detection,
                    labels.get((int) outputClasses[0][i] + labelOffset),
                    outputScores[0][i]
            ));
        }
        return detections;
    }
}

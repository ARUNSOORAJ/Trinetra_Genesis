package com.example.trinetra;

import android.graphics.Color;
import android.graphics.RectF;

import java.util.Random;

public class DetectedObject {

    private RectF location;
    private int ID;
    private String title;
    private float confidence;
    private int boxColor;
    private boolean counted = false;
    private boolean inRegion = false;

    public DetectedObject() {
        this.boxColor = Color.rgb(
                new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255)
        );
        this.ID = new Random().nextInt(10000);
    }

    public DetectedObject(RectF location, String title, float confidence) {
        this.location = location;
        this.title = title;
        this.confidence = confidence;

        this.boxColor = Color.rgb(
                new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255)
        );
        this.ID = new Random().nextInt(10000);
    }

    public DetectedObject(RectF location, int ID, String title, float confidence, int boxColor) {
        this.location = location;
        this.title = title;
        this.confidence = confidence;

        this.boxColor = boxColor;
        this.ID = ID;
    }

    public RectF getLocation() {
        return location;
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public int getBoxColor() {
        return boxColor;
    }

    public void setBoxColor(int boxColor) {
        this.boxColor = boxColor;
    }

    public boolean isCounted() {
        return counted;
    }

    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    public boolean isInRegion() {
        return inRegion;
    }

    public void setInRegion(boolean inRegion) {
        this.inRegion = inRegion;
    }

    @Override
    public String toString() {
        String str = "";
        str += "ID = "+ID +", Title = "+ title + ", Conf = " + confidence;
        return str;
    }
}

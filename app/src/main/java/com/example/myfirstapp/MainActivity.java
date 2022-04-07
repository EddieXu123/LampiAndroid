package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

public class MainActivity extends AppCompatActivity {

    View view;
    ColorSeekBar colorSeekBar;
    ColorSeekBar saturation_seekbar;
    ColorSeekBar brightness_seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton button = (ImageButton) this.findViewById(R.id.imageButton);
        button.setColorFilter(Color.argb(255, 0, 0, 255)); // Blue Tint
        button.bringToFront();
        view = findViewById(R.id.view);



        float[] hsv = new float[3];
//        Color.RGBToHSV(red, green, blue, hsv);


        seekBarCreation();

    }


    public void seekBarCreation() {

        colorSeekBar = findViewById(R.id.color_seek_bar);
        saturation_seekbar = findViewById((R.id.saturation_seekbar));
        brightness_seekbar = findViewById((R.id.brightness_seekbar));

        // i is hue (outa 100), i1 is saturation (out of 255) i2 color
        colorSeekBar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            saturation_seekbar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            view.setBackgroundColor(saturation_seekbar.getColor());
            Log.d("Color Change", Integer.toString(colorBarPosition));
        });

        saturation_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            view.setBackgroundColor(color);
            Log.d("Saturation Change", Integer.toString(colorBarPosition));
        });

        brightness_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
//            view.(saturation_seekbar.getColor());
            Log.d("Brightness Change", Integer.toString(color));
        });
    }
}
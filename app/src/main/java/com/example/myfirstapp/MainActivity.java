package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.rtugeek.android.colorseekbar.OnColorChangeListener;
import com.rtugeek.android.colorseekbar.thumb.DefaultThumbDrawer;

public class MainActivity extends AppCompatActivity {

    View view;
    ColorSeekBar colorSeekBar;
    ColorSeekBar alphaSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton button = (ImageButton) this.findViewById(R.id.imageButton);
        button.setColorFilter(Color.argb(255, 0, 0, 255)); // White Tint

        view = findViewById(R.id.view);

        colorSeekBar = findViewById(R.id.color_seek_bar);
        colorSeekBar.setColor(Color.RED);
        alphaSeekBar = findViewById(R.id.alpha_seek_bar);
        view.setBackgroundColor(colorSeekBar.getColor());

//        Log.d("GettingColor", Integer.toString(colorSeekBar.getColor()));

//        alphaSeekBar.setColorSeeds(new int[]{Color.WHITE, Color.RED});

        seekBarCreation();

        Log.d("Startup", "STARTED");
    }


    public void seekBarCreation() {
        colorSeekBar.setOnColorChangeListener((progress, color) -> {
            alphaSeekBar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            view.setBackgroundColor(color);
        });

        alphaSeekBar.setOnColorChangeListener((progress, color) -> {
            Log.d("Current Color", Integer.toString(alphaSeekBar.getColor()));
            view.setBackgroundColor(alphaSeekBar.getColor());
        });
    }

//    public void seekBarCreation() {
//        LinearGradient test = new LinearGradient(0.f, 0.f, 300.f, 0.0f,
//
//                new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
//                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
//                null, Shader.TileMode.CLAMP);
//        ShapeDrawable shape = new ShapeDrawable(new RectShape());
//        shape.getPaint().setShader(test);
//
//        SeekBar seekBarFont = (SeekBar) findViewById(R.id.seekbar_font);
//        seekBarFont.setProgressDrawable((Drawable) shape);
//
//    }

}
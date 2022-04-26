package com.example.myfirstapp;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.bluetooth.BluetoothGatt;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.rx.transformer.ReplayingShare;

import android.graphics.LinearGradient;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

//import com.clj.fastble.BleManager;
//import com.clj.fastble.callback.BleGattCallback;
//import com.clj.fastble.callback.BleScanCallback;
//import com.clj.fastble.data.BleDevice;
//import com.clj.fastble.exception.BleException;
//import com.clj.fastble.scan.BleScanRuleConfig;
//import com.google.android.gms.location.LocationServices;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.rtugeek.android.colorseekbar.ColorSeekBar;
//import com.welie.blessed.BluetoothCentralManager;
//import com.welie.blessed.BluetoothCentralManagerCallback;
//import com.welie.blessed.BluetoothPeripheral;
//import com.welie.blessed.BluetoothPeripheralCallback;
//import com.welie.blessed.ConnectionPriority;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Arrays;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 1;
    ColorSeekBar colorSeekBar;
    ColorSeekBar saturation_seekbar;
    ColorSeekBar brightness_seekbar;
    ImageButton button;
    private int brightness_position = 100;
    private int color_position = 100;
    private int saturation_position = 100;
    boolean isOn = true;
    ImageView imView;
    private final String lampiMacAddress = "B8:27:EB:CB:AF:1F";
    private GradientDrawable rectangle;
    private RxBleDevice myLampi;

    Disposable temp;
    private final UUID onOffUUID = UUID.fromString("0004A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID brightnessUUID = UUID.fromString("0003A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID hsvUUID = UUID.fromString("0002A7D3-D8A4-4FEA-8174-1736E808C066");


    private byte[] getByteValues(String bar) {
        RxBleDevice myLampi = getMyLampi(lampiMacAddress);

        UUID tempUUID = onOffUUID;
        switch (bar) {
            case "hsv":
                tempUUID = hsvUUID;
                break;

            case "brightness":
                tempUUID = brightnessUUID;
                break;

            case "onOff":
                tempUUID = onOffUUID;
                break;

            default:
                Log.d("CHANGED", "NOTHING TO READ");
                break;
        }

        UUID toReadUUID = tempUUID;
        final byte[][] output = {new byte[]{50}};

        temp = myLampi.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(toReadUUID))
                .subscribe(
                        characteristicValue -> {
                            // Read characteristic value.
                            output[0] = characteristicValue;

//                           temp.dispose();
                            if (bar.equals("brightness")) {
                                brightness_position = byteToInt(output[0][0]);
                                brightness_seekbar.setPosition(brightness_position, 0);
                            }
                            if (bar.equals("hsv")) {
                                Log.d("Result", "SAT POS : " + saturation_position);
                                Log.d("Result", "ACTUAL HUE : " + output[0][0] + " Actual: SATURATION " + output[0][1]);
                                color_position = byteToInt(output[0][0]);
                                colorSeekBar.setPosition(color_position, 0);
                                saturation_position = byteToInt(output[0][1]);
                                saturation_seekbar.setPosition(saturation_position, 0);
                            }

                            rectangle.setColorFilter(applyLightness(100 - brightness_position));
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.d("Result", "READING" + " " + throwable.toString());
                        });
        return output[0];
    }


    private void writeToLamp(String bar, byte[] bytesToWrite) {
        RxBleDevice myLampi = getMyLampi(lampiMacAddress);
        UUID toWriteUUID;

        switch (bar) {
            case "hsv":
                toWriteUUID = hsvUUID;
                break;

            case "brightness":
                toWriteUUID = brightnessUUID;
                break;

            case "onOff":
                toWriteUUID = onOffUUID;
                break;

            default:
                Log.d("CHANGED", "Changed: NOTHING");
                return;
        }

        temp = myLampi.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(toWriteUUID, bytesToWrite))
                .subscribe(
                        characteristicValue -> {
                            // Characteristic value confirmed.
                            Log.d("Result", "CHANGED: " + characteristicValue.toString());
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.d("Result", "ERROR : " + throwable.toString());
                        }
                );

    }

    private RxBleDevice getMyLampi(String lampiMAC) {
        RxBleClient rxBleClient = RxBleClient.create(this);

        // Get my device
        RxBleDevice device = rxBleClient.getBleDevice(lampiMAC);
        return device;
    }

    private int byteToInt(byte input) {
        int answer;
        if (input < 0) {
            answer = (int) ((input + 255.0) / (127.0 / 50.0));
        } else {
            answer = (int) ((input) / (127.0 / 50.0));
        }

        return answer;
    }

    private byte[] getInputByte(int colorBarPosition) {
        double answer;

        // colorBarPosition =>
        // Integer: 0 -> 50 -> 51 -> 100
        // Byte: 0 -> 128 -> -127 -> -1
        if (colorBarPosition < 50) { // 0->127
            answer = (127.0 / 50.0) * (double) colorBarPosition;
        } else {
            answer = ((127.0 / 50.0) * (double) colorBarPosition) - 255;
        }

        Log.d("BRIGHTNESS", answer + "");
        return new byte[]{(byte) answer};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (ImageButton) this.findViewById(R.id.imageButton);
        Log.d("Here", "SeekBarCreation");


        imView = findViewById(R.id.rectangleBackground);
        rectangle = (GradientDrawable) imView.getBackground();

        RxBleClient.updateLogOptions(new LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        );

        // Enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;
        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        myLampi = getMyLampi(lampiMacAddress);

        seekBarCreation();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getByteValues("hsv");
                getByteValues("brightness");

//                temp.dispose();
            }
        }, 0, 3000);


        button.bringToFront();
        button.setOnClickListener(v -> {
            isOn = !isOn;
            Log.d("Button", Boolean.toString(button.isSelected()));

//                button.set
            if (isOn) {
                writeToLamp("onOff", new byte[]{1});
                button.setColorFilter(saturation_seekbar.getColor());
            } else {
                writeToLamp("onOff", new byte[]{0});
                button.setColorFilter(Color.WHITE);
            }
        });
    }


    // Every second, run


//
//    @Override
//    protected void onResume() {
//        handler.postDelayed(runnable = new Runnable() {
//            public void run() {
//                handler.postDelayed(runnable, delay);
//                // Constantly read the KIVY UI and write to Android
////                byte[] byteToSetBrightness = getByteValues("brightness");
//
////                writeToLamp("brightness", getByteValues("brightness"));
//            }
//        }, 5000);
//        super.onResume();
//    }

    public void seekBarCreation() {
        colorSeekBar = findViewById(R.id.color_seek_bar);
        saturation_seekbar = findViewById((R.id.saturation_seekbar));
        brightness_seekbar = findViewById((R.id.brightness_seekbar));

        colorSeekBar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            rectangle.setColor(saturation_seekbar.getColor());
            saturation_seekbar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            if (isNear(colorBarPosition, color_position)) {
                return;
            }



            color_position = colorBarPosition;
            byte[] toWrite = new byte[3];
            toWrite[0] = getInputByte(color_position)[0];
            toWrite[1] = getInputByte(saturation_position)[0];
            toWrite[2] = -1;

            writeToLamp("hsv", toWrite);

            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });

        saturation_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            rectangle.setColor(color);
            if (isNear(colorBarPosition, saturation_position)) {
                return;
            }

            saturation_position = colorBarPosition;
            byte[] toWrite = new byte[3];
            toWrite[0] = getInputByte(color_position)[0];
            toWrite[1] = getInputByte(saturation_position)[0];
            toWrite[2] = -1;

//            byte[] readingByte = getByteValues("hsv");
            writeToLamp("hsv", toWrite);


            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });


        brightness_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            if (isNear(colorBarPosition, brightness_position)) {
                return;
            }

            brightness_position = colorBarPosition;
            byte[] byteToChange = getInputByte(colorBarPosition);
            writeToLamp("brightness", byteToChange);
            rectangle.setColor(saturation_seekbar.getColor());
            rectangle.setColorFilter(applyLightness(100 - brightness_position));
        });
    }


    private boolean isNear(int colorBarPosition, int position) {
        return (position == colorBarPosition
                || position == colorBarPosition - 1
                || position == colorBarPosition + 1
                || position == colorBarPosition - 2
                || position == colorBarPosition + 2);
    }

    private PorterDuffColorFilter applyLightness(int progress) {
        return new PorterDuffColorFilter(Color.argb(progress * 255 / 100, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
    }
}


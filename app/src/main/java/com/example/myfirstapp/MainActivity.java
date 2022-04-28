package com.example.myfirstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

import io.reactivex.disposables.Disposable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    /* Instance Variables */
    private ColorSeekBar colorSeekBar;
    private ColorSeekBar saturation_seekbar;
    private ColorSeekBar brightness_seekbar;
    private ImageButton button;

    private boolean isOn = true;
    private GradientDrawable rectangle;

    Disposable temp;
    private final UUID onOffUUID = UUID.fromString("0004A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID brightnessUUID = UUID.fromString("0003A7D3-D8A4-4FEA-8174-1736E808C066");
    private final UUID hsvUUID = UUID.fromString("0002A7D3-D8A4-4FEA-8174-1736E808C066");
    private final String lampiMacAddress = "B8:27:EB:CB:AF:1F";

    private int brightness_position = 100;
    private int color_position = 100;
    private int saturation_position = 100;
    private int readsSinceStart = 0;

    /**
     * Function to read a specific characteristic of the lamp
     * @param bar: The characteristic I wish to read (hsv, brightness, onOff)
     */
    private void readFromLamp(String bar) {
        RxBleDevice myLampi = getMyLampi(lampiMacAddress);

        // Figure out which UUID I wish to read from
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

        // Reading from the Lamp
        temp = myLampi.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.readCharacteristic(toReadUUID))
                .subscribe(
                        characteristicValue -> {
                            Log.d("Result", "READING VALS: " + characteristicValue[0]);
                            // Update seekbar sliders accordingly
                            if (bar.equals("brightness")) {
                                brightness_position = byteToInt(characteristicValue[0]);
                                brightness_seekbar.setPosition(brightness_position, 0);
                            } else if (bar.equals("hsv")) {
                                color_position = byteToInt(characteristicValue[0]);
                                colorSeekBar.setPosition(color_position, 0);

                                saturation_position = byteToInt(characteristicValue[1]);
                                saturation_seekbar.setPosition(saturation_position, 0);
                            }

                            Log.d("Result", "NEW VALS: " + saturation_position + " " + color_position + " " + brightness_position);
                            // Update button and rectangle background colors
                            button.setColorFilter(saturation_seekbar.getColor());
                            rectangle.setColorFilter(applyLightness(100 - brightness_position));
                            brightness_position = brightness_seekbar.getColorBarPosition();
                            saturation_position = saturation_seekbar.getColorBarPosition();
                            color_position = colorSeekBar.getColorBarPosition();
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.d("Reading", "ERROR READING:" + " " + throwable.toString());
                        });
    }

    /**
     * Function to write to a characteristic of the lamp
     * @param bar: The type of characteristic I wish to write to (hsv, brightness, onOff)
     * @param bytesToWrite: The byte array of data I wish to write to the lamp
     */
    private void writeToLamp(String bar, byte[] bytesToWrite) {
        RxBleDevice myLampi = getMyLampi(lampiMacAddress);
        UUID toWriteUUID;

        Log.d("FixesColor", "WRITTEN: " + Arrays.toString(bytesToWrite) + " " + saturation_position + " " + brightness_position + " " + color_position);

        // Figure out which UUID I wish to write to
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

        // Write to the Lamp
        temp = myLampi.establishConnection(false)
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(toWriteUUID, bytesToWrite))
                .subscribe(
                        characteristicValue -> {
                            // Characteristic value confirmed.
                            Log.d("FixesColor", "SAME?: " + Arrays.toString(characteristicValue) + " " + Arrays.toString(bytesToWrite));
                            if (bar.equals("brightness")) {
                                brightness_position = byteToInt(characteristicValue[0]);
                                brightness_seekbar.setPosition(brightness_position, 0);
                            } else if (bar.equals("hsv")) {
                                color_position = byteToInt(characteristicValue[0]);
                                colorSeekBar.setPosition(color_position, 0);

                                saturation_position = byteToInt(characteristicValue[1]);
                                saturation_seekbar.setPosition(saturation_position, 0);
                            } else if (bar.equals("onOff")) {
                                isOn = characteristicValue[0] == (byte) 1;
                                button.setColorFilter(isOn ? saturation_seekbar.getColor() : Color.WHITE);
                            }

                            Log.d("Result", "NEW SAT: " + saturation_position + " " + color_position);

                            brightness_position = brightness_seekbar.getColorBarPosition();
                            saturation_position = saturation_seekbar.getColorBarPosition();
                            color_position = colorSeekBar.getColorBarPosition();
                            rectangle.setColorFilter(applyLightness(100 - brightness_position));
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.d("Result", "ERROR : " + throwable.toString());
                        }
                );
    }

    /**
     * Function to get the RxBleDevice with MAC address lampiMAC
     * @param lampiMAC: The MAC address of the device I am trying to connect to
     * @return: The device I am trying to connect to
     */
    private RxBleDevice getMyLampi(String lampiMAC) {
        RxBleClient rxBleClient = RxBleClient.create(this);

        return rxBleClient.getBleDevice(lampiMAC);
    }

    /**
     * Function for reading from Lamp: Used to convert Byte[] read from Lamp to change the seekbars of the app UI
     * @param input: The input byte array read from the lamp
     * @return: The integer value representing where the seekbar should be positioned
     */
    private int byteToInt(byte input) {
        int answer;
        if (input < 0) {
            answer = (int) ((input + 255.0) / (127.0 / 50.0));
        } else {
            answer = (int) ((input) / (127.0 / 50.0));
        }

        return answer;
    }

    /**
     * Function for writing to Lamp: Used to convert integer of color/saturation/brightness_seekbar to byte array (for writing to Lamp)
     * @param colorBarPosition: The seekbar position (0-100)
     * @return: The byte array equivalent value for the seekbar on the Lampi
     */
    private byte[] getInputByte(int colorBarPosition) {
        double answer;

        if (colorBarPosition < 50) { // 0 => 127
            answer = (127.0 / 50.0) * (double) colorBarPosition;
        } else { // -128 => -1
            answer = ((127.0 / 50.0) * (double) colorBarPosition) - 255;
        }

        return new byte[]{(byte) answer};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate button, seekbars, and rectangle background
        button = (ImageButton) this.findViewById(R.id.imageButton);
        button.setColorFilter(Color.WHITE);
        ImageView imView = findViewById(R.id.rectangleBackground);
        rectangle = (GradientDrawable) imView.getBackground();

        // Create the seekbars
        seekBarCreation();

        // Bluetooth - Logging for Testing Purposes
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

        // Sync the app UI with lamp
        readFromLamp("hsv");
        readFromLamp("brightness");

        Timer myTimer = new Timer();
        // Constantly read the KIVY UI for updates (to change app sliders accordingly)
//        myTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if (readsSinceStart < 7) {
//                    readsSinceStart++;
//                    readFromLamp("hsv");
//                    readFromLamp("brightness");
//                    if (readsSinceStart == 5) {
//                        Log.d("Result", "WARNING: SOON");
//                    }
//                } else {
//                    myTimer.cancel();
//                    Log.d("Result", "NO MORE");
//                }
//            }
//        }, 0, 1000);


        isOn = true;
        handleIntent(this.getIntent());
    }

    /**
     * Function to create the seekbars and button for the app UI
     */
    public void seekBarCreation() {
        // Instantiate variables
        colorSeekBar = findViewById(R.id.color_seek_bar);
        saturation_seekbar = findViewById((R.id.saturation_seekbar));
        brightness_seekbar = findViewById((R.id.brightness_seekbar));

        Log.d("Result", "AFTER READING " + saturation_position + " " + color_position);

        colorSeekBar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            // On color slider change, change the slider gradient color of saturation slider
            rectangle.setColor(saturation_seekbar.getColor());
            saturation_seekbar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            if (isNear(colorBarPosition, color_position)) {
                return;
            }

            color_position = colorBarPosition;
            byte[] toWrite = new byte[3];
            toWrite[0] = getInputByte(color_position)[0]; // hue
            toWrite[1] = getInputByte(saturation_position)[0]; // saturation
            toWrite[2] = -1; // v

            writeToLamp("hsv", toWrite); // Write the changes to the lamp

            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor()); // If the lamp is on, change the color of the app power button
            }
        });

        saturation_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            rectangle.setColor(color);
            if (isNear(colorBarPosition, saturation_position)) {
                return;
            }

            saturation_position = colorBarPosition;
            byte[] toWrite = new byte[3];
            toWrite[0] = getInputByte(color_position)[0]; // hue
            toWrite[1] = getInputByte(saturation_position)[0]; // saturation
            toWrite[2] = -1; //v

            writeToLamp("hsv", toWrite); // Write the changes to the lamp

            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor()); // If the lamp is on, change the color of the app power button
            }
        });

        // Listener for Brightness Seekbar
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

        // Creation of Button
        button.bringToFront();
        button.setOnClickListener(v -> {
            isOn = !isOn;
            Log.d("onOff", isOn + "");
            if (isOn) {
                writeToLamp("onOff", new byte[]{1});
                button.setColorFilter(saturation_seekbar.getColor());
            } else {
                writeToLamp("onOff", new byte[]{0});
                button.setColorFilter(Color.WHITE);
            }
        });
    }

    // To prevent bluetooth library from disconnecting due to rapid writes
    private boolean isNear(int colorBarPosition, int position) {
        return (position == colorBarPosition
                || position == colorBarPosition - 1
                || position == colorBarPosition + 1
                || position == colorBarPosition - 2
                || position == colorBarPosition + 2);
    }

    // Change the brightness of the app
    private PorterDuffColorFilter applyLightness(int progress) {
        return new PorterDuffColorFilter(Color.argb(progress * 255 / 100, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
    }

    /* App Actions for Google Assistant Integration */
    private void handleIntent(Intent data) {
        Log.d("FixesColor", "NEW INFO: " + color_position + " " + saturation_position);
        Log.d("Result", data.getAction() + " EDDIE " + data.getData());
        switch (data.getAction()) {
            case Intent.ACTION_VIEW:
                Log.d("Result", "THIS WAY");
                handleDeepLink(data.getData());
                break;
            default:
                Log.d("Result", "NOTHING CHANGES");
                break;
        }
    }

    private void handleDeepLink(Uri data) {
        Log.d("Result", data + "");
        switch(data.getPath()) {
            case "/open":
                String featureType = data.getQueryParameter("featureName");
                navigateToActivity(featureType);
                break;

            case "/turn":
                String onOffFeature = data.getQueryParameter("text1");
                Log.d("Result", "TURNED " + onOffFeature);
                break;

            default:
                Log.d("Result", "ENTERED BAD");
                navigateToActivity(data.getQueryParameter("featureName"));
                break;
        }
    }

    private void navigateToActivity(String featureType) {
        Log.d("Result", featureType);
        switch (featureType.toLowerCase()) {
            case "home":
                Log.d("Result", "GOING HOME");
                break;
            case "and turn off my lamp":
            case "and turn my lamp off":
                isOn = false;
                button.setColorFilter(Color.WHITE);
                writeToLamp("onOff", new byte[]{0});
                break;
            case "my lamp and turn":
            case "and turn on my lamp":
            case "and turn my lamp on":
                Log.d("Result", "TURNED ON");
                isOn = true;
                button.setColorFilter(saturation_seekbar.getColor());
                writeToLamp("onOff", new byte[]{1});
                break;

            case "and change the saturation to 0":
            case "and turn my saturation to 0":
            case "and turn my saturation off":
            case "and turn the saturation off":
            case "and turn off the saturation":
            case "and turn the saturation all the way down":
                Log.d("Result", "COLOR POS: " + color_position);
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], 0, -1});
                saturation_position = 0;
                saturation_seekbar.setPosition(0, 0);
                break;
            case "and change the saturation to 25":
            case "and turn my saturation to 25":
            case "and turn the saturation to 25":
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], 64, -1});
                saturation_position = 25;
                saturation_seekbar.setPosition(25, 0);
                break;
            case "and change the saturation to 50":
            case "and turn my saturation to 50":
            case "and turn the saturation to 50":
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], 127, -1});
                saturation_position = 50;
                saturation_seekbar.setPosition(50, 0);
                break;
            case "and change the saturation to 75":
            case "and turn my saturation to 75":
            case "and turn the saturation to 75":
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], -63, -1});
                saturation_position = 75;
                saturation_seekbar.setPosition(75, 0);
                break;
            case "and change the saturation to 100":
            case "and turn the saturation to 100":
            case "and turn my saturation to 100":
            case "and turn the saturation all the way up":
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], -1, -1});
                saturation_position = 100;
                saturation_seekbar.setPosition(100, 0);
                break;
            case "and turn up the saturation":
            case "and turn up my saturation":
            case "and turn my saturation up":
                Log.d("Result", saturation_position + " .");
                writeToLamp("hsv", new byte[]{getInputByte(color_position)[0], -80, -1});
                break;


            case "and change the lamp color to red":
            case "and change my lamp color to red":
            case "and turn the color red":
            case "and turn the color to red":
            case "and turn my lamp red":
                Log.d("FixesColor", saturation_position + " ");
                writeToLamp("hsv", new byte[]{-1,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to yellow":
            case "and change my lamp color to yellow":
            case "and turn the color yellow":
            case "and turn the color to yellow":
            case "and turn my lamp yellow":
                writeToLamp("hsv", new byte[]{55,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to green":
            case "and change my lamp color to green":
            case "and turn the color green":
            case "and turn the color to green":
            case "and turn my lamp green":
                writeToLamp("hsv", new byte[]{75,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to purple":
            case "and change my lamp color to purple":
            case "and turn the color purple":
            case "and turn the color to purple":
            case "and turn my lamp purple":
                writeToLamp("hsv", new byte[]{-35,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to blue":
            case "and change my lamp color to blue":
            case "and turn the color blue":
            case "and turn the color to blue":
            case "and turn my lamp blue":
                writeToLamp("hsv", new byte[]{-85,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to magenta":
            case "and change my lamp color to magenta":
            case "and turn the color magenta":
            case "and turn the color to magenta":
            case "and turn my lamp magenta":
                writeToLamp("hsv", new byte[]{-20,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to orange":
            case "and change my lamp color to orange":
            case "and turn the color orange":
            case "and turn the color to orange":
            case "and turn my lamp orange":
                writeToLamp("hsv", new byte[]{15,getInputByte(saturation_position)[0] , -1});
                break;
            case "and change the lamp color to turquoise":
            case "and change my lamp color to turquoise":
            case "and turn the color turquoise":
            case "and turn the color to turquoise":
            case "and turn my lamp turquoise":
            case "and change the lamp color to teal":
            case "and change my lamp color to teal":
            case "and turn the color teal":
            case "and turn the color to teal":
            case "and turn my lamp teal":
            case "and change the lamp color to cyan":
            case "and change my lamp color to cyan":
            case "and turn the color cyan":
            case "and turn the color to cyan":
            case "and turn my lamp cyan":
                writeToLamp("hsv", new byte[]{-110,getInputByte(saturation_position)[0] , -1});
                break;

            case "and change the brightness to 0":
            case "and turn my brightness to 0":
            case "and turn my brightness off":
            case "and turn the brightness off":
            case "and turn off the brightness":
            case "and turn the brightness all the way down":
                writeToLamp("brightness", new byte[]{0});
                brightness_position = 0;
                brightness_seekbar.setPosition(brightness_position, 0);
                break;
            case "and change the brightness to 25":
            case "and turn my brightness to 25":
            case "and turn the brightness to 25":
                writeToLamp("brightness", new byte[]{64});
                brightness_position = 25;
                brightness_seekbar.setPosition(brightness_position, 0);
                break;
            case "and change the brightness to 50":
            case "and turn my brightness to 50":
            case "and turn the brightness to 50":
                writeToLamp("brightness", new byte[]{-127});
                brightness_position = 50;
                brightness_seekbar.setPosition(brightness_position, 0);
                break;
            case "and change the brightness to 75":
            case "and turn my brightness to 75":
            case "and turn the brightness to 75":
                writeToLamp("brightness", new byte[]{-63});
                brightness_position = 75;
                brightness_seekbar.setPosition(75, 0);
                break;
            case "and change the brightness to 100":
            case "and turn the brightness to 100":
            case "and turn my brightness to 100":
            case "and turn the brightness all the way up":
            case "and turn my brightness all the way up":
            case "and turn my brightness up all the way":
            case "and turn the brightness up all the way":
                writeToLamp("brightness", new byte[]{-1});
                brightness_position = 100;
                brightness_seekbar.setPosition(brightness_position, 0);
                break;
            case "and turn up the brightness":
            case "and turn up my brightness":
            case "and increase my brightness":
            case "and turn my brightness up":
                writeToLamp("brightness", new byte[]{-80});
                break;
            case "and decrease my brightness by 35":
            case "and decrease the brightness by 35":
                writeToLamp("brightness", new byte[]{-90});
                break;
            default:
                break;
        }
    }
}


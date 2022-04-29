package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.ImageButton;
import android.widget.ImageView;

import com.polidea.rxandroidble2.LogConstants;
import com.polidea.rxandroidble2.LogOptions;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

import io.reactivex.disposables.Disposable;

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

                            // Update button and rectangle background colors
                            button.setColorFilter(saturation_seekbar.getColor());
                            rectangle.setColorFilter(applyLightness(100 - brightness_position));
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
                            switch (bar) {
                                case "brightness":
                                    brightness_position = byteToInt(characteristicValue[0]);
                                    brightness_seekbar.setPosition(brightness_position, 0);
                                    break;
                                case "hsv":
                                    color_position = byteToInt(characteristicValue[0]);
                                    colorSeekBar.setPosition(color_position, 0);

                                    saturation_position = byteToInt(characteristicValue[1]);
                                    saturation_seekbar.setPosition(saturation_position, 0);
                                    break;
                                case "onOff":
                                    isOn = characteristicValue[0] == (byte) 1;
                                    button.setColorFilter(isOn ? saturation_seekbar.getColor() : Color.WHITE);
                                    break;
                            }

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
        button = this.findViewById(R.id.imageButton);
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

    /* Below is code for Google Assistant Integration */
    private void handleIntent(Intent data) {
        switch (data.getAction()) {
            case Intent.ACTION_VIEW:
                Log.d("Result", "THIS WAY");
                handleDeepLink(data.getData());
                break;
            case Intent.ACTION_MAIN:
                Log.d("Result", "MAIN ACTION");
                break;
            default:
                Log.d("Result", "NOTHING CHANGES");
                break;
        }
    }

    // Handle the action
    private void handleDeepLink(Uri data) {
        if ("/open".equals(data.getPath())) {
            String seekbarType = data.getQueryParameter("slider_name");
            String seekbarNewValue = data.getQueryParameter("slider_value");
            navigateToActivity(seekbarType, seekbarNewValue);
        }
    }

    private void navigateToActivity(String seekbarType, String seekBarValue) {
        byte[] hsvByte = new byte[3];
        hsvByte[2] = -1; // val
        if (seekbarType.equals("saturation")) {
            hsvByte[1] = Byte.parseByte(seekBarValue);
            hsvByte[0] = getInputByte(color_position)[0];
        } else {
            // color
            hsvByte[0] = Byte.parseByte(seekBarValue);
            hsvByte[1] = getInputByte(saturation_position)[0];
        }
        switch (seekbarType.toLowerCase()) {
            case "saturation":
            case "color":
                writeToLamp(seekbarType, hsvByte);
                break;
            default: // brightness or onOff
                writeToLamp(seekbarType, new byte[]{getInputByte(Integer.parseInt(seekBarValue))[0]});
            }
    }
}


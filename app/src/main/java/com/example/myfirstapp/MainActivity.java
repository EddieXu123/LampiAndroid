package com.example.myfirstapp;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

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
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 1;
    ColorSeekBar colorSeekBar;
    ColorSeekBar saturation_seekbar;
    ColorSeekBar brightness_seekbar;
    ImageButton button;
    boolean isOn = true;
    ImageView imView;

    // Create BluetoothCentral and receive callbacks on the main thread
    BluetoothCentralManager central;



    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION_REQUEST);
                return false;
            }
        }
        return true;
    }

//    private BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
//        @Override
//        public void onServicesDiscovered(BluetoothPeripheral peripheral) {
//            super.onServicesDiscovered(peripheral);
//            Log.d("DeviceName", peripheral.getName());
//        }
//    };

//    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
//        @Override
//        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
//            central.stopScan();
//            central.connectPeripheral(peripheral, peripheralCallback);
//        }
//    };

//    @Override
//    public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
//        Log.d("Hey", "HEY");
//    }


    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(final BluetoothPeripheral peripheral) {
            Log.d("Here", "discovered services");

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);

            if (peripheral.getName() != null) {
                Log.d("Peripheral Name", peripheral.getName());

            } else {
                Log.d("Peripheral Name", "Does not exist");
            }
//            // Turn on notifications for Data Characteristic
//            if(peripheral.getService(DATA_CHARACTERISTIC_UUID) != null) {
//                peripheral.setNotify(peripheral.getCharacteristic(SPOT_AG_SERVICE, DATA_CHARACTERISTIC_UUID), true);
//            }
//
//            // Turn on notifications for Status Characteristic
//            if(peripheral.getService(STATUS_CHARACTERISTIC_UUID) != null) {
//                peripheral.setNotify(peripheral.getCharacteristic(SPOT_AG_SERVICE, STATUS_CHARACTERISTIC_UUID), true);
//            }
//            // For TEST with UART Fanstel
//            // Turn on notification for Health Thermometer Service
//            if(peripheral.getService(UART_TX_CHARACTERISTIC_UUID) != null) {
//                Timber.i("discovered services UART TX");
//                peripheral.setNotify(peripheral.getCharacteristic(UART_FANSTEL_UUID, UART_TX_CHARACTERISTIC_UUID), true);
//            }
        }
//
//        @Override
//        public void onNotificationStateUpdate(BluetoothPeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {
//            if( status == GATT_SUCCESS) {
//                if(peripheral.isNotifying(characteristic)) {
//                    Timber.i("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid());
//                } else {
//                    Timber.i("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid());
//                }
//            } else {
//                Timber.e("ERROR: Changing notification state failed for %s", characteristic.getUuid());
//            }
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
//            if( status == GATT_SUCCESS) {
//                Timber.i("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
//            } else {
//                Timber.i("ERROR: Failed writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
//            }
//        }
//
//        @Override
//        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
//            if(status != GATT_SUCCESS) return;
//            UUID characteristicUUID = characteristic.getUuid();
//            BluetoothBytesParser parser = new BluetoothBytesParser(value);
//
//            if (characteristicUUID.equals(SECURITY_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on Security Characteristic");
//            }
//            else if(characteristicUUID.equals(STATUS_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on Status Characteristic");
//            }
//            else if(characteristicUUID.equals(CONTROL_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on Control Characteristic");
//            }
//            else if(characteristicUUID.equals(DATA_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on Data Characteristic");
//            }
//            else if(characteristicUUID.equals(CLI_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on Data Characteristic");
//            }//For Test UART
//            else if(characteristicUUID.equals(UART_TX_CHARACTERISTIC_UUID)) {
//                Timber.d("%s", "Change on TX Fanstel Characteristic");
//            }
//        }
    };







    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (ImageButton) this.findViewById(R.id.imageButton);
        Log.d("Here", "SeekBarCreation");

        imView = findViewById(R.id.imageView3);


        seekBarCreation();
        Log.d("Here", "SeekBarCreation");

//        BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
//            @Override
//            public void onServicesDiscovered(BluetoothPeripheral peripheral) {
//                super.onServicesDiscovered(peripheral);
//                Log.d("DeviceName", peripheral.getName());
//            }
//        };
//
        BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
            @Override
            public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
                Log.d("Here", "Discovered Peripheral");
                central.stopScan();
                central.connectPeripheral(peripheral, peripheralCallback);
            }
        };

        central = new BluetoothCentralManager(getApplicationContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
        Log.d("Here", "CentralCreation");
        // Scan for peripherals with a certain service UUID
        String[] lampi = new String[]{"LAMPI b827eb6105b5"};
//        Log.d("")
//        central.scanForPeripheralsWithNames(lampi);

        button.bringToFront();
//
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
//        {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
//            {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
//                return;
//            }
//        }






        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isOn = !isOn;
                Log.d("Button", Boolean.toString(button.isSelected()));

//                button.set
                if (isOn) {
                    button.setColorFilter(saturation_seekbar.getColor());
                } else {
                    button.setColorFilter(Color.WHITE);
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void seekBarCreation() {

        colorSeekBar = findViewById(R.id.color_seek_bar);
        saturation_seekbar = findViewById((R.id.saturation_seekbar));
        brightness_seekbar = findViewById((R.id.brightness_seekbar));
//        colorSeekBar.setPosition( 50, 0);
        brightness_seekbar.setPosition(0, 0);
//        saturation_seekbar.setPosition(50, 0);

//        imView.setColorFilter(new BlendModeColorFilter(Color.YELLOW, BlendMode.SRC_ATOP));
//
//        Resources res = getResources();
//        final Drawable drawable = res.getDrawable(R.drawable.rectangle);
//        drawable.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
//        ImageView img = (ImageView)findViewById(R.id.iv_priority);
//        img.setBackgroundDrawable(drawable);

//        GradientDrawable drawable = (GradientDrawable) imView.getDrawable();
//        drawable.setColor(Color.YELLOW);




//        Drawable unwrappedDrawable = AppCompatResources.getDrawable(imView.getContext(), R.drawable.rectangle);
//        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
//        DrawableCompat.setTint(wrappedDrawable, Color.YELLOW);

        // i is hue (outa 100), i1 is saturation (out of 255) i2 color
        colorSeekBar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);
            Log.d("ColorSeekbar", hsv[0] + " " + hsv[1] + " " + hsv[2]);

            saturation_seekbar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            imView.setColorFilter(saturation_seekbar.getColor());
//            view.setBackgr
            Log.d("Color Change", Integer.toString(colorBarPosition));
            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });

        saturation_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);
            Log.d("SaturationSeekBar", hsv[0] + " " + hsv[1] + " " + hsv[2]);
            imView.setColorFilter(color);
            Log.d("Saturation Change", Integer.toString(colorBarPosition));
            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });

        brightness_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
//            view.(saturation_seekbar.getColor());

//            Bitmap bm=((BitmapDrawable)imView.getDrawable()).getBitmap();
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);
            int brightChange = (int)(hsv[2] * 255);
            Log.d("BrightnessSeekbar", hsv[0] + " " + hsv[1] + " " + hsv[2] + " " + colorBarPosition + " " + alphaBarPosition + " " + brightChange );

//
//            Drawable gradientDrawable= ((GradientDrawable) imView.getBackground()).mutate();
//            ((GradientDrawable)gradientDrawable ).setColor(Color.LTGRAY);

//            imView.setColorFilter(brightIt((brightChange)));


//            BlendModeColorFilter bm = new BlendModeColorFilter(saturation_seekbar.getColor(), BlendMode.LUMINOSITY);

//            bm.getMode().
//            imView.setColorFilter(bm);

//            Drawable unwrappedDrawable = AppCompatResources.getDrawable(imView.getContext(), R.drawable.rectangle);
//            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
//            DrawableCompat.setTint(wrappedDrawable, Color.YELLOW);
            Color.HSVToColor(hsv);
//            view.setBackgroundColor(color);
//            imView.setColorFilter(saturation_seekbar.getColor());
//            imView.setBackgroundColor(saturation_seekbar.getColor());
//            imView.setColorFilter(saturation_seekbar.getColor());
            imView.setColorFilter(brightIt(brightChange));
//            changeBrightness(imView);
//            imView.setColorFilter(saturation_seekbar.getColor());

//            imView.setColorFilter(brightIt(brightChange)); // 0 - 255
//            imView.setImageBitmap(changeBitmapContrastBrightness(bitmap, 1, 0));
//            imView.setImageBitmap(changeBitmapContrastBrightness(BitmapFactory.decodeResource(getResources(), R.drawable.rectangle), (float) colorBarPosition / 100f, 1));
//            imView.setColorFilter(brightness(200));
            Log.d("Brightness Change", Integer.toString(color));
        });
    }


//    private void changeBrightness(ImageView imView) {
//        float brightness = (float) brightness_seekbar.getColorBarPosition();
//
//        float[] colorMatrix = {
//                1, 0, 0, 0, brightness,
//                0, 1, 0, 0, brightness,
//                0, 0, 1, 0, brightness,
//                0, 0, 0, 1, 0
//        };
//
//        ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
//        imView.setColorFilter(colorFilter);
//
//    }

    private ColorMatrixColorFilter brightness(float value) {
        ColorMatrix cmB = new ColorMatrix();
        cmB.set(new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0});
        return new ColorMatrixColorFilter(cmB);
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }
//
//    /**
//     *
//     * @param bmp input bitmap
//     * @param contrast 0..10 1 is default
//     * @param brightness -255..255 0 is default
//     * @return new bitmap
//     */
//    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
//    {
//        ColorMatrix cm = new ColorMatrix(new float[]
//                {
//                        contrast, 0, 0, 0, brightness,
//                        0, contrast, 0, 0, brightness,
//                        0, 0, contrast, 0, brightness,
//                        0, 0, 0, 1, 0
//                });
//
//        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
//
//        Canvas canvas = new Canvas(ret);
//
//        Paint paint = new Paint();
//        paint.setColorFilter(new ColorMatrixColorFilter(cm));
//        canvas.drawBitmap(bmp, 0, 0, paint);
//
//        return ret;
//    }

    public ColorMatrixColorFilter brightIt(int fb) {
        float[] hsv = new float[3];

        int red = colorSeekBar.getColor();
        Color.colorToHSV(red, hsv);
//        int ret = Color.HSVToColor(hsv);

        float input = hsv[0];
        Log.d("RED", colorSeekBar.getColor() + "" );
        ColorMatrix cmB = new ColorMatrix();
        cmB.set(new float[] {
                1, 0, 0, 0, fb,
                0, 1, 0, 0, fb,
                0, 0, 1, 0, fb,
                0, 0, 0, 1, 0   });

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(cmB);

//        colorMatrix.
//Canvas c = new Canvas(b2);
//Paint paint = new Paint();
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);
//paint.setColorFilter(f);
        return f;
    }

//
//    private ColorMatrixColorFilter brightness(float value) {
//        ColorMatrix cmB = new ColorMatrix();
//        cmB.set(new float[]{
//                1, 0, 0, 0, value,
//                0, 1, 0, 0, value,
//                0, 0, 1, 0, value,
//                0, 0, 0, 1, 0});
//        return new ColorMatrixColorFilter(cmB);
//    }

//
//    public Bitmap SetBrightness(Bitmap src, int value) {
//        // original image size
//        int width = src.getWidth();
//        int height = src.getHeight();
//        // create output bitmap
//        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
//        // color information
//        int A, R, G, B;
//        int pixel;
//
//        // scan through all pixels
//        for(int x = 0; x < width; ++x) {
//            for(int y = 0; y < height; ++y) {
//                // get pixel color
//                pixel = src.getPixel(x, y);
//                A = Color.alpha(pixel);
//                R = Color.red(pixel);
//                G = Color.green(pixel);
//                B = Color.blue(pixel);
//
//                // increase/decrease each channel
//                R += value;
//                if(R > 255) { R = 255; }
//                else if(R < 0) { R = 0; }
//
//                G += value;
//                if(G > 255) { G = 255; }
//                else if(G < 0) { G = 0; }
//
//                B += value;
//                if(B > 255) { B = 255; }
//                else if(B < 0) { B = 0; }
//
//                // apply new pixel color to output bitmap
//                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
//            }
//        }
//
//        // return final image
//        return bmOut;
//    }

//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 1:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 &&
//                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Log.d("MainActivity", "Permission approved");
//                } else {
//                    Log.d("MainActivity", "Error getting permission");
//                }
//                return;
//        }
//
//    }


}


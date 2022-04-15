package com.example.myfirstapp;

import static android.bluetooth.BluetoothGatt.CONNECTION_PRIORITY_HIGH;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import androidx.appcompat.app.AppCompatActivity;

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
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanSettings;
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

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_COARSE_LOCATION_REQUEST = 1;
    ColorSeekBar colorSeekBar;
    ColorSeekBar saturation_seekbar;
    ColorSeekBar brightness_seekbar;
    ImageButton button;
    boolean isOn = true;
    ImageView imView;
    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private final String lampiMacAddress = "b8:27:eb:61:05:b5";
    private GradientDrawable rectangle;
//    private ScanResultsAdapter resultsAdapter;

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
        public void onServicesDiscovered(BluetoothPeripheral peripheral) {
            super.onServicesDiscovered(peripheral);
        }
    };
//    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
//        @Override
//        public void onServicesDiscovered(final BluetoothPeripheral peripheral) {
//            Log.d("Here", "discovered services");
//
//            // Request a new connection priority
//            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
//
//            if (peripheral.getName() != null) {
//                Log.d("Peripheral Name", peripheral.getName());
//
//            } else {
//                Log.d("Peripheral Name", "Does not exist");
//            }
//        }
//
////            // Turn on notifications for Data Characteristic
////            if(peripheral.getService(DATA_CHARACTERISTIC_UUID) != null) {
////                peripheral.setNotify(peripheral.getCharacteristic(SPOT_AG_SERVICE, DATA_CHARACTERISTIC_UUID), true);
////            }
////
////            // Turn on notifications for Status Characteristic
////            if(peripheral.getService(STATUS_CHARACTERISTIC_UUID) != null) {
////                peripheral.setNotify(peripheral.getCharacteristic(SPOT_AG_SERVICE, STATUS_CHARACTERISTIC_UUID), true);
////            }
////            // For TEST with UART Fanstel
////            // Turn on notification for Health Thermometer Service
////            if(peripheral.getService(UART_TX_CHARACTERISTIC_UUID) != null) {
////                Timber.i("discovered services UART TX");
////                peripheral.setNotify(peripheral.getCharacteristic(UART_FANSTEL_UUID, UART_TX_CHARACTERISTIC_UUID), true);
////            }
////
////        @Override
////        public void onNotificationStateUpdate(BluetoothPeripheral peripheral, BluetoothGattCharacteristic characteristic, int status) {
////            if( status == GATT_SUCCESS) {
////                if(peripheral.isNotifying(characteristic)) {
////                    Timber.i("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid());
////                } else {
////                    Timber.i("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid());
////                }
////            } else {
////                Timber.e("ERROR: Changing notification state failed for %s", characteristic.getUuid());
////            }
////        }
////
////        @Override
////        public void onCharacteristicWrite(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
////            if( status == GATT_SUCCESS) {
////                Timber.i("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
////            } else {
////                Timber.i("ERROR: Failed writing <%s> to <%s>", bytes2String(value), characteristic.getUuid().toString());
////            }
////        }
////
////        @Override
////        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, int status) {
////            if(status != GATT_SUCCESS) return;
////            UUID characteristicUUID = characteristic.getUuid();
////            BluetoothBytesParser parser = new BluetoothBytesParser(value);
////
////            if (characteristicUUID.equals(SECURITY_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on Security Characteristic");
////            }
////            else if(characteristicUUID.equals(STATUS_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on Status Characteristic");
////            }
////            else if(characteristicUUID.equals(CONTROL_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on Control Characteristic");
////            }
////            else if(characteristicUUID.equals(DATA_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on Data Characteristic");
////            }
////            else if(characteristicUUID.equals(CLI_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on Data Characteristic");
////            }//For Test UART
////            else if(characteristicUUID.equals(UART_TX_CHARACTERISTIC_UUID)) {
////                Timber.d("%s", "Change on TX Fanstel Characteristic");
////            }
////        }
//    };

    RxBleDevice device;



    private void scanBleDevices() {
//        scanDisposable = (Disposable) rxBleClient.scanBleDevices(
//                new ScanSettings.Builder()
//                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                        .build(),
//                new ScanFilter.Builder()
//                            .setDeviceAddress(lampiMacAddress)
//                        // add custom filters if needed
//                        .build()
//        );

//        Log.d("Scanned", scanDisposable.toString());
//        scanDisposable.dispose();
    }


    //    @RequiresApi(api = Build.VERSION_CODES.Q)
//    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (ImageButton) this.findViewById(R.id.imageButton);
        Log.d("Here", "SeekBarCreation");

        imView = findViewById(R.id.rectangleBackground);
        rectangle = (GradientDrawable) imView.getBackground();

        rxBleClient = RxBleClient.create(this);

        discoverBluetooth();



//        Disposable scanSubscription = (Disposable) rxBleClient.scanBleDevices(ssB);

//        Disposable disposable = device.establishConnection(false) // <-- autoConnect flag
//                .subscribe(
//                        rxBleConnection -> {
//                            // All GATT operations are done through the rxBleConnection.
//                            Log.d("Connected", "CONNECTED");
//                        },
//                        throwable -> {
//                            // Handle an error here.
//                        }
//                );

// When done... dispose and forget about connection teardown :)
//        disposable.dispose();

//        com.polidea.rxandroidble2.scan.ScanSettings ssBuilder = new com.polidea.rxandroidble2.scan.ScanSettings.Builder()
//                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                .build();
//        Disposable scanSubscription = rxBleClient.scanBleDevices(ssBuilder)
//                // add filters if needed
//
//                .subscribe(
//                        scanResult -> {
//                            // Process scan result here.
//                            Log.d("BLE", "FOUND DEVICE");
//                        },
//                        throwable -> {
//                            // Handle an error here.
//                        }
//                );

// When done, just dispose.
//        scanSubscription.dispose();

//        Disposable scanSubscription = rxBleClient.scanBleDevices(new ScanSettings.Builder().build());
//                new ScanSettings.Builder().build())
//                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                        .build()
//        )
//                .subscribe(
//                        scanResult -> {
//                            // Process scan result here.
//                        },
//                        throwable -> {
//                            // Handle an error here.
//                        }
//                );

// When done, just dispose.
//        scanSubscription.dispose();
//
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        int REQUEST_ENABLE_BT = 1;
//        rxBleClient.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

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
//        BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
//            @Override
//            public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
//                Log.d("Here", "Discovered Peripheral");
//                central.stopScan();
//                central.connectPeripheral(peripheral, peripheralCallback);
//            }
//        };

//        central = new BluetoothCentralManager(getApplicationContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
//        central.stopScan();
//        central.
        Log.d("Here", "CentralCreation");
        // Scan for peripherals with a certain service UUID
        String[] lampi = new String[]{"LAMPI b827eb6105b5"};
//        Log.d("")
//        central.scanForPeripheralsWithNames(lampi);
//        central.scanForPeripherals();

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

    public void discoverBluetooth() {
        RxBleClient rxBleClient = RxBleClient.create(this);
        String lampiAddress = "LAMPI b827eb6105b5";





        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        int REQUEST_ENABLE_BT = 1;


        this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        scanBleDevices();


//        Disposable scanSubscription = rxBleClient.scanBleDevices(
//
//
//                new ScanSettings.Builder()
//                         .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
//                         .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
//                        .build()
//                // add filters if needed
//        )
//                .subscribe(
//                        scanResult -> {
//                            // Process scan result here.
//                            Log.d("Subscribed", scanResult.getBleDevice().getName());
//                        },
//                        throwable -> {
//                            // Handle an error here.
//                            Log.d("Subscribed", "Not Subbed");
//                        }
//                );
//
//// When done, just dispose.
//        scanSubscription.dispose();
//
//        Log.d("Here!!", "REACHED");
//
//        device = rxBleClient.getBleDevice(lampiMacAddress);
//
//        Log.d("Here!.", "Reached...");
//        Disposable disposable = device.establishConnection(true) // <-- autoConnect flag
//                .subscribe(
//                        rxBleConnection -> {
//                            // All GATT operations are done through the rxBleConnection.
//                            Log.d("Here!?", "REACHED ");
//                        },
//                        throwable -> {
//                            // Handle an error here.
//                            Log.d("Here!?", "Not Reached");
//                        }
//                );
//
//        Log.d("Here!,", "HERE");
//// When done... dispose and forget about connection teardown :)
//        disposable.dispose();
    }


    public void seekBarCreation() {
        colorSeekBar = findViewById(R.id.color_seek_bar);
        saturation_seekbar = findViewById((R.id.saturation_seekbar));
        brightness_seekbar = findViewById((R.id.brightness_seekbar));
        brightness_seekbar.setPosition(0, 0);

        colorSeekBar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);

            saturation_seekbar.setColorSeeds(new int[]{Color.WHITE, colorSeekBar.getColor()});
            rectangle.setColor(saturation_seekbar.getColor());

            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });

        saturation_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            rectangle.setColor(color);
            if (isOn) {
                button.setColorFilter(saturation_seekbar.getColor());
            }
        });

        brightness_seekbar.setOnColorChangeListener((colorBarPosition, alphaBarPosition, color) -> {
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);

            rectangle.setColor(saturation_seekbar.getColor());
            rectangle.setColorFilter(applyLightness(colorBarPosition));
        });
    }

    private PorterDuffColorFilter applyLightness(int progress) {
        return new PorterDuffColorFilter(Color.argb(progress*255/100, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
    }
}


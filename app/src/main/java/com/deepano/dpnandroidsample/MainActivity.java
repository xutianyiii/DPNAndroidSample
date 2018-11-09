package com.deepano.dpnandroidsample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DeepanoApi";
    final String ACTION_USB_PERMISSION = "Deepano_USB_PERMISSION";

    private UsbManager usbManager = null;
    private PendingIntent mPermissionIntent = null;

    private int fd = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null){
                Log.e(TAG,"BroadcastReceiver action is null");
            }else{
                switch (action){
                    case ACTION_USB_PERMISSION:
                        Log.e(TAG,"ACTION_USB_PERMISSION");
                        synchronized (this){
                            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (usbDevice != null) {
                                    UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
                                    if (connection != null) {
                                        String path =
                                                Environment.getExternalStorageDirectory().getPath()+"/SSD_MobileNet_object.blob";
                                        fd = connection.getFileDescriptor();
                                        DeepanoApiFactory.initDevice(fd);
                                        //DeepanoApiFactory.startCamera();
                                        DeepanoApiFactory.netProc(path);
                                    } else
                                        Log.e(TAG, "UsbManager openDevice failed");
                                }
                            } else {
                                Log.e(TAG, "permission is denied");
                            }
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        Log.e(TAG,"ACTION_USB_DEVICE_ATTACHED");
                        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        while (deviceIterator.hasNext()) {
                            UsbDevice device = deviceIterator.next();
                            Log.e(TAG, device.getDeviceName() + " " + Integer.toHexString(device.getVendorId()) +
                                    " " + Integer.toHexString(device.getProductId()));
                            usbManager.requestPermission(device, mPermissionIntent);
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        Log.e(TAG,"ACTION_USB_DEVICE_DETACHED");
                        break;
                    case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                        Log.e(TAG,"ACTION_USB_ACCESSORY_ATTACHED");
                        break;
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                        Log.e(TAG,"ACTION_USB_ACCESSORY_DETACHED");
                        break;
                    default:
                        break;
                }
            }
        }
    };

}

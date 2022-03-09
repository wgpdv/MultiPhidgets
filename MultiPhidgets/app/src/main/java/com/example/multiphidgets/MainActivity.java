package com.example.multiphidgets;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Bundle;

import com.phidget22.*;

public class MainActivity extends Activity {

    VoltageRatioInput voltageRatioInput0;
    RCServo rcServo0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            //Allow direct USB connection of Phidgets
            if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                com.phidget22.usb.Manager.Initialize(this);

            //Enable server discovery to list remote Phidgets
            this.getSystemService(Context.NSD_SERVICE);
            Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);
            
            Net.addServer("", "137.44.182.135", 5661, "", 0);

            //Create your Phidget channels
            voltageRatioInput0 = new VoltageRatioInput();
            rcServo0 = new RCServo();

            //Set addressing parameters to specify which channel to open (if any)
            voltageRatioInput0.setDeviceSerialNumber(30683);
            voltageRatioInput0.setChannel(0);
            rcServo0.setDeviceSerialNumber(307804);
            //Set the sensor type to match the analog sensor you are using after opening the Phidget
//            voltageRatioInput0.setSensorType(VoltageRatioSensorType.PN_1128);

            voltageRatioInput0.addAttachListener(onCh_Attach);
            voltageRatioInput0.addDetachListener(onCh_Detach);
            voltageRatioInput0.addVoltageRatioChangeListener(onCh_VoltageRatioChange);
            rcServo0.addAttachListener(onCh_Attach);
            rcServo0.addDetachListener(onCh_Detach);
            rcServo0.addPositionChangeListener(onCh_PositionChange);

            voltageRatioInput0.open(5000);
            rcServo0.open(5000);

        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }
    }

    public VoltageRatioInputVoltageRatioChangeListener onCh_VoltageRatioChange =
            new VoltageRatioInputVoltageRatioChangeListener() {
                @Override
                public void onVoltageRatioChange(VoltageRatioInputVoltageRatioChangeEvent e) {
                    Log.d("Voltage Ratio Value: ", String.valueOf(e.getVoltageRatio()));
//                    Log.d("Voltage Ratio Value: ", e.toString());
                    try {
                        rcServo0.setTargetPosition(e.getVoltageRatio()*180);
                        rcServo0.setEngaged(true);
//                        Thread.sleep(100);
//                        rcServo0.setEngaged(false);
                    } catch (PhidgetException phidgetException) {
                        phidgetException.printStackTrace();
                    }
                }
            };

    public static RCServoPositionChangeListener onCh_PositionChange =
            new RCServoPositionChangeListener() {
                @Override
                public void onPositionChange(RCServoPositionChangeEvent e) {
                    Log.d("RCServo Position: ", String.valueOf(e.getPosition()));
                }
            };

    public static AttachListener onCh_Attach =
            new AttachListener() {
                @Override
                public void onAttach(AttachEvent e) {
                    Log.d("Attach Listener", e.toString());
                }
            };

    public static DetachListener onCh_Detach =
            new DetachListener() {
                @Override
                public void onDetach(DetachEvent e) {
                    Log.d("Detach Listener", e.toString());
                }
            };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //Close your Phidgets once the program is done.
            voltageRatioInput0.close();
            rcServo0.close();
            Log.d("onDestroy: ", "Closed channels.");
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }
}
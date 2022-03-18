package com.example.multiphidgets;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Bundle;
import android.util.Pair;

import com.phidget22.*;
import com.phidget22.usb.Manager;

import java.util.ArrayList;


/**
 *  Issie TODO:
 *      -   API @ https://www.phidgets.com/?view=api&lang=Java (select PhidgetInterfaceKit Green, then next to that select Didgital Output API)
 *
 *      -   Interesting methods are setState() and getState() in the DigitalOutput Class [DigitalOutput out = new DigitalOutput()]
 *
 *      -   Should just be a case of a for each loop over the (ArrayList) and out.setState(False) [or true, obvs]
 */

public class MainActivity extends Activity {

    final int LIGHT_COUNT = 6;
    final int COLUMN_1_NUM = 4;
    final int COLUMN_2_NUM = 2;

    final int V_R_SERIAL = 39830;

    private enum pattern {
        checker,
        on,
        off
    };

    /**
     * Braille letters as arrays based on the light setup:
     * 0 5
     * 1 4
     * 2 3
     */
    Pair<Character, int[]> brailleA = new Pair<Character, int[]>('A', new int[] {1,0,0,0,0,0}); // every line needs to look like this :/
    int[] brailleB = {1,1,0,0,0,0};
    int[] brailleC = {1,0,0,0,0,1};
    int[] brailleD = {1,0,0,0,1,1};
    int[] brailleE = {1,0,0,0,1,0};
    int[] brailleF = {1,1,0,0,0,1};
    int[] brailleG = {1,1,0,0,1,1};
    int[] brailleH = {1,1,0,0,1,0};
    int[] brailleI = {0,1,0,0,0,1};
    int[] brailleJ = {0,1,0,0,1,1};
    int[] brailleK = {1,0,1,0,0,0};
    int[] brailleL = {1,1,1,0,0,0};
    int[] brailleM = {1,0,1,0,0,1};
    int[] brailleN = {1,0,1,0,1,1};
    int[] brailleO = {1,0,1,0,1,0};
    int[] brailleP = {1,1,1,0,0,1};
    int[] brailleQ = {1,1,1,1,1,0};
    int[] brailleR = {1,1,1,0,1,0};
    int[] brailleS = {0,1,1,0,0,1};
    int[] brailleT = {0,1,1,0,1,1};
    int[] brailleU = {1,0,1,1,0,0};
    int[] brailleV = {1,1,1,1,0,0};
    int[] brailleW = {0,1,0,1,1,1};
    int[] brailleX = {1,0,1,1,0,1};
    int[] brailleY = {1,0,1,1,1,1};
    int[] brailleZ = {1,0,1,1,1,0};



    private pattern state = pattern.off;

    ArrayList<DOut> dOuts = new ArrayList<DOut>();

    VoltageRatioInput voltageRatioInput0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            //Allow direct USB connection of Phidgets
            if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST))
                Manager.Initialize(this);

            //Enable server discovery to list remote Phidgets
            this.getSystemService(Context.NSD_SERVICE);
            Net.enableServerDiscovery(ServerType.DEVICE_REMOTE);

            Net.addServer("DESKTOP-P20L7MD", "137.44.128.78", 5661, "admin", 0);
//            Net.addServer("KCW-DESKTOP", "192.168.1.78", 5661, "admin", 0);

            //Create your Phidget channels
            voltageRatioInput0 = new VoltageRatioInput();


            for(int i = 0; i < LIGHT_COUNT; i++) {
                dOuts.add(new DOut(i));

            }

            //Set addressing parameters to specify which channel to open (if any)
            voltageRatioInput0.setDeviceSerialNumber(V_R_SERIAL);
            voltageRatioInput0.setChannel(0);
            for(int i = 0; i < dOuts.size(); i++) {
                dOuts.get(i).getDigitalOutput().setChannel(i);
            }

            //Set the sensor type to match the analog sensor you are using after opening the Phidget
//            voltageRatioInput0.setSensorType(VoltageRatioSensorType.PN_1128);

            voltageRatioInput0.addAttachListener(onCh_Attach);
            voltageRatioInput0.addDetachListener(onCh_Detach);
            voltageRatioInput0.addVoltageRatioChangeListener(onCh_VoltageRatioChange);




            voltageRatioInput0.open(5000);
            for (DOut d : dOuts) {
                d.getDigitalOutput().open(5000);
            }
            // rcServo0.open(5000);

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
                        if (voltageRatioInput0.getVoltageRatio() < 0.33) {
                            state = pattern.on;
                        } else if (voltageRatioInput0.getVoltageRatio() > 0.66) {
                            state = pattern.off;
                        } else {
                            state = pattern.checker;
                        }
                    } catch (PhidgetException phidgetException) {
                        phidgetException.printStackTrace();
                    }
//                    UpdateLEDs(); // This must be uncommented if slider is to work (also check method for comment)
                }
            };

    public void UpdateLEDs(int[] letter) {
        try {
            // Turn them all off first, then some back on if needed
//            for(DOut d : dOuts) {
//                d.getDigitalOutput().setState(false);
//            }

            for (int i = 0; i < dOuts.size(); i++) {
                if (letter[i] == 1) {
                    dOuts.get(i).getDigitalOutput().setState(true);
                } else {
                    dOuts.get(i).getDigitalOutput().setState(false);
                }
            }

//            switch (state) {
//                case on:
//                    for (DOut d : dOuts) {
//                        d.getDigitalOutput().setState(true);
//                    }
//                    break;
//                case off:
//                    // Already turned off
//                    break;
//                case checker:
//                    for (int i = 0; i < dOuts.size(); i++) {
//                        if (i%2 == 0) {
//                            dOuts.get(i).getDigitalOutput().setState(true);
//                        } else {
//                            dOuts.get(i).getDigitalOutput().setState(false);
//                        }
//
//                    }
//                    break;
//                default:
//                    // Nothing
//                    break;
//            }
        } catch (PhidgetException pE) {
            pE.printStackTrace();
        }
    }

    public void ParseLetterToBraille(char c) {
        c = Character.toUpperCase(c);


    }

//    public static RCServoPositionChangeListener onCh_PositionChange =
//            new RCServoPositionChangeListener() {
//                @Override
//                public void onPositionChange(RCServoPositionChangeEvent e) {
//                    Log.d("RCServo Position: ", String.valueOf(e.getPosition()));
//                }
//            };

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
            for (DOut d : dOuts) {
                d.getDigitalOutput().close();
            }
            Log.d("onDestroy: ", "Closed channels.");
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }

    public class DOut {
        private int portNum;
        private DigitalOutput d;

        public DOut(int portNum) {
            this.portNum = portNum;
            try {
                this.d = new DigitalOutput();
            } catch (PhidgetException pE) {
                pE.printStackTrace();
            }
        }

        public int getPortNum() {
            return portNum;
        }

        public  DigitalOutput getDigitalOutput() {
            return d;
        }
    }
}


package com.example.multiphidgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

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

    final String    SERVER_NAME     = "DESKTOP-P20L7MD";
    final String    SERVER_IP       = "137.44.129.206";
    final int       SERVER_PORT     = 5661;
    final String    SERVER_PASSWORD = "admin";

    final int LIGHT_COUNT = 6;
    final int COLUMN_1_NUM = 4;
    final int COLUMN_2_NUM = 2;

    final int V_R_SERIAL = 39830;

    private enum pattern {
        checker,
        on,
        off
    };

    static String sentence = "This is the default sentence";
    static char[] sentenceLetters;
    static int charIndex = 0;
    static String[] sentences = {"Hes going to sacrifice himself", "Connor sucks","Its over anakin I have the high ground"};

    /**
     * Braille letters as arrays based on the light setup:
     * 0 5
     * 1 4
     * 2 3
     */
    Pair<Character, int[]> brailleA = new Pair<Character, int[]>('A', new int[] {1,0,0,0,0,0}); // every line needs to look like this :/
    Pair<Character, int[]> brailleB = new Pair<Character, int[]>('B', new int[] {1,1,0,0,0,0});
    Pair<Character, int[]> brailleC = new Pair<Character, int[]>('C', new int[] {1,0,0,0,0,1});
    Pair<Character, int[]> brailleD = new Pair<Character, int[]>('D', new int[] {1,0,0,0,1,1});
    Pair<Character, int[]> brailleE = new Pair<Character, int[]>('E', new int[] {1,0,0,0,1,0});
    Pair<Character, int[]> brailleF = new Pair<Character, int[]>('F', new int[] {1,1,0,0,0,1});
    Pair<Character, int[]> brailleG = new Pair<Character, int[]>('G', new int[] {1,1,0,0,1,1});
    Pair<Character, int[]> brailleH = new Pair<Character, int[]>('H', new int[] {1,1,0,0,1,0});
    Pair<Character, int[]> brailleI = new Pair<Character, int[]>('I', new int[] {0,1,0,0,0,1});
    Pair<Character, int[]> brailleJ = new Pair<Character, int[]>('J', new int[] {0,1,0,0,1,1});
    Pair<Character, int[]> brailleK = new Pair<Character, int[]>('K', new int[] {1,0,1,0,0,0});
    Pair<Character, int[]> brailleL = new Pair<Character, int[]>('L', new int[] {1,1,1,0,0,0});
    Pair<Character, int[]> brailleM = new Pair<Character, int[]>('M', new int[] {1,0,1,0,0,1});
    Pair<Character, int[]> brailleN = new Pair<Character, int[]>('N', new int[] {1,0,1,0,1,1});
    Pair<Character, int[]> brailleO = new Pair<Character, int[]>('O', new int[] {1,0,1,0,1,0});
    Pair<Character, int[]> brailleP = new Pair<Character, int[]>('P', new int[] {1,1,1,0,0,1});
    Pair<Character, int[]> brailleQ = new Pair<Character, int[]>('Q', new int[] {1,1,1,1,1,0});
    Pair<Character, int[]> brailleR = new Pair<Character, int[]>('R', new int[] {1,1,1,0,1,0});
    Pair<Character, int[]> brailleS = new Pair<Character, int[]>('S', new int[] {0,1,1,0,0,1});
    Pair<Character, int[]> brailleT = new Pair<Character, int[]>('T', new int[] {0,1,1,0,1,1});
    Pair<Character, int[]> brailleU = new Pair<Character, int[]>('U', new int[] {1,0,1,1,0,0});
    Pair<Character, int[]> brailleV = new Pair<Character, int[]>('V', new int[] {1,1,1,1,0,0});
    Pair<Character, int[]> brailleW = new Pair<Character, int[]>('W', new int[] {0,1,0,1,1,1});
    Pair<Character, int[]> brailleX = new Pair<Character, int[]>('X', new int[] {1,0,1,1,0,1});
    Pair<Character, int[]> brailleY = new Pair<Character, int[]>('Y', new int[] {1,0,1,1,1,1});
    Pair<Character, int[]> brailleZ = new Pair<Character, int[]>('Z', new int[] {1,0,1,1,1,0});
    Pair<Character, int[]> brailleSpace = new Pair<Character, int[]>(' ', new int[] {0,0,0,0,0,0});

    Pair[] letters;

    private pattern state = pattern.off;

    ArrayList<DOut> dOuts = new ArrayList<DOut>();

    VoltageRatioInput voltageRatioInput0;
    RFID ch;
    Toast errToast;

    Vibrator vib;

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

            Net.addServer(SERVER_NAME, SERVER_IP, SERVER_PORT, SERVER_PASSWORD, 0);
//            Net.addServer("KCW-DESKTOP", "192.168.1.78", 5661, "admin", 0);

            //Create your Phidget channels
            voltageRatioInput0 = new VoltageRatioInput();
            ch = new RFID();

            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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

            // RFID stuff
            ch.addAttachListener(new AttachListener() {
                public void onAttach(final AttachEvent attachEvent) {
                    AttachEventHandler handler = new AttachEventHandler(ch);
                    runOnUiThread(handler);
                }
            });

            ch.addDetachListener(new DetachListener() {
                public void onDetach(final DetachEvent detachEvent) {
                    DetachEventHandler handler = new DetachEventHandler(ch);
                    runOnUiThread(handler);

                }
            });

            ch.addErrorListener(new ErrorListener() {
                public void onError(final ErrorEvent errorEvent) {
                    ErrorEventHandler handler = new ErrorEventHandler(ch, errorEvent);
                    runOnUiThread(handler);

                }
            });

            ch.addTagListener(new RFIDTagListener() {
                public void onTag(RFIDTagEvent tagEvent) {
                    RFIDTagEventHandler handler = new RFIDTagEventHandler(ch, tagEvent);
                    runOnUiThread(handler);
                }
            });

            ch.addTagLostListener(new RFIDTagLostListener() {
                public void onTagLost(RFIDTagLostEvent tagLostEvent) {
                    RFIDTagLostEventHandler handler = new RFIDTagLostEventHandler(ch, tagLostEvent);
                    runOnUiThread(handler);
                }
            });

            ch.open();

            // Initialise letters and such
            letters = LettersAsArray();
            UpdateSentence(sentence);

            // Get buttons
            Button btnPrev = findViewById(R.id.buttonPrevious);
            Button btnNext = findViewById(R.id.buttonNext);

            //Do stuff with clicks
            btnPrev.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onClick(View view) {
                    Log.d("Button", "Previous Clicked");
                    if (charIndex > 0) {
                        charIndex--;
                        UpdateLEDs(getBrailleLayout(sentenceLetters[charIndex]));
                        // Vibrate
                        vib.vibrate(20);
                        // Sound?
                    }
                }
            });
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Button", "Next Clicked");
                    if (charIndex < sentenceLetters.length-1) {
                        charIndex++;
                        UpdateLEDs(getBrailleLayout(sentenceLetters[charIndex]));
                        // Vibrate?
                        // Sound?
                    }
                }
            });

//            UpdateLEDs(getBrailleLayout(sentenceLetters[charIndex]));

        } catch (PhidgetException pe) {
            pe.printStackTrace();
        }
    }

    public VoltageRatioInputVoltageRatioChangeListener onCh_VoltageRatioChange =
            new VoltageRatioInputVoltageRatioChangeListener() {
                @Override
                public void onVoltageRatioChange(VoltageRatioInputVoltageRatioChangeEvent e) {
//                    Log.d("Voltage Ratio Value: ", String.valueOf(e.getVoltageRatio()));
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

            System.out.println("Displaying:\n"
                    + letter[0] + " " + letter[5] + "\n"
                    + letter[1] + " " + letter[4] + "\n"
                    + letter[2] + " " + letter[3]);

            for (int i = 0; i < dOuts.size(); i++) {
                System.out.println("Port " + i + " = " + letter[i]);
                if (letter[i] == 1) {
                    dOuts.get(i).getDigitalOutput().setState(true);
                } else {
                    dOuts.get(i).getDigitalOutput().setState(false);
                }
                for (DOut d : dOuts) {
                    System.out.println(d.getPortNum() + " : " + d.getDigitalOutput().getState());
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

    public Pair<Character,int[]>[] LettersAsArray() {
        return new Pair[]{brailleA, brailleB, brailleC, brailleD, brailleE,
                brailleF, brailleG, brailleH, brailleI, brailleJ, brailleK,
                brailleL, brailleM, brailleN, brailleO, brailleP, brailleQ,
                brailleR, brailleS, brailleT, brailleU, brailleV, brailleW,
                brailleX, brailleY, brailleZ, brailleSpace};
    }

    public int[] getBrailleLayout(Character c) {
        c = Character.toUpperCase(c);
        System.out.println("Finding '" + c + "'");
        for (Pair p : letters) {
            if (p.first == c) {
                System.out.println("Found '" + c + "'!");
                return (int[]) p.second;
            }
        }
        // If not in array
        System.out.println("Error, no such letter");
        return new int[]{0, 0, 0, 0, 0, 0};
    }

    public static void UpdateSentence(String s) {
        sentence = s;
        sentenceLetters = s.toCharArray();
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

    class RFIDTagEventHandler implements Runnable {
        Phidget ch;
        RFIDTagEvent tagEvent;

        public RFIDTagEventHandler(Phidget ch, RFIDTagEvent tagEvent) {
            this.ch = ch;
            this.tagEvent = tagEvent;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            System.out.println(tagEvent.getTag() + " has been detected!");
            switch (tagEvent.getTag()) {
                case "01069341f9":
                    MainActivity.UpdateSentence(sentences[0]);
                    break;
                case "0106934df3":
                    MainActivity.UpdateSentence(sentences[1]);
                    break;
                case "010238865b":
                    MainActivity.UpdateSentence(sentences[2]);
                    break;
                default:
                    MainActivity.UpdateSentence("This is the default sentence");
                    break;
            }
//            charIndex = 0;
//            UpdateLEDs(getBrailleLayout(sentenceLetters[charIndex]));

            vib.vibrate(200);

//            TextView tagTxt = (TextView)findViewById(R.id.tagTxt);
//            TextView protocolTxt = (TextView)findViewById(R.id.protocolTxt);

//            tagTxt.setText(tagEvent.getTag());
//            protocolTxt.setText(tagEvent.getProtocol().getMessage());

        }
    }

    class RFIDTagLostEventHandler implements Runnable {
        Phidget ch;
        RFIDTagLostEvent tagLostEvent;

        public RFIDTagLostEventHandler(Phidget ch, RFIDTagLostEvent tagLostEvent) {
            this.ch = ch;
            this.tagLostEvent = tagLostEvent;
        }

        public void run() {
            System.out.println(tagLostEvent.getTag() + " has been detected!");

//            TextView tagTxt = (TextView)findViewById(R.id.tagTxt);
//            TextView protocolTxt = (TextView)findViewById(R.id.protocolTxt);

//            tagTxt.setText("");
//            protocolTxt.setText("");
        }
    }

    class AttachEventHandler implements Runnable {
        Phidget ch;

        public AttachEventHandler(Phidget ch) {
            this.ch = ch;
        }

        public void run() {
//            LinearLayout settingsAndData = (LinearLayout) findViewById(R.id.settingsAndData);
//            settingsAndData.setVisibility(LinearLayout.VISIBLE);

//            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);

//            attachedTxt.setText("Attached");
//            try {
////                TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
////                TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
////                TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
////                TextView channelTxt = (TextView) findViewById(R.id.channelTxt);
////                TextView hubPortTxt = (TextView) findViewById(R.id.hubPortTxt);
////                TextView labelTxt = (TextView) findViewById(R.id.labelTxt);
//                TextView tagTxt = (TextView) findViewById(R.id.tagTxt);
////                TextView protocolTxt = (TextView) findViewById(R.id.protocolTxt);
////                CheckBox antennaEnabledBox = (CheckBox) findViewById(R.id.enableAntennaBox);
//
//                nameTxt.setText(ch.getDeviceName());
//                serialTxt.setText(Integer.toString(ch.getDeviceSerialNumber()));
//                versionTxt.setText(Integer.toString(ch.getDeviceVersion()));
//                channelTxt.setText(Integer.toString(ch.getChannel()));
//                hubPortTxt.setText(Integer.toString(ch.getHubPort()));
//                labelTxt.setText(ch.getDeviceLabel());
//
//                tagTxt.setText("");
//                protocolTxt.setText("");
//                antennaEnabledBox.setChecked(((RFID)ch).getAntennaEnabled());
//            } catch (PhidgetException e) {
//                e.printStackTrace();
//            }

            //notify that we're done
            synchronized (this) {
                this.notify();
            }
        }
    }

    class DetachEventHandler implements Runnable {
        Phidget ch;

        public DetachEventHandler(Phidget ch) {
            this.ch = ch;
        }

        public void run() {

            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class ErrorEventHandler implements Runnable {
        Phidget ch;
        ErrorEvent errorEvent;

        public ErrorEventHandler(Phidget ch, ErrorEvent errorEvent) {
            this.ch = ch;
            this.errorEvent = errorEvent;
        }
        public void run() {
            if (errToast == null)
                errToast = Toast.makeText(getApplicationContext(), errorEvent.getDescription(), Toast.LENGTH_SHORT);

            //replace the previous toast message if a new error occurs
            errToast.setText(errorEvent.getDescription());
            errToast.show();
        }
    }
}


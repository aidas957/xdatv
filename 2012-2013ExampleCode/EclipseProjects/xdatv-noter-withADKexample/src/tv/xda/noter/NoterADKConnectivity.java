/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package tv.xda.noter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.google.android.apps.adk2.BTConnection;
import com.google.android.apps.adk2.BTDeviceListActivity;
import com.google.android.apps.adk2.Connection;
import com.google.android.apps.adk2.UsbConnection;
import com.google.android.apps.adk2.Utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class NoterADKConnectivity extends Activity implements OnClickListener,
        Callback, OnSharedPreferenceChangeListener, Runnable {
    public static final String TAG = "ADK_2012";
    private Handler mDeviceHandler;
    private Handler mSettingsPollingHandler;

    private UsbManager mUSBManager;
    private SharedPreferences mPreferences;



    private byte[] mQueryBuffer = new byte[4];
    private byte[] mEmptyPayload = new byte[0];

    public static Connection BluetoothSerialConnection=null; //XDA

    UsbAccessory mAccessory;

    private boolean mPollSettings = false;


    private ArrayList<String> mSoundFiles;
    static final String TUNES_FOLDER = "/Tunes";

    static final byte CMD_GET_PROTO_VERSION = 1; // () -> (u8 protocolVersion)
    static final byte CMD_GET_SENSORS = 2; // () -> (sensors:
                                            // i32,i32,i32,i32,u16,u16,u16,u16,u16,u16,u16,i16,i16,i16,i16,i16,i16)
    static final byte CMD_FILE_LIST = 3; // FIRST: (char name[]) -> (fileinfo or
                                            // single zero byte) OR NONLATER: ()
                                            // -> (fileinfo or empty or single
                                            // zero byte)
    static final byte CMD_FILE_DELETE = 4; // (char name[0-255)) -> (char
                                            // success)
    static final byte CMD_FILE_OPEN = 5; // (char name[0-255]) -> (char success)
    static final byte CMD_FILE_WRITE = 6; // (u8 data[]) -> (char success)
    static final byte CMD_FILE_CLOSE = 7; // () -> (char success)
    static final byte CMD_GET_UNIQ_ID = 8; // () -> (u8 uniq[16])
    static final byte CMD_BT_NAME = 9; // (char name[]) -> () OR () -> (char
                                        // name[])
    static final byte CMD_BT_PIN = 10; // (char PIN[]) -> () OR () -> (char
                                        // PIN[])
    static final byte CMD_TIME = 11; // (timespec) -> (char success)) OR () >
                                        // (timespec)
    static final byte CMD_SETTINGS = 12; // () ->
                                            // (alarm:u8,u8,u8,brightness:u8,color:u8,u8,u8:volume:u8)
                                            // or
                                            // (alarm:u8,u8,u8,brightness:u8,color:u8,u8,u8:volume:u8)
                                            // > (char success)
    static final byte CMD_ALARM_FILE = 13; // () -> (char file[0-255]) OR (char
                                            // file[0-255]) > (char success)
    static final byte CMD_GET_LICENSE = 14; // () -> (u8 licensechunk[]) OR ()
                                            // if last sent
    static final byte CMD_DISPLAY_MODE = 15; // () -> (u8) OR (u8) -> ()
    static final byte CMD_LOCK = 16; // () -> (u8) OR (u8) -> ()

    private static final boolean gLogPackets = false;

    static final int DIALOG_NO_PRESETS_ID = 0;

    private static NoterADKConnectivity sHomeActivity = null;

    private static String curBtName = "<UNKNOWN>";

    public static NoterADKConnectivity get() {
        return sHomeActivity;
    }

    public boolean startPollingSettings() {
        boolean wasPolling = mPollSettings;
        mPollSettings = true;
        if (!wasPolling) {
            pollSettings();
        }
        return wasPolling;
    }

    public void stopPollingSettings() {
        mPollSettings = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDeviceHandler = new Handler(this);
        mSettingsPollingHandler = new Handler(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mUSBManager = UsbManager.getInstance(this);

        mSoundFiles = new ArrayList<String>();



        connectToAccessory();

        startLicenseUpload();
        sHomeActivity = this;

        startPollingSettings();
    }



    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (id == DIALOG_NO_PRESETS_ID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unsupported feature.");
            dialog = builder.create();
        }
        return dialog;
    }

    private void changeBtName() {

        // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog, null);
        final EditText e = (EditText) textEntryView
                .findViewById(R.id.btname_edit);

        AlertDialog ad = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("Set ADK Bluetooth Name")
                .setView(textEntryView)
                .setPositiveButton(R.string.set_bt_name_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                curBtName = e.getText().toString();
                                if (curBtName.equals(""))
                                    curBtName = "ADK 2012";

                                byte b[] = null;
                                try {
                                    b = curBtName.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e1) {
                                    // well aren't you SOL....
                                    e1.printStackTrace();
                                }
                                byte b2[] = new byte[b.length + 1];
                                for (int i = 0; i < b.length; i++)
                                    b2[i] = b[i];
                                b2[b.length] = 0;

                                sendCommand(CMD_BT_NAME, CMD_BT_NAME, b2);
                            }
                        })
                .setNegativeButton(R.string.set_bt_name_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                // user cancels
                            }
                        }).create();

        e.setText(curBtName);
        ad.show();
    }

    private void disconnect() {
        finish();
    }

    private void startLicenseUpload() {
        Log.i(NoterADKConnectivity.TAG, "startLicenseUpload");
        new ByteArrayOutputStream();
        sendCommand(CMD_GET_LICENSE, 33);
    }

    private void pollSettings() {
        if (mPollSettings) {
            sendCommand(CMD_SETTINGS, CMD_SETTINGS);
            sendCommand(CMD_DISPLAY_MODE, CMD_DISPLAY_MODE);
            sendCommand(CMD_LOCK, CMD_LOCK);
            Message msg = mSettingsPollingHandler.obtainMessage(99);
            if (!mSettingsPollingHandler.sendMessageDelayed(msg, 500)) {
                Log.e(NoterADKConnectivity.TAG, "faled to queue settings message");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        pollSettings();
    }

    @Override
    public void onDestroy() {
        sHomeActivity = null;
        if (BluetoothSerialConnection != null) {
            try {
                BluetoothSerialConnection.close();
            } catch (IOException e) {
            } finally {
                BluetoothSerialConnection = null;
            }
        }
        super.onDestroy();
    }

    
    //view handler
    public void onClick(View v) {
        
        
        
    }



    public void connectToAccessory() {
        // bail out if we're already connected
        if (BluetoothSerialConnection != null)
            return;

        if (getIntent().hasExtra(BTDeviceListActivity.EXTRA_DEVICE_ADDRESS)) {
            String address = getIntent().getStringExtra(
                    BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Log.i(NoterADKConnectivity.TAG, "want to connect to " + address);
            BluetoothSerialConnection = new BTConnection(address);
            performPostConnectionTasks();
        } else {
            // assume only one accessory (currently safe assumption)
            UsbAccessory[] accessories = mUSBManager.getAccessoryList();
            UsbAccessory accessory = (accessories == null ? null
                    : accessories[0]);
            if (accessory != null) {
                if (mUSBManager.hasPermission(accessory)) {
                    openAccessory(accessory);
                } else {
                    // synchronized (mUsbReceiver) {
                    // if (!mPermissionRequestPending) {
                    // mUsbManager.requestPermission(accessory,
                    // mPermissionIntent);
                    // mPermissionRequestPending = true;
                    // }
                    // }
                }
            } else {
                // Log.d(TAG, "mAccessory is null");
            }
        }

    }

    public void disconnectFromAccessory() {
        closeAccessory();
    }

    private void openAccessory(UsbAccessory accessory) {
        BluetoothSerialConnection = new UsbConnection(this, mUSBManager, accessory);
        performPostConnectionTasks();
    }

    private void performPostConnectionTasks() {
        sendCommand(CMD_GET_PROTO_VERSION, CMD_GET_PROTO_VERSION);
        sendCommand(CMD_SETTINGS, CMD_SETTINGS);
        sendCommand(CMD_BT_NAME, CMD_BT_NAME);
        sendCommand(CMD_ALARM_FILE, CMD_ALARM_FILE);
        listDirectory(TUNES_FOLDER);

        Thread thread = new Thread(null, this, "ADK 2012");
        thread.start();
    }

    public void closeAccessory() {
        try {
            BluetoothSerialConnection.close();
        } catch (IOException e) {
        } finally {
            BluetoothSerialConnection = null;
        }
    }

    public void run() {
        int ret = 0;
        byte[] buffer = new byte[16384];
        int bufferUsed = 0;

        while (ret >= 0) {
            try {
                ret = BluetoothSerialConnection.getInputStream().read(buffer, bufferUsed,
                        buffer.length - bufferUsed);
                bufferUsed += ret;
                int remainder = process(buffer, bufferUsed);
                if (remainder > 0) {
                    System.arraycopy(buffer, remainder, buffer, 0, bufferUsed
                            - remainder);
                    bufferUsed = remainder;
                } else {
                    bufferUsed = 0;
                }
            } catch (IOException e) {
                break;
            }
        }
        Intent connectIntent = new Intent(this, MainActivity.class);
        connectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(connectIntent);
    }

    public int process(byte[] buffer, int bufferUsed) {
        if (gLogPackets) {
            Log.i(NoterADKConnectivity.TAG,
                    "read " + bufferUsed + " bytes: "
                            + Utilities.dumpBytes(buffer, bufferUsed));
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer, 0,
                bufferUsed);
        ProtocolHandler ph = new ProtocolHandler(mDeviceHandler, inputStream);
        ph.process();
        return inputStream.available();
    }

    public void listDirectory(String path) {
        mSoundFiles.clear();
        byte[] payload = new byte[path.length() + 1];
        for (int i = 0; i < path.length(); ++i) {
            payload[i] = (byte) path.charAt(i);
        }
        payload[path.length()] = 0;
        sendCommand(CMD_FILE_LIST, CMD_FILE_LIST, payload);
    }

    public void getSensors() {
        sendCommand(CMD_GET_SENSORS, CMD_GET_SENSORS);
    }

    public byte[] sendCommand(int command, int sequence, byte[] payload,
            byte[] buffer) {
        int bufferLength = payload.length + 4;
        if (buffer == null || buffer.length < bufferLength) {
            Log.i(NoterADKConnectivity.TAG, "allocating new command buffer of length "
                    + bufferLength);
            buffer = new byte[bufferLength];
        }

        buffer[0] = (byte) command;
        buffer[1] = (byte) sequence;
        buffer[2] = (byte) (payload.length & 0xff);
        buffer[3] = (byte) ((payload.length & 0xff00) >> 8);
        if (payload.length > 0) {
            System.arraycopy(payload, 0, buffer, 4, payload.length);
        }
        if (BluetoothSerialConnection != null && buffer[1] != -1) {
            try {
                if (gLogPackets) {
                    Log.i(NoterADKConnectivity.TAG,
                            "sendCommand: "
                                    + Utilities
                                            .dumpBytes(buffer, buffer.length));
                }
                BluetoothSerialConnection.getOutputStream().write(buffer);
            } catch (IOException e) {
                Log.e(NoterADKConnectivity.TAG, "accessory write failed", e);
            }
        }
        return buffer;
    }

    public byte[] sendCommand(int command, int sequence, byte[] payload) {
        return sendCommand(command, sequence, payload, null);
    }

    public byte[] sendCommand(int command, int sequence) {
        return sendCommand(command, sequence, mEmptyPayload, mQueryBuffer);
    }

   

    public Object getAccessory() {
        return mAccessory;
    }

      

   

    private static class ProtocolHandler {
        InputStream mInputStream;
        Handler mHandler;

        public ProtocolHandler(Handler handler, InputStream inputStream) {
            mHandler = handler;
            mInputStream = inputStream;
        }

        int readByte() throws IOException {
            int retVal = mInputStream.read();
            if (retVal == -1) {
                throw new RuntimeException("End of stream reached.");
            }
            return retVal;
        }

        int readInt16() throws IOException {
            int low = readByte();
            int high = readByte();
            if (gLogPackets) {
                Log.i(NoterADKConnectivity.TAG, "readInt16 low=" + low + " high=" + high);
            }
            return low | (high << 8);
        }

        byte[] readBuffer(int bufferSize) throws IOException {
            byte readBuffer[] = new byte[bufferSize];
            int index = 0;
            int bytesToRead = bufferSize;
            while (bytesToRead > 0) {
                int amountRead = mInputStream.read(readBuffer, index,
                        bytesToRead);
                if (amountRead == -1) {
                    throw new RuntimeException("End of stream reached.");
                }
                bytesToRead -= amountRead;
                index += amountRead;
            }
            return readBuffer;
        }

        public void process() {
            mInputStream.mark(0);
            try {
                while (mInputStream.available() > 0) {
                    if (gLogPackets)
                        Log.i(NoterADKConnectivity.TAG, "about to read opcode");
                    int opCode = readByte();
                    if (gLogPackets)
                        Log.i(NoterADKConnectivity.TAG, "opCode = " + opCode);
                    if (isValidOpCode(opCode)) {
                        int sequence = readByte();
                        if (gLogPackets)
                            Log.i(NoterADKConnectivity.TAG, "sequence = " + sequence);
                        int replySize = readInt16();
                        if (gLogPackets)
                            Log.i(NoterADKConnectivity.TAG, "replySize = " + replySize);
                        byte[] replyBuffer = readBuffer(replySize);
                        if (gLogPackets) {
                            Log.i(NoterADKConnectivity.TAG,
                                    "replyBuffer: "
                                            + Utilities.dumpBytes(replyBuffer,
                                                    replyBuffer.length));
                        }
                        processReply(opCode & 0x7f, sequence, replyBuffer);
                        mInputStream.mark(0);
                    }
                }
                mInputStream.reset();
            } catch (IOException e) {
                Log.i(NoterADKConnectivity.TAG, "ProtocolHandler error " + e.toString());
            }
        }

        boolean isValidOpCode(int opCodeWithReplyBitSet) {
            if ((opCodeWithReplyBitSet & 0x80) != 0) {
                int opCode = opCodeWithReplyBitSet & 0x7f;
                return ((opCode >= CMD_GET_PROTO_VERSION) && (opCode <= CMD_LOCK));
            }
            return false;
        }

        private void processReply(int opCode, int sequence, byte[] replyBuffer) {
            Message msg = mHandler.obtainMessage(opCode, sequence, 0,
                    replyBuffer);
            mHandler.sendMessage(msg);
        }
    }

    public String[] getAlarmSounds() {
        String[] r = new String[mSoundFiles.size()];
        return mSoundFiles.toArray(r);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean handleMessage(Message msg) {
        // TODO Auto-generated method stub
        return false;
    }
}

/*XDATV Noter provides a place to store notes
 *Copyright (C) 2013  Adam Outler for XDA-Developer TV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tv.xda.noter;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.apps.adk2.BTDeviceListActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener {
    String mMenuButton;
    TextView mTextOutput;
    EditText mTextInput;
    final static String FILENAME = "notes.txt";
    static final int PICK_BLUETOOTH= 0;
    String mBluetoothDataOut="XDATV";

    boolean mConnectionCommanded=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWidgets();
        loadTextFromFile();
    }

    /*
     * loads line-by-line from text file into the textOutput in reverse order
     */
    private void loadTextFromFile() {
        File f = new File(getFilesDir(), FILENAME);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                mTextOutput.setText(line + "\n" + mTextOutput.getText());
            }
            br.close();
            

         
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * sets up items displayed on view
     */
    private void setupWidgets() {
        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);
        mTextOutput = (TextView) findViewById(R.id.textView);
        mTextInput = (EditText) findViewById(R.id.textInput);
        mTextOutput.setMovementMethod(new ScrollingMovementMethod());
        
        mTextOutput.setOnClickListener(this); //if textView is touched
        
        
    }

    /*
     * Handles clicks for XDATVNother
     */
    @Override
    public void onClick(View v) { // view handler
        if (v.getId() == R.id.save) { 
            doSaveButtonPress();
        }
    }

    /*
     * OnClick save-button, write textInput to textOuput and save text to file
     */
    private void doSaveButtonPress() { // button handler
        String text = mTextInput.getText().toString();
        mTextInput.setText("");
        mTextOutput.setText(text + "\n" + mTextOutput.getText().toString());
        
        //bluetooth send data if connection was commanded
        mBluetoothDataOut=text;
        if (mConnectionCommanded){ //false by default until turned on
            sendBluetoothData(); 
        }
        
        try {
            FileOutputStream fo = openFileOutput(FILENAME, Context.MODE_APPEND);
            fo.write(text.getBytes());
            fo.write("\n".getBytes());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.connect_bluetooth:  //bluetooth connection button
                mConnectionCommanded=true;   //we want connection now
                NoterADKConnectivity.BluetoothSerialConnection=null; //reset the connection
                connectToBluetoothDevice(); //connect to device
                return true;
            default:
                return true;
        }
    }
    
    /*
     * sends bluetooth data
     */
    private void sendBluetoothData() { 
        if (NoterADKConnectivity.BluetoothSerialConnection!=null && mBluetoothDataOut!=""){  //only if connected and have data to send
            //Command 17 is the scroller
            Log.v("ADKReturn",new NoterADKConnectivity().sendCommand(17, 17, mBluetoothDataOut.getBytes()).toString());
        }
    }
    /*
     * Connects to bluetooth or provides options
     */
    private void connectToBluetoothDevice(){
        NoterADKConnectivity.BluetoothSerialConnection=null;
        startActivityForResult(new Intent(this, BTDeviceListActivity.class),PICK_BLUETOOTH);
    }
    

    //performs actions when return from result. 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_BLUETOOTH) {
            sendBluetoothData(); //on connect
        }
    }
}
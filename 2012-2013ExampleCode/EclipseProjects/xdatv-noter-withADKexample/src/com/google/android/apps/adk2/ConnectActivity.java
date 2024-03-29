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
package com.google.android.apps.adk2;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import tv.xda.noter.R;



public class ConnectActivity extends Activity implements OnClickListener {
	private Button mBluetoothButton;

	public void onCreate(Bundle savedInstanceState) {
	    Log.e("test","opened");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);
		mBluetoothButton = (Button) findViewById(R.id.connect_bluetooth_button);
		mBluetoothButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.connect_bluetooth_button) {
			startActivityForResult(new Intent(this, BTDeviceListActivity.class),0);
		}
	}
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == 0) {

        }
    }
}

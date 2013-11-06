package com.google.android.DemoKit;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

public class OutputController extends AccessoryController {

	private boolean mVertical;

	OutputController(DemoKitActivity hostActivity, boolean vertical) {
		super(hostActivity);
		// mVertical = vertical;
	}

	protected void onAccesssoryAttached() {

		setupLEDController(1, R.id.LED1);
		setupLEDController(2, R.id.LED2);
		setupLEDController(3, R.id.LED3);
		setupLEDController(4, R.id.LED4);
		setupLEDController(5, R.id.LED5);
		setupLEDController(6, R.id.LED6);
		setupLEDController(7, R.id.LED7);
		setupLEDController(8, R.id.LED8);
		setupLEDController(9, R.id.LED9);
		setupLEDController(10, R.id.LED10);
		setupLEDController(11, R.id.LED11);
		setupLEDController(12, R.id.LED12);
		setupLEDController(13, R.id.LED13);
		setupLEDController(14, R.id.LED14);
		setupLEDController(15, R.id.LED15);
		setupLEDController(16, R.id.LED16);
	
	}


	private void setupLEDController(int index, int viewId) {
		LEDController r = new LEDController(mHostActivity, index,
				getResources());
		r.attachToView((ViewGroup) findViewById(viewId));

	}

}



package com.example.mytodolist;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class LaunchOther  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = "http://jenkins.casual-dev.com"; //removed secure URL key
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        System.exit(0);
    }

}
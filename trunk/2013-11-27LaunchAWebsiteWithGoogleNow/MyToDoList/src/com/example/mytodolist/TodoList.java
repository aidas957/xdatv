
package com.example.mytodolist;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class TodoList extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String url = "http://builds.casual-dev.com/todo.html"; //specify the website
        Intent i = new Intent(Intent.ACTION_VIEW); //make a new intent
        i.setData(Uri.parse(url)); //set the intent as a URI with our website
        startActivity(i); //launch the intent
        System.exit(0); //exit because this is a website launcher app.  
    }

}

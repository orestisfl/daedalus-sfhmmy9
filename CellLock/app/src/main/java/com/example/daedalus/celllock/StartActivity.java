package com.example.daedalus.celllock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class StartActivity extends Activity {


    private static final String EXTRA_MESSAGE = "0";
    protected BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);


    }

    public void startConnection(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);


    }
}

package com.cs736.batterydrain;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {

    // Layout widgets
    public Button btnStart;
    public Button btnStop;
    public EditText testDurationInput;
    public GridLayout testDurationLayout;
    public Spinner spnTestSelection;
    public TextView batteryLevelValue;
    public TextView batteryTempValue;
    public TextView batteryVoltageValue;
    public TextView testTimeRemaining;

    // Test threads
    public Thread test1Thread;
    public Thread test2Thread;
    public Thread test3Thread;
    public Thread test4Thread;

    // Action handlers
    private BatteryStatusReceiver batteryStatusReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the Test Selection dropdown menu
        ArrayList<String> testSelectionList = new ArrayList<String>();
        spnTestSelection = (Spinner) findViewById(R.id.testSelection);

        // Setup the different panels
        setupBatteryPanel();
        setupTestPanel();

        // Setup test threads
        setupTestThreads();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Initialize the battery panel at the top of the screen
     */
    public void setupBatteryPanel() {
        batteryLevelValue = (TextView) findViewById(R.id.batteryLevelValue);
        batteryTempValue = (TextView) findViewById(R.id.batteryTempValue);
        batteryVoltageValue = (TextView) findViewById(R.id.batteryVoltageValue);

        batteryLevelValue.setText(BatteryManager.EXTRA_LEVEL);
        batteryTempValue.setText("?");
        batteryVoltageValue.setText("?");

        // Setup the battery status receiver
        batteryStatusReceiver = new BatteryStatusReceiver();
        batteryStatusReceiver.setMainActivityHandler(this);

        IntentFilter callInterceptorIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryStatusReceiver,  callInterceptorIntentFilter);
    }

    public class BatteryStatusReceiver extends BroadcastReceiver {

        MainActivity main = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            float temp = (float)intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10;
            float voltage = (float)intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000;

            Log.d("BatteryActivity", "Battery level: " + level + "%");
            Log.d("BatteryActivity", "Battery temp: " + temp + "C");
            Log.d("BatteryActivity", "Battery voltage: " + voltage + "V");

            main.updateBatteryPanel(level, temp, voltage);
        }

        void setMainActivityHandler(MainActivity main)
        {
            this.main = main;
        }

    }

    /*
    Initialize the test panel at the bottom
     */
    public void setupTestPanel() {
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        testDurationLayout = (GridLayout) findViewById(R.id.testDurationLayout);
        testDurationInput = (EditText) findViewById(R.id.testDurationInput);
        testTimeRemaining = (TextView) findViewById(R.id.testTimeRemaining);

        btnStop.setEnabled(false);
        testDurationInput.setText("15");
        testTimeRemaining.setVisibility(View.GONE);
    }

    /*
    Reset layout elements in the test panel (typically called after completing a test)
     */
    public void resetTestPanel() {
        testDurationLayout.setVisibility(View.VISIBLE);
        testTimeRemaining.setVisibility(View.GONE);
        spnTestSelection.setEnabled(true);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    /*
    This is where the magic happens. These are the threads that run while the tests are active.
     */
    public void setupTestThreads() {

        // Test 1 is a simple test that runs the CPU
        test1Thread = new Thread(new Runnable() {
            public void run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                while(!Thread.interrupted()) {
                    try {
                        // First do ten floating point division operations
                        for(int i = 0; i < 10; i ++) {
                            Random rand = new Random();
                            float rand1 = rand.nextFloat();
                            float rand2 = rand.nextFloat();
                            float randDiv = rand1 / rand2;
                        }
                        // Then sleep for 1ms
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // We've been interrupted: no more messages.
                        return;
                    }
                }
            }
        });

        test2Thread = new Thread(new Runnable() {
            public void run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                while(!Thread.interrupted()) {
                    try {
                        Log.d("test2Thread", "Test 2 running!");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // We've been interrupted: no more messages.
                        return;
                    }
                }
            }
        });

        test3Thread = new Thread(new Runnable() {
            public void run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                while(!Thread.interrupted()) {
                    try {
                        Log.d("test3Thread", "Test 3 running!");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // We've been interrupted: no more messages.
                        return;
                    }
                }
            }
        });

        test4Thread = new Thread(new Runnable() {
            public void run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                while(!Thread.interrupted()) {
                    try {
                        Log.d("test4Thread", "Test 4 running!");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // We've been interrupted: no more messages.
                        return;
                    }
                }
            }
        });
    }


    /*
    Update the battery information panel. This gets called from the BatteryStatusReceiver class.
     */
    public void updateBatteryPanel(int level, float temp, float voltage) {
        //Log.d("MainActivity", "updateBatteryPanel");
        batteryLevelValue.setText(level + "%");
        batteryTempValue.setText(String.format("%s", temp) + "C");
        batteryVoltageValue.setText(String.format("%s", voltage) + "V");
    }

    /*
    Event handler for the start test button
     */
    public void onClickBtnStart(View v) {

        // Adjust some layout elements
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        spnTestSelection.setEnabled(false);
        testTimeRemaining.setVisibility(View.VISIBLE);
        testDurationLayout.setVisibility(View.GONE);

        // Start the CountdownTimer object.
        int testDuration = Integer.valueOf(testDurationInput.getText().toString()) * 1000;
        new CountDownTimer(testDuration, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutesRemaining = ((int)millisUntilFinished / 1000) / 60;
                int secondsRemaining = ((int)millisUntilFinished / 1000) % 60;
                testTimeRemaining.setText("Time remaining: " + String.format("%02d", minutesRemaining) + ":" + String.format("%02d", secondsRemaining));
            }
            public void onFinish() {
                // Stop the running thread
                switch(spnTestSelection.getSelectedItem().toString()) {
                    case "Test 1": test1Thread.interrupt(); break;
                    case "Test 2": test2Thread.interrupt(); break;
                    case "Test 3": test3Thread.interrupt(); break;
                    case "Test 4": test4Thread.interrupt(); break;
                }
                resetTestPanel();
            }
        }.start();

        // Start the selected thread!
        switch(spnTestSelection.getSelectedItem().toString()) {
            case "Test 1": test1Thread.start(); break;
            case "Test 2": test2Thread.start(); break;
            case "Test 3": test3Thread.start(); break;
            case "Test 4": test4Thread.start(); break;
        }

    }

    /*
    Event handler for the stop test button
     */
    public void onClickBtnStop(View v) {
        // Stop the running thread
        switch(spnTestSelection.getSelectedItem().toString()) {
            case "Test 1": test1Thread.interrupt(); break;
            case "Test 2": test2Thread.interrupt(); break;
            case "Test 3": test3Thread.interrupt(); break;
            case "Test 4": test4Thread.interrupt(); break;
        }
        resetTestPanel();
    }
}

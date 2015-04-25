package com.cs736.batterydrain;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.PowerManager.*;
import android.os.Process;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.app.AlarmManager;
import android.app.PendingIntent;

import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {

    // Application context
    public Context mainContext;

    // Layout widgets
    public Button btnReset;
    public Button btnStart;
    public Button btnStop;
    public EditText testDurationInput;
    public GridLayout testSetupLayout;
    public GridLayout testResultsLayout;
    public Spinner spnTestSelection;
    public TextView batteryLevelValue;
    public TextView batteryTempValue;
    public TextView batteryVoltageValue;
    public TextView testTimeRemainingValue;
    public TextView testBatteryLevelDrainValue;
    public TextView testBatteryTempIncreaseValue;
    public TextView testBatteryVoltageDrainValue;
    public Vibrator phoneVibrate;

    // Test threads
    public Thread test1Thread;
    public Thread test2Thread;
    public Thread test3Thread;
    public Thread test4Thread;
    public Thread test5Thread;
    public Thread test6Thread;

    // Manager, peripherals, etc.
    BluetoothAdapter btAdapter;
    int testDuration;
    PowerManager powerManager;
    WakeLock partialWakeLock;

    int startBatteryLevel = 0;
    float startBatteryTemp = 0;
    float startBatteryVoltage = 0;

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

        // Setup any other objects or services
        phoneVibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize wakelocks, peripherals, adapters, etc.
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mainContext = getApplicationContext();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Keep the screen on, no matter what!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        btnReset = (Button) findViewById(R.id.btnReset);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        testDurationInput = (EditText) findViewById(R.id.testDurationInput);
        testTimeRemainingValue = (TextView) findViewById(R.id.testTimeRemainingValue);
        testBatteryLevelDrainValue = (TextView) findViewById(R.id.testBatteryLevelDrainValue);
        testBatteryTempIncreaseValue = (TextView) findViewById(R.id.testBatteryTempIncreaseValue);
        testBatteryVoltageDrainValue = (TextView) findViewById(R.id.testBatteryVoltageDrainValue);
        testResultsLayout = (GridLayout) findViewById(R.id.testResultsLayout);
        testSetupLayout = (GridLayout) findViewById(R.id.testSetupLayout);

        btnReset.setEnabled(false);
        btnStop.setEnabled(false);
        testDurationInput.setText("1200");
        testResultsLayout.setVisibility(View.GONE);
    }

    /*
    Reset layout elements in the test panel (typically called after completing a test)
     */
    public void resetTestPanel() {
        testSetupLayout.setVisibility(View.VISIBLE);
        testResultsLayout.setVisibility(View.GONE);
        btnReset.setEnabled(false);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    /*
    This is where the magic happens. All the battery exhaustion code goes in the threads!
     */
    public void setupTestThreads() {

        // Test 1 is Idle
        test1Thread = new Thread(new Runnable() {
            public void run() {
                // Do nothing!
            }
        });

        // Test 2 is Real world workload
        test2Thread = new Thread(new Runnable() {
            public void run() {

            }
        });

        // Test 3 is Real world workload + Alarm
        test3Thread = new Thread(new Runnable() {
            public void run() {
                openApplication(getBaseContext(), "cs736.nosleepalarm");
            }
        });

        // Test 4 is Real world workload + Wakelock
        test4Thread = new Thread(new Runnable() {
            public void run() {
                openApplication(getBaseContext(), "cs736.nosleepuserwakelock");
            }
        });

        // Test 5 is Real world workload + Malicious Workload + Alarm/Wakelock
        test5Thread = new Thread(new Runnable() {
            public void run() {
                openApplication(getBaseContext(), "cs736.com.stealthninja");
            }
        });

        // Test 6 is Full throttle (upper bound)
        test6Thread = new Thread(new Runnable() {
            public void run() {
                openApplication(getBaseContext(), "com.cs736.fullthrottle");
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

    public void alarmCalled(){
        // Stop the running thread
        stopActiveThread();

        // Reset layout elements
        btnStop.setEnabled(false);
        btnReset.setEnabled(true);
        testTimeRemainingValue.setText("00:00");

        // Release the wakelock if held
        if(partialWakeLock != null) {
            partialWakeLock.release();
        }

        // Output the results to file
        int endBatteryLevel = Integer.valueOf(batteryLevelValue.getText().toString().replace("%", ""));
        float endBatteryTemp = Float.valueOf(batteryTempValue.getText().toString().replace("C",""));
        float endBatteryVoltage = Float.valueOf(batteryVoltageValue.getText().toString().replace("V",""));
        outputResultsToFile(spnTestSelection.getSelectedItem().toString(), (testDuration/1000), startBatteryLevel, startBatteryTemp, startBatteryVoltage, endBatteryLevel, endBatteryTemp, endBatteryVoltage);

        // Vibrate the phone five times
        for(int i = 1; i <= 5; i ++)
        {
            phoneVibrate.vibrate(500);
            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex) {
                // do nothing
            }
        }
    }

    /*
    Event handler for the start test button
     */
    public void onClickBtnStart(View v) {

        // Adjust some layout elements
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        testSetupLayout.setVisibility(View.GONE);
        testResultsLayout.setVisibility(View.VISIBLE);

        // Measure start values of power elements
        startBatteryLevel = Integer.valueOf(batteryLevelValue.getText().toString().replace("%", ""));
        startBatteryTemp = Float.valueOf(batteryTempValue.getText().toString().replace("C",""));
        startBatteryVoltage = Float.valueOf(batteryVoltageValue.getText().toString().replace("V",""));

        // Start the CountdownTimer object.
        testDuration = Integer.valueOf(testDurationInput.getText().toString()) * 1000;

        //schedule alarm here!
        createAlarm(testDuration);

        //Log.d("MainActivity", "onClickBtnStart, starting the thread");
        // Start the selected thread!
        switch(spnTestSelection.getSelectedItem().toString()) {
            case "Idle (lower bound)": test1Thread.start(); break;
            case "Real world workload": test2Thread.start(); break;
            case "Real world workload + Alarm": test3Thread.start(); break;
            case "Real world workload + Wakelock": test4Thread.start(); break;
            case "Real world workload + Malicious workload + Alarm/Wakelock": test5Thread.start(); break;
            case "Full throttle (upper bound)": test6Thread.start(); break;
        }
    }

    BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(alarmReceiver);
            alarmCalled();
        }
    };

    public static final String ACTION_NAME = "com.helloword.MYACTION";
    private IntentFilter myFilter = new IntentFilter(ACTION_NAME);

    private void createAlarm(int millis){

        registerReceiver(alarmReceiver, myFilter);
        Intent intent = new Intent(ACTION_NAME);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millis, pendingIntent);
    }

    public void stopActiveThread() {
        switch(spnTestSelection.getSelectedItem().toString()) {
            case "Idle (lower bound)":
                test1Thread.interrupt();
                try { test1Thread.join(); }
                catch (InterruptedException ex) {}
                test1Thread = null;
                break;
            case "Real world workload":
                test2Thread.interrupt();
                try { test2Thread.join(); }
                catch (InterruptedException ex) {}
                test2Thread = null;
                break;
            case "Real world workload + Alarm":
                test3Thread.interrupt();
                try { test3Thread.join(); }
                catch (InterruptedException ex) {}
                test3Thread = null;
                break;
            case "Real world workload + Wakelock":
                test4Thread.interrupt();
                try { test4Thread.join(); }
                catch (InterruptedException ex) {}
                test4Thread = null;
                break;
            case "Real world workload + Malicious workload + Alarm/Wakelock":
                test5Thread.interrupt();
                try { test5Thread.join(); }
                catch (InterruptedException ex) {}
                test5Thread = null;
                break;
            case "Full throttle (upper bound)":
                test6Thread.interrupt();
                try { test6Thread.join(); }
                catch (InterruptedException ex) {}
                test6Thread = null;
                break;
        }

        // Request the garbage collector three times to make sure it runs, because Java
        System.gc();
        System.gc();
        System.gc();

        // Re-initialize the threads so we can run them again
        setupTestThreads();
    }

    /*
    Event handler for the stop test button
     */
    public void onClickBtnStop(View v) {
        stopActiveThread();
        resetTestPanel();

        // Release the wakelock if held
        if(partialWakeLock != null) {
            partialWakeLock.release();
        }
    }

    /*
    Event handler for the reset button
     */
    public void onClickBtnReset(View v)
    {
        resetTestPanel();
    }


    /*
    Utility function, outputs test results data to a file in the /Download folder.
     */
    public void outputResultsToFile(String testName, int testDuration, int startLevel, float startTemp, float startVoltage, int endLevel, float endTemp, float endVoltage)
    {
        File resultsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", "CS736BatteryDrain-Results.csv");
        String thisResult = "";

        if(!resultsFile.exists())
        {
            thisResult = "Test Name,Test Date,Test Duration (s),Start Level (%),Start Temp (C),Start Voltage (V),End Level (%),End Temp (C),End Voltage (V),Diff Level (%),Diff Temp (C),Diff Voltage(V)\n";
        }

        // Get current date and time
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentDate = dateFormat.format(new Date());

        // Calculate difference in battery level, temperature and voltage
        int diffLevel = endLevel - startLevel;
        float diffTemp = endTemp - startTemp;
        float diffVoltage = endVoltage - startVoltage;

        thisResult += testName + "," + currentDate + "," + testDuration + "," + startLevel + "," + startTemp + "," + startVoltage + "," + endLevel + "," + endTemp + "," + endVoltage + "," + diffLevel + "," + diffTemp + "," + diffVoltage +  "\n";
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(resultsFile, true));
            out.write(thisResult.getBytes());
            out.close();
        }
        catch(IOException ex) {

        }
    }

    /** Open another app.
     * Stolen from: http://stackoverflow.com/questions/2780102/open-another-application-from-your-own-intent/7596063#7596063
     * @param context current Context, like Activity, App, or Service
     * @param packageName the full package name of the app to open
     * @return true if likely successful, false if unsuccessful
     */
    public static boolean openApplication(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
                //throw new PackageManager.NameNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.putExtra("uid", Process.myUid());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d("openApplication", "Starting " + packageName + " with uid=" + Process.myUid());
            context.startActivity(i);



            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /*
    Utility function, reads in the frequency from cpu0. Used to measure CPU frequency switching.
     */
    int getCpu0Frequency()
    {
        int frequency = -1;
        try
        {
            Scanner cpu0FreqScanner = new Scanner(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"));
            frequency = Integer.parseInt(cpu0FreqScanner.nextLine());
            cpu0FreqScanner.close();
        }
        catch(FileNotFoundException ex)
        {

        }
        return frequency;
    }

}

/*
                // Grab high thread priority
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

                // Grab a partial wakelock
                partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
                partialWakeLock.acquire();

                // Set screen to maximum brightness
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 1F;
                getWindow().setAttributes(layout);

                // Run a Wifi scanner. At the end of every scan, start a new scan
                final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                class WifiReceiver extends BroadcastReceiver {
                    public void onReceive(Context c, Intent intent) {
                        //Log.d("test1Thread", "Received wifi results, starting a new wifi scan");
                        wifiManager.startScan();
                    }
                }
                WifiReceiver receiverWifi = new WifiReceiver();
                registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();

                // Run a Bluetooth scanner
                btAdapter.startDiscovery();

                // Run vibration -- actually don't run vibration because real malware would never do this
                //long[] vibratePattern = {0, 1000, 100};
                //phoneVibrate.vibrate(vibratePattern, 0);

                // Run the CPU
                float rand1;
                float rand2;
                float randDiv;
                Random rand = new Random();
                while (!Thread.interrupted()) {
                    rand1 = rand.nextFloat();
                    rand2 = rand.nextFloat();
                    randDiv = rand1 / rand2;
                    if(!btAdapter.isDiscovering()) {
                        Log.d("test1Thread", "Restarting Bluetooth discovery");
                        btAdapter.startDiscovery();
                    }
                }

                // Looks like the thread got interrupted. Shut down things that need to be shut down.
                phoneVibrate.cancel();
                return;
                */

/*
// Grab high thread priority
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

                // Grab a partial wakelock
                //partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
                //partialWakeLock.acquire();

                // Set screen to maximum brightness
                /*
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 1F;
                getWindow().setAttributes(layout);
                */

// Run a Wifi scanner. At the end of every scan, start a new scan
                /*
                final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                class WifiReceiver extends BroadcastReceiver {
                    public void onReceive(Context c, Intent intent) {
                        if(!Thread.interrupted()) {
                            Log.d("test3Thread", "Received wifi results, starting a new wifi scan");
                            wifiManager.startScan();
                        }
                    }
                }
                WifiReceiver receiverWifi = new WifiReceiver();
                registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
                */

                /*
                // Run a Bluetooth scanner
                btAdapter.startDiscovery();

                // Check Bluetooth discovery, make sure it restarts as often as possible
                while (!Thread.interrupted()) {
                    if(!btAdapter.isDiscovering()) {
                        Log.d("test3Thread", "Restarting Bluetooth discovery");
                        btAdapter.startDiscovery();
                        try {
                            Thread.sleep(500);
                        }
                        catch(Exception ex) {

                        }
                    }
                }
                */

/*
                // Grab high thread priority
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);

                // Set screen to maximum brightness
                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = 1F;
                getWindow().setAttributes(layout);

                // Run the CPU
                float rand1;
                float rand2;
                float randDiv;
                Random rand = new Random();
                while (!Thread.interrupted()) {
                    rand1 = rand.nextFloat();
                    rand2 = rand.nextFloat();
                    randDiv = rand1 / rand2;
                }

                // Looks like the thread got interrupted. Shut down things that need to be shut down.
                return;
                */


/*
            public void run() {
                //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                String applicationName = "com.inkle.eightydays";
                openApplication(mainContext, applicationName);

                try {
                    Thread.sleep(2000);
                }
                catch(Exception ex) {
                    // do nothing;
                }

                ActivityManager am = (ActivityManager)mainContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
                int processId = 0;
                Log.d("test3Thread", "My process pid=" + Process.myPid() + ", uid=" + Process.myUid());
                for(int i = 0; i < pids.size(); i++)
                {
                    Log.d("test3Thread", "i=" + i + ", processName=" + pids.get(i).processName + ", pid=" + pids.get(i).pid + ", uid=" + pids.get(i).uid);
                    ActivityManager.RunningAppProcessInfo info = pids.get(i);
                    if(pids.get(i).processName.toString().equals(applicationName))
                    {
                        Log.d("test3Thread", "Killing process name=" + pids.get(i).processName + ", id=" + pids.get(i).pid + ", uid=" + pids.get(i).uid);

                        Process.killProcess(pids.get(i).pid);
                        am.killBackgroundProcesses(applicationName);

                    }

                }

            }
            */
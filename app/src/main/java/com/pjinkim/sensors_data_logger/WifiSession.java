package com.pjinkim.sensors_data_logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.KeyException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;

public class WifiSession implements Runnable {
    public interface WifiScannerCallback {
        void processWifiScanResult(final int recordNums, final int currentApNums);
    }

    // properties
    private final static String LOG_TAG = WifiSession.class.getName();

    private final static int DEFAULT_INTERVAL = 1000;
    private int mScanInterval = DEFAULT_INTERVAL;

    private MainActivity mContext;
    private Handler mHandler = new Handler();

    private AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private AtomicBoolean mIsWritingFile = new AtomicBoolean(false);
    private AtomicInteger mNumScans = new AtomicInteger(0);

    private WifiManager mWifiManager;
    private WifiResultStreamer mFileStreamer;
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // scan wifi signals with WifiManager
            if (!mIsRunning.get()) {
                return;
            }
            mWifiManager.startScan();
            List<ScanResult> results = mWifiManager.getScanResults();
            Log.i(LOG_TAG, "onReceive: Scan result received. Number of AP: " + String.valueOf(results.size()));

            // save the wifi scan results to text file
            if (mIsWritingFile.get()) {
                try {
                    mFileStreamer.addWifiRecord(results);
                } catch (IOException | KeyException e) {
                    Log.e(LOG_TAG, "onReceive: Cannot add the scan results to file");
                    e.printStackTrace();
                }
            }

            // process wifi scan result in main class
            int numScan = mNumScans.addAndGet(1);
            mContext.processWifiScanResult(numScan, results.size());
        }
    };


    // constructor
    public WifiSession(@NonNull MainActivity context, int interval) {
        this.mContext = context;
        this.mScanInterval = interval;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    WifiSession(@NonNull MainActivity context) {
        this(context, DEFAULT_INTERVAL);
    }


    // methods
    public void stopSession() {

        if (mIsWritingFile.get()) {
            try {
                mFileStreamer
            }
        }
    }




    public void startSession(final String streamFolder) {

        // check wifi hardware is turned on
        if (!mWifiManager.isWifiEnabled()) {
            mContext.showAlertAndStop("Please turn on Wifi first!");
            return;
        }

        mIsRunning.set(true);
    }






    class WifiResultStreamer extends FileStreamer {

        // properties
        private BufferedWriter mWriter;


        // constructor
        WifiResultStreamer(final Context context, final String outputFolder) throws IOException {
            super(context, outputFolder);
            addFile("wifi", "wifi.txt");
            mWriter = getFileWriter("wifi");
        }


        // methods
        public void addWifiRecord(final List<ScanResult> results) throws IOException, KeyException {

            // execute the block with only one thread
            synchronized (this) {

                // check 'mWriter' variable
                if (mWriter == null) {
                    throw new KeyException("File writer wifi not found.");
                }

                // record wifi-related information in text file
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(results.size());
                stringBuilder.append('\n');
                for (ScanResult eachResult : results) {
                    stringBuilder.append(eachResult.timestamp);
                    stringBuilder.append('\t');
                    stringBuilder.append(eachResult.BSSID);
                    stringBuilder.append('\t');
                    stringBuilder.append(String.valueOf(eachResult.level));
                    stringBuilder.append('\n');
                }
                mWriter.write(stringBuilder.toString());
            }
        }

        @Override
        public void endFiles() throws IOException {

            // execute the block with only one thread
            synchronized (this) {
                mWriter.write("-1");
                mWriter.flush();
                mWriter.close();
            }
        }
    }











    // getter and setter
    public void setScanInterval(int newInterval) {
        mScanInterval = newInterval;
    }

    public boolean isRunning() {
        return mIsRunning.get();
    }

    public boolean isWritingFile() {
        return mIsWritingFile.get();
    }
}
/*
 * Copyright (c) 2013-2014 Aplix and/or its affiliates. All rights reserved.
 */
package com.chklab.apppass.app;

import com.aplixcorp.android.ble.beacon.Beacon;
import com.aplixcorp.android.ble.beacon.BeaconLocationListener;
import com.aplixcorp.android.ble.beacon.BeaconLocationManager;
import com.aplixcorp.android.ble.beacon.BeaconLocationManager.Error;
import com.aplixcorp.android.ble.beacon.BeaconRegion;
import com.aplixcorp.android.ble.beacon.BeaconRegion.RegionState;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Service for beacon monitoring in background.
 */
public class BgScanService extends Service {
    private static final String LOG_NAME = BgScanService.class.getSimpleName() + ".";
    private static final String LOG_TAG = "BeaconSample";

    /** action string with which this service invokes the activity. */
    public static final String ACTION_INVOKE_BY_BEACON = "com.aplixcorp.android.ble.beacon.INVOKE";

    public static final String KEY_CMD   = "cmd";         // int
    public static final String KEY_NAME  = "name";        // String
    public static final String KEY_UUID  = "uuid";        // String
    public static final String KEY_MAJOR = "major";       // int
    public static final String KEY_MINOR = "minor";       // int
    public static final String KEY_INVOKE_ON = "invoke_on"; // boolean

    public static final int EVENT_ON_ENTER         = (1<<0);
    public static final int EVENT_ON_EXIT          = (1<<1);
    public static final int EVENT_ON_RANGE_IMM     = (1<<2);
    public static final int EVENT_ON_RANGE_NEAR    = (1<<3);
    public static final int EVENT_ON_RANGE_FAR     = (1<<4);
    public static final int EVENT_ON_RANGE_UNKNOWN = (1<<5);
    private static final int EVENT_MASK_RANGE = (EVENT_ON_RANGE_IMM
                                                |EVENT_ON_RANGE_NEAR
                                                |EVENT_ON_RANGE_FAR
                                                |EVENT_ON_RANGE_UNKNOWN);

    public static final int CMD_START_MONITOR   = 1;
    public static final int CMD_STOP_MONITOR    = 2;
    public static final int CMD_START_RANGE = 3;

    private static final int INVALID_MAJOR_MINOR = 0x10000;

    private static final String NTF_TITLE = "BeaconService";

    private BeaconLocationManager      mBeaconLocationManager;
    private BeaconLocationListenerImpl mBeaconLocationListener;

    private NotificationManager        mNotificationManager;


    //public static methods
    public static void putIntentExtra(Intent intent, BeaconRegion region) {
        intent.putExtra(BgScanService.KEY_UUID,  region.getUuid().toString());
        intent.putExtra(BgScanService.KEY_MAJOR, region.getMajor());
        intent.putExtra(BgScanService.KEY_MINOR, region.getMinor());
        intent.putExtra(BgScanService.KEY_NAME,  region.getIdentifier());
    }
    public static BeaconRegion getIntentRegionExtra(Intent intent) {
        BeaconRegion region = null;
        String name    = intent.getStringExtra(KEY_NAME);
        String uuidStr = intent.getStringExtra(KEY_UUID);
        int    major   = intent.getIntExtra(KEY_MAJOR, INVALID_MAJOR_MINOR);
        int    minor   = intent.getIntExtra(KEY_MINOR, INVALID_MAJOR_MINOR);
        try {
            UUID   uuid = UUID.fromString(uuidStr);
            region = new BeaconRegion(uuid, major, minor, name);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, LOG_NAME + "getIntentRegionExtra - caught " + e, e);
            Log.e(LOG_TAG, LOG_NAME + "getIntentRegionExtra: name[" + name 
                    + "] uuidStr[" + uuidStr + "]" 
                    + String.format(" major[0x%04x] minor[0x%04x]", major, minor));
        }
        return region;
    }

    public BgScanService() {
        //Log.d(LOG_TAG, LOG_NAME + "<init>");
    }
    @Override
    public void onCreate() {
        //Log.d(LOG_TAG, LOG_NAME + "onCreate");
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        //Log.d(LOG_TAG, LOG_NAME + "onBind");
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(LOG_TAG, LOG_NAME + "onStartCommand(" + startId + ")");
        if (intent != null) {
            parseIntent(intent);
        }
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        disposeManager();
    }

    private void parseIntent(final Intent intent) {
        int    command = intent.getIntExtra(KEY_CMD, 0);
        BeaconRegion region  = getIntentRegionExtra(intent);
        int    invokeTrigger = intent.getIntExtra(KEY_INVOKE_ON, 0);
        region.setNotifyEntryStateOnDisplay(true);
        Log.d(LOG_TAG, LOG_NAME + "parseIntent(" + region + ":notifyOnDisplay=" + region.getNotifyEntryStateOnDisplay() + ")");

        switch (command) {
            case CMD_START_MONITOR:
                startMonitor(region, invokeTrigger);
                break;
            case CMD_STOP_MONITOR:
                stopMonitor(region);
                break;
            case CMD_START_RANGE:
                startRange(region, invokeTrigger);
                break;
        }

    }

    private void startMonitor(final BeaconRegion region, final int invokeTrigger) {
        mInvokeTriggers.put(region.getIdentifier(), invokeTrigger);
        BeaconLocationManager mgr = setupManager();
        mgr.startMonitoringForRegion(region);
    }

    private void stopMonitor(final BeaconRegion region) {
        BeaconLocationManager mgr = setupManager();
        mgr.stopMonitoringForRegion(region);
    }

    private void startRange(final BeaconRegion region, final int invokeTrigger) {
        mInvokeTriggers.put(region.getIdentifier(), invokeTrigger);
        BeaconLocationManager mgr = setupManager();
        mgr.startRangingBeaconsInRegion(region);
    }

    private BeaconLocationManager setupManager() {
        if (mBeaconLocationManager == null) {
            mBeaconLocationManager = new BeaconLocationManager(this);
            mBeaconLocationManager.setListener(getListener());
        }
        return mBeaconLocationManager;
    }

    private BeaconLocationListenerImpl getListener() {
        if (mBeaconLocationListener == null) {
            mBeaconLocationListener = new BeaconLocationListenerImpl();
        }
        return mBeaconLocationListener;
    }
    
    private void appendNotification(final String msg) {
        Notification ntf = new Notification.Builder(this)
                .setContentTitle(NTF_TITLE)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        mNotificationManager.notify(getNewId(), ntf);

    }

    private static int sId = 0;
    private synchronized int getNewId() {
        return ++ sId;
    }

    private final class BeaconLocationListenerImpl implements BeaconLocationListener {

        @Override
        public void notifyStartedMonitoringForRegion(BeaconLocationManager mgr, BeaconRegion region) {
            Log.d(LOG_TAG, LOG_NAME + "notifyStartedMonitoringForRegion(" + region.getIdentifier() +")");
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyEnterRegion(BeaconLocationManager mgr, BeaconRegion region) {
            Log.d(LOG_TAG, LOG_NAME + "notifyEnterRegion(" + region.getIdentifier() +")");
            String regionName = region.getIdentifier();
            appendNotification("Entered BeaconRegion[" + regionName + "]\n" );

            if (hasInvokeTrigger(regionName, EVENT_ON_ENTER)) {
                invokeActivity(region, EVENT_ON_ENTER);
            }
        }

        @Override
        public void notifyExitRegion(BeaconLocationManager mgr, BeaconRegion region) {
            Log.d(LOG_TAG, LOG_NAME + "notifyExitRegion(" + region.getIdentifier() +")");
            String regionName = region.getIdentifier();
            appendNotification("Exit BeaconRegion[" + regionName + "]\n" );

            if (hasInvokeTrigger(regionName, EVENT_ON_EXIT)) {
                invokeActivity(region, EVENT_ON_EXIT);
            }
        }

        @Override
        public void failedMonitoringForRegion(BeaconLocationManager mgr, BeaconRegion region,
                Error error) {
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyRangeBeaconsInRegion(BeaconLocationManager mgr,
                ArrayList<Beacon> beacons, BeaconRegion region) {
            Log.d(LOG_TAG, LOG_NAME + "notifyRangeBeaconasInRegion(" + beacons.size() + "-beacons," + region.getIdentifier() +")");
            String regionName = region.getIdentifier();
            appendNotification("Entered BeaconRegion[" + regionName + "]\n" );
            if (!beacons.isEmpty()) {
                if (mInvokeTriggers != null) {
                    Integer trigger = mInvokeTriggers.get(regionName);
                    if (trigger != null && (trigger & EVENT_MASK_RANGE) != 0) {
                        for (Beacon b: beacons) {
                            int event = 0;
                            switch(b.getProximity()) {
                                case Immediate: event = EVENT_ON_RANGE_IMM; break;
                                case Near:      event = EVENT_ON_RANGE_NEAR; break;
                                case Far:       event = EVENT_ON_RANGE_FAR; break;
                                case Unknown:   event = EVENT_ON_RANGE_UNKNOWN; break;
                            }
                            if ((trigger & event) != 0) {
                                invokeActivity(region, event);
                                break;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void failedRangingBeaconsInRegion(BeaconLocationManager mgr, BeaconRegion region,
                Error error) {
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyDeterminedStateForRegion(BeaconLocationManager mgr, RegionState state,
                BeaconRegion region) {
            Log.d(LOG_TAG, LOG_NAME + "notifyDeterminedStateForRegion(" + state + "-beacons," + region.getIdentifier() +")");
        }

    }

    private final HashMap<String,Integer> mInvokeTriggers = new HashMap<String, Integer>();


    private boolean hasInvokeTrigger(String regionName, int event) {
        if (mInvokeTriggers != null) {
            Integer trigger = mInvokeTriggers.get(regionName);
            if (trigger != null) {
                int t = trigger;
                if ((t & event) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
    private void invokeActivity(BeaconRegion region, int trigger) {
        Log.d(LOG_TAG, LOG_NAME + ".invokeActivity(" + region.getIdentifier() + ", " + trigger +")");
        mInvokeTriggers.clear();
        disposeManager();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_INVOKE_BY_BEACON);
        putIntentExtra(intent, region);
        intent.putExtra(KEY_INVOKE_ON, trigger);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopSelf();
    }

    private void disposeManager() {
        if (mBeaconLocationManager != null) {
            Log.d(LOG_TAG, LOG_NAME + "disposeManager()");
            mBeaconLocationManager.setListener(null);
            mBeaconLocationListener = null;

            // NOTICE:
            // The ranging currently running MUST be stopped at closing each 
            // BeaconLocationManager instances, 
            // because the ranging CANNOT be stopped by another BeaconLocationManager
            // instance.
            Set<BeaconRegion> regions = mBeaconLocationManager.getRangedRegions();
            if (0 < regions.size()) {
                Iterator<BeaconRegion> iregions = regions.iterator();
                while (iregions.hasNext()) {
                    BeaconRegion region = iregions.next();
                    if (region != null) {
                        mBeaconLocationManager.stopRangingBeaconsInRegion(region);
                    }
                    iregions.remove();
                }
            }
            // NOTE:
            // The monitoring currently running SHOULD also be stopped at closing each 
            // BeaconLocationManager instances.
            regions = mBeaconLocationManager.getMonitoredRegions();
            if (0 < regions.size()) {
                Iterator<BeaconRegion> iregions = regions.iterator();
                while (iregions.hasNext()) {
                    BeaconRegion region = iregions.next();
                    if (region != null) {
                        mBeaconLocationManager.stopMonitoringForRegion(region);
                    }
                    iregions.remove();
                }
            }

            mBeaconLocationManager  = null;
        }
    }

}

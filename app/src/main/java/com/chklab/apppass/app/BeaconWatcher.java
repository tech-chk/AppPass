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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Main code to interact with BeaconLocationManager.
 *
 * This sample code mainly to show
 * <OL>
 * <LI>how to get a {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager} instance.
 * <LI>how to start/stop monitoring a beacon-region.
 * <LI>how to start/stop ranging a beacon-region.
 * <LI>how to receive {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener} callback events.
 * </OL>
 * 
 * <h3>Monitoring a beacon-region</h3>
 * Typical scenario of Beacon monitoring is the followings.
 * <OL>
 * <LI>[SDKAPI.1] Get a BeaconLocationManager instance.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#BeaconLocationManager(android.content.Context)} (constructor)
 * <LI>[SDKAPI.2] Set a BeaconLocationListener implementation to BeaconLocationManager instance.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#setListener(com.aplixcorp.android.ble.beacon.BeaconLocationListener)}
 * <LI>[SDKAPI.3.1] Start monitoring a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#startMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.5.1] Callback : monitoring-started.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyStartedMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.5.2] Callback : entered to a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyEnterRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.7.1] Callback : determined the region status as Inside.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyDeterminedStateForRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion.RegionState, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.5.3] Callback : exited from a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyExitRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.7.1] Callback : determined the region status as Outside.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyDeterminedStateForRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion.RegionState, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.5.4] Callback : error occurred.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#failedMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion, com.aplixcorp.android.ble.beacon.BeaconLocationManager.Error)}
 * <LI>[SDKAPI.3.2] Stop monitoring a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#stopMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * </OL>
 *  Note that the pair of callbacks [5.2] and [5.3] may occur multiple times
 * between [3.1] and [3.2].<BR>
 *<P> 
 *  Usually, the callback [5.4] does not occur
 * unless the Bluetooth on the device is disabled by the user or other application.<BR>
 *<P> 
 *  If the major or minor value of the monitored beacon-region is 'DEFAULT',
 * multiple beacons may match the beacon-region.
 * In the case, BeaconLocationListener#notifyEnterRegion(BeaconLocationManager, BeaconRegion)
 * is called when any one of those beacons is detected first,
 * and BeaconLocationListener#notifyExitRegion(BeaconLocationManager, BeaconRegion)
 * is called when the last one of those beacons is lost.
 * 
 * <h3>Ranging a beacon-region</h3>
 *  Typical scenario of Beacon ranging is the followings.
 * <OL>
 * <LI>[SDKAPI.1] Get BeaconLocationManager instance.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#BeaconLocationManager(android.content.Context)}
 * <LI>[SDKAPI.2] Set BeaconLocationListener implementation to BeaconLocationManager.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#setListener(com.aplixcorp.android.ble.beacon.BeaconLocationListener)}
 * <LI>[SDKAPI.4.1] Start ranging a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#startRangingBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.6.1] Callback : beacons ranged.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyRangeBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, java.util.ArrayList, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>[SDKAPI.6.2] Callback : error occurred.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#failedRangingBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, com.aplixcorp.android.ble.beacon.BeaconRegion, com.aplixcorp.android.ble.beacon.BeaconLocationManager.Error)}
 * <LI>[SDKAPI.4.2] Stop ranging a beacon-region.<BR>
 *    {@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#stopRangingBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * </OL>
 * Note that the callback [6.1] is called repeatedly
 * <UL>
 * <LI>when a beacon belonging to the beacon-region is detected,
 * <LI>when a beacon belonging to the beacon-region is lost,
 * <LI>when the estimated distance from a beacon changes.
 * </UL>
 * If the major or minor value of the ranged beacon-region is 'DEFAULT',
 * multiple beacons may match the beacon-region.
 * In the case, 
 * {@link com.aplixcorp.android.ble.beacon.BeaconLocationListener#notifyRangeBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconLocationManager, java.util.ArrayList, com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * is called every time a new beacon matching to the region is detected or lost.
 *
 * <H3>Beacon SDK API used in this class<H3>
 * <UL>
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#BeaconLocationManager(android.content.Context)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#setListener(com.aplixcorp.android.ble.beacon.BeaconLocationListener)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#startMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#stopMonitoringForRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#startRangingBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#stopRangingBeaconsInRegion(com.aplixcorp.android.ble.beacon.BeaconRegion)}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#getMonitoredRegions()}
 * <LI>{@link com.aplixcorp.android.ble.beacon.BeaconLocationManager#getRangedRegions()}
 * </UL>
 */
class BeaconWatcher implements BeaconLocationListener {
    private static final boolean TRACE = true;
    
    // constants
    private static final String TAG = "BeaconSample";
    private static final String NAME = BeaconWatcher.class.getSimpleName() + ".";

    static final int EVENT_MONITORING_STARTED =   1;
    static final int EVENT_MONITORING_ENTERED =   2;
    static final int EVENT_MONITORING_EXIT    =   3;
    static final int EVENT_MONITORING_ERROR   =   4;
    static final int EVENT_RANGING_UPDATED    =   5;
    static final int EVENT_RANGING_ERROR      =   6;
    static final int EVENT_DETERMINED_STATE   =   7;

    // instance fields
    private final BeaconLocationManager mMgr;

    /** Ranging beacon data store. */
    private final HashMap<String,ArrayList<Beacon>> mRangingReport
        = new HashMap<String,ArrayList<Beacon>>();

    private final HashMap<String,RegionStateHolder> mStateHolder
        = new HashMap<String,RegionStateHolder>();

    private Handler mHandler = null;

    BeaconWatcher(final Context context)
    {
        if (TRACE) { trace("<init>"); }

//[SDKAPI.1] getting a BeaconLocationManager instance.
        mMgr = new BeaconLocationManager(context);

        refreshListener();
    }
    private void refreshListener() {

//[SDKAPI.2] setting a BeaconLocationLister instance to BeaconLocationManger.
        mMgr.setListener(this);
    }

    synchronized void setHandler(Handler h)
    {
        if (TRACE) { trace("setHandler"); }
        mHandler = h;
    }

    void startMonitoring(final String regionName) {
        trace("startMonitoring(" + regionName + ")");
        BeaconRegion r = TestData.getRegion(regionName);
        if (r == null) {
            trace( "startMonitoring - unknown region");
            return;
        }

        getHolder(r).setMonitoring(true);

//[SDKAPI.3.1] Start monitoring a beacon-region.
//   By calling this method multiple times,
//   you can monitor multiple beacon-regions at the same time.
        mMgr.startMonitoringForRegion(r);
    }


    void stopMonitoring(final String regionName) {
        trace("stopMonitoring(" + regionName + ")");
        BeaconRegion r = TestData.getRegion(regionName);
        if (r == null) {
            trace( "stopMonitoring - unknown region");
            return;
        }
        //[BEACON-OPERATION]
//[SDKAPI.3.2] Stop monitoring a beacon-region.
        mMgr.stopMonitoringForRegion(r);
        getHolder(r).setMonitoring(false);
    }

    void startRanging(final String regionName) {
        trace("startRanging(" + regionName + ")");
        BeaconRegion r = TestData.getRegion(regionName);
        if (r == null) {
            trace( "startRanging:ERR - unknown region:" + regionName);
            return;
        }
        getHolder(r).setRanging(true);


//[SDKAPI.4.1] Start ranging a beacon-region.
//-  By calling this method multiple times,
//   you can monitor multiple beacon-regions at the same time.
//-  If the you set the major or minor value DEFAULT to the BeaconRegion object,
//   multiple Beacon objects can be reported in notifyRangeBeaconsInRegion().
        mMgr.startRangingBeaconsInRegion(r);



        synchronized(mRangingReport) {
            if (mRangingReport.containsKey(regionName)) {
                trace( "startRanging:WRN - already ranging:" + regionName);
                return;
            }

            //add to report-store.
            ArrayList<Beacon> beacons = new ArrayList<Beacon>();
            mRangingReport.put(regionName, beacons);
        }
    }

    void stopRanging(final String regionName) {
        trace("stopRanging(" + regionName + ")");
        BeaconRegion r = TestData.getRegion(regionName);
        if (r == null) {
            trace( "stopRanging - unknown region:" + regionName);
            return;
        }

        //[BEACON-OPERATION]
//[SDKAPI.4.2] Stop ranging a beacon-region.
        mMgr.stopRangingBeaconsInRegion(r);



        getHolder(r).setRanging(false);
        //remove from report-store.
        synchronized(mRangingReport) {
            ArrayList<Beacon> beacons = mRangingReport.remove(regionName);
            if ( beacons == null) {
                trace( "stopRanging:WRN - not ranging:" + regionName);
                return;
            }
        }
    }

    void requestStateForRegion(String regionName) {
        trace("requestStatusForRegion(" + regionName + ")");
        BeaconRegion r = TestData.getRegion(regionName);
        if (r == null) {
            trace( "requestStatusForRegion - unknown region:" + regionName);
            return;
        }

        RegionStateHolder h = getHolder(r);
        if (h.toggleRequestingState()) {
            
            //[BEACON-OPERATION]
            mMgr.requestStateForRegion(r);
        }

    }

// [SDKAPI.5.1] Called when beacon-region monitoring started.
    @Override
    public void notifyStartedMonitoringForRegion(BeaconLocationManager mgr,
            BeaconRegion region) {
        if (TRACE) { trace("notifyStartedMonitoringForRegion: region[" + region.getIdentifier() +"]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        if (h != null) {
            h.setMonStarted(true);
            if (mHandler != null) {
                Message msg = Message.obtain(mHandler, EVENT_MONITORING_STARTED, region);
                mHandler.sendMessage(msg);
            }
        }
    }

// [SDKAPI.5.2] Called when the device entered one of the monitored beacon-regions.
    @Override
    //as BeaconLocationListener
    public void notifyEnterRegion(BeaconLocationManager mgr, BeaconRegion region) {
        if (TRACE) { trace("notifyEnterRegion: region[" + region.getIdentifier() +"]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        if (h != null) {
            h.setRegionState(RegionState.Inside);
            if (mHandler != null) {
                Message msg = Message.obtain(mHandler, EVENT_MONITORING_ENTERED, h);
                mHandler.sendMessage(msg);
            }
        } else if (TRACE) { trace("notifyEnterRegion: holder not found."); }
    }

// [SDKAPI.5.3] Called when the device exited one of the monitored beacon-regions.
    //as BeaconLocationListener
    @Override
    public void notifyExitRegion(BeaconLocationManager mgr, BeaconRegion region) {
        if (TRACE) { trace("notifyExitRegion: region[" + region.getIdentifier() +"]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        if (h != null) {
            h.setRegionState(RegionState.Outside);
            if (mHandler != null) {
                Message msg = Message.obtain(mHandler, EVENT_MONITORING_EXIT, h);
                mHandler.sendMessage(msg);
            }
        } else if (TRACE) { trace("notifyExitRegion: holder not found."); }
    }

// [SDKAPI.5.4] Called when an error occurs while monitoring a beacon region.
    //as BeaconLocationListener
    @Override
    public void failedMonitoringForRegion(BeaconLocationManager mgr,
            BeaconRegion region, Error error) {
        if (TRACE) { trace("failedMonitoringForRegion: region[" + region.getIdentifier() +"] error[" + error.getErrorCode() + "]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        int errorCode = error.getErrorCode();
        if (h != null) {
            h.setMonitorError(errorCode);
        }
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, EVENT_MONITORING_ERROR, errorCode, 0, h);
            mHandler.sendMessage(msg);
        }

    }

// [SDKAPI.6.1] Callback: reporting ranged beacons in the region.
    //as BeaconLocationListener
    @Override
    public void notifyRangeBeaconsInRegion(BeaconLocationManager mgr,
            ArrayList<Beacon> beacons, BeaconRegion region) {
        if (TRACE) {
            trace("notifyRangeBeaconsInRegion: region[" + region.getIdentifier() +"] beacons {");
            for (Beacon b : beacons) { trace("\t" + b + ":rssi=" + b.getRssi() + ":prox=" + b.getProximity()); }
            trace("}");
        }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        if (h != null) {
            h.setBeacons(beacons);
            if (mHandler != null) {
                Message msg = Message.obtain(mHandler, EVENT_RANGING_UPDATED, h);
                mHandler.sendMessage(msg);
            }
        } else if (TRACE) { trace("notifyRangeBeaconsInRegion: holder not found."); }



        String regionName = region.getIdentifier();
        ArrayList<Beacon> lastReport = mRangingReport.get(regionName);
        
        if (lastReport == null) {
            trace("notifyRangeBeaconsInRegion:WRN - region[" + regionName +"] is not ranging");
            return;
        }
        
        //COMPARE WITH THE LAST REPORT for the specified region.
        for (Beacon b : beacons) {
            int i = lastReport.indexOf(b);
            if (0 <= i) {
                //Beacon reported both last time and this time.
                //NOTE : even though (b.equals(ob))== true, rssi or prox could be different.
                Beacon ob = lastReport.get(i);
                trace("Ranging : " + b + " MOVED  :: curr{rssi=" + b.getRssi() + ", prox=" + b.getProximity()
                                               + "}  last{rssi=" + ob.getRssi() + ", prox=" + ob.getProximity() + "} ");
                lastReport.remove(i);
            } else {
                //Beacon reported this time but not in the last time.
                trace("Ranging : " + b + " ENTERED :: curr {rssi=" + b.getRssi() + ", prox=" + b.getProximity() +"}");
            }
        }
        //Remained ones in lastReport are  the WITH THE LAST REPORT for the specified region.
        for (Beacon ob : lastReport) {
                //Beacon reported last time but not this time.
                trace("Ranging : " + ob + " EXIT    :: last {rssi=" + ob.getRssi() + ", prox=" + ob.getProximity() +"}");
        }
        synchronized(mRangingReport) {
            mRangingReport.put(regionName, beacons);
        }
    }

// [SDKAPI.6.2] Called when an error occurs while ranging a beacon region.
// Typically, this method is called when the Bluetooth is disabled on the device.
    //as BeaconLocationListener
    @Override
    public void failedRangingBeaconsInRegion(BeaconLocationManager mgr,
            BeaconRegion region, Error error) {
        if (TRACE) { trace("failedRangingBeaconsInRegion: region[" + region.getIdentifier() +"] error[" + error + "]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());

        int errorCode = error.getErrorCode();
        if (h != null) {
            h.setRangeError(errorCode);
        }
        if (mHandler != null) {
            Message msg = Message.obtain(mHandler, EVENT_RANGING_ERROR, errorCode, 0, h);
            mHandler.sendMessage(msg);
        }

    }

// [SDKAPI.7.1] Called when an error occurs while ranging a beacon region.
// Typically, this method is called when the Bluetooth is disabled on the device.
    //as BeaconLocationListener
    @Override
    public void notifyDeterminedStateForRegion(BeaconLocationManager mgr,
            RegionState state, BeaconRegion region) {
        if (TRACE) { trace("notifyDeterminedStateForRegion: state[" + state + "] region[" + region.getIdentifier() +"]"); }

        RegionStateHolder h = getStateHolder(region.getIdentifier());
        if (h != null) {
            if (h.isRequestingState()) {
                h.setDeterminedState(state);
            }
            if (h.isMonitoring()) {
                h.setRegionState(state);
            }

            if (mHandler != null) {
                Message msg = Message.obtain(mHandler, EVENT_DETERMINED_STATE, h);
                mHandler.sendMessage(msg);
            }
        } else if (TRACE) { trace("notifyDeterminedStateForRegion: holder not found."); }

    }

    private RegionStateHolder getHolder(BeaconRegion r) {
        String name = r.getIdentifier();
        RegionStateHolder h = mStateHolder.get(name);
        if (h == null) {
            h = new RegionStateHolder(r);
            mStateHolder.put(name, h);
        }
        return h;
    }
    private RegionStateHolder removeHolder(BeaconRegion r) {
        String name = r.getIdentifier();
        RegionStateHolder h = mStateHolder.get(name);
        if (h != null) {
            if (TRACE) { trace("removeHolder(): region[" + name +"]"); }
            return mStateHolder.remove(h);
        }
        return null;
    }

    boolean isMonitoring(String regionName) {
        RegionStateHolder h = mStateHolder.get(regionName);
        return (h != null && h.isMonitoring());
    }
    boolean isRanging(String regionName) {
        RegionStateHolder h = mStateHolder.get(regionName);
        return (h != null && h.isRanging());
    }

    RegionState getRegionState(String regionName) {
        RegionStateHolder h = mStateHolder.get(regionName);
        if (h != null) {
            return h.getRegionState();
        }
        return RegionState.Unknown;
    }
    RegionStateHolder getStateHolder(final String regionName) {
        return mStateHolder.get(regionName);
    }

    class RegionStateHolder {
        private final BeaconRegion mRegion;
        private boolean mMonitoring;
        private boolean mMonStarted;
        private boolean mRanging;
        private boolean mRequestingState;
        private int     mMonitorError;
        private int     mRangeError;

        private RegionState mRegionState = RegionState.Unknown; 
        private ArrayList<Beacon> mBeacons;
        private RegionState mDeterminedState;

        RegionStateHolder(final BeaconRegion r) {
            mRegion = r;
        }
        BeaconRegion getRegion() {
            return mRegion;
        }

        boolean isMonitoring() {
            return mMonitoring;
        }
        void setMonitoring(boolean state) {
            mMonitoring = state;
            if (!state) {
                setRegionState(RegionState.Unknown);
                setMonitorError(0);

                if (!isRanging() && !isRequestingState()) {
                    removeHolder(mRegion);
                }
            }
        }

        boolean isRanging() {
            return mRanging;
        }
        void setRanging(boolean state) {
            mRanging = state;
            if (!state) {
                setBeacons(null);

                if (!isMonitoring() && !isRequestingState()) {
                    removeHolder(mRegion);
                }
            }
            setRangeError(0);
        }
        ArrayList<Beacon> getBeacons() {
            return mBeacons;
        }
        void setBeacons(ArrayList<Beacon> beacons) {
            if (beacons == null) {
                mBeacons = null;
            } else {
                mBeacons = new ArrayList<Beacon>(beacons);
            }
        }

        boolean isRequestingState() {
            return mRequestingState;
        }
        boolean toggleRequestingState() {
            mRequestingState = !mRequestingState;
            setDeterminedState(null);//reset
            return mRequestingState;
        }

        RegionState getRegionState() {
            return mRegionState;
        }
        void setRegionState(RegionState state) {
            mRegionState = state;
        }

        boolean isMonStarted() {
            return mMonStarted;
        }
        void setMonStarted(boolean state) {
            mMonStarted = state;
        }

        RegionState getDeterminedRegionState() {
            return mDeterminedState;
        }
        void setDeterminedState(RegionState state) {
            mDeterminedState = state;

            if (state == RegionState.Unknown && !isRanging() && !isMonitoring()) {
                removeHolder(mRegion);
            }
        }

        int getMonitorError() {
            return mMonitorError;
        }
        void setMonitorError(int errorCode) {
            mMonitorError = errorCode;
        }
        int getRangeError() {
            return mRangeError;
        }
        void setRangeError(int errorCode) {
            mRangeError = errorCode;
        }
    }

    private static final void trace(Object o) {
        Log.d(TAG, NAME + o);
    }
    private static final void trace(Object o, Throwable e) {
        Log.e(TAG, NAME + o, e);
    }

    public void stopAll() {
        trace("stopAll() <");
        Set<BeaconRegion> regions = mMgr.getMonitoredRegions();
        if (regions != null) {
            for (BeaconRegion r: regions) {
                stopMonitoring(r.getIdentifier());
            }
        }
        regions = mMgr.getRangedRegions();
        if (regions != null) {
            for (BeaconRegion r: regions) {
                stopRanging(r.getIdentifier());
            }
        }
        mStateHolder.clear();
        trace("stopAll() >");
    }

}

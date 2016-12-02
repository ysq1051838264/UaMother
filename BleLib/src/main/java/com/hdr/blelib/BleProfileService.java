/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.hdr.blelib;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
    @SuppressWarnings("unused")
    private static final String TAG = "BleProfileService";

    public static final String BROADCAST_CONNECTION_STATE = "com.hdr.BROADCAST_CONNECTION_STATE";
    public static final String BROADCAST_SERVICES_DISCOVERED = "com.hdr.BROADCAST_SERVICES_DISCOVERED";
    public static final String BROADCAST_DEVICE_READY = "com.hdr.DEVICE_READY";
    public static final String BROADCAST_BOND_STATE = "com.hdr.BROADCAST_BOND_STATE";
    public static final String BROADCAST_ERROR = "com.hdr.BROADCAST_ERROR";

    /**
     * The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to
     */
    public static final String EXTRA_DEVICE_ADDRESS = "com.hdr.EXTRA_DEVICE_ADDRESS";
    /**
     * The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}.
     */
    public static final String EXTRA_DEVICE_NAME = "com.hdr.EXTRA_DEVICE_NAME";
    public static final String EXTRA_CONNECTION_STATE = "com.hdr.EXTRA_CONNECTION_STATE";
    public static final String EXTRA_BOND_STATE = "com.hdr.EXTRA_BOND_STATE";
    public static final String EXTRA_SERVICE_PRIMARY = "com.hdr.EXTRA_SERVICE_PRIMARY";
    public static final String EXTRA_SERVICE_SECONDARY = "com.hdr.EXTRA_SERVICE_SECONDARY";
    public static final String EXTRA_ERROR_MESSAGE = "com.hdr.EXTRA_ERROR_MESSAGE";
    public static final String EXTRA_ERROR_CODE = "com.hdr.EXTRA_ERROR_CODE";

    public static final int STATE_LINK_LOSS = -1;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTING = 3;

    private MultiBleManager<BleManagerCallbacks> mMultiBleManager;
    protected Handler mHandler;

    protected boolean mBound;
    private boolean mActivityFinished;

    public class LocalBinder extends Binder {


        /**
         * Sets whether the binded activity if finishing or not. If <code>true</code>, we will turn off battery level notifications in onUnbind(..) method below.
         *
         * @param finishing true if the binded activity is finishing
         */
        public void setActivityIsFinishing(final boolean finishing) {
            mActivityFinished = finishing;
        }


    }

    /**
     * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the binded activity.
     *
     * @return the service binder
     */
    protected LocalBinder getBinder() {
        // default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
        return new LocalBinder();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        mBound = true;
        return getBinder();
    }

    @Override
    public final void onRebind(final Intent intent) {
        mBound = true;

        if (mActivityFinished)
            onRebind();
    }

    /**
     * Called when the activity has rebinded to the service after being recreated. This method is not called when the activity was killed and recreated just to change the phone orientation.
     */
    protected void onRebind() {
        // empty
    }

    @Override
    public final boolean onUnbind(final Intent intent) {
        mBound = false;

        if (mActivityFinished)
            onUnbind();

        // When we are connected, but the application is not open, we are not really interested in battery level notifications. But we will still be receiving other values, if enabled.


        // we must allow to rebind to the same service
        return true;
    }

    /**
     * Called when the activity has unbinded from the service before being finished. This method is not called when the activity is killed to be recreated just to change the phone orientation.
     */
    protected void onUnbind() {
        // empty
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler();

        // initialize the manager
        mMultiBleManager = initializeManager();
        mMultiBleManager.setGattCallbacks(this);
    }

    @SuppressWarnings("rawtypes")
    protected abstract MultiBleManager initializeManager();
    /**
     * Called when the service has been started. The device name and address are set. It nRF Logger is installed than logger was also initialized.
     */
    protected void onServiceStarted() {
        // empty default implementation
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // shutdown the manager
        mMultiBleManager.closeAll();
        mMultiBleManager = null;
    }

    @Override
    public void onDeviceConnected(String address) {
    }

    @Override
    public void onDeviceDisconnecting(String address) {
    }

    /**
     * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
     * In that case the {@link #stopService()} method must be called when done.
     *
     * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
     */
    protected boolean stopWhenDisconnected() {
        return true;
    }

    @Override
    public void onDeviceDisconnected(String address) {

        final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        if (stopWhenDisconnected())
            stopService();
    }

    protected void stopService() {
        // user requested disconnection. We must stop the service
        stopSelf();
    }

    @Override
    public void onLinklossOccur(String address) {

        final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onServicesDiscovered(final String address) {
        final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDeviceReady(String address) {
        final Intent broadcast = new Intent(BROADCAST_DEVICE_READY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onDeviceNotSupported(String address) {
        final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        // no need for disconnecting, it will be disconnected by the manager automatically
    }

    @Override
    public void onBondingRequired(String address) {
        final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onBonded(String address) {
        final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }

    @Override
    public void onError(String address, final String message, final int errorCode) {
        final Intent broadcast = new Intent(BROADCAST_ERROR);
        broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
        broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

        mMultiBleManager.disconnect(address);
        stopSelf();
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected void showToast(final int messageResId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected void showToast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


}

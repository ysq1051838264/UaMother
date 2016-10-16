package com.hdr.blelib;


import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import java.util.*;

/**
 * 可以连接多个的BleManager
 *
 * @param <E>
 */
public abstract class MultiBleManager<E extends BleManagerCallbacks> {
    @SuppressWarnings("unused")
    private final static String TAG = "MultiBleManager";

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
    private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
    private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";
    private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
    private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";

    protected E mCallbacks;
    private Handler mHandler;
    protected final Map<String, BluetoothGatt> gattMap = new HashMap<>();
    private Context mContext;

    private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final String address = device.getAddress();
            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            // Skip other devices
            if (gattMap.get(address) == null)
                return;

            switch (bondState) {
                case BluetoothDevice.BOND_BONDING:
                    mCallbacks.onBondingRequired(address);
                    break;
                case BluetoothDevice.BOND_BONDED:
                    mCallbacks.onBonded(address);

                    // Start initializing again.
                    // In fact, bonding forces additional, internal service discovery (at least on Nexus devices), so this method may safely be used to start this process again.
                    gattMap.get(address).discoverServices();
                    break;
            }
        }
    };

    private final BroadcastReceiver mPairingRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final String address = device.getAddress();
            // Skip other devices
            if (gattMap.get(address) == null)
                return;

            // String values are used as the constants are not available for Android 4.3.
            final int variant = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT"/*BluetoothDevice.EXTRA_PAIRING_VARIANT*/, 0);

            // The API below is available for Android 4.4 or newer.

            // An app may set the PIN here or set pairing confirmation (depending on the variant) using:
            // device.setPin(new byte[] { '1', '2', '3', '4', '5', '6' });
            // device.setPairingConfirmation(true);
        }
    };

    public MultiBleManager(final Context context) {
        mContext = context;
        mHandler = new Handler();

        // Register bonding broadcast receiver
        context.registerReceiver(mBondingBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        context.registerReceiver(mPairingRequestBroadcastReceiver, new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST"/*BluetoothDevice.ACTION_PAIRING_REQUEST*/));
    }

    /**
     * Returns the context that the manager was created with.
     *
     * @return the context
     */
    protected Context getContext() {
        return mContext;
    }

    /**
     * This method must return the gatt callback used by the manager.
     * This method must not create a new gatt callback each time it is being invoked, but rather return a single object.
     *
     * @return the gatt callback object
     */
    protected abstract BleManagerGattCallback getGattCallback();

    /**
     * Returns whether to directly connect to the remote device (false) or to automatically connect as soon as the remote
     * device becomes available (true).
     *
     * @return autoConnect flag value
     */
    protected boolean shouldAutoConnect() {
        return false;
    }

    /**
     * Connects to the Bluetooth Smart device
     *
     * @param device a device to connect to
     */
    public synchronized void connect(final BluetoothDevice device) {
        String address = device.getAddress();
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            gatt.close();
            gattMap.remove(address);
        }

        final boolean autoConnect = shouldAutoConnect();
        gattMap.put(address, device.connectGatt(mContext, autoConnect, getGattCallback()));
    }

    /**
     * Disconnects from the device. Does nothing if not connected.
     *
     * @param address
     * @return true if device is to be disconnected. False if it was already disconnected.
     */
    public boolean disconnect(String address) {
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            mCallbacks.onDeviceDisconnecting(address);
            gatt.disconnect();
            return true;
        }
        return false;
    }

    /**
     * Closes and releases resources. May be also used to unregister broadcast listeners.
     *
     * @param address
     */
    public synchronized void close(String address) {

        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            gatt.close();
            gattMap.remove(address);
        }
    }

    public synchronized void closeAll() {
        try {
            mContext.unregisterReceiver(mBondingBroadcastReceiver);
            mContext.unregisterReceiver(mPairingRequestBroadcastReceiver);
        } catch (Exception e) {
            // the receiver must have been not registered or unregistered before
        }
        for (BluetoothGatt gatt : gattMap.values()) {
            gatt.close();
        }
        gattMap.clear();
    }


    /**
     * Sets the manager callback listener
     *
     * @param callbacks the callback listener
     */
    public void setGattCallbacks(E callbacks) {
        mCallbacks = callbacks;
    }

    /**
     * Enables notifications on given characteristic
     *
     * @return true is the request has been sent, false if one of the arguments was <code>null</code> or the characteristic does not have the CCCD.
     */
    protected final boolean enableNotifications(String address, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /**
     * Enables indications on given characteristic
     *
     * @return true is the request has been sent, false if one of the arguments was <code>null</code> or the characteristic does not have the CCCD.
     */
    protected final boolean enableIndications(String address, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0)
            return false;

        gatt.setCharacteristicNotification(characteristic, true);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }
        return false;
    }

    /**
     * Sends the read request to the given characteristic.
     *
     * @param address
     * @param characteristic the characteristic to read
     * @return true if request has been sent
     */
    public final boolean readCharacteristic(String address, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) == 0)
            return false;

        return gatt.readCharacteristic(characteristic);
    }

    /**
     * Writes the characteristic value to the given characteristic.
     *
     * @param address
     * @param characteristic the characteristic to write to
     * @return true if request has been sent
     */
    public final boolean writeCharacteristic(String address, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            return false;

        return gatt.writeCharacteristic(characteristic);
    }

    public static final class Request {
        private enum Type {
            WRITE,
            READ,
            ENABLE_NOTIFICATIONS,
            ENABLE_INDICATIONS
        }

        private final Type type;
        private final BluetoothGattCharacteristic characteristic;
        private final byte[] value;

        private Request(final Type type, final BluetoothGattCharacteristic characteristic) {
            this.type = type;
            this.characteristic = characteristic;
            this.value = null;
        }

        private Request(final Type type, final BluetoothGattCharacteristic characteristic, final byte[] value) {
            this.type = type;
            this.characteristic = characteristic;
            this.value = value;
        }

        public static Request newReadRequest(final BluetoothGattCharacteristic characteristic) {
            return new Request(Type.READ, characteristic);
        }

        public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value) {
            return new Request(Type.WRITE, characteristic, value);
        }

        public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
            return new Request(Type.ENABLE_NOTIFICATIONS, characteristic);
        }

        public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
            return new Request(Type.ENABLE_INDICATIONS, characteristic);
        }
    }

    public abstract class BleManagerGattCallback extends BluetoothGattCallback {
        private Map<String, Queue<Request>> mQueueMap = new HashMap<>();
        private boolean mInitInProgress;

        /**
         * This method should return <code>true</code> when the gatt device supports the required services.
         *
         * @param gatt the gatt device with services discovered
         * @return <code>true</code> when the device has teh required service
         */
        protected abstract boolean isRequiredServiceSupported(final BluetoothGatt gatt);

        /**
         * This method should return <code>true</code> when the gatt device supports the optional services.
         * The default implementation returns <code>false</code>.
         *
         * @param gatt the gatt device with services discovered
         * @return <code>true</code> when the device has teh optional service
         */
        protected boolean isOptionalServiceSupported(final BluetoothGatt gatt) {
            return false;
        }


        public BluetoothGattCharacteristic getCharacteristic(final BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid) {
            BluetoothGattService service = gatt.getService(serviceUuid);
            if (service == null) {
                return null;
            }
            return service.getCharacteristic(characteristicUuid);
        }

        /**
         * Called then the initialization queue is complete.
         *
         * @param address
         */
        protected void onDeviceReady(String address) {
            mCallbacks.onDeviceReady(address);
        }

        protected abstract void onDeviceConnected(String address);

        /**
         * This method should nullify all services and characteristics of the device.
         *
         * @param address
         */
        protected abstract void onDeviceDisconnected(String address);

        protected abstract void onDeviceServiceDiscovered(String address);

        /**
         * Callback reporting the result of a characteristic read operation.
         *
         * @param address
         * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * @param characteristic Characteristic that was read from the associated
         */
        protected void onCharacteristicRead(String address, final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // do nothing
        }

        /**
         * Callback indicating the result of a characteristic write operation.
         * <p/>
         * <p>If this callback is invoked while a reliable write transaction is
         * in progress, the value of the characteristic represents the value
         * reported by the remote device. An application should compare this
         * value to the desired value to be written. If the values don't match,
         * the application must abort the reliable write transaction.
         *
         * @param address
         * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
         * @param characteristic Characteristic that was written to the associated
         */
        protected void onCharacteristicWrite(String address, final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // do nothing
        }

        protected void onCharacteristicNotified(String address, final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // do nothing
        }

        protected void onCharacteristicIndicated(String address, final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // do nothing
        }

        private void onError(String address, final String message, final int errorCode) {
            mCallbacks.onError(address, message, errorCode);
        }

        @Override
        public final void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                // Notify the parent activity/service
                onDeviceConnected(address);
                mCallbacks.onDeviceConnected(address);
                gatt.discoverServices();

            } else {
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onDeviceDisconnected(address);
                    mCallbacks.onDeviceDisconnected(address);
                    close(address);
                    return;
                }

                // TODO Should the disconnect method be called or the connection is still valid? Does this ever happen?
                mCallbacks.onError(address, ERROR_CONNECTION_STATE_CHANGE, status);
            }
        }

        @Override
        public final void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (isRequiredServiceSupported(gatt)) {
                    mQueueMap.put(address, new LinkedList<Request>());
                    // Notify the parent activity
                    onDeviceServiceDiscovered(address);
                    mCallbacks.onServicesDiscovered(address);

                    mInitInProgress = true;

                    // Obtain the queue of initialization requests
                    nextRequest(address);
                } else {
                    mCallbacks.onDeviceNotSupported(address);
                    disconnect(address);
                }
            } else {
                onError(address, ERROR_DISCOVERY_SERVICE, status);
            }
        }

        @Override
        public final void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The value has been read. Notify the manager and proceed with the initialization queue.
                onCharacteristicRead(address, gatt, characteristic);
                nextRequest(address);
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    mCallbacks.onError(address, ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                onError(address, ERROR_READ_CHARACTERISTIC, status);
            }
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The value has been written. Notify the manager and proceed with the initialization queue.
                onCharacteristicWrite(address, gatt, characteristic);
                nextRequest(address);
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    mCallbacks.onError(address, ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                onError(address, ERROR_READ_CHARACTERISTIC, status);
            }
        }

        @Override
        public final void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            String address = gatt.getDevice().getAddress();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                nextRequest(address);
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
                    mCallbacks.onError(address, ERROR_AUTH_ERROR_WHILE_BONDED, status);
                }
            } else {
                onError(address, ERROR_WRITE_DESCRIPTOR, status);
            }
        }

        @Override
        public final void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            String address = gatt.getDevice().getAddress();
            final BluetoothGattDescriptor cccd = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
            final boolean notifications = cccd == null || cccd.getValue() == null || cccd.getValue().length != 2 || cccd.getValue()[0] == 0x01;

            if (notifications) {
                onCharacteristicNotified(address, gatt, characteristic);
            } else { // indications
                onCharacteristicIndicated(address, gatt, characteristic);
            }
        }

        public void addRequest(String address, Request request) {
            Queue<Request> queue = mQueueMap.get(address);
            if (queue == null) {
                return;
            }
            queue.add(request);
        }

        /**
         * Executes the next initialization request. If the last element from the queue has been executed a {@link #onDeviceReady(String)} callback is called.
         *
         * @param address
         */
        private void nextRequest(final String address) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final Queue<Request> requests = mQueueMap.get(address);

                    // Get the first request from the queue
                    final Request request = requests != null && !requests.isEmpty() ? requests.poll() : null;

                    // Are we done?
                    if (request == null) {
                        if (mInitInProgress) {
                            mInitInProgress = false;
                            onDeviceReady(address);
                        }
                        return;
                    }

                    switch (request.type) {
                        case READ: {
                            readCharacteristic(address, request.characteristic);
                            break;
                        }
                        case WRITE: {
                            final BluetoothGattCharacteristic characteristic = request.characteristic;
                            characteristic.setValue(request.value);
                            writeCharacteristic(address, characteristic);
                            break;
                        }
                        case ENABLE_NOTIFICATIONS: {
                            enableNotifications(address, request.characteristic);
                            break;
                        }
                        case ENABLE_INDICATIONS: {
                            enableIndications(address, request.characteristic);
                            break;
                        }
                    }
                }
            });
        }
    }
}
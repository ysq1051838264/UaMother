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
package com.uamother.bluetooth.ble.helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

public interface BleManagerCallbacks {

	/**
	 * Called when the device has been connected. This does not mean that the application may start communication. A service discovery will be handled automatically after this call. Service discovery
	 * may ends up with calling {@link #onServicesDiscovered(String)} or {@link #onDeviceNotSupported(String)} if required services have not been found.
	 * @param address
	 */
	void onDeviceConnected(String address);

	/**
	 * Called when user initialized disconnection.
	 * @param address
	 */
	void onDeviceDisconnecting(String address);

	/**
	 * Called when the device has disconnected (when the callback returned {@link BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)} with state DISCONNECTED.
	 * @param address
	 */
	void onDeviceDisconnected(String address);

	/**
	 * This callback is invoked when the Ble Manager lost connection to a device that has been connected with autoConnect option. Otherwise a {@link #onDeviceDisconnected(String)}
	 * method will be called on such event.
	 * @param address
	 */
	void onLinklossOccur(String address);

	/**
	 * Called when service discovery has finished and primary services has been found. The device is ready to operate. This method is not called if the primary, mandatory services were not found
	 * during service discovery. For example in the Blood Pressure Monitor, a Blood Pressure service is a primary service and Intermediate Cuff Pressure service is a optional secondary service.
	 * Existence of battery service is not notified by this call.
	 *
	 * @param address
	 *            if <code>true</code> the secondary services were also found on the device.
	 */
	void onServicesDiscovered(final String address);

	/**
	 * Method called when all initialization requests has been completed.
	 * @param address
	 */
	void onDeviceReady(String address);

	/**
	 * Called when an {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION} error occurred and the device bond state is NOT_BONDED
	 * @param address
	 */
	void onBondingRequired(String address);

	/**
	 * Called when the device has been successfully bonded.
	 * @param address
	 */
	void onBonded(String address);

	/**
	 * Called when a BLE error has occurred
	 * @param address
	 * @param message
 *            the error message
	 * @param errorCode
	 */
	void onError(String address, final String message, final int errorCode);

	/**
	 * Called when service discovery has finished but the main services were not found on the device.
	 * @param address
	 */
	void onDeviceNotSupported(String address);
}

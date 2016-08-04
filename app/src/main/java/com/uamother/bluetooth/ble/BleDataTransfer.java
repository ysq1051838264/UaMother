package com.uamother.bluetooth.ble;

import java.util.UUID;

/**
 * Created by hdr on 15/9/17.
 */
public interface BleDataTransfer {
    void writeData(UUID serviceUUID, UUID characteristicUUID, byte[] data);

    void setNotification(UUID serviceUUID, UUID characteristicUUID);

    void setIndication(UUID serviceUUID, UUID characteristicUUID);
}

package com.uamother.bluetooth.ble;

import java.util.UUID;

/**
 * Created by hdr on 15/5/25.
 */
public interface BleConst {

    String KEY_SHOW_INVALID_DATA_DIALOG = "key_show_invalid_data_dialog";

    UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    UUID CLIENT_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    byte PROTOCOL_1ST = 0x01;
    byte PROTOCOL_2ND = 0x11;
    byte PROTOCOL_3RD = 0x12;
    byte PROTOCOL_NEW_YOLANDA = 0x15;


    String ACTION_BLE_READ = "action_ble_read";
    String ACTION_BLE_WRITE = "action_ble_write";

    String ACTION_BLE_SCAN = "action_ble_scan";
    String ACTION_BLE_SCAN_BIND = "action_ble_scan_bind";

    String ACTION_BLE_CONNECTED = "action_ble_connected";
    String ACTION_BLE_CONNECTED_BIND = "action_ble_connected_bind";

    String ACTION_BLE_DISCOVERED = "action_ble_discovered";
    String ACTION_BLE_DISCOVERED_BIND = "action_ble_discovered_bind";

    String ACTION_BLE_RECEIVE_DATA = "action_ble_receive_data";
    String ACTION_BLE_RECEIVE_DATA_BIND = "action_ble_receive_data_bind";

    String ACTION_BLE_DISCONNECTED = "action_ble_disconnected";
    String ACTION_BLE_DISCONNECTED_BIND = "action_ble_disconnected_bind";


    String KEY_DEVICE_NAME = "device_name";
    String KEY_SCAN_RSSI = "scan_rssi";
    String KEY_SCAN_RECORD = "scan_record";
    String KEY_MAC = "mac";
    String KEY_UUID = "uuid";
    String KEY_DATA = "data";



}

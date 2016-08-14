package com.uamother.bluetooth.xrz;

import android.util.Log;
import com.hdr.wristband.utils.StringUtils;

import java.util.Random;

/**
 * Created by ysq on 16/7/31.
 */
public class ProtocolHelper {
    private final static byte COMMAND_ENCRYPT_OFFSET = (byte) 0xD5;

    private final static byte COMMAND_FIRST_BYTE = (byte) 0x8A;
    private final static byte COMMAND_SECOND_BYTE = (byte) 0xFF;
    private final static byte COMMAND_LAST_BYTE = 0x06;

    private final static Random random = new Random();


    public static byte[] merge() {
        byte[] SendDatabyte = new byte[4];

        SendDatabyte[0] = (byte)0x3F;
        SendDatabyte[1] = 0x02;
        SendDatabyte[2] = (byte) 0xa1;
        SendDatabyte[3] = (byte) 0x9c;

        String code = StringUtils.INSTANCE.format(SendDatabyte);
        Log.i("ysq", "发送数据:" + code);
        return SendDatabyte;
    }


    public static void decryptData(byte[] cmd) {
        byte secretKey = (byte) (cmd[8] ^ (cmd[4] & cmd[5]));
        for (int i = 9; i < cmd.length - 2; i++) {
            cmd[i] = (byte) (cmd[i] ^ secretKey);
        }
    }

    public static byte[] mergeData(byte[] cmdHead, byte data) {

        byte[] SendDatabyte1 = new byte[8];
        int VerifyCode1 = 0;
        SendDatabyte1[0] = (byte)0x3F;
        SendDatabyte1[1] = 0x06;
        SendDatabyte1[2] = (byte) 0xa0;
        //传的时候，先传亲和力
        SendDatabyte1[3] =  cmdHead[2];
        SendDatabyte1[4] =  cmdHead[0];
        SendDatabyte1[5] =  cmdHead[1];
        SendDatabyte1[6] = data;
        for (int i = 0; i <= 6; i++) {
            VerifyCode1 ^= SendDatabyte1[i];
        }
        SendDatabyte1[7] = (byte) VerifyCode1;

        String code = StringUtils.INSTANCE.format(SendDatabyte1);
        Log.i("ysq", "发送数据:" + code);

        return SendDatabyte1;
    }

    /**
     * 合并 命令头,数据
     *
     * @param cmdHead 命令头
     * @param data    数据
     * @return 返回最终下发的命令
     */
    public static byte[] merge(byte[] cmdHead, byte[] data) {
        byte[] randomBuf = new byte[2];
        random.nextBytes(randomBuf);
        final byte secretKey = (byte) ((randomBuf[0] ^ randomBuf[1]) & COMMAND_ENCRYPT_OFFSET);
        final byte publicKey = (byte) ((cmdHead[0] & cmdHead[1]) ^ secretKey);
        int cmdLength = 11 + data.length;
        byte[] cmd = new byte[cmdLength];
        cmd[0] = COMMAND_FIRST_BYTE;
        cmd[1] = COMMAND_SECOND_BYTE;
        cmd[2] = (byte) (cmdLength / 256);
        cmd[3] = (byte) (cmdLength % 256);
        cmd[4] = cmdHead[0];
        cmd[5] = cmdHead[1];
        cmd[6] = randomBuf[0];
        cmd[7] = randomBuf[1];
        cmd[8] = publicKey;

        if (data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (secretKey ^ data[i]);
            }
            System.arraycopy(data, 0, cmd, 9, data.length);
        }
        cmd[cmdLength - 1] = COMMAND_LAST_BYTE;

        String code = StringUtils.INSTANCE.format(cmd);
        Log.i("ysq", "发送数据:" + code);
        return cmd;
    }
}

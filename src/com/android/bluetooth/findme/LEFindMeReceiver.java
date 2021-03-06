/*
 * Copyright (c) 2012, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of Linux Foundation nor
 *          the names of its contributors may be used to endorse or promote
 *          products derived from this software without specific prior written
 *          permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT ARE DISCLAIMED.    IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.bluetooth.findme;

import java.util.ArrayList;
import java.util.Arrays;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

public class LEFindMeReceiver extends BroadcastReceiver {
    private final static String TAG = "LEFindMeReceiver";
    private static Handler handler = null;

    public void onReceive(Context context, Intent intent) {
        String action = (intent == null) ? null : intent.getAction();
        if (action == null) {
            Log.e(TAG, "action is null");
            return;
        }
        Intent in = new Intent();
        in.putExtras(intent);
        in.setClass(context, LEFindMeServices.class);
        in.putExtra("action", action);

        if (action.equals(BluetoothDevice.ACTION_GATT)) {
            try {
                Log.d(TAG, " ACTION GATT INTENT RECEIVED");
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Remote Device: " + remoteDevice.getAddress());

                ParcelUuid uuid = (ParcelUuid) intent.getExtra(BluetoothDevice.EXTRA_UUID);
                Log.d(TAG, " UUID: " + uuid);

                String[] ObjectPathArray = (String[]) intent
                        .getExtra(BluetoothDevice.EXTRA_GATT);
                if (ObjectPathArray != null) {
                    Log.d(TAG, " objPathList length : "+ ObjectPathArray.length);
                    Message objMsg = new Message();
                    objMsg.what = LEFindMeServices.GATT_SERVICE_STARTED_OBJ;
                    Bundle objBundle = new Bundle();
                    ArrayList<String> gattDataList =
                            new ArrayList<String>(Arrays.asList(ObjectPathArray));
                    gattDataList.add(uuid.toString());
                    objBundle.putStringArrayList(
                                LEFindMeServices.ACTION_GATT_SERVICE_EXTRA_OBJ,
                                gattDataList);
                    Log.d(TAG, " gattDataList  : " + gattDataList.get(0));
                    objMsg.setData(objBundle);
                    handler.sendMessage(objMsg);
                } else {
                    Log.e(TAG, "No object paths in the ACTION GATT intent");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            Log.d(TAG, "Received ACTION_ACL_CONNECTED intent");
            BluetoothDevice remoteDevice = intent
                                           .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            String connDevAddr = remoteDevice.getAddress();
            Log.d(TAG, "Received ACTION_ACL_CONNECTED, bt device: "
                 + connDevAddr);

            Message msg = new Message();
            msg.what = LEFindMeServices.GATT_DEVICE_CONNECTED;
            Bundle b = new Bundle();
            b.putParcelable(LEFindMeServices.REMOTE_DEVICE, remoteDevice);
            msg.setData(b);
            handler.sendMessage(msg);
        }
    }

    public static void registerHandler(Handler handle) {
        Log.d(TAG, " Registered Find Me Service Handler ::");
        handler = handle;
    }
}

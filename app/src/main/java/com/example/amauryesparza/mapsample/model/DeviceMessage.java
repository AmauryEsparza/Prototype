package com.example.amauryesparza.mapsample.model;

import android.os.Build;
import android.util.Log;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created by AmauryEsparza on 12/19/15.
 */

/**
 * Used to prepare the payload for a
 * {@link com.google.android.gms.nearby.messages.Message Nearby Message}. Adds a unique id (an
 * InstanceID) to the Message payload, which helps Nearby distinguish between multiple devices with
 * the same model name.
 */
public class DeviceMessage {
    private static final Gson gson = new Gson();


    private final String mInstanceId;
    private final String mMessageBody;


    private DeviceMessage(String instanceId) {
        this.mInstanceId = instanceId;
        this.mMessageBody = Build.MODEL;
    }

    private DeviceMessage(String instanceId, String message) {
        this.mInstanceId = instanceId;
        this.mMessageBody = Build.MODEL + ": " + message;
    }

    /**
     * Builds a new {@link Message} object using a unique identifier.
     * @param instanceId: unique message ID
     * @param message: Text message to send. If message is null then return the device name.
     *
     */
    public static Message newNearbyMessage(String instanceId, String message) {

        if(message != null){
            DeviceMessage deviceMessage = new DeviceMessage(instanceId, message);
            return new Message(gson.toJson(deviceMessage).toString().getBytes(Charset.forName("UTF-8")));
        }

        DeviceMessage deviceMessage = new DeviceMessage(instanceId);
        return new Message(gson.toJson(deviceMessage).toString().getBytes(Charset.forName("UTF-8")));

    }

    /**
     * Creates a {@code DeviceMessage} object from the string used to construct the payload to a
     * {@code Nearby} {@code Message}.
     */
    public static DeviceMessage fromNearbyMessage(Message message) {
        String nearbyMessageString = new String(message.getContent()).trim();
        Log.i("DeviceMessage: ", nearbyMessageString);
        return gson.fromJson(
                (new String(nearbyMessageString.getBytes(Charset.forName("UTF-8")))),
                DeviceMessage.class);
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public String getMessageBodyWithoutHeader(){
        return mMessageBody.replaceFirst("[\\w\\s]+", "Me");
    }

}

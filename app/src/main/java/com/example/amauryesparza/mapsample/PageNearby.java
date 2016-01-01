package com.example.amauryesparza.mapsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.amauryesparza.mapsample.model.DeviceMessage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;

/**
 * Created by AmauryEsparza on 12/10/15.
 */
public class PageNearby extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    /**
     * Tracks if we are currently resolving an error related to Nearby permissions. Used to avoid
     * duplicate Nearby permission dialogs if the user initiates both subscription and publication
     * actions without having opted into Nearby.
     */
    private boolean mResolvingNearbyPermissionError = false;

    private static final String TAG = "PageNearby";

    /**
     * Provides an entry point for Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Sets the time in seconds for a published message or a subscription to live. Set to three
     * minutes.
     */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(Constants.TTL_IN_SECONDS).build();

    /**
     * A {@link MessageListener} for processing messages from nearby devices.
     */
    private MessageListener mMessageListener;

    /**
     * The {@link Message} object used to broadcast information about the device to nearby devices.
     */
    private Message mDeviceInfoMessage;

    /**
     * Adapter for working with messages from nearby devices.
     */
    private ArrayAdapter<String> mNearbyDevicesArrayAdapter;

    /**
     * Backing data structure for {@code mNearbyDevicesArrayAdapter}.
     */
    private final ArrayList<String> mNearbyDevicesArrayList = new ArrayList<>();

    private ProgressBar mSubscriptionProgressBar;
    private ImageButton mSubscriptionImageButton;
    private ProgressBar mPublicationProgressBar;
    private ImageButton mPublicationImageButton;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        finishedResolvingNearbyPermissionError();
        if(requestCode == Constants.REQUEST_RESOLVE_ERROR){
            if(resultCode == Activity.RESULT_OK){
                executePendingTasks();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.page_nearby, container, false);
        mSubscriptionProgressBar = (ProgressBar) view.findViewById(
                R.id.subscription_progress_bar);
        mSubscriptionImageButton = (ImageButton) view.findViewById(R.id.subscription_image_button);
        mSubscriptionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subscriptionTask = getPubSubTask(Constants.KEY_SUBSCRIPTION_TASK);
                if (TextUtils.equals(subscriptionTask, Constants.TASK_NONE) ||
                        TextUtils.equals(subscriptionTask, Constants.TASK_UNSUBSCRIBE)) {
                    updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK,
                            Constants.TASK_SUBSCRIBE);
                } else {
                    updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK,
                            Constants.TASK_UNSUBSCRIBE);
                }
            }
        });

        mPublicationProgressBar = (ProgressBar) view.findViewById(R.id.publication_progress_bar);
        mPublicationImageButton = (ImageButton) view.findViewById(R.id.publication_image_button);
        mPublicationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String publicationTask = getPubSubTask(Constants.KEY_PUBLICATION_TASK);
                if (TextUtils.equals(publicationTask, Constants.TASK_NONE) ||
                        TextUtils.equals(publicationTask, Constants.TASK_UNPUBLISH)) {
                    updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_PUBLISH);
                } else {
                    updateSharedPreference(Constants.KEY_PUBLICATION_TASK,
                            Constants.TASK_UNPUBLISH);
                }
            }
        });
        final ListView nearbyDevicesListView = (ListView) view.findViewById(
                R.id.nearby_devices_list_view);
        mNearbyDevicesArrayAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1,
                mNearbyDevicesArrayList);
        nearbyDevicesListView.setAdapter(mNearbyDevicesArrayAdapter);

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNearbyDevicesArrayAdapter.add(
                                DeviceMessage.fromNearbyMessage(message).getMessageBody());
                    }
                });
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNearbyDevicesArrayAdapter.remove(
                                DeviceMessage.fromNearbyMessage(message).getMessageBody());
                    }
                });
            }
        };

        // Upon orientation change, ensure that the state of the UI is maintained.
        updateUI();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);

        mDeviceInfoMessage = DeviceMessage.newNearbyMessage(
                InstanceID.getInstance(getActivity().getApplicationContext()).getId());
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected() && !getActivity().isChangingConfigurations()) {
            // Using Nearby is battery intensive. To preserve battery, stop subscribing or
            // publishing when the fragment is inactive.
            unsubscribe();
            unpublish();
            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE);
            updateSharedPreference(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE);

            mGoogleApiClient.disconnect();
            getActivity().getPreferences(Context.MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        // If the user has requested a subscription or publication task that requires
        // GoogleApiClient to be connected, we keep track of that task and execute it here, since
        // we now have a connected GoogleApiClient.
        executePendingTasks();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended: "
                + connectionSuspendedCauseToString(cause));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.equals(key, Constants.KEY_SUBSCRIPTION_TASK)) {
                    executePendingSubscriptionTask();
                } else if (TextUtils.equals(key, Constants.KEY_PUBLICATION_TASK)) {
                    executePendingPublicationTask();
                }
                updateUI();
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // For simplicity, we don't handle connection failure thoroughly in this sample. Refer to
        // the following Google Play services doc for more details:
        // http://developer.android.com/google/auth/api-client.html
        Log.i(TAG, "connection to GoogleApiClient failed");
    }

    private static String connectionSuspendedCauseToString(int cause) {
        switch (cause) {
            case CAUSE_NETWORK_LOST:
                return "CAUSE_NETWORK_LOST";
            case CAUSE_SERVICE_DISCONNECTED:
                return "CAUSE_SERVICE_DISCONNECTED";
            default:
                return "CAUSE_UNKNOWN: " + cause;
        }
    }

    protected void finishedResolvingNearbyPermissionError() {
        mResolvingNearbyPermissionError = false;
    }

    void executePendingTasks() {
        executePendingSubscriptionTask();
        executePendingPublicationTask();
    }

    /**
     * Invokes a pending task based on the subscription state.
     */
    void executePendingSubscriptionTask() {
        String pendingSubscriptionTask = getPubSubTask(Constants.KEY_SUBSCRIPTION_TASK);
        if (TextUtils.equals(pendingSubscriptionTask, Constants.TASK_SUBSCRIBE)) {
            subscribe();
        } else if (TextUtils.equals(pendingSubscriptionTask, Constants.TASK_UNSUBSCRIBE)) {
            unsubscribe();
        }
    }

    /**
     * Invokes a pending task based on the publication state.
     */
    void executePendingPublicationTask() {
        String pendingPublicationTask = getPubSubTask(Constants.KEY_PUBLICATION_TASK);
        if (TextUtils.equals(pendingPublicationTask, Constants.TASK_PUBLISH)) {
            publish();
        } else if (TextUtils.equals(pendingPublicationTask, Constants.TASK_UNPUBLISH)) {
            unpublish();
        }
    }

    /**
     * Subscribes to messages from nearby devices. If not successful, attempts to resolve any error
     * related to Nearby permissions by displaying an opt-in dialog. Registers a callback which
     * updates state when the subscription expires.
     */
    private void subscribe() {
        Log.i(TAG, "trying to subscribe");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            clearDeviceList();
            SubscribeOptions options = new SubscribeOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new SubscribeCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            Log.i(TAG, "no longer subscribing");
                            updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK,
                                    Constants.TASK_NONE);
                        }
                    }).build();

            Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "subscribed successfully");
                            } else {
                                Log.i(TAG, "could not subscribe");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Ends the subscription to messages from nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by
     * displaying an opt-in dialog.
     */
    private void unsubscribe() {
        Log.i(TAG, "trying to unsubscribe");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "unsubscribed successfully");
                                updateSharedPreference(Constants.KEY_SUBSCRIPTION_TASK,
                                        Constants.TASK_NONE);
                            } else {
                                Log.i(TAG, "could not unsubscribe");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Publishes device information to nearby devices. If not successful, attempts to resolve any
     * error related to Nearby permissions by displaying an opt-in dialog. Registers a callback
     * that updates the UI when the publication expires.
     */
    private void publish() {
        Log.i(TAG, "trying to publish");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            PublishOptions options = new PublishOptions.Builder()
                    .setStrategy(PUB_SUB_STRATEGY)
                    .setCallback(new PublishCallback() {
                        @Override
                        public void onExpired() {
                            super.onExpired();
                            Log.i(TAG, "no longer publishing");
                            updateSharedPreference(Constants.KEY_PUBLICATION_TASK,
                                    Constants.TASK_NONE);
                        }
                    }).build();

            Nearby.Messages.publish(mGoogleApiClient, mDeviceInfoMessage, options)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "published successfully");
                            } else {
                                Log.i(TAG, "could not publish");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Stops publishing device information to nearby devices. If successful, resets state. If not
     * successful, attempts to resolve any error related to Nearby permissions by displaying an
     * opt-in dialog.
     */
    private void unpublish() {
        Log.i(TAG, "trying to unpublish");
        // Cannot proceed without a connected GoogleApiClient. Reconnect and execute the pending
        // task in onConnected().
        if (!mGoogleApiClient.isConnected()) {
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Nearby.Messages.unpublish(mGoogleApiClient, mDeviceInfoMessage)
                    .setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "unpublished successfully");
                                updateSharedPreference(Constants.KEY_PUBLICATION_TASK,
                                        Constants.TASK_NONE);
                            } else {
                                Log.i(TAG, "could not unpublish");
                                handleUnsuccessfulNearbyResult(status);
                            }
                        }
                    });
        }
    }

    /**
     * Clears items from the adapter.
     */
    private void clearDeviceList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNearbyDevicesArrayAdapter.clear();
            }
        });
    }

    /**
     * Handles errors generated when performing a subscription or publication action. Uses
     * {@link Status#startResolutionForResult} to display an opt-in dialog to handle the case
     * where a device is not opted into using Nearby.
     */
    private void handleUnsuccessfulNearbyResult(Status status) {
        Log.i(TAG, "processing error, status = " + status);
        if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
            if (!mResolvingNearbyPermissionError) {
                try {
                    mResolvingNearbyPermissionError = true;
                    status.startResolutionForResult(getActivity(),
                            Constants.REQUEST_RESOLVE_ERROR);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (status.getStatusCode() == ConnectionResult.NETWORK_ERROR) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "No connectivity, cannot proceed. Fix in 'Settings' and try again.",
                        Toast.LENGTH_LONG).show();
                resetToDefaultState();
            } else {
                // To keep things simple, pop a toast for all other error messages.
                Toast.makeText(getActivity().getApplicationContext(), "Unsuccessful: " +
                        status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Based on values stored in SharedPreferences, determines the subscription or publication task
     * that should be performed.
     */
    private String getPubSubTask(String taskKey) {
        return getActivity()
                .getPreferences(Context.MODE_PRIVATE)
                .getString(taskKey, Constants.TASK_NONE);
    }

    /**
     * Helper for editing entries in SharedPreferences.
     */
    private void updateSharedPreference(String key, String value) {
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    /**
     * Updates the UI when the state of a subscription or publication action changes.
     */
    private void updateUI() {
        String subscriptionTask = getPubSubTask(Constants.KEY_SUBSCRIPTION_TASK);
        String publicationTask = getPubSubTask(Constants.KEY_PUBLICATION_TASK);

        // Using Nearby is battery intensive. For this reason, progress bars are visible when
        // subscribing or publishing.
        mSubscriptionProgressBar.setVisibility(
                TextUtils.equals(subscriptionTask, Constants.TASK_SUBSCRIBE) ? View.VISIBLE :
                        View.INVISIBLE);
        mPublicationProgressBar.setVisibility(
                TextUtils.equals(publicationTask, Constants.TASK_PUBLISH) ? View.VISIBLE :
                        View.INVISIBLE);

        mSubscriptionImageButton.setImageResource(
                TextUtils.equals(subscriptionTask, Constants.TASK_SUBSCRIBE) ?
                        R.drawable.ic_cancel : R.drawable.ic_nearby);

        mPublicationImageButton.setImageResource(
                TextUtils.equals(publicationTask, Constants.TASK_PUBLISH) ?
                        R.drawable.ic_cancel : R.drawable.ic_share);
    }

    /**
     * Resets the state of pending subscription and publication tasks.
     */
    void resetToDefaultState() {
        getActivity().getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.KEY_SUBSCRIPTION_TASK, Constants.TASK_NONE)
                .putString(Constants.KEY_PUBLICATION_TASK, Constants.TASK_NONE)
                .apply();
    }
}

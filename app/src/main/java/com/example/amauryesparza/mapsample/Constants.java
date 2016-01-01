package com.example.amauryesparza.mapsample;

/**
 * Created by AmauryEsparza on 12/19/15.
 */
class Constants {
    /**
     * Request code to use when launching the resolution activity.
     */
    static final int REQUEST_RESOLVE_ERROR = 1001;


    // The time-to-live when subscribing or publishing in this sample. Three minutes.
    static final int TTL_IN_SECONDS = 3 * 60;


    // Keys to get and set the current subscription and publication tasks using SharedPreferences.
    static final String KEY_SUBSCRIPTION_TASK = "subscription_task";
    static final String KEY_PUBLICATION_TASK = "publication_task";


    // Tasks constants.
    static final String TASK_SUBSCRIBE = "task_subscribe";
    static final String TASK_UNSUBSCRIBE = "task_unsubscribe";
    static final String TASK_PUBLISH = "task_publish";
    static final String TASK_UNPUBLISH = "task_unpublish";
    static final String TASK_NONE = "task_none";
}

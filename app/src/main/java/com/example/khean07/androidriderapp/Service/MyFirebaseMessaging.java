package com.example.khean07.androidriderapp.Service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Khean07 on 4/14/2018.
 */

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Because i will send the firebase message with contain lat and lng from rider app
        //So i need covert message to LatLng
//        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);
//
//        Intent intent = new Intent(getBaseContext(), CustommerCall.class);
//        intent.putExtra("lat",customer_location.latitude);
//        intent.putExtra("lng",customer_location.longitude);
//
//        startActivity(intent);

    }
}

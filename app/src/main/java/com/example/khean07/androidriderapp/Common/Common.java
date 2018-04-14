package com.example.khean07.androidriderapp.Common;

import com.example.khean07.androidriderapp.Remote.FCMClient;
import com.example.khean07.androidriderapp.Remote.IFCMService;
import com.example.khean07.androidriderapp.Remote.RetrofitClient;

/**
 * Created by Khean07 on 4/9/2018.
 */

public class Common {

    public static final String driver_tbl = "Drivers";
    public static final String token_tbl = "Tokens";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmUrl).create(IFCMService.class);
    }


}

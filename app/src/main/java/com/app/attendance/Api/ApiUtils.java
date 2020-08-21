package com.app.attendance.Api;

import android.content.Context;

/**
 * Created by Khan on 2/21/2018.
 */

public class ApiUtils {
    private static Context mContext;

    public static final String BASE_URL = "http://128.199.215.102:4040/api/";

    public static APIInterface getService() {
        return RetrofitClient.getClient(BASE_URL).create(APIInterface.class);
    }
}

package com.xianggu.getdeviceid;

import android.app.Application;
import android.content.Context;

import com.bun.miitmdid.core.JLibrary;
import com.bun.miitmdid.core.MdidSdkHelper;

/**
 * @Description:
 * @Author: xianggu
 * @CreateDate: 2019-09-17 16:11
 */
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        JLibrary.InitEntry(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }



}

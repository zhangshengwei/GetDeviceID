package com.xianggu.getdeviceid;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "device card id---->>";
    private TextView androidIDTv;
    private TextView serialIDTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //获取设备信息
        getDeviceData();

    }

    private void initView() {
        androidIDTv = findViewById(R.id.android_id);
        serialIDTv = findViewById(R.id.serial_id);
    }

    private void getDeviceData() {
        // TODO: 2019-08-26   android ID 在android9.0中 通过debug模式安装的不同apk可以获取一致的id，但如果使用不同的jks安装的应用获取到的id不一致
        String androidID = getAndroidId(this);
        androidIDTv.setText("androidID: " + androidID);
        Log.d(TAG, "androidID id: " + androidID);

        // TODO: 2019-08-26 Build.class的源码，可以发现字段SERIAL已经被标注为@Deprecated了，
        String serialID = getUniquePsuedoID();
        serialIDTv.setText("serialID: " + serialID);
        Log.d(TAG, "serialID id: " + serialID);


    }

    public static String getAndroidId (Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }


    //获得独一无二的Psuedo ID
    public static String getUniquePsuedoID() {
        String serial = null;
        String m_szDevIDShort = "35" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化,随意值
            serial = "serial";

            Log.d(TAG, "serialID id 获取失败" );
        }

        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }
}

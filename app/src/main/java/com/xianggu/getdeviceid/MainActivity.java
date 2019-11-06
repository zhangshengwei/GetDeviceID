package com.xianggu.getdeviceid;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.xianggu.getdeviceid.utils.DeviceDataUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DeviceID---->>";
    private static final String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private List<String> permissionList = new ArrayList<>();

    //存放UUID的文件名
    private static final String saveFileName = "ADMObileDeviceIdFile";

    //在低于android Q的版本中，直接通过路径获取
    private static final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + saveFileName;

    private TextView getDataTv;
    private TextView uuidResultTv;

    private TextView useMSASolutionTv;

    private TextView getAndroidID, getIMEI, getMAC, idresultTv;

    private int deviceVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDataTv = findViewById(R.id.getData);
        uuidResultTv = findViewById(R.id.uuidResult);
        useMSASolutionTv = findViewById(R.id.toMSATv);

        getAndroidID = findViewById(R.id.getAndroidId);
        getIMEI = findViewById(R.id.getAndroidIMEI);
        getMAC = findViewById(R.id.getAndroidMAC);
        idresultTv = findViewById(R.id.idresult);

        applyPermission();

        //获取当前设备版本号: 列入 5.1.1--->取5
        String[] versionSplit = Build.VERSION.RELEASE.split("\\.");
        deviceVersion = Integer.parseInt(versionSplit[0]);

        //生成文件保存在多媒体目录下
        getDataTv.setOnClickListener(view -> {
            //判断是否已经存在唯一标识符，并返回标识符内容
            String checkStr = checkUUIDFileByUri();
            if (!TextUtils.isEmpty(checkStr)) {
                // TODO: 2019-08-27 可能需要在这里做一些数据上报等工作
                uuidResultTv.setText("文件得到的uuid:\n" + checkStr);
            } else {
                //生成标识符文件
                creatUUIDFile();
                uuidResultTv.setText("创建得到的uuid:\n" + checkUUIDFileByUri());
            }
        });

        useMSASolutionTv.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, MSASolutionActivity.class));
        });

        // 获取AndroidID
        getAndroidID.setOnClickListener(view -> {
            String ANDROID_ID = DeviceDataUtils.getAndroidId(this);
            idresultTv.setText("获取的AndroidID :" + "\t" + ANDROID_ID);
        });

        // 获取IMEI
        getIMEI.setOnClickListener(view -> {
            String imei = DeviceDataUtils.getAndroidIMEI(this);

            if (TextUtils.isEmpty(imei)) {
                idresultTv.setText("获取的IMEI :" + "\t获取失败");
            } else {
                idresultTv.setText("获取的IMEI :" + "\t" + imei);
            }
        });

        // 获取MAC
        getMAC.setOnClickListener(view -> {
            String mac = "";
            mac = DeviceDataUtils.getMacAddress(this);
            if (TextUtils.isEmpty(mac)) {
                idresultTv.setText("获取的MAC :" + "\t获取失败");
            } else {
                idresultTv.setText("获取的MAC :" + "\t" + mac);
            }
        });
    }


    /**
     * 检查文件是否存在
     *
     * @return
     */
    private String checkUUIDFileByUri() {

        String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID
        };
        //查询
        ContentResolver contentResolver = this.getContentResolver();

        // 添加筛选条件,根据SQLite表里的 display_name是否与saveFileName一直
        String selection = MediaStore.Images.Media.DISPLAY_NAME + "=" + "'" + saveFileName + "'";

        //EXTERNAL_CONTENT_URI 为查询外置内存卡
        Cursor mCursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        String getSaveContent = "";
        if (mCursor != null) {
            while (mCursor.moveToNext()) {

                int fileIdIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                String thumbPath = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(mCursor.getInt(fileIdIndex))).build().toString();
                Uri fileUri = Uri.parse(thumbPath);

                FileInputStream inputStream = null;
                try {
                    if (deviceVersion >= 10) {
                        ParcelFileDescriptor fielDescriptor = contentResolver.openFileDescriptor(fileUri, "r", null);
                        inputStream = new FileInputStream(fielDescriptor.getFileDescriptor());
                    } else {
                        File file = new File(filePath);
                        inputStream = new FileInputStream(file);
                    }
                    getSaveContent = inputStreamToString(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //只有在得到的唯一标识符不为空的情况下才结束循环
                if (!TextUtils.isEmpty(getSaveContent)) {
                    break;
                }
            }
            mCursor.close();
        }
        return getSaveContent;
    }


    /**
     * 在媒体文件中 生成fileName文件
     * 向Mediastore添加内容
     */
    private void creatUUIDFile() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, saveFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

        ContentResolver contentResolver = this.getContentResolver();

        Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            FileOutputStream outputStream = null;
            //访问 对于单个媒体文件，请使用 openFileDescriptor()。
            if (deviceVersion >= 10) {
                ParcelFileDescriptor fielDescriptor = contentResolver.openFileDescriptor(uri, "w", null);
                outputStream = new FileOutputStream(fielDescriptor.getFileDescriptor());
            } else {
                File file = new File(filePath);
                outputStream = new FileOutputStream(file);
            }
            try {
                //讲UUID写入到文件中
                String uuidStr = UUID.randomUUID().toString();
                outputStream.write(uuidStr.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            contentResolver.update(uri, values, null, null);
            values.clear();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 将内容选择编码格式输出
     *
     * @param inputStream
     * @return
     */
    private static String inputStreamToString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 申请权限
     */
    private void applyPermission() {
        // 存在未申请的权限则先申请
        for (String permission : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission);
            if (PackageManager.PERMISSION_GRANTED == checkSelfPermission) {
                continue;
            }
            permissionList.add(permission);
        }


        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions
                    (this, permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
        }
    }


}


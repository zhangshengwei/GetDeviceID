package com.xianggu.getdeviceid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.xianggu.getdeviceid.utils.FileUtils;

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
    private TextView androidIDTv;
    private TextView serialIDTv;
    private TextView uuidTv;

    //文件名   需要保存到多媒体目录下
    private String filePath = "";
    private static final String checkFileName = "admobileUniqueIdentifler";


    private static final String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<String> permissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidIDTv = findViewById(R.id.android_id);
        serialIDTv = findViewById(R.id.serial_id);
        uuidTv = findViewById(R.id.uuid);

        //申请权限
        getPermission();

        //获取设备信息
//        getDeviceData();
    }

    /**
     * 申请媒体文件读写权限
     */
    private void getPermission() {

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
        }else{
            initUUIDData();
        }
    }

    /**
     * 初始化uuid
     */
    private void initUUIDData(){
        if (Build.VERSION.SDK_INT >= 29){
            if (!checkIsExistUUIDFile()){
                Log.i(TAG, "文件不存在");
                //存入UUID
                saveUUIDFile();
            }else{
                //获取UUID
                String uuidContent = getUUIDFileData();
                Log.i(TAG, "getUUID content:"+uuidContent);
                uuidTv.setText("uuid:"+uuidContent);
            }
        }
    }


    /**
     * 检查是否存在UUID文件
     */
    private boolean checkIsExistUUIDFile() {
        // 得到的目录为: /storage/emulated/0/DCIM
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        Log.d(TAG, "checkIsExistUUIDFile: 目录:"+filePath);

        List<String> fileNameList = FileUtils.getFilesAllName(filePath);
        if (fileNameList==null || fileNameList.isEmpty()){
           return false;
        }

        boolean isExist = false;
        for (String fileName : fileNameList){
            if (checkFileName.equals(fileName)){
                isExist = true;
                break;
            }
        }

        return isExist;
    }



    /**
     * 保存UUID文件
     */
    private void saveUUIDFile() {
        //唯一标识文本内容
        String uuidStr = UUID.randomUUID().toString();
        Log.d(TAG, "开始写入文件 saveUUIDFile: " + uuidStr);
        //将uuid字符串写入到文件中

        try{
            File file = new File(filePath + "/" + checkFileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte [] bytes = uuidStr.getBytes();
            fos.write(bytes);
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 读取UUID文件的内容
     * @return
     */
    private String getUUIDFileData(){
        String fp = filePath + "/" + checkFileName;
        String result="";
        File file = new File(fp);
        try {
            InputStream inputStream = new FileInputStream(file);
            result = getString(inputStream);
            //处理业务
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 将内容选择编码格式输出
     * @param inputStream
     * @return
     */
    private static String getString(InputStream inputStream) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            initUUIDData();
        }
    }
}


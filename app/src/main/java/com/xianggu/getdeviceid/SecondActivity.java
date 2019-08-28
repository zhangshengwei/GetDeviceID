package com.xianggu.getdeviceid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.xianggu.getdeviceid.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

/**
 * @Description:
 * @Author: xianggu
 * @CreateDate: 2019-08-27 14:15
 */
public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "----->>SecondAct";
    private String uuidStr = "null";
    private static final String saveFileName = "ADMobileDeviceID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


    }


    /**
     * 检查是否存在UUID文件
     */
    private String filePath = "";
    private String checkUUIDFileByFile() {
        // 得到的目录为: /storage/emulated/0
        filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

        String uuidContent = "";
        List<String> fileNameList = FileUtils.getFilesAllName(filePath);
        if (fileNameList==null || fileNameList.isEmpty()){
            return uuidContent;
        }

        for (String fileName : fileNameList){
            if (saveFileName.equals(fileName)){
                break;
            }
        }


        return uuidContent;
    }



    /**
     * 保存UUID文件
     */
    private void createUUIDFileUnderVersionQ() {
        //唯一标识文本内容
        String uuidStr = UUID.randomUUID().toString();
        //将uuid字符串写入到文件中

        try{
            File file = new File(filePath + "/" + saveFileName);
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
        String fp = filePath + "/" + saveFileName;
        String result="";
        try{
            FileInputStream fis = this.openFileInput(fp);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
}

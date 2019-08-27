package com.xianggu.getdeviceid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

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

        uuidStr = UUID.randomUUID().toString();


        String checkStr = checkUUIDFileByUri();
        if (!TextUtils.isEmpty(checkStr)){
            Log.d(TAG, "onCreate: checkStr:"+checkStr);
        }else{

            insertMediastore();
            Log.d(TAG, "onCreate: uuidStr:"+uuidStr);
        }


    }

    /**
     * 在媒体文件中 生成fileName文件
     * 向Mediastore添加内容
     */
    private void insertMediastore() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, saveFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/*");
        // TODO: 2019-08-27 IS_PENDING = 1表示对应的item还没准备好
        values.put(MediaStore.Images.Media.IS_PENDING,1);
//        values.put(MediaStore.Images.Media.RELATIVE_PATH,"");

        ContentResolver resolver = this.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        Uri uri = resolver.insert(collection,values);

        try {
            //访问 对于单个媒体文件，请使用 openFileDescriptor()。
            ParcelFileDescriptor fielDescriptor = resolver.openFileDescriptor(uri,"w",null);
            FileOutputStream outputStream = new FileOutputStream(fielDescriptor.getFileDescriptor());
            try {
                //讲UUID写入到文件中
                outputStream.write(uuidStr.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);          //设置为0
            resolver.update(uri,values,null,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查文件是否存在
     * @return
     */
    private String checkUUIDFileByUri(){
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID
        };
        //查询
        ContentResolver contentResolver = this.getContentResolver();
        Cursor mCursor = contentResolver.query(
                mImageUri, projection, null, null,
                null);

        String getSaveContent = "";
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                int fileNameIndex = mCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int fileIdIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);

                String fileName = String.valueOf(mCursor.getString(fileNameIndex));
                Log.d(TAG, "checkUUIDFileByUri: fileName:"+fileName);

                if (saveFileName.equals(fileName)){


                    String thumbPath = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                            .appendPath(String.valueOf(mCursor.getInt(fileIdIndex))).build().toString();

                    Log.d(TAG, "checkUUIDFileByUri: path:"+thumbPath);

                    Uri fileUri = Uri.parse(thumbPath);

                    try {
                        ParcelFileDescriptor fielDescriptor = contentResolver.openFileDescriptor(fileUri,"r",null);
                        FileInputStream inputStream = new FileInputStream(fielDescriptor.getFileDescriptor());

                        getSaveContent = inputStreamToString(inputStream);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }


                    break;
                }
            }
            mCursor.close();
            if (!TextUtils.isEmpty(getSaveContent)){
                return getSaveContent;
            }

        }

        return getSaveContent;
    }


    /**
     * 借助ContentResolver来判断文件是否存在
     * @param context
     * @param uri
     * @return
     */
    public static boolean isContentUriExists(Context context, Uri uri){
        if (null == context) {
            return false;
        }
        ContentResolver cr = context.getContentResolver();
        try {
            AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
            if (null == afd) {
                return false;
            } else {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        } catch (FileNotFoundException e) {
            return false;
        }

        return true;
    }







    /**
     * 将内容选择编码格式输出
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
}

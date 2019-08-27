package com.xianggu.getdeviceid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DeviceID---->>";

    //存放UUID的文件名
    private static final String saveFileName = "ADMobileDeviceID";

    private TextView getDataTv;
    private TextView uuidResultTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDataTv = findViewById(R.id.getData);
        uuidResultTv = findViewById(R.id.uuidResult);

        getDataTv.setOnClickListener(view -> {

            //判断是否已经存在唯一标识符，并返回标识符内容
            String checkStr = checkUUIDFileByUri();
            if (!TextUtils.isEmpty(checkStr)){

                Log.d(TAG, "onCreate: checkStr:"+checkStr);
                uuidResultTv.setText("uuid:"+checkStr);
                // TODO: 2019-08-27 可能需要在这里做一些数据上报等工作 
            }else{
                
                Log.d(TAG, "onCreate: 没有监测到相关文件");
                //生成标识符文件
                creatUUIDFile();
            }
        });


    }

    /**
     * 在媒体文件中 生成fileName文件
     * 向Mediastore添加内容
     */
    private void creatUUIDFile() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, saveFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/*");
        // TODO: 2019-08-27 IS_PENDING = 1表示对应的item还没准备好
        values.put(MediaStore.Images.Media.IS_PENDING,1);

        ContentResolver resolver = this.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        Uri uri = resolver.insert(collection,values);

        try {
            //访问 对于单个媒体文件，请使用 openFileDescriptor()。
            ParcelFileDescriptor fielDescriptor = resolver.openFileDescriptor(uri,"w",null);
            FileOutputStream outputStream = new FileOutputStream(fielDescriptor.getFileDescriptor());
            try {
                //讲UUID写入到文件中
                String uuidStr = UUID.randomUUID().toString();
                outputStream.write(uuidStr.getBytes());
                outputStream.close();
                Log.d(TAG, "写入 uuidStr:"+uuidStr);
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

        // 添加筛选条件
        String selection = MediaStore.Images.Media.DISPLAY_NAME + "=" + "'" + saveFileName + "'";
        Cursor mCursor = contentResolver.query(mImageUri, projection, selection, null, null);

        String getSaveContent = "";
        if (mCursor != null) {
            while (mCursor.moveToNext()) {

                int fileIdIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
                String thumbPath = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(mCursor.getInt(fileIdIndex))).build().toString();
                Uri fileUri = Uri.parse(thumbPath);
                try {
                    ParcelFileDescriptor fielDescriptor = contentResolver.openFileDescriptor(fileUri,"r",null);
                    FileInputStream inputStream = new FileInputStream(fielDescriptor.getFileDescriptor());
                    getSaveContent = inputStreamToString(inputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //只有在得到的唯一标识符不为空的情况下才结束循环，否则一直循环
                if (!TextUtils.isEmpty(getSaveContent)){
                    break;
                }
            }
            mCursor.close();

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


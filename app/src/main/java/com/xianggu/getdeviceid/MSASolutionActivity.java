package com.xianggu.getdeviceid;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.xianggu.getdeviceid.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @Description: MSA 关于android Q 唯一标识符的解决方案
 * @Author: xianggu
 * @CreateDate: 2019-08-27 14:15
 */
public class MSASolutionActivity extends AppCompatActivity implements MiitHelper.AppIdsUpdater {

    private static final String TAG = "--->>";

    private TextView getDataTv;
    private TextView uuidResultTv;

    private MiitHelper miitHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msa_solution);
        miitHelper = new MiitHelper(this);
        initView();
    }

    private void initView() {

        getDataTv = findViewById(R.id.getData);
        uuidResultTv = findViewById(R.id.uuidResult);

        getDataTv.setOnClickListener(view -> {
            miitHelper.getDeviceIds(MSASolutionActivity.this);
        });
    }


    @Override
    public void OnIdsAvalid(@NonNull String ids) {
        Log.d(TAG, "OnIdsAvalid: -->>ids:"+ids);

        uuidResultTv.setText(ids);
    }
}

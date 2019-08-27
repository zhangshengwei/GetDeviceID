package com.xianggu.getdeviceid.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: xianggu
 * @CreateDate: 2019-08-27 09:52
 */
public class FileUtils {

    //读取指定目录下的文件名
    public static List<String> getFilesAllName(String filePath) {
        File file=new File(filePath);
        File[] files=file.listFiles();
        if (files == null){
            return null;
        }
        List<String> fileNameList = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            fileNameList.add(files[i].getName());
        }
        return fileNameList;
    }

}

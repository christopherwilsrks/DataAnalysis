package com.nlp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author christ
 *
 * 2019/4/30 15:08
 **/
public class FileUtil {

    private List<File> fileList;
    private String suffix;

    public FileUtil(String suffix) {
        this.fileList = new ArrayList<>();
        this.suffix = suffix.contains(".") ? suffix : '.' + suffix;
    }

    public List<File> getFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory()) { // 判断是文件还是文件夹
                    getFileList(file.getAbsolutePath()); // 获取文件绝对路径
                } else if (fileName.endsWith(suffix)) { // 判断文件名是否以.avi结尾
                    String strFileName = file.getAbsolutePath();
                    System.out.println("---" + strFileName);
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

}

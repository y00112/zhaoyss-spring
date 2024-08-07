package com.zhaoyss.utils;

import com.zhaoyss.io.InputStreamCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 获取 path 的输入流
 */
public class ClassPathUtils {

    public static <T> T readInputStream(String path, InputStreamCallback<T> inputStreamCallback){
        if (path.startsWith("/")){
            path = path.substring(1);
        }
        try (InputStream input = getContextClassLoader().getResourceAsStream(path)){
            if (input == null){
                throw new FileNotFoundException("File not found in classpath: " + path);
            }
            return inputStreamCallback.doWithInputStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static ClassLoader getContextClassLoader(){
        ClassLoader cl = null;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null){
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }
}

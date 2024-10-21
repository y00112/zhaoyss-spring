package com.zhaoyss.web;

import com.zhaoyss.boot.ZhaoyssApplication;

public class Main
{
    public static void main(String[] args) throws Exception {
        // 判定是否从jar/war启动：
        ZhaoyssApplication.run("step-by-step/hello-boot/src/main/webapp", "step-by-step/hello-boot/target/classes", HelloConfiguration.class, args);
    }
}

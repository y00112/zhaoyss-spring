package com.zhaoyss.web;

import com.zhaoyss.boot.ZhaoyssApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args) throws Exception {
        // 判定是否从jar/war启动：
        String jarFile = Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        boolean isJarFile = jarFile.endsWith(".war") || jarFile.endsWith(".jar");
        // 定位 webapp 根目录
        String webDir = isJarFile ? "tmp-webapp" : "step-by-step/hello-boot/src/main/webapp";
        if (isJarFile){
            // 解压到 tmp-webapp
            Path baseDir = Paths.get(webDir).normalize().toAbsolutePath();
            if (Files.isDirectory(baseDir)){
                Files.delete(baseDir);
            }
            Files.createDirectories(baseDir);
            System.out.println("extract to: "+ baseDir);
            try(JarFile jar = new JarFile(jarFile)){
                List<JarEntry> entries = jar.stream().sorted(Comparator.comparing(JarEntry::getName)).collect(Collectors.toList());
                for (JarEntry entry: entries){
                    Path res = baseDir.resolve(entry.getName());
                    if (!entry.isDirectory()){
                        System.out.println(res);
                        Files.createDirectories(res.getParent());
                        Files.copy(jar.getInputStream(entry),res);
                    }
                }
            }
            // jvm退出时自动删除tmp-webapp:
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try {
                    Files.walk(baseDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }));
        }
        // ZhaoyssApplication.run("step-by-step/hello-boot/src/main/webapp", "step-by-step/hello-boot/target/classes", HelloConfiguration.class, args);
        ZhaoyssApplication.run(webDir,isJarFile? "tmp-webapp":"step-by-step/hello-boot/target/classes",HelloConfiguration.class,args);
    }
}

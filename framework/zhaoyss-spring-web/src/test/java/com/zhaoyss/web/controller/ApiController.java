package com.zhaoyss.web.controller;

import com.zhaoyss.annotation.*;
import com.zhaoyss.web.utils.JsonUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class ApiController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/api/hello/{name}")
    @ResponseBody
    String hello(@PathVariable("name") String name) {
        return JsonUtils.writeJson(Map.of("name", name));
    }

    @GetMapping("/api/greeting")
    Map<String,Object> greeting(@RequestParam(value = "action",defaultValue = "Hello") String action,@RequestParam("name") String name){
        return Map.of("action",Map.of("name",name));
    }

    @GetMapping("/api/download/{file}")
    FileObj downloadPart(@RequestParam("file") String file, @RequestParam("time")Float downloadTime, @RequestParam("md5") String md5,
                      @RequestParam("length") int length, @RequestParam("hasChecksum") boolean checksum, HttpServletResponse resp){
        var f = new FileObj();
        f.setFile(file);
        f.setLength(length);
        f.setDownloadTime(downloadTime);
        f.setMd5(md5);
        f.setContent("A".repeat(length).getBytes(StandardCharsets.UTF_8));
        return f;
    }

    public static class FileObj{
        public String file;
        public int length;
        public Float downloadTime;
        public String md5;
        public byte[] content;

        public FileObj() {
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public Float getDownloadTime() {
            return downloadTime;
        }

        public void setDownloadTime(Float downloadTime) {
            this.downloadTime = downloadTime;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }

    public static class SigninObj{
        public String name;
        public String password;
    }

}

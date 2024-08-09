package com.zhaoyss.entity;



import com.zhaoyss.annotation.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class AbstractEntity {
    private long id;

    private Long createdAt;


    public long getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getCreatedDateTime() {
        return Instant.ofEpochMilli(this.createdAt).atZone(ZoneId.systemDefault());
    }

    public void preInsert() {
        setCreatedAt(System.currentTimeMillis());
    }
}

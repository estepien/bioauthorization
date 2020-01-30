package com.bioauthorization.proxy.database.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class User {

    @Id
    public String id;

    @Indexed(unique = true)
    public String idpId;

    public String seed;

    public byte[] rsaKey;

    private String pushNotificationData;

    @CreatedDate
    public Date createdDate;

    @LastModifiedDate
    public Date lastEntry;

    @Builder
    public User(String idpId, String seed) {
        this.idpId = idpId;
        this.seed = seed;
    }
}

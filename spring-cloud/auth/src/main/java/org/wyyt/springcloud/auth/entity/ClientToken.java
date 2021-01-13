package org.wyyt.springcloud.auth.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonSerialize
public class ClientToken implements Serializable {
    private Long appId;
    private String accessToken;
    private Long expiresTime;
}
package com.comp5348.dto.auth;

import lombok.Data;

import java.io.Serializable;
@Data
public class JwtAuthResponse implements Serializable {
    private String token;
}

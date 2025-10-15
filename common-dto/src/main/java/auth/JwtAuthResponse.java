package auth;

import lombok.Data;

import java.io.Serializable;
@Data
public class JwtAuthResponse implements Serializable {
    private String token;
}

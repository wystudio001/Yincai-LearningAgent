package xyz.wystudio.yincai.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long tenantId;
    private String username;
    private String roleCode;
    private Set<String> permissions;
}

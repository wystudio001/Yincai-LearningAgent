package xyz.wystudio.yincai.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private long pageNo = 1;
    private long pageSize = 20;
    private String sortField;
    private String sortOrder;  // ASC / DESC
}

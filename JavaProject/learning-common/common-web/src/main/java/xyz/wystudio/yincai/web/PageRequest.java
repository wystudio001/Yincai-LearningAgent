package xyz.wystudio.yincai.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.wystudio.yincai.core.PageQuery;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    private long pageNo = 1;
    private long pageSize = 20;
    private String sortField;
    private String sortOrder;

    public PageQuery toCoreQuery() {
        return new PageQuery(pageNo, pageSize, sortField, sortOrder);
    }
}

package com.product.entity;

import lombok.Data;

@Data
public class CodeProdInfo extends ProdInfo {
    /**
     * 仓库ID
     */
    private String repositoryId;
    /**
     * 代码语言
     */
    private Integer language;


}

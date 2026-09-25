package com.ruoyi.doc.domain.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文档查询条件
 */
@Data
public class DocInfoBo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String docName;

    private String projectName;

    private String provinceCode;

}

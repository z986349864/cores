package com.zd.core.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class User implements Serializable {

    @ExcelProperty("id")
    private Integer id;
    @ExcelProperty("姓名")
    private String name;
}

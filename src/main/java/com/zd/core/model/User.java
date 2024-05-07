package com.zd.core.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class User implements Serializable {

    private Integer id;
    private String name;
}

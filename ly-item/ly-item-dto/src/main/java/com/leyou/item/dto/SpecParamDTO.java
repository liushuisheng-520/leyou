package com.leyou.item.dto;

import lombok.Data;

@Data
public class SpecParamDTO {
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    private Boolean isNumeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;
}

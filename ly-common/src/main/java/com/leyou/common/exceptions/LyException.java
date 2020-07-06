package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

/**
 *
 */
@Getter
public class LyException extends RuntimeException{

    private int status;


    public LyException(int status,String message) {
        super(message);
        this.status = status;
    }

    //创建枚举类的构造方法
    public LyException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.status = exceptionEnum.getStatus();
    }

}

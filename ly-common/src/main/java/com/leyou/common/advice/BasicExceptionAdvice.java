package com.leyou.common.advice;


import com.leyou.common.exceptions.ExceptionResult;
import com.leyou.common.exceptions.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice //但凡经过controller的方法都会进入到这个类中
public class BasicExceptionAdvice {

    //处理异常
    @ExceptionHandler(LyException.class) //出现LyException就会进入到 这个方法中
    public ResponseEntity<ExceptionResult> exceptionHandler(LyException e){

        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));

    }

}

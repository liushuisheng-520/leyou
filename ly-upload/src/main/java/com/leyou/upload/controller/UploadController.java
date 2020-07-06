package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 上传图片到本地
     * @param file
     * @return
     */
    @PostMapping(value = "/image", name = "图片上传到本地")
    public ResponseEntity<String> uploadImage(MultipartFile file) {

        String imageUrl=uploadService.uploadImage(file);

        return ResponseEntity.ok(imageUrl);
    }

    /**
     * OSS web直传需要的签名
     * @return
     */
    @GetMapping(value = "/signature",name = "OSS web直传需要的签名")
    public ResponseEntity<Map<String,String>> signature (){
        Map<String,String> signatureMap= uploadService.signature();

        return ResponseEntity.ok(signatureMap);
    }
}
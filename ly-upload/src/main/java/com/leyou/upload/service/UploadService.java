package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;


@Service
public class UploadService {

    private List<String> imageMimeTypeList = Arrays.asList("image/jpg", "image/jpeg", "image/gif", "image/png");

    /**
     * 上传图片到本地
     *
     * @param file
     * @return
     */
    public String uploadImage(MultipartFile file) {

        //1.判断是不是图片
        //判断imageMimeTypeList中是否包含file类型
        String contentType = file.getContentType();//获取file的类型

        if (!imageMimeTypeList.contains(contentType)) {

            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //2.判断是不是假图片
        try {
            //用ImageIO的read方法读取file内容
            BufferedImage read = ImageIO.read(file.getInputStream());

            if (read == null) {//没有读到东西 就是空图片
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }


        //上传时是否出异常
        String filename = UUID.randomUUID().toString() + file.getOriginalFilename();
        try {
            //将上传的图片保存到本地
            file.transferTo(new File("D:\\Java\\nginx\\nginx-1.16.0\\html\\" + filename));
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }


        return "http://image.leyou.com/" + filename;
    }


    /**
     * OSS web直传需要的签名
     *
     * @return
     */
    @Autowired
    private OSSProperties ossProperties;

    @Autowired
    private OSS client;

    public Map<String, String> signature() {


//        String accessId = "<yourAccessKeyId>"; // 请填写您的AccessKeyId。
//        String accessKey = "<yourAccessKeySecret>"; // 请填写您的AccessKeySecret。
//        String endpoint = "oss-cn-hangzhou.aliyuncs.com"; // 请填写您的 endpoint。
//        String bucket = "bucket-name"; // 请填写您的 bucketname 。
//        String host = "https://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
//        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
//        String callbackUrl = "http://88.88.88.88:8888";
//        String dir = "user-dir-prefix/"; // 用户上传文件时指定的前缀。

//        OSSClient client = new OSSClient(endpoint, accessId, accessKey);
        try {
            long expireTime =ossProperties.getExpireTime() ;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProperties.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", ossProperties.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProperties.getDir());
            respMap.put("host", ossProperties.getHost());
            respMap.put("expire", String.valueOf(expireEndTime));
            // respMap.put("expire", formatISO8601Date(expiration));

            return respMap;

        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}

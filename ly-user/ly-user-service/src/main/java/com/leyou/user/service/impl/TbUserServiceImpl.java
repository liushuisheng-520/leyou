package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.mapper.TbUserMapper;
import com.leyou.user.service.TbUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.common.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.common.constants.SmsConstants.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-24
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    /**
     * 验证用户名和手机号是否重复
     *
     * @param data
     * @param type
     * @return
     */
    @Override
    public Boolean checkData(String data, Integer type) {
        //查询
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper();


        //判断type是1还是2
        switch (type) {
            case 1: {
                //type是1 就查询用户名
                queryWrapper.lambda().eq(TbUser::getUsername, data);
                break;

            }
            case 2: {
                //type是2 就查询手机号
                queryWrapper.lambda().eq(TbUser::getPhone, data);
                break;
            }
            default: {
                //都不是显示异常
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);

            }
        }
        //返回查询到的数据的次数
        int count = this.count(queryWrapper);


        //判断结果是不是0 0代表没有重复用户名或者手机号
        return count == 0 ? true : false;
    }

    /**
     * 发送短信
     *
     * @param phone
     */
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCode(String phone) {
        //将要发送的内容放入到MQ中
        //String PhoneNumbers=  map.get(SMS_PARAM_KEY_PHONE).toString();
//      String SignName=  map.get(SMS_PARAM_KEY_SIGN_NAME).toString();
//      String TemplateCode=  map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();
//      String TemplateParam=  map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();
        Map map = new HashMap();
        //随机产生4位数字
        String randomNumeric = RandomStringUtils.randomNumeric(4);

        map.put(SMS_PARAM_KEY_PHONE, phone);
        map.put(SMS_PARAM_KEY_SIGN_NAME, "黑马旅游网");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE, "SMS_178761210");
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM, "{\"code\":\"" + randomNumeric + "\"}");//注意转义字符

        //将消息放入到MQ中的topic和tags中
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME + ":" + VERIFY_CODE_TAGS, map);

        //将发送的验证码保存到redis中(设置30秒有效时间)
        stringRedisTemplate.boundValueOps("ly:sms:" + phone).set(randomNumeric, 30, TimeUnit.SECONDS);

    }

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 注册和校验验证码和密码加密
     * @param user
     * @param code
     */
    @Override
    public void register(TbUser user, String code) {
        //1.对比验证码
        String code_redis = stringRedisTemplate.boundValueOps("ly:sms:" + user.getPhone()).get();
        //判断验证码是否为空
        if (StringUtils.isEmpty(code_redis)) {
            throw new LyException(ExceptionEnum.TIME_OUT_CODE);
        }
        //页面验证码和redis中的验证码进行比较
        if (!StringUtils.equals(code_redis, code)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        //2.密码加密
        String password = user.getPassword();

        password= bCryptPasswordEncoder.encode(password);
        user.setPassword(password);

        //3.将数据写入数据库表
        boolean save = this.save(user);
        //判断是否写入表中
        if (!save){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //4.把redis中的失效验证码删除
        stringRedisTemplate.delete("ly:sms:"+user.getPhone());
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {

        //1.用户名对比
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbUser::getUsername,username);
        //执行查询
        TbUser tbUser = this.getOne(queryWrapper);
        //判断根据用户名是否查到数据 如果空则用户名错误
        if (tbUser==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        //2.密码对比
        //获取表中密码
        String password1 = tbUser.getPassword();
        //用spring的加密类的方法解密
        boolean matches = bCryptPasswordEncoder.matches(password, password1);

        //判断解密是否成功 false就显示异常
        if (!matches){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


        return BeanHelper.copyProperties(tbUser,UserDTO.class);

    }

}

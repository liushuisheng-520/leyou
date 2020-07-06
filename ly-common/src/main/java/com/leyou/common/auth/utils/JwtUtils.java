package com.leyou.common.auth.utils;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.utils.JsonUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

/**
 * jwt生成token
 */
public class JwtUtils {

    private static final String JWT_PAYLOAD_USER_KEY = "user";

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位分钟
     * @return JWT
     */
    public static String generateTokenExpireInMinutes(Object userInfo, PrivateKey privateKey, int expire) {
        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JsonUtils.toString(userInfo))
                .setId(createJTI())
                .setExpiration(DateTime.now().plusMinutes(expire).toDate())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位秒
     * @return JWT
     */
    public static String generateTokenExpireInSeconds(Object userInfo, PrivateKey privateKey, int expire) {
        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JsonUtils.toString(userInfo))
                .setId(createJTI())
                .setExpiration(DateTime.now().plusSeconds(expire).toDate())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 公钥解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥
     * @return Jws<Claims>
     */
    private static Jws<Claims> parserToken(String token, PublicKey publicKey) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }

    private static String createJTI() {
        return new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    }

    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey, Class<T> userType) {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        Payload<T> claims = new Payload<>();
        claims.setId(body.getId());
        claims.setUserInfo(JsonUtils.toBean(body.get(JWT_PAYLOAD_USER_KEY).toString(), userType));
        claims.setExpiration(body.getExpiration());
        return claims;
    }

    /**
     * 获取token中的载荷信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey) {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        Payload<T> claims = new Payload<>();
        claims.setId(body.getId());
        claims.setExpiration(body.getExpiration());
        return claims;
    }

    //测试 生成token
    public static void main(String[] args) throws Exception {
       /* //需要userInfo
        UserInfo userInfo = new UserInfo(10l, "lisi", "vip");
        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey("F:\\LeyouXiangMu\\id_rsa");
        //生成tiken需要 userInf,私钥,超时时间
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, privateKey, 1);

        System.out.println(token);
*/
        //String token = "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxMCxcInVzZXJuYW1lXCI6XCJsaXNpXCIsXCJyb2xlXCI6XCJ2aXBcIn0iLCJqdGkiOiJNMkkyTjJNeU56a3RNV1F4TVMwME1ESmhMV0l3T1dRdFpUQXlOREU1TWpkaE1HRXgiLCJleHAiOjE1ODI3MTQxOTZ9.TDp9FDET2R1bTmEfMFSbMe0RHlUH5b3mD6jqar09IXMaCAm7PKZSzAzFBX1FkDvSRGtnTvEaVda41z8zBjw65Mjd9XXFViA9qKf2dvKcQadbQuN32fHtsDRgTiIx7SjXgnpzNHnr5UEav6UkcfB7ZDLS-sgOzx-NamDuwon4wvCu4Oqvg6qC70YWdXt61SwKk8FgIaVOidpgAmGGZ3v7ela3J4R4gmuf7sQQ6nPtu6onCoIqXwKEd2HEHM6jUrDxvTVXfNrCEFiEnfG5QTT-jDV9muh7FcxaBaL1BLGtGqO-15Sr32fd6Rss3HxDkKsCTYR7NvUMy7b_tXQsX8CAiw";

        String token="eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxMCxcInVzZXJuYW1lXCI6XCJsaXNpXCIsXCJyb2xlXCI6XCJ2aXBcIn0iLCJqdGkiOiJZalk0T0RJME0ySXRPVFF5TnkwME1qRTRMV0l6TURrdE5UZGlZbUUzTlRoa1pUVTQiLCJleHAiOjE1ODI3MTQzMjR9.Bk5HG4lfl6ohWwpSeHZDmjj9ZI8ztcLJmxTWLT0p9D2tPu6neQRnPBN-jMq9txNaAhw5CbmPbndvM_hsl5BiPL3mP84GEJtbNFu0MtDj_LFuUpC1ML-zbizar9fDsGQSPGpRwSZ-WbjeB5ba4_wjfPzQIakaUxZtvntPTwRE7cRjDKepbZxeLMdnrWCrvTLQNTyf9f1QvCBviPst0o6kPMxM8YwZlRHkY5khSMt3TqfzzJQAJdOQHBk6MakGTPk90Nh8IFMYDijqo7Vi6MP_Pb3KUtZCzGSJ73R4EhthDL7Z60Eevn3gBbgBtvFiFk3XfEelFrYdH8J8GBjNy3UjbA";
        //解密token 获取用户信息
        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey("F:\\LeyouXiangMu\\id_rsa.pub");
        //解密token需要token,公钥,生成的类型
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

        System.out.println(payload.getUserInfo());
    }
}

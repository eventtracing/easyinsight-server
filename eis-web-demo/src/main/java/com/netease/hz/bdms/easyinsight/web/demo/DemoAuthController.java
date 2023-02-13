package com.netease.hz.bdms.easyinsight.web.demo;

import com.netease.hz.bdms.easyinsight.common.constant.DemoConst;
import com.netease.hz.bdms.easyinsight.common.dto.app.AppSimpleDTO;
import com.netease.hz.bdms.easyinsight.common.dto.common.UserDTO;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@Controller
public class DemoAuthController {

    public static final String NAME = "eis-token";

    /**
     * 用户请求登录
     *
     * 重定向向到AAC进行验证，验证完成后会回调
     */
    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }

    /**
     * 用户请求登出
     */
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
    }

    /**
     * AAC登录后回调
     */
    @RequestMapping("/logon")
    public void verify(HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

    /**
     * 清理token
     *
     * @param localToken token
     */
    @ResponseBody
    @GetMapping("/clearToken")
    public void clearToken(HttpServletRequest request, @RequestParam(name = "localToken", required = false) String localToken) {
    }

    @RequestMapping(value = "/check/login")
    @ResponseBody
    public HttpResult checkLogin(HttpServletRequest request) throws Exception {
        CheckLoginResult result = new CheckLoginResult();
        result.setLogon(Boolean.TRUE);
        result.setLocalToken(DemoConst.SYSTEM_USER_TOKEN);
        UserDTO currentUser = DemoConst.SYSTEM_USER_DTO;
        result.setDomainId(DemoConst.DEMO_DOMAIN_ID);
        result.setUser(currentUser);
        return HttpResult.success(result);
    }

    /**
     * 设置cookie
     *
     * @param token  token
     * @param maxAge cookie过期时间
     */
    private void setCookie(HttpServletResponse response, String token, int maxAge, String serverName) {
        Cookie cookie = new Cookie(NAME, token);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    @Data
    private static class CheckLoginResult {

        private Boolean logon;
        private String localToken;
        private UserDTO user;
        private Long domainId;
        private AppSimpleDTO app;
    }

}

package com.netease.hz.bdms.easyinsight.common.constant;

/**
 * description: 响应状态码常量
 *
 * @author: gaoshuangchao
 * @createDate: 2020-05-25
 * @version: 1.1
 */
public class ResponseCodeConstant {

  /**
   * 成功
   */
  public static final int OK = 0;

  /**
   * 服务器错误，空指针、数组越界等非业务代码抛出异常
   */
  public static final int SYSTEM_ERROR = -1;

  /**
   * 非法请求，参数异常、参数格式错误等接口的请求非法性抛出的通用错误
   */
  public static final int BAD_REQUEST = -2;

  /**
   * 无权限
   */
  public static final int NO_PERMISSION = -3;

  /**
   * 用户未登录，且该接口需要登录
   */
  public static final int NO_LOGIN = -4;

  /**
   * 无效参数异常
   */
  public static final int PARAM_INVALID = -5;

  /**
   * 域异常
   */
  public static final int NO_DOMAIN = -6;

  /**
   * 调用的远程接口出现异常
   */
  public static final int REMOTE_API_EXCEPTION = 1;

  /**
   * 域异常
   */
  public static final int DOMAIN_ERROR = 201;
  /**
   * 参数异常
   */
  public static final int PARAM_ERROR = 401;
  /**
   * 事件类型异常
   */
  public static final int EVENT_ERROR = 701;
  /**
   * 终端异常
   */
  public static final int TERMINAL_ERROR = 801;
  /**
   * 参数绑定异常
   */
  public static final int PARAM_BIND_ERROR = 901;

  /**
   * 上传异常
   */
  public static final int UPLOAD_ERROR = 1001;
  /**
   * 对象异常
   */
  public static final int OBJ_ERROR = 1201;
  /**
   * 需求异常
   */
  public static final int REQUIRE_ERROR = 1501;
  /**
   * 实时测试异常
   */
  public static final int REAL_TIME_TEST_ERROR = 1700;
  /**
   * 用户管理异常
   */
  public static final int USER_MANAGEMENT_ERROR = 1800;
}

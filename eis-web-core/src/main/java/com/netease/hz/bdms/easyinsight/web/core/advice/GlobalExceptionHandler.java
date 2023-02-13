package com.netease.hz.bdms.easyinsight.web.core.advice;

import com.netease.hz.bdms.easyinsight.common.constant.GlobalConst;
import com.netease.hz.bdms.easyinsight.common.constant.ResponseCodeConstant;
import com.netease.hz.bdms.easyinsight.common.exception.*;
import com.netease.hz.bdms.easyinsight.common.http.HttpResult;
import com.netease.hz.bdms.easyinsight.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.AccessDeniedException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @Value("${spring.servlet.multipart.max-file-size:5MB}")
  private String maxUploadSize;

  /**
   * 文件上传超过最大限制
   */
  @ExceptionHandler(value = MaxUploadSizeExceededException.class)
  @ResponseBody
  public HttpResult clientAbort(MaxUploadSizeExceededException e) {
    return HttpResult.error(ResponseCodeConstant.BAD_REQUEST, "文件上传超过了最大限制 " + maxUploadSize);
  }

  /**
   * 参数异常处理
   */
  @ExceptionHandler(value = {
          MissingServletRequestParameterException.class,
          MethodArgumentTypeMismatchException.class,
          ConstraintViolationException.class,
          IllegalArgumentException.class,
          MethodArgumentNotValidException.class,
          BindException.class
  })
  @ResponseBody
  public HttpResult paramException(HttpServletRequest request, Exception e) {
    List<String> errors = new LinkedList<>();
    if (e instanceof MissingServletRequestParameterException) {
      MissingServletRequestParameterException missingException = (MissingServletRequestParameterException) e;
      errors.add(missingException.getParameterName() + "不可以为空");
    } else if (e instanceof MethodArgumentTypeMismatchException) {
      MethodArgumentTypeMismatchException mismatchException = (MethodArgumentTypeMismatchException) e;
      errors.add(mismatchException.getName() + "格式不匹配");
    } else if (e instanceof ConstraintViolationException) {
      ConstraintViolationException violationException = (ConstraintViolationException) e;
      for (ConstraintViolation<?> s : violationException.getConstraintViolations()) {
        errors.add(s.getMessage());
      }
    } else if (e instanceof IllegalArgumentException) {
      errors.add(e.getMessage());
    } else if (e instanceof MethodArgumentNotValidException) {
      List<String> list = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors()
              .stream()
              .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
              .collect(Collectors.toList());
      errors.addAll(list);
    } else if (e instanceof BindException) {
      BindException bindException = (BindException) e;
      List<String> errorMessageList = bindException.getFieldErrors()
              .stream()
              .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
              .collect(Collectors.toList());
      errors.addAll(errorMessageList);
    }
    log.warn("Invalid request parameter, requestUri={}, params={}, errors={}",
        request.getRequestURI(), JsonUtils.toJson(request.getParameterMap()),
        JsonUtils.toJson(errors), e);
    return HttpResult.error(ResponseCodeConstant.PARAM_INVALID, JsonUtils.toJson(errors));
  }

  /**
   * 参数异常
   */
  @ExceptionHandler(ParamInvalidException.class)
  @ResponseBody
  public HttpResult paramException(HttpServletRequest request, ParamInvalidException e) {
    log.warn("Invalid parameter, requestUri={}, queryString={}", request.getRequestURI(),
        request.getQueryString(), e);
    return HttpResult.error(e.getCode(), e.getMessage());
  }


  /**
   * 权限异常
   */
  @ExceptionHandler(value = AuthException.class)
  @ResponseBody
  public HttpResult authException(HttpServletRequest request, AuthException e) {
    log.warn("Invalid permission, requestUri={}, queryString={}", request.getRequestURI(),
        request.getQueryString(), e);
    return HttpResult
        .error(ResponseCodeConstant.REMOTE_API_EXCEPTION, e.getMessage());
  }

  /**
   * over-mind异常
   */
  @ExceptionHandler(value = OvermindApiException.class)
  @ResponseBody
  public HttpResult overmindApiException(HttpServletRequest request, OvermindApiException e) {
    log.error("overmind远程调用异常, requestUri={}, queryString={}", request.getRequestURI(),
            request.getQueryString(), e);
    return HttpResult
            .error(ResponseCodeConstant.REMOTE_API_EXCEPTION, e.getMessage());
  }

  /**
   * 其它已知异常
   */
  @ExceptionHandler(value = {
      CommonException.class,
      AccessDeniedException.class,
      ServerException.class,
      DomainException.class,
      ParamBindException.class,
      ParamException.class,
      TerminalException.class,
      ObjException.class,
      ReqirementException.class,
      RealTimeTestException.class,
      UserManagementException.class,
  })
  @ResponseBody
  public HttpResult knownException(HttpServletRequest httpRequest, AbstractCommonException e) {
    log.warn("ignore known exception, requestUri={}, queryString={}", httpRequest.getRequestURI(),
        httpRequest.getQueryString(), e);
    return HttpResult.error(e.getCode(), e.getMessage());
  }


  /**
   * 未知异常
   */
  @ExceptionHandler(Exception.class)
  @ResponseBody
  public HttpResult unknownException(Exception e, HttpServletRequest request) {
    log.error("unknown error, requestUri={}, queryString={}", request.getRequestURI(),
        request.getQueryString(), e);
    return HttpResult.error(ResponseCodeConstant.SYSTEM_ERROR, GlobalConst.DEFAULT_INTERNAL_SERVER_ERROR_MESSAGE + " " + e.getMessage());
  }
}

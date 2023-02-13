package com.netease.hz.bdms.easyinsight.web.core.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * description:
 *
 * @author: gaoshuangchao
 * @createDate: 2020-05-21
 * @version: 1.0
 */
@Slf4j
public class ServletInputStreamFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("StreamFilter初始化...");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    ServletRequest requestWrapper = new RequestWrapper((HttpServletRequest) request);
    chain.doFilter(requestWrapper, response);
  }

  @Override
  public void destroy() {
    log.info("StreamFilter销毁...");
  }

  public static class RequestWrapper extends HttpServletRequestWrapper {

    /**
     * 存储body数据的容器
     */
    private final byte[] body;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestWrapper(HttpServletRequest request) {
      super(request);
      // 将body数据存储起来
      String bodyStr = getBodyString(request);
      body = bodyStr.getBytes(Charset.defaultCharset());
    }

    /**
     * 获取请求Body
     *
     * @param request request
     * @return String
     */
    public String getBodyString(final ServletRequest request) {
      try {
        return inputStream2String(request.getInputStream());
      } catch (IOException e) {
        log.error("Get inputStream failed: ", e);
        throw new RuntimeException(e);
      }
    }

    public String getBodyString() {
      final InputStream inputStream = new ByteArrayInputStream(body);

      return inputStream2String(inputStream);
    }

    /**
     * 将inputStream里的数据读取出来并转换成字符串
     *
     * @param inputStream inputStream
     * @return String
     */
    private String inputStream2String(InputStream inputStream) {
      StringBuilder sb = new StringBuilder();
      BufferedReader reader = null;

      try {
        reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        String line;
        while ((line = reader.readLine()) != null) {
          sb.append(line);
        }
      } catch (IOException e) {
        log.error("Get inputStream toString failed:", e);
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            log.error("Close reader failed: ", e);
          }
        }
      }

      return sb.toString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
      return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

      final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);

      return new ServletInputStream() {
        @Override
        public int read() throws IOException {
          return inputStream.read();
        }

        @Override
        public boolean isFinished() {
          return false;
        }

        @Override
        public boolean isReady() {
          return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
      };
    }
  }

}

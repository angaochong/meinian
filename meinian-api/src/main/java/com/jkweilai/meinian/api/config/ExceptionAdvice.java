package com.jkweilai.meinian.api.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.json.JSONObject;
import com.jkweilai.meinian.api.exception.HisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    /*
     * 捕获异常，并且返回500状态码
     */
    @ResponseBody // 响应JSON字符串到前端
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 状态码500
    @ExceptionHandler(Exception.class) // 除了没登录异常，其它异常都走这个处理器
    public String exceptionHandler(Exception e) {
        JSONObject json = new JSONObject();
        if (e instanceof HttpMessageNotReadableException ex) {
            // 当HTTP请求体（Body）的数据格式错误或无法解析时，就会发生此异常
            log.error("error", ex);
            json.set("error", "请求未提交数据或者数据有误");
        } else if (e instanceof MissingServletRequestPartException ex) {
            // 当请求中缺少预期的文件（@RequestPart）或表单字段时，就会发生此异常
            log.error("error", ex);
            json.set("error", "请求提交数据错误");
        } else if (e instanceof HttpRequestMethodNotSupportedException ex) {
            // 当客户端使用了一个该URL不支持的HTTP方法（如用GET访问只支持POST的接口）时，就会发生此异常。
            log.error("error", ex);
            json.set("error", "HTTP请求方法类型错误");
        } else if (e instanceof BindException ex) {
            // 当请求参数绑定到Java对象（如使用@RequestBody或@ModelAttribute）失败时，就会发生此异常
            String defaultMessage = ex.getFieldError().getDefaultMessage();
            log.error(defaultMessage, ex);
            json.set("error", defaultMessage);
        } else if (e instanceof MethodArgumentNotValidException ex) {
            // 没有通过后端验证产生的异常
            // 当使用 @Valid 注解校验请求体参数失败时，就会发生此异常
            json.set("error", ex.getBindingResult().getFieldError().getDefaultMessage());
        } else if (e instanceof HisException ex) {
            // 处理业务异常
            log.error("执行异常", e);
            json.set("error", ex.getMsg());
        } else {
            // 处理其余的异常
            log.error("执行异常", e);
            json.set("error", "执行异常");
        }
        return String.valueOf(json);
    }

    /*
     * 捕获异常，并且返回401状态码
     */
    @ResponseBody // 响应JSON给前端
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 状态码：401
    @ExceptionHandler(NotLoginException.class) // 当Sa-Token检测到用户没有登录时就会抛出这个异常
    public String unLoginHandler(Exception e) {
        JSONObject json = new JSONObject();
        json.set("error", e.getMessage());
        return String.valueOf(json);
    }
}

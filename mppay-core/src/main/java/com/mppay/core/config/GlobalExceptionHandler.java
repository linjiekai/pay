package com.mppay.core.config;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.mppay.core.exception.CheckParamsException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.mppay.core.exception.BusiException;
import com.mppay.core.utils.ApplicationYmlUtil;
import com.mppay.core.utils.ResponseUtil;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Object argumentHandler(MethodArgumentTypeMismatchException e){
        e.printStackTrace();
        log.error("捕获异常(MethodArgumentTypeMismatchException.class):{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public Object argumentHandler(MissingServletRequestParameterException e){
        e.printStackTrace();
        log.error("捕获异常(MissingServletRequestParameterException.class):{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public Object httpMessageNotReadableHandler(HttpMessageNotReadableException e){
        e.printStackTrace();
        log.error("捕获异常(HttpMessageNotReadableException.class):{}", e);
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public Object handle(ValidationException e) {
        e.printStackTrace();
        log.error("捕获异常(ValidationException.class):{}", e);
        if(e instanceof ConstraintViolationException){
            ConstraintViolationException exs = (ConstraintViolationException) e;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            for (ConstraintViolation<?> item : violations) {
                String message = ((PathImpl)item.getPropertyPath()).getLeafNode().getName() +item.getMessage();
                return ResponseUtil.fail("402", message);
            }
        }
        return ResponseUtil.badArgumentValue();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(Exception e){
        e.printStackTrace();
        log.error("捕获异常(Exception.class):{}", e);
        return ResponseUtil.serious();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Object httpRequestMethodHandler(HttpRequestMethodNotSupportedException e){
        e.printStackTrace();
        log.error("捕获异常(HttpRequestMethodNotSupportedException.class):{}", e);
        return ResponseUtil.fail("405", "该方法仅支持 " + e.getSupportedHttpMethods() + " 请求方式");
    }

    @ExceptionHandler(BusiException.class)
    @ResponseBody
    public Object busiExceptionHandler(BusiException e){
        log.error("捕获异常(BusiException.class):{}", e);
        
        String msg = e.getMsg();
        if (StringUtils.isBlank(msg)) {
        	msg = ApplicationYmlUtil.get(e.getCode());
        }
        
        return ResponseUtil.fail(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(CheckParamsException.class)
    @ResponseBody
    public Object checkParamsExceptionHandler(CheckParamsException e){
        log.error("捕获异常(CheckParamsException.class):{}", e);
        return ResponseUtil.fail("11111", e.getMsg());
    }
}

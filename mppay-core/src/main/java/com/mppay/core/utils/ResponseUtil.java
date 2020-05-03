package com.mppay.core.utils;

import org.springframework.validation.BindingResult;

import com.mppay.core.constant.ConstEC;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseUtil {

    private static Map<String, Object> map = new HashMap<>();// APP要求返回一个空的JSON

    public static Object ok() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", ConstEC.SUCCESS_10000);
        obj.put("msg", ApplicationYmlUtil.get(ConstEC.SUCCESS_10000));
        obj.put("data", map);
        return obj;
    }

    public static Object ok(Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", ConstEC.SUCCESS_10000);
        obj.put("msg", ApplicationYmlUtil.get(ConstEC.SUCCESS_10000));
        obj.put("data", data);
        return obj;
    }

    public static Object ok(String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", ConstEC.SUCCESS_10000);
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object ok(String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", ConstEC.SUCCESS_10000);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(Integer key) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", key.toString());
        obj.put("msg", ApplicationYmlUtil.get(key));
        return obj;
    }

    public static Object fail() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", "-1");
        obj.put("msg", "错误");
        return obj;
    }

    public static Object fail(Integer code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code.toString());
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(String code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }
    
    public static Object fail(Integer code, String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code.toString());
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object result(Integer code, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code.toString());
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data", data == null ? map: data);
        return obj;
    }

    public static Object result(Integer code) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code.toString());
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data",map);
        return obj;
    }

    public static Object badArgument(){
        return fail(401, ApplicationYmlUtil.get(401));
    }


    public static Object badArgumentValue(){
        return fail(402, ApplicationYmlUtil.get(402));
    }

    public static Object unlogin(){
        return fail(501, ApplicationYmlUtil.get(501));
    }

    public static Object serious(){
        return fail(502, ApplicationYmlUtil.get(502));
    }

    public static Object unsupport(){
        return fail(503, ApplicationYmlUtil.get(503));
    }

    public static Object badValidate(BindingResult bindingResult){
        return fail(403, bindingResult.getFieldErrors().stream().map(x -> x.getDefaultMessage()).collect(Collectors.joining(";")));
    }

    public static Object badBean(){
        return fail(404, ApplicationYmlUtil.get(404));
    }

    /**
     * "禁止操作他人数据
     * @return
     */
    public static Object forbid(){
        return fail(11112,ApplicationYmlUtil.get(11112));
    }
}


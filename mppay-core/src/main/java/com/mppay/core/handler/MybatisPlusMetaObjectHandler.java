package com.mppay.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 有属性addTime和updateTime
 * 当属性字段addTime加了注解：@TableField(fill = FieldFill.INSERT)，说明添加bean到数据库时这个属性默认值是当前时间，如果手动设置了addTime,那么就按手动的为主
 * 当属性字段updateTime加了注解：@TableField(fill = FieldFill.UPDATE)，说明更新bean到数据库时这个属性默认值是当前时间,如果手动设置了updateTime,那么就按手动的为主
 */
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        final Object addTime = getFieldValByName("addTime", metaObject);
        if (addTime == null) {// 没有设置时间就默认设置当前时间
            this.setFieldValByName("addTime", LocalDateTime.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {// 同上
        final Object addTime = getFieldValByName("updateTime", metaObject);
        if (addTime == null) {// 没有设置时间就默认设置当前时间
            this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        }
    }
}
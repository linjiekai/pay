package com.mppay.core.handler.type;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 当Entity中有String[]的属性时，把其他转成["xx","yy"...]格式存数据库;从数据库获取时转成String[]
 * 在配置文件中：type-handlers-package: com.xfhl.meeting.handler.type
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(value = String[].class)
public class ArrayStringTypeHandler extends BaseTypeHandler<String[]> {

    /**
     * 把String[]转成[xx,yy...]
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    /**
     * 处理的类型是String,把[xx,yy...]转成String[]
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        if (StringUtils.isBlank(result)) {
            return new String[0];
        }
        return JSON.parseObject(result, String[].class);
    }

    @Override
    public String[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        if (StringUtils.isBlank(result)) {
            return new String[0];
        }
        return JSON.parseObject(result, String[].class);
    }

    @Override
    public String[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        if (StringUtils.isBlank(result)) {
            return new String[0];
        }
        return JSON.parseObject(result, String[].class);
    }
}

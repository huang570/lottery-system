package com.example.lotterysystem.dao.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@MappedTypes(Encrypt.class) // 被处理的类型
@MappedJdbcTypes(JdbcType.VARCHAR) // 转换后的 JDBC 类型
public class EncryptTypeHandler extends BaseTypeHandler<Encrypt> {
    private final byte[] key = "123456789abcdefg".getBytes(StandardCharsets.UTF_8);

    /**
     * 设置参数
     * @param ps SQL 预编译对象
     * @param i 指的是需要赋值的索引位置
     * @param parameter 原本位置 i 需要赋的值
     * @param jdbcType JDBC 类型
     * @throws SQLException
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Encrypt parameter, JdbcType jdbcType) throws SQLException {
        if(parameter == null || parameter.getValue() == null){
            ps.setString(i,"");
            return;
        }

        log.info("加密内容: {}",parameter.getValue());

        // 加密
        AES aes = SecureUtil.aes(key);
        String str = aes.encryptHex(parameter.getValue());
        ps.setString(i,str);
    }

    /**
     * 获取值
     * @param rs            结果集
     * @param columnName    索引名
     * @return
     * @throws SQLException
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, String columnName) throws SQLException {
        //log.info("获取值得到的加密内容: {}",rs.getString(columnName));
        return decrypt(rs.getString(columnName));
    }

    /**
     * 获取值
     * @param rs            结果集
     * @param columnIndex   索引
     * @return
     * @throws SQLException
     */
    @Override
    public Encrypt getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        //log.info("获取值得到的加密内容: {}",rs.getString(columnIndex));
        return decrypt(rs.getString(columnIndex));
    }

    /**
     * 获取值
     *
     * @param cs            结果集
     * @param columnIndex   索引
     * @return
     * @throws SQLException
     */
    @Override
    public Encrypt getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        //log.info("获取值得到的加密内容: {}",cs.getString(columnIndex));
        return decrypt(cs.getString(columnIndex));
    }

    /**
     * 解密
     * @param str
     * @return
     */
    private Encrypt decrypt(String str){
        if(!StringUtils.hasLength(str)){
            return null;
        }
        return new Encrypt(SecureUtil.aes(key).decryptStr(str));
    }
}

package com.example.lotterysystem.common.exception;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException{
    /**
     * 异常码
     * @see com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants
     */
    private Integer code;

    /**
     * 异常消息
     */
    private String massage;

    public ServiceException(){
    }

    public ServiceException(Integer code,String massage){
        this.code = code;
        this.massage = massage;
    }

    public ServiceException(ErrorCode errorCode){
        this.code = errorCode.getCode();
        this.massage = errorCode.getMsg();
    }

}

package com.example.lotterysystem.common.exception;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

// @Data 会生成自己的equals方法和hashcode方法，但不会包括父类属性，可能会出现问题
// 因此 加上@EqualsAndHashCode(callSuper = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ControllerException extends RuntimeException{
    /**
     * 异常码
     * @see com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants
     */
    private Integer code;

    /**
     * 异常消息
     */
    private String massage;

    public ControllerException(){
    }

    public ControllerException(Integer code,String massage){
        this.code = code;
        this.massage = massage;
    }

    public ControllerException(ErrorCode errorCode){
        this.code = errorCode.getCode();
        this.massage = errorCode.getMsg();
    }
}

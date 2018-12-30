package cn.openread.enums;

import lombok.ToString;

/**
 * 错误枚举类
 *
 * @author Simon
 */
@ToString
public enum ErrorEnum implements IError500Enum<ErrorEnum> {
    DEV_REPEAT("设备重复接入异常", 500000),
    MISS_PARAMS_DEV_ID("请求缺少参数 devId", 500001);

    private String reason;
    private Integer code;

    ErrorEnum(String reason, int code) {
        this.reason = reason;
        this.code = code;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}

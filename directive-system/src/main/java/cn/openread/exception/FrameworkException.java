package cn.openread.exception;

import cn.openread.enums.IErrorEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 设备重复接入异常
 *
 * @author Simon
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class FrameworkException extends Exception {
    protected IErrorEnum errorEnum;
    protected String remark;

    protected FrameworkException(IErrorEnum errorEnum) {
        this.errorEnum = errorEnum;
    }

    public String toString() {
        return String.format("%s {errorEnum => %s,remark => %s}", this.getClass().getSimpleName(), this.errorEnum, this.remark);
    }
}

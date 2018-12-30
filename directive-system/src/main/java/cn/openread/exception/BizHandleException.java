package cn.openread.exception;

import cn.openread.enums.IErrorEnum;

/**
 * 业务处理接入异常
 *
 * @author Simon
 */
public class BizHandleException extends FrameworkException {

    public BizHandleException(IErrorEnum errorEnum) {
        super(errorEnum);
    }

    public BizHandleException(IErrorEnum errorEnum, String remark) {
        super(errorEnum, remark);
    }
}

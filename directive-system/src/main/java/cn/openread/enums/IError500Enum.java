package cn.openread.enums;

/**
 * 500类请求处理
 *
 * @author Simon
 */
public interface IError500Enum<T extends Enum<T>> extends IErrorEnum<T> {
    default Integer configCode() {
        return 500;
    }

    default String configReason() {
        return "[Request Error] - 本次请求发生错误,详情请查阅服务器日志";
    }
}
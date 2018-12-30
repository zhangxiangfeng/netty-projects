package cn.openread.enums;

/**
 * 定义基本异常原型属性
 *
 * @author Simon
 */
public interface IErrorEnum<T extends Enum<T>> extends ICodeEnum<T, Integer> {
    String configReason();

    Integer configCode();

    default String getReason() {
        return this.configReason();
    }

    default Integer getCode() {
        return this.configCode();
    }

    default T codeOf(Integer code) {
        return null;
    }
}

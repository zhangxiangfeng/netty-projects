package cn.openread.enums;

public interface ICodeEnum<T extends Enum<T>, C> {
    static ICodeEnum codeOf(Enum instance, Object code) {
        ICodeEnum sub = (ICodeEnum) instance;
        return (ICodeEnum) sub.codeOf(code);
    }

    C getCode();

    T codeOf(C var1);
}
package cn.openread.kits;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Channel 通道的工具类
 *
 * @author Simon
 */
public class ChannelAttrKits {

    public static void setAttr(Channel channel, String name) {
        Attribute<String> nameAttr = channel.attr(AttributeKey.valueOf(name));
        nameAttr.set(name);
    }

    public static String getAttr(Channel channel, String name) {
        AttributeKey<String> nameAttrKey = AttributeKey.valueOf(name);
        Attribute<String> attr = channel.attr(nameAttrKey);
        return attr == null ? null : attr.get();
    }


}

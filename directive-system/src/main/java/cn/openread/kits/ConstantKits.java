package cn.openread.kits;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 常量工具类
 *
 * @author Simon
 */
public class ConstantKits {

    /**
     * 存储所有的Channel
     */
    public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 设备ID
     */
    public static final String DEV_ID = "devId";
}

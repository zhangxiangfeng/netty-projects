package cn.openread.kits;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelMatcher;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据name属性匹配Channel
 *
 * @author Simon
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MatcherChannelKits implements ChannelMatcher {
    private String name;
    private String value;

    /**
     * 通过matcher找channel list
     *
     * @param matcher 需要匹配
     * @return Channel数组
     */
    public static List<Channel> getChannelsFromMatcher(ChannelMatcher matcher) {
        Object[] channels = ConstantKits.channels.toArray();
        List<Channel> list = new ArrayList<>();
        Channel channel;
        for (Object ch : channels) {
            if (matcher.matches((Channel) ch)) {
                channel = (Channel) ch;
                list.add(channel);
            }
        }
        return list;
    }

    public static synchronized Channel getChannelByNameAndValue(String name, String value) {
        MatcherChannelKits matcher = new MatcherChannelKits(name, value);
        List<Channel> list = getChannelsFromMatcher(matcher);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public boolean matches(Channel channel) {
        String value = ChannelAttrKits.getAttr(channel, name);
        return StringUtils.equals(this.value, value);
    }
}

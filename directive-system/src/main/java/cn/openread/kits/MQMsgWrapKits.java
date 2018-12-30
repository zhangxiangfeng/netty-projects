package cn.openread.kits;

import cn.openread.dto.MQMsgDTO;
import com.alibaba.fastjson.JSON;

/**
 * MQ消息包装类
 *
 * @author Simon
 */
public class MQMsgWrapKits {

    /**
     * 解码
     */
    public static MQMsgDTO decodeMsg(String msg) {
        return JSON.parseObject(msg, MQMsgDTO.class);
    }


    /**
     * 编码
     */
    public static String encodeMsg(MQMsgDTO mqMsgDTO) {
        return JSON.toJSONString(mqMsgDTO);
    }
}

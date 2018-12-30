package cn.openread.handler;

import cn.openread.enums.ErrorEnum;
import cn.openread.exception.BizHandleException;
import cn.openread.kits.ChannelAttrKits;
import cn.openread.kits.ConstantKits;
import cn.openread.kits.HttpKits;
import cn.openread.kits.MatcherChannelKits;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.Map;

/**
 * 处理 Http 请求
 *
 * @author Simon
 */
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> { //1
    private static File INDEX;

    static {
        try {
            String staticFileName = "barrage.html";
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            InputStream inputStream = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + staticFileName).getInputStream();

            String tmpPath = System.getProperty("java.io.tmpdir");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            File targetFile = new File(tmpPath + File.separator + staticFileName);
            if (targetFile.exists())
                targetFile.delete();
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            INDEX = targetFile;
            log.info("静态文件路径 => {}", INDEX.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private final String wsUri;

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        if (request.uri().startsWith(wsUri)) {
            Map<String, String> stringMap = HttpKits.parseURI(request.uri());
            for (String key : stringMap.keySet()) {
                log.debug(" 键值对 => {} - {}", key, stringMap.get(key));
            }

            //step 1.基本参数检查
            final String devId = stringMap.get(ConstantKits.DEV_ID);
            if (StringUtils.isBlank(devId)) {
                ctx.fireExceptionCaught(new BizHandleException(ErrorEnum.MISS_PARAMS_DEV_ID));
            }

            //step 2.检测设备重复接入
            Channel channel = MatcherChannelKits.getChannelByNameAndValue(ConstantKits.DEV_ID, devId);
            if (channel != null) {
                ctx.fireExceptionCaught(new BizHandleException(ErrorEnum.DEV_REPEAT, "设备重复请求长连接 => " + channel.remoteAddress()));
            }

            //step 3.设置设备属性
            ChannelAttrKits.setAttr(ctx.channel(), ConstantKits.DEV_ID, devId);

            ctx.fireChannelRead(request.retain());
        } else {
            if (HttpUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            RandomAccessFile file = new RandomAccessFile(INDEX, "r");

            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

            boolean keepAlive = HttpUtil.isKeepAlive(request);

            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            ctx.write(response);

            if (ctx.pipeline().get(SslHandler.class) == null) {
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }

            file.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel incoming = ctx.channel();
        System.out.println("客户端 => " + incoming.remoteAddress() + " 异常");
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}

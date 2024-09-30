import com.sik.siknet.netty.DefaultDecoder
import com.sik.siknet.netty.DefaultEncoder
import com.sik.siknet.netty.DefaultProcessHandler
import com.sik.siknet.netty.DefaultSenderHandler
import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.MessageToMessageDecoder

/**
 * Netty配置类
 */
abstract class NettyConfig<T> {
    /**
     * 主机地址
     */
    open val host: String = "127.0.0.1"

    /**
     * 端口
     */
    open val port: Int = 80

    /**
     * Channel处理器
     */
    open val initChannel: (Channel) -> Unit = {}

    /**
     * 解码器
     */
    open val decoder: MessageToMessageDecoder<T>? = DefaultDecoder()

    /**
     * 编码器
     */
    open val encoder: MessageToByteEncoder<T>? = DefaultEncoder()

    /**
     * 发送者
     */
    open val sender: ChannelInboundHandler = DefaultSenderHandler()

    /**
     * 处理器
     */
    open val process: SimpleChannelInboundHandler<T>? = DefaultProcessHandler()

    /**
     * 连接成功
     */
    open val connectSuccess: () -> Unit = {}

    /**
     * 重试次数
     * -1为一直重试
     */
    open val retryTimes: Int = 0

    /**
     * 重试时间间隔，毫秒
     */
    open val retryTime: Long = 10 * 1000L
}
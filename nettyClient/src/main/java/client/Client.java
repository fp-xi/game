package client;

import handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class Client {

    private static final Charset UTF_8 = Charset.forName("utf-8");

    private ClientHandler clientHandler = new ClientHandler();

    private Bootstrap bootstrap;

    private ChannelFuture future;

    private boolean init = false;

    private boolean isClosed = false;

    public void start() {
        if(init) {
            throw new RuntimeException("client is already started");
        }
        //thread model: one worker thread pool,contains selector thread and workers‘.
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);//1 is OK
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class) //create SocketChannel transport
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(10240, 0, 2, 0, 2))
                                    .addLast(new StringDecoder(UTF_8))
                                    .addLast(new LengthFieldPrepender(2))
                                    .addLast(new StringEncoder(UTF_8))
                                    .addLast(clientHandler);//the same as ServerBootstrap
                        }
                    });
            //keep the connection with server，and blocking until closed!
            future = bootstrap.connect(new InetSocketAddress("121.4.70.234", 18080)).sync();
            //future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 18080)).sync();
            init = true;
        } catch (Exception e) {
            isClosed = true;
        } finally {
            if(isClosed) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public void close() {
        if(isClosed) {
            return;
        }
        try {
            future.channel().close();
        } finally {
            bootstrap.group().shutdownGracefully();
        }
        isClosed = true;
    }

    /**
     * 发送消息
     * @param message
     * @return
     * @throws Exception
     */
    public String send(String message) throws Exception {
        if(isClosed || !init) {
            throw new RuntimeException("client has been closed!");
        }
        //send a request call,and blocking until recevie a response from server.
        return clientHandler.call(message,future.channel());
    }
}

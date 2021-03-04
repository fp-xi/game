package server;

import handler.ServerHandler;
import handler.TestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

public class Server {

    private static final Charset UTF_8 = Charset.forName("utf-8");

    private ChannelFuture future;

    private boolean isClosed = false;

    private boolean init = false;

    private ServerBootstrap bootstrap;

    public void start() {

        //thread model：one selector thread，and one worker thread pool。
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//more than 1 is not needed!
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        try {
            bootstrap = new ServerBootstrap();//create ServerSocket transport。
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(10240, 0, 2, 0, 2))
                                    .addLast(new StringDecoder(UTF_8))
                                    .addLast(new LengthFieldPrepender(2))
                                    .addLast(new StringEncoder(UTF_8))
                                    .addLast(new ServerHandler())
                                    .addLast(new TestHandler());
                        }
                    }).childOption(ChannelOption.TCP_NODELAY, true);
            future = bootstrap.bind(18080).sync();
            init = true;
            //
            System.out.println("server started");
        } catch (Exception e) {
            isClosed = true;
        } finally {
            if(isClosed) {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
                System.out.println("server closed");
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
            bootstrap.childGroup().shutdownGracefully();
            bootstrap.group().shutdownGracefully();
        }
        isClosed = true;
        System.out.println("server closed");
    }


}

package codehumane.nettyecho;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {

    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
        }

        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        final EchoServerHandler serverHandler = new EchoServerHandler();

        // Creates the EventLoopGroup
        final NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            // Creates the ServerBootstrap
            final ServerBootstrap b = new ServerBootstrap();

            b.group(group)
                    // Specifies the use of an NIO transport Channel
                    .channel(NioServerSocketChannel.class)
                    // Sets the socket address using the specified port
                    .localAddress(new InetSocketAddress(port))
                    // Adds an EchoServerHandler to the Channel's ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // EchoServerHandler is @Sharable so we can always use the same one
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast(serverHandler);
                        }
                    });

            // Binds the server asynchronously;
            // sync() waits for the bind to complete
            final ChannelFuture f = b.bind().sync();
            // Gets the CloseFuture of the Channel and blocks the current thread until it's complete
            f.channel().closeFuture().sync();
        } finally {
            // Shuts down the EventLoopGroup, releasing all resources
            group.shutdownGracefully().sync();
        }
    }
}

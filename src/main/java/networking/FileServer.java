package networking;

import files.FileHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import message.Message;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Paths;

/**
 * Handles a server-side channel.
 */
public class FileServer {

    private int port;
    private boolean debug;
    private String usersPath;

    public FileServer(int port, boolean debug, String usersPath) {
        this.port = port;
        this.debug = debug;
        this.usersPath = usersPath;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final FileHandler fileHandler = new FileHandler(usersPath);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new FileServerInitializer(debug, fileHandler))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        boolean debug = true;
        String usersPaths = FileSystems.getDefault().getPath("users").toString();
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new FileServer(port, debug, usersPaths).run();
        /*String path1 = FileSystems.getDefault().getPath("test").toString();
        //Path path = FileSystems.getDefault().getPath(Paths.get(path1),"uses.conf");
        Path path = Paths.get(path1, "users.conf");
        System.out.println(path.toAbsolutePath().toString());
        //BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        //System.out.println(reader.readLine());*/
    }
}
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
import message.MsgReply;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import static sun.nio.ch.IOStatus.EOF;

/**
 * Handles a server-side channel.
 */
public class FileServer {

    private int port;
    private boolean debug;
    private String usersPath;
    private int queueSize;
    private int partSize;

    public FileServer(int port, int queueSize, int partSize, String usersPath, boolean debug) {
        this.port = port;
        this.queueSize = queueSize;
        this.partSize = partSize;
        this.usersPath = usersPath;
        this.debug = debug;
    }

    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new FileServerInitializer(usersPath, queueSize, partSize, debug))
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
        int queueSize = 256;
        int partSize = (int) (0.5 * 1024 * 1024);
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new FileServer(port, queueSize, partSize, usersPaths, debug).run();
        //String path1 = FileSystems.getDefault().getPath("test").toString();
        //Path path = FileSystems.getDefault().getPath(Paths.get(path1),"uses.conf");
        /*Path path = Paths.get("users.conf");
        System.out.println(path.toAbsolutePath().toString());
        RandomAccessFile file = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
        path = Paths.get("users2.conf");
        RandomAccessFile file2 = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");
        System.out.println(file.length());
        byte[] buff = new byte[4];
        int n;
        while((n = file.read(buff)) != -1) {
            byte[] slice = Arrays.copyOfRange(buff, 0, n);
            System.out.print(new String(slice, Charset.forName("UTF-8")));
            file2.write(slice);
        }
        buff = "abc".getBytes();
        file2.write(buff);
        file.close();
        file2.close();*/
    }
}
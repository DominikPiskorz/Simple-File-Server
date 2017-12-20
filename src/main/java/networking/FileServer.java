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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;


/**
 * Main class initiating threads and loading settings.
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
        // Netty server initiation. Start a group of threads for connections
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

    // Main function
    public static void main(String[] args) throws Exception {
        // Read settings from file
        Map<String, String> settings = readSettings();

        int port = Integer.parseInt(settings.get("port"));
        int queueSize = Integer.parseInt(settings.get("queueSize"));
        int partSize = Integer.parseInt(settings.get("partSize"));
        String usersPaths = settings.get("usersPaths");
        boolean debug = Boolean.parseBoolean(settings.get("debug"));

        // Start server
        new FileServer(port, queueSize, partSize, usersPaths, debug).run();
    }

    /**
     * Read settings from settings.conf
     * @return A map with settings and their values
     */
    private static Map<String, String> readSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("settings.conf"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String parts[] = line.split(";");
                settings.put(parts[0], parts[1]);
            }
            br.close();
            return settings;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
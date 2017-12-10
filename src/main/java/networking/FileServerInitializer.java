package networking;

import files.FileHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import message.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileServerInitializer extends ChannelInitializer<SocketChannel> {
    private boolean debug;
    private String usersPath;
    private int queueSize;
    private int partSize;

    public FileServerInitializer(String usersPath, int queueSize, int partSize, boolean debug){
        this.usersPath = usersPath;
        this.queueSize = queueSize;
        this.partSize = partSize;
        this.debug = debug;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        BlockingQueue<Message> inQueue = new ArrayBlockingQueue<Message>(queueSize);
        BlockingQueue<Message> outQueue = new ArrayBlockingQueue<Message>(queueSize);
        FileHandler fileHandler = new FileHandler(usersPath, inQueue, outQueue, partSize);
        Thread fileHandlerThread = new Thread(fileHandler);
        fileHandlerThread.start();

        if (debug)
            ch.pipeline().addLast(new LoggingHandler());
        ch.pipeline().addLast(new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(Message.class.getClassLoader())),
                new FileServerHandler(inQueue, outQueue, partSize));
    }
}

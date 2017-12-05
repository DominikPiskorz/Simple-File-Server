package networking;

import files.FileHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import message.Message;

public class FileServerInitializer extends ChannelInitializer<SocketChannel> {
    private boolean debug;
    FileHandler fileHandler;

    public FileServerInitializer(boolean debug, FileHandler fileHandler){
        this.debug = debug;
        this.fileHandler = fileHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        if (debug)
            ch.pipeline().addLast(new LoggingHandler());
        ch.pipeline().addLast(new ObjectEncoder(),
                new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(Message.class.getClassLoader())),
                new FileServerHandler(fileHandler));
    }
}

package networking;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

public class LoginHandler extends SimpleChannelInboundHandler<Message> {
    private BlockingQueue<Message> inQueue;
    private BlockingQueue<Message> outQueue;
    private int partSize;


    public LoginHandler(BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue, int partSize) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.partSize = partSize;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg){
        if (msg.getType() != Message.Type.LOGIN) {
            ctx.writeAndFlush(new MsgError("Wrong message type"));
            return;
        }

        MsgLogin msglog = (MsgLogin) msg;
        try (BufferedReader br = Files.newBufferedReader(Paths.get("users.conf"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (msglog.getUsername().equals(parts[0]) && msglog.getPassword().equals(parts[1])) {
                    System.out.println("Zalogowano: " + msglog.getUsername());
                    ctx.writeAndFlush(new MsgOk());

                    ctx.pipeline().addLast(new FileServerHandler(inQueue, outQueue, msglog.getUsername(), partSize));
                    ctx.pipeline().remove(this);
                    return;
                }
            }

            ctx.writeAndFlush(new MsgError("Wrong login or password"));
        } catch (IOException e) {
            e.printStackTrace();
            ctx.writeAndFlush(new MsgError("Error opening users file."));
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Polaczenie");
        ChannelFuture f = ctx.writeAndFlush(new MsgSettings(partSize));
        //f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised.
        System.out.println("Zakonczono polaczenie");
        ctx.close();
    }

}

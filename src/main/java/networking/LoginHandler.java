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

/**
 * A handler for unlogged connection. After users logs in it is swapped for regular handler.
 */
public class LoginHandler extends SimpleChannelInboundHandler<Message> {
    private BlockingQueue<Message> inQueue;
    private BlockingQueue<Message> outQueue;
    private int partSize;


    public LoginHandler(BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue, int partSize) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.partSize = partSize;
    }


    /**
     * Netty method called each time a new message arrives.
     * Accept only Login-type messages, check credentials, login and swap handlers.
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg){
        if (msg.getType() != Message.Type.LOGIN) {
            ctx.writeAndFlush(new MsgError("Wrong message type"));
            return;
        }

        MsgLogin msglog = (MsgLogin) msg;
        try (BufferedReader br = Files.newBufferedReader(Paths.get("users.conf"))) {
            // Go through users.conf and compare credentials
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (msglog.getUsername().equals(parts[0]) && msglog.getPassword().equals(parts[1])) {
                    System.out.println("Logged in: " + msglog.getUsername());
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

    /**
     * On new connection print info and send partSize setting to user
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Connection");
        ChannelFuture f = ctx.writeAndFlush(new MsgSettings(partSize));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised.
        System.out.println("Disconnected.");
        ctx.close();
    }

}

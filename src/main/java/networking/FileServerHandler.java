package networking;

import io.netty.channel.*;
import message.*;

import java.util.concurrent.BlockingQueue;

/**
 * Handles a server-side channel.
 */
//public class FileServerHandler extends ChannelInboundHandlerAdapter{
public class FileServerHandler extends SimpleChannelInboundHandler<Message>{
    private BlockingQueue<Message> inQueue;
    private BlockingQueue<Message> outQueue;
    private String user;
    private int partSize;
    private enum State {
        IDLE, DWN, UPL
    }
    private State state = State.IDLE;
    private int fileParts;


    public FileServerHandler(BlockingQueue<Message> inQueue, BlockingQueue<Message> outQueue, String user, int partSize) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.user = user;
        this.partSize = partSize;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg){
        System.out.println("Otrzymano:");
        System.out.println(msg.toString());
        Message reply;
        if ((reply = processMessage(ctx, msg)) != null)
            ctx.writeAndFlush(reply);

        //ctx.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    private Message processMessage(ChannelHandlerContext ctx, Message msg) {
        try {
            switch (msg.getType()) {
                case PING:
                    MsgReply reply = new MsgReply();
                    return reply;
                case ADDFILE:
                    if (!((MsgAddFile) msg).getUser().equals(user))
                        return new MsgError("Wrong user.");

                    state = State.DWN;
                    fileParts = (int) (((MsgAddFile) msg).getFileSize() + partSize) / partSize;
                    inQueue.put(msg);
                    return null;
                case CHUNK:
                    if (state != State.DWN)
                        //throw new IllegalStateException("Nie pobieram pliku.");
                        return null;
                    inQueue.put(msg);
                    if (((MsgFileChunk) msg).getPart() == fileParts)
                        return outQueue.take();
                    return null;
                case GETFILE:
                    if (!((MsgGetFile) msg).getUser().equals(user))
                        return new MsgError("Wrong user.");

                    state = State.UPL;
                    sendFile(ctx, (MsgGetFile) msg);
                    return new MsgOk();
                /*case CHUNK:
                case LIST:
                case REPLY:
                case LOGIN:*/
                default:
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendFile(ChannelHandlerContext ctx, MsgGetFile msg) {
        try {
            inQueue.put(msg);
            Message out;
            do {
                out = outQueue.take();
                ctx.writeAndFlush(out);
            } while (out instanceof MsgFileChunk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
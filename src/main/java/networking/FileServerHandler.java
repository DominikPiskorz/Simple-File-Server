package networking;

import files.FileHandler;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import message.*;

/**
 * Handles a server-side channel.
 */
//public class FileServerHandler extends ChannelInboundHandlerAdapter{
public class FileServerHandler extends SimpleChannelInboundHandler<Message>{

    private FileHandler fileHandler;


    public FileServerHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg){
        System.out.println("Otrzymano:");
        System.out.println(msg.toString());
        processMessage(ctx, msg);
        //ctx.close();

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Polaczenie");
        ChannelFuture f = ctx.writeAndFlush(new MsgPing());
        //f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    private Message processMessage(ChannelHandlerContext ctx, Message msg) {
        switch (msg.getType()) {
            case PING:
                MsgReply reply = new MsgReply();
                return reply;
            case ADDFILE:
                fileHandler.addFile((MsgAddFile) msg);
                return null;
            case SENDFILE:
                sendFile(ctx, (MsgSendFile) msg);
            /*case CHUNK:
            case LIST:
            case REPLY:
            case LOGIN:*/
            default:
                return null;
        }
    }

    private void sendFile(ChannelHandlerContext ctx, MsgSendFile msg) {
        byte[] buff;
        //for(int n = 0; buff = fileHandler.filePart(msg.getPath(), n); n++);
        int n = 0;
        while(true){
            buff = fileHandler.filePart(msg.getPath(), n);
            if (buff == null)
                break;
            ctx.writeAndFlush(new MsgFileChunk(buff, n));
            n++;
        }
    }
}
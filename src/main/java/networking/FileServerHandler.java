package networking;

import files.FileHandler;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import message.Message;
import message.MsgAddFile;
import message.MsgPing;
import message.MsgReply;

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
        processMessage(msg);
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

    private Message processMessage(Message msg) {
        switch (msg.getType()) {
            case PING:
                MsgReply reply = new MsgReply();
                return reply;
            case ADDFILE:
                fileHandler.addFile((MsgAddFile) msg);
                return null;
            /*case CHUNK:
            case LIST:
            case REPLY:
            case LOGIN:*/
            default:
                return null;
        }
    }
}
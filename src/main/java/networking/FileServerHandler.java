package networking;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import utils.UnixTime;

/**
 * Handles a server-side channel.
 */
public class FileServerHandler extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        UnixTime m = (UnixTime) msg;
        System.out.println("Otrzymano:");
        System.out.println(m);
        ctx.close();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Polaczenie");
        ChannelFuture f = ctx.writeAndFlush(new UnixTime());
        //f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
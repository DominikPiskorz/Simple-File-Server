package networking;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import utils.UnixTime;

import java.util.List;

public class FileServerDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(
            ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        System.out.println("decoder");
        out.add(new UnixTime(in.readUnsignedInt()));
    }
}
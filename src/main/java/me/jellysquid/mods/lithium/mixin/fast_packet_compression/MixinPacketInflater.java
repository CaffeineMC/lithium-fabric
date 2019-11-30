//package me.jellysquid.mods.lithium.mixin.network;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.DecoderException;
//import net.jpountz.lz4.LZ4Factory;
//import net.jpountz.lz4.LZ4FastDecompressor;
//import net.minecraft.network.PacketInflater;
//import net.minecraft.util.PacketByteBuf;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import java.util.List;
//
//@Mixin(PacketInflater.class)
//public abstract class MixinPacketInflater {
//    private static final int MAX_COMPRESSED_SIZE = 2097152;
//
//    private LZ4FastDecompressor decoder;
//
//    @Shadow
//    private int minCompressedSize;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onConstructed(int thresholdIn, CallbackInfo ci) {
//        this.decoder = LZ4Factory.fastestInstance().fastDecompressor();
//    }
//
//    /**
//     * @author JellySquid
//     * @reason Changes implementation to use faster LZ4 compression
//     */
//    @Overwrite
//    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
//        if (in.readableBytes() != 0) {
//            PacketByteBuf pbuf = new PacketByteBuf(in);
//
//            int length = pbuf.readVarInt();
//
//            if (length == 0) {
//                out.add(pbuf.readBytes(pbuf.readableBytes()));
//            } else {
//                if (length < this.minCompressedSize) {
//                    throw new DecoderException("Badly compressed packet - size of " + length + " is below server threshold of " + this.minCompressedSize);
//                }
//
//                if (length > MAX_COMPRESSED_SIZE) {
//                    throw new DecoderException("Badly compressed packet - size of " + length + " is larger than protocol maximum of " + MAX_COMPRESSED_SIZE);
//                }
//
//                byte[] compressed = new byte[pbuf.readableBytes()];
//                pbuf.readBytes(compressed);
//
//                byte[] decompressed = new byte[length];
//
//                this.decoder.decompress(compressed, decompressed);
//
//                out.add(Unpooled.wrappedBuffer(decompressed));
//            }
//
//        }
//    }
//
//}

//package me.jellysquid.mods.lithium.mixin.network;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import net.jpountz.lz4.LZ4Compressor;
//import net.jpountz.lz4.LZ4Factory;
//import net.minecraft.network.PacketDeflater;
//import net.minecraft.util.PacketByteBuf;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(PacketDeflater.class)
//public abstract class MixinPacketDeflater {
//    private LZ4Compressor encoder;
//
//    @Shadow
//    private int compressionThreshold;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onConstructed(int thresholdIn, CallbackInfo ci) {
//        this.encoder = LZ4Factory.fastestInstance().fastCompressor();
//    }
//
//    /**
//     *
//     * @author JellySquid
//     * @reason Changes the implementation to use LZ4 compression
//     */
//    @Overwrite
//    public void method_10741(ChannelHandlerContext channelHandlerContext_1, ByteBuf src, ByteBuf dst) {
//        int bytes = src.readableBytes();
//
//        PacketByteBuf pbuf = new PacketByteBuf(dst);
//
//        if (bytes < this.compressionThreshold) {
//            pbuf.writeVarInt(0);
//            pbuf.writeBytes(src);
//        } else {
//            byte[] uncompressed = new byte[bytes];
//            src.readBytes(uncompressed);
//
//            byte[] compressed = this.encoder.compress(uncompressed);
//
//            pbuf.writeVarInt(bytes);
//            pbuf.writeBytes(compressed);
//        }
//    }
//
//}

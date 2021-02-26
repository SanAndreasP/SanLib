////////////////////////////////////////////////////////////////////////////////
// This file is subject to the terms and conditions defined in the             /
// file '.github/LICENSE.md', which is part of this source code package.       /
////////////////////////////////////////////////////////////////////////////////

package de.sanandrew.mods.sanlib.lib.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class MessageHandler
{
    public final SimpleChannel channel;
    private int discriminatorId = 0;

    public MessageHandler(String modId, String protocolVersion) {
        this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(modId, "main"),
                                                        () -> protocolVersion,
                                                        protocolVersion::equals,
                                                        protocolVersion::equals);
    }

    public <T extends SimpleMessage> void registerMessage(Class<T> msgClass, Function<PacketBuffer, T> ctor) {
        this.channel.registerMessage(this.discriminatorId++, msgClass, T::encode, ctor, MessageHandler::handle);
    }

    public void sendToServer(SimpleMessage msg) {
        this.channel.sendToServer(msg);
    }

    public void sendToPlayer(SimpleMessage msg, ServerPlayerEntity player) {
        this.channel.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public void sendToAll(SimpleMessage msg, ServerPlayerEntity player) {
        this.channel.send(PacketDistributor.ALL.noArg(), msg);
    }

    public void sendToAllInChunk(SimpleMessage msg, Chunk chunk) {
        this.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

    public void sendToAllInDimension(SimpleMessage msg, RegistryKey<World> dimension) {
        this.channel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
    }

    public void sendToAllNear(SimpleMessage msg, PacketDistributor.TargetPoint target) {
        this.channel.send(PacketDistributor.NEAR.with(() -> target), msg);
    }

    public static <T extends SimpleMessage> void handle(T msg, Supplier<NetworkEvent.Context> context) {
        if( msg.handleOnMainThread() ) {
            context.get().enqueueWork(() -> msg.handle(context));
        } else {
            msg.handle(context);
        }
    }
}

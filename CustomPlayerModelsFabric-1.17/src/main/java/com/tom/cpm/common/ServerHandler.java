package com.tom.cpm.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage.EntityTracker;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import com.tom.cpm.shared.network.NetH;
import com.tom.cpm.shared.network.NetHandler;

import io.netty.buffer.Unpooled;

public class ServerHandler {
	public static NetHandler<Identifier, NbtCompound, ServerPlayerEntity, PacketByteBuf, ServerPlayNetworkHandler> netHandler;

	static {
		netHandler = new NetHandler<>(Identifier::new);
		netHandler.setNewNbt(NbtCompound::new);
		netHandler.setNewPacketBuffer(() -> new PacketByteBuf(Unpooled.buffer()));
		netHandler.setIsDedicatedServer(p -> p.getServer().isDedicated());
		netHandler.setGetPlayerUUID(ServerPlayerEntity::getUuid);
		netHandler.setWriteCompound(PacketByteBuf::writeNbt, PacketByteBuf::readNbt);
		netHandler.setSendPacket((c, rl, pb) -> c.sendPacket(new CustomPayloadS2CPacket(rl, pb)), (spe, rl, pb) -> NetworkHandler.sendToAllTrackingAndSelf(spe, new CustomPayloadS2CPacket(rl, pb), ServerHandler::hasMod, null));
		netHandler.setWritePlayerId((pb, pl) -> pb.writeVarInt(pl.getId()));
		netHandler.setNBTSetters(NbtCompound::putBoolean, NbtCompound::putByteArray, NbtCompound::putFloat);
		netHandler.setNBTGetters(NbtCompound::getBoolean, NbtCompound::getByteArray, NbtCompound::getFloat);
		netHandler.setContains(NbtCompound::contains);
		netHandler.setFindTracking((p, f) -> {
			for(EntityTracker tr : ((ServerWorld)p.world).getChunkManager().threadedAnvilChunkStorage.entityTrackers.values()) {
				if(tr.entity instanceof PlayerEntity && tr.listeners.contains(p.networkHandler)) {
					f.accept((ServerPlayerEntity) tr.entity);
				}
			}
		});
		netHandler.setSendChat((p, m) -> p.networkHandler.sendPacket(new GameMessageS2CPacket(new TranslatableText(m), MessageType.CHAT, Util.NIL_UUID)));
		netHandler.setExecutor(n -> ((IServerNetHandler)n).cpm$getServer());
		netHandler.setGetNet(spe -> spe.networkHandler);
		netHandler.setGetPlayer(net -> net.player);
	}

	public static void onPlayerJoin(ServerPlayerEntity spe) {
		netHandler.onJoin(spe);
	}

	public static void onTrackingStart(ServerPlayerEntity spe, Entity target) {
		ServerPlayNetworkHandler handler = spe.networkHandler;
		NetH h = (NetH) handler;
		if(h.cpm$hasMod()) {
			if(target instanceof PlayerEntity) {
				netHandler.sendPlayerData((ServerPlayerEntity) target, spe);
			}
		}
	}

	public static boolean hasMod(ServerPlayerEntity spe) {
		return ((NetH)spe.networkHandler).cpm$hasMod();
	}
}

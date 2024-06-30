package com.rosymaple.randomrespawns.mixin;

import com.rosymaple.randomrespawns.CommonConfig;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    private static final int MAX_HEIGHT = 256;
    private static final int MIN_HEIGHT = 64;

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"))
    private void setTimeToZero(ServerPlayer player, boolean keepEverything, CallbackInfoReturnable<ServerPlayer> info) {
        if(CommonConfig.SetTimeToZeroOnRespawn.get())
            player.getLevel().setDayTime(0);

        if(CommonConfig.SetWeatherClearOnRespawn.get())
            player.getLevel().setWeatherParameters(6000, 0, false, false);
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnPosition()Lnet/minecraft/core/BlockPos;"))
    private BlockPos onGetRespawnPosition(ServerPlayer player) {
        if(!CommonConfig.EnableRandomRespawns.get())
            return player.getRespawnPosition();

        Random random = player.getRandom();
        ServerLevel level = player.getLevel();

        int maxRadius = Math.max(1, CommonConfig.RandomRespawnRadius.get());

        int originX = 0, originZ = 0;
        if(CommonConfig.DeathPositionIsOrigin.get()) {
            originX = player.getBlockX();
            originZ = player.getBlockZ();
        }

        long startTime = Instant.now().getEpochSecond();
        long timeNow;
        int randomX, randomZ;
        BlockPos pos;
        do {
            randomX = random.nextInt(originX - maxRadius, originX + maxRadius);
            randomZ = random.nextInt(originZ - maxRadius, originZ + maxRadius);

            if(CommonConfig.AvoidOceans.get()) {
                while (level.getBiome(new BlockPos(randomX, MIN_HEIGHT, randomZ)).is(Tags.Biomes.IS_WATER)) {
                    randomX = random.nextInt(originX - maxRadius, originX + maxRadius);
                    randomZ = random.nextInt(originZ - maxRadius, originZ + maxRadius);

                    timeNow = Instant.now().getEpochSecond();
                    if(timeNow - startTime >= 10) {
                        player.sendMessage(new TranslatableComponent("randomrespawns.message.respawn_time_out"), Util.NIL_UUID);
                        return calculateRespawnPosition(level, 0, 0);
                    }
                }
            }

            pos = calculateRespawnPosition(level, randomX, randomZ);

            timeNow = Instant.now().getEpochSecond();
            if(timeNow - startTime >= 10) {
                player.sendMessage(new TranslatableComponent("randomrespawns.message.respawn_time_out"), Util.NIL_UUID);
                return calculateRespawnPosition(level, 0, 0);
            }

        } while(CommonConfig.AvoidHazardsOnRespawn.get() && (isHazard(level.getBlockState(pos.below()).getBlock()) || isHazard(level.getBlockState(pos).getBlock())));

        return pos;
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;findRespawnPositionAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;"))
    private Optional<Vec3> onFindRespawnPositionAndUseSpawnBlock(ServerLevel level, BlockPos pos, float f, boolean b1, boolean b2) {
        if(!CommonConfig.EnableRandomRespawns.get())
            return Player.findRespawnPositionAndUseSpawnBlock(level, pos, f, b1, b2);

        return Optional.of(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Redirect(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;moveTo(DDDFF)V"))
    private void onMoveTo(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
        if(!CommonConfig.EnableRandomRespawns.get()) {
            player.moveTo(x, y, z, yaw, pitch);
            return;
        }

        player.moveTo(x + 0.5F, y, z + 0.5F, yaw, pitch);
    }


    private boolean isHazard(Block block) {
        return block == Blocks.LAVA || block == Blocks.CACTUS ||
                block == Blocks.POWDER_SNOW || block == Blocks.MAGMA_BLOCK || block == Blocks.FIRE;
    }

    private BlockPos calculateRespawnPosition(ServerLevel level, int x, int z) {
        int middle;
        BlockPos pos = null;
        int upper = MAX_HEIGHT, lower = MIN_HEIGHT;
        while(lower < upper) {
            middle = (upper + lower) / 2;
            pos = new BlockPos(x, middle, z);
            if(level.getBlockState(pos).getBlock() == Blocks.AIR) {
                upper = middle - 1;
                continue;
            }

            lower = middle + 1;
        }

        return pos;
    }
}

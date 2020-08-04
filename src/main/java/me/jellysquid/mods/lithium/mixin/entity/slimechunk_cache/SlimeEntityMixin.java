package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.ChunkRandom;

/**
 * Adds data to the chunk a slime attempts to spawn in to reduce the amount of random calls and
 * object allocations made. Also does early stopping in a better order after some checks fail
 */
@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends MobEntity {
    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Overwrite
    public static boolean canSpawn(EntityType<SlimeEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        world.getWorld().getProfiler().push("Slime spawning");
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            Biome biome = world.getBiome(pos);
            if(biome == Biomes.SWAMP) {
                if (pos.getY() > 50 && pos.getY() < 70 && random.nextFloat() < 0.5F && random.nextFloat() < world.getMoonSize() && world.getLightLevel(pos) <= random.nextInt(8)) {
                    world.getWorld().getProfiler().pop();
                    return (canMobSpawn(type, world, spawnReason, pos, random));
                }
            // dont even bother with normal slime chunks if in a swamp
            } else {
                if (!(world instanceof ServerWorldAccess)) {
                    world.getWorld().getProfiler().pop();
                    return (false);
                }

                // check for stored value of slime chunk 
                // 2 = unchecked
                // 1 = true
                // 0 = false
                int slimeChunk = ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).isSlimeChunk();
                boolean isSlimeChunk = slimeChunk == 1;
                if(slimeChunk == 2) {
                    slimeChunk = ChunkRandom.getSlimeRandom(pos.getX() >> 4, pos.getZ() >> 4, ((ServerWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0 ? 1 : 0;
                    // set the flag to the new value of the chunk
                    ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).setSlimeChunk(slimeChunk);
                }
                if (isSlimeChunk && pos.getY() < 40 && random.nextInt(10) == 0) {
                    world.getWorld().getProfiler().pop();
                    return (canMobSpawn(type, world, spawnReason, pos, random));
                }
            }
        }
        world.getWorld().getProfiler().pop();
        return (false);
    }

}
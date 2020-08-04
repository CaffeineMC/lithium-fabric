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
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            Biome biome = world.getBiome(pos);
            if(biome == Biomes.SWAMP) {
                if (pos.getY() > 50 && pos.getY() < 70 && random.nextFloat() < 0.5F && random.nextFloat() < world.getMoonSize() && world.getLightLevel(pos) <= random.nextInt(8)) {
                    return (canMobSpawn(type, world, spawnReason, pos, random));
                }
            // dont even bother with normal slime chunks if not in a swamp
            } else {
                if (!(world instanceof ServerWorldAccess)) {
                    return (false);
                }

                // check for stored value of slime chunk
                boolean isSlimeChunk = ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).isSlimeChunk();
                if(isSlimeChunk) {
                    // double check that value isn't just default
                    ChunkPos chunkPos = new ChunkPos(pos);
                    isSlimeChunk = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, ((ServerWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;
                    // set the value to be accurate (even if it didn't change)
                    ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).setSlimeChunk(isSlimeChunk);
                }
                if (isSlimeChunk && pos.getY() < 40 && random.nextInt(10) == 0) {
                    return (canMobSpawn(type, world, spawnReason, pos, random));
                }
            }
        }
        return (false);
    }

}
package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.ChunkRandom;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin extends MobEntity {
    
    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Overwrite
    public static boolean canSpawn(EntityType<SlimeEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL) {
            Biome biome = world.getBiome(pos);
            if (biome == Biomes.SWAMP && pos.getY() > 50 && pos.getY() < 70 && random.nextFloat() < 0.5F && random.nextFloat() < world.getMoonSize() && world.getLightLevel(pos) <= random.nextInt(8)) {
                return canMobSpawn(type, world, spawnReason, pos, random);
            }

            if (!(world instanceof ServerWorldAccess)) {
                return false;
            }

            // boolean isSlimeChunk = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, ((ServerWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;
            boolean isSlimeChunk = ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).isSlimeChunk();
            if(isSlimeChunk) {
                isSlimeChunk = ChunkRandom.getSlimeRandom(pos.getX(), pos.getZ(), ((ServerWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;
                ((ChunkWithSlimeTag)((WorldView)world).getChunk(pos)).setSlimeChunk(isSlimeChunk);
            }
            if (pos.getY() < 40 && isSlimeChunk && random.nextInt(10) == 0) {
                return canMobSpawn(type, world, spawnReason, pos, random);
            }
        }

        return false;
    }

}
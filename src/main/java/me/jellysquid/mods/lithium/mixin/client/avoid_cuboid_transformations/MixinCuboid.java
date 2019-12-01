package me.jellysquid.mods.lithium.mixin.client.avoid_cuboid_transformations;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.Cuboid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Cuboid.class)
public abstract class MixinCuboid {
    @Shadow
    public boolean field_3664;

    @Shadow
    public boolean visible;

    @Shadow
    private boolean compiled;

    @Shadow
    public float x;

    @Shadow
    public float y;

    @Shadow
    public float z;

    @Shadow
    public float pitch;

    @Shadow
    public float yaw;

    @Shadow
    public float roll;

    @Shadow
    public float rotationPointX;

    @Shadow
    public float rotationPointY;

    @Shadow
    public float rotationPointZ;

    @Shadow
    public List<Cuboid> children;

    @Shadow
    private int list;

    @Shadow
    protected abstract void compile(float float_1);

    /**
     * Reduces the number of matrix operations by combining translations or avoiding them all together.
     *
     * @author JellySquid
     */
    @Overwrite
    public void render(float scale) {
        if (this.field_3664 || !this.visible) {
            return;
        }

        if (!this.compiled) {
            this.compile(scale);
        }

        GlStateManager.pushMatrix();

        if (this.pitch == 0.0F && this.yaw == 0.0F && this.roll == 0.0F) {
            if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
                GlStateManager.translatef(this.x, this.y, this.z);
                GlStateManager.callList(this.list);

                if (this.children != null) {
                    for (Cuboid child : this.children) {
                        child.render(scale);
                    }
                }
            } else {
                GlStateManager.translatef(this.x + (this.rotationPointX * scale), this.y + (this.rotationPointY * scale), this.z + (this.rotationPointZ * scale));
                GlStateManager.callList(this.list);

                if (this.children != null) {
                    for (Cuboid child : this.children) {
                        child.render(scale);
                    }
                }
            }
        } else {
            GlStateManager.translatef(this.x + (this.rotationPointX * scale), this.y + (this.rotationPointY * scale), this.z + (this.rotationPointZ * scale));

            if (this.roll != 0.0F) {
                GlStateManager.rotatef(this.roll * 57.295776F, 0.0F, 0.0F, 1.0F);
            }

            if (this.yaw != 0.0F) {
                GlStateManager.rotatef(this.yaw * 57.295776F, 0.0F, 1.0F, 0.0F);
            }

            if (this.pitch != 0.0F) {
                GlStateManager.rotatef(this.pitch * 57.295776F, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.callList(this.list);

            if (this.children != null) {
                for (Cuboid child : this.children) {
                    child.render(scale);
                }
            }
        }

        GlStateManager.popMatrix();

    }
}

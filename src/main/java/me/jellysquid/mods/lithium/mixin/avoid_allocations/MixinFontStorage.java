package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(FontStorage.class)
public class MixinFontStorage {
    @Shadow
    @Final
    private List<GlyphAtlasTexture> glyphAtlases;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    @Final
    private Identifier id;

    @Shadow
    private GlyphRenderer blankGlyphRenderer;

    /**
     * Avoids unnecessary allocations.
     *
     * @author JellySquid
     */
    @Overwrite
    private GlyphRenderer getGlyphRenderer(RenderableGlyph glyph) {
        if (this.glyphAtlases.size() == 0) {
            GlyphAtlasTexture texture = new GlyphAtlasTexture(new Identifier(this.id.getNamespace(), this.id.getPath() + "/" + this.glyphAtlases.size()), glyph.hasColor());

            this.glyphAtlases.add(texture);
            this.textureManager.registerTexture(texture.getId(), texture);

            GlyphRenderer renderer = texture.getGlyphRenderer(glyph);

            return renderer == null ? this.blankGlyphRenderer : renderer;
        }

        return this.glyphAtlases.get(0).getGlyphRenderer(glyph);
    }
}

package honey.mechanics;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Effect;
import honey.player.armory.Weapon;
import honey.player.inventory.Item;
import honey.player.inventory.KeyItem;
import honey.rendering.Rendering;
import honey.world.Biome;
import honey.world.Entity;
import honey.world.Projectile;
import honey.world.Tile;
import honey.world.WorldObject;

public class AssetManager {

    public static ConfigManager config;

    public static void preloadImages() {
        // Entities: static and gif variants, respecting natColor per biome
        for (String entityKey : Entity.entityTextures.keySet()) {
            final Map<String, String> tex = Entity.entityTextures.get(entityKey);
            final String staticBase = tex.get("texture");
            final String gifBase = tex.get("gif");
            if (staticBase == null && gifBase == null) {
                continue;
            }
            final String anim = tex.getOrDefault("anim", "");
            final int frameSize = Entity.entityAttributes.get(entityKey).getOrDefault("frameSize", 16).intValue();
            final String natColorId = tex.get("natColor");
            if (natColorId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorId);
                    if (color != null) {
                        for (String suffix : entitySuffixes(anim)) {
                            if (staticBase != null) {
                                Rendering.registerImage(staticBase + suffix, color);
                            }
                            if (gifBase != null) {
                                Rendering.registerGIF(gifBase + suffix, color, frameSize, frameSize);
                            }
                        }
                    }
                }
            } else {
                final String color = tex.get("baseColor");
                for (String suffix : entitySuffixes(anim)) {
                    if (staticBase != null) {
                        Rendering.registerImage(staticBase + suffix, color);
                    }
                    if (gifBase != null) {
                        Rendering.registerGIF(gifBase + suffix, color, frameSize, frameSize);
                    }
                }
            }
        }

        // Objects: static and gif variants, respecting natColor per biome
        for (int objKey : WorldObject.objTextures.keySet()) {
            final Map<String, String> tex = WorldObject.objTextures.get(objKey);
            final String staticBase = tex.get("texture");
            final String gifBase = tex.get("gif");
            if (staticBase == null && gifBase == null) {
                continue;
            }
            final Map<String, Number> attrs = WorldObject.objAttributes.get(objKey);
            final String natColorId = tex.get("natColor");
            if (natColorId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorId);
                    if (color != null) {
                        final int frameSize = attrs.getOrDefault("frameSize", 16).intValue();
                        for (String postfix : objSuffixes(tex, attrs)) {
                            if (staticBase != null) {
                                Rendering.registerImage(staticBase + postfix, color);
                            }
                            if (gifBase != null) {
                                Rendering.registerGIF(gifBase + postfix, color, frameSize, frameSize);
                            }
                        }
                    }
                }
            } else {
                final String color = tex.get("baseColor");
                final int frameSize = attrs.getOrDefault("frameSize", 16).intValue();
                for (String postfix : objSuffixes(tex, attrs)) {
                    if (staticBase != null) {
                        Rendering.registerImage(staticBase + postfix, color);
                    }
                    if (gifBase != null) {
                        Rendering.registerGIF(gifBase + postfix, color, frameSize, frameSize);
                    }
                }
            }
        }

        // Projectiles
        for (Map<String, String> tex : Projectile.projTextures.values()) {
            final String base = tex.get("texture");
            if (base != null) {
                Rendering.registerImage(base, null);
            }
        }

        // Weapons: item icon + attack GIFs per swing/stab color
        for (String weaponKey : Weapon.weaponTextures.keySet()) {
            final Map<String, String> tex = Weapon.weaponTextures.get(weaponKey);
            final String itemTex = tex.get("itemTexture");
            if (itemTex != null) {
                Rendering.registerImage(itemTex, "#e8f1ff");
            }
            final String swingColor = tex.get("swingColor");
            if (swingColor != null) {
                Rendering.registerGIF("attacks/slash", swingColor, config.slashFrameSize, config.slashFrameSize);
            }
            final String stabColor = tex.get("stabColor");
            if (stabColor != null) {
                Rendering.registerGIF("attacks/stab", stabColor, config.stabFrameWidth, config.stabFrameHeight);
            }
        }

        // Armor front/back textures
        for (Map<String, String> tex : Armor.armorTextures.values()) {
            final String front = tex.get("front");
            final String back = tex.get("back");
            if (front != null) {
                Rendering.registerImage(front, null);
            }
            if (back != null) {
                Rendering.registerImage(back, null);
            }
        }

        // Items, ammo, key items, effects
        for (Map<String, String> tex : Item.itemTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) {
                Rendering.registerImage(t, null);
            }
        }
        for (Map<String, String> tex : Ammo.ammoTextures.values()) {
            final String t = tex.get("itemTexture");
            if (t != null) {
                Rendering.registerImage(t, null);
            }
        }
        for (Map<String, String> tex : KeyItem.keyTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) {
                Rendering.registerImage(t, null);
            }
        }
        for (Map<String, String> tex : Effect.effectTextures.values()) {
            final String t = tex.get("texture");
            if (t != null) {
                Rendering.registerImage(t, null);
            }
        }

        // Biome overlays
        for (Map<String, String> texMap : Biome.biometextureMap.values()) {
            final String overlay = texMap.get("overlayTexture");
            if (overlay != null) {
                Rendering.registerImage(overlay, null);
            }
        }

        // Tiles: edge textures only, respecting natColorEdge per biome
        for (Map<String, String> tex : Tile.tileTextures.values()) {
            final String edgeTex = tex.get("textureEdge");
            if (edgeTex == null) {
                continue;
            }
            final String natColorEdgeId = tex.get("natColorEdge");
            if (natColorEdgeId != null) {
                for (Map<String, String> biomeColors : Biome.biometextureMap.values()) {
                    final String color = biomeColors.get(natColorEdgeId);
                    if (color != null) {
                        Rendering.registerImage(edgeTex, color);
                    }
                }
            } else {
                Rendering.registerImage(edgeTex, tex.get("baseColorEdge"));
            }
        }
    }

    public static void registerFont() {
        try {
            final File fontFile = new File(AssetManager.class.getResource("/fonts/VT323/VT323-Regular.ttf").toURI());
            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            final Font sizedFont = font.deriveFont(Font.PLAIN, 24f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sizedFont);
        } catch (FontFormatException e) {
            System.out.println("AssetManager ERROR: Failed to format text font.");
        } catch (IOException e) {
            System.out.println("AssetManager ERROR: Failed to import text font file.");
        } catch (URISyntaxException e) {
            System.out.println("AssetManager ERROR: Could not find text font file.");
        }
    }

    private static List<String> objSuffixes(Map<String, String> tex, Map<String, Number> attrs) {
        final String anim = tex.getOrDefault("anim", "");
        final int variants = attrs.getOrDefault("variants", 1).intValue();
        final List<String> result = new ArrayList<>();
        for (int v = variants > 1 ? 1 : 0; variants > 1 ? v <= variants : v < 1; v++) {
            final String variantSuffix = variants > 1 ? "_" + v : "";
            result.add(variantSuffix);
            if (anim.contains("_destroyed_")) {
                result.add(variantSuffix + "_destroyed");
            }
        }
        return result;
    }

    private static List<String> entitySuffixes(String anim) {
        List<String> dirs;
        if (anim.contains("_xy_")) {
            dirs = List.of("_up", "_right", "_down", "_left");
        } else if (anim.contains("_x_")) {
            dirs = List.of("_left", "_right");
        } else if (anim.contains("_y_")) {
            dirs = List.of("_up", "_down");
        } else {
            dirs = List.of("");
        }

        final List<String> states = new ArrayList<>();
        if (anim.contains("_chase_")) {
            states.add("_chase");
        }
        if (anim.contains("_hesitate_")) {
            states.add("_hesitate");
        }
        if (anim.contains("_shoot_")) {
            states.add("_shoot");
        }
        if (anim.contains("_summon_")) {
            states.add("_summon");
        }

        final int n = states.size();
        final List<String> stateCombos = new ArrayList<>(1 << n);
        for (int mask = 0; mask < (1 << n); mask++) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    sb.append(states.get(i));
                }
            }
            stateCombos.add(sb.toString());
        }

        final List<String> result = new ArrayList<>(dirs.size() * stateCombos.size());
        for (String dir : dirs) {
            for (String stateCombo : stateCombos) {
                result.add(dir + stateCombo);
            }
        }
        return result;
    }
}

package honey.player.build;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import honey.mechanics.ConfigManager;
import honey.player.Player;
import honey.player.inventory.Inventory;
import honey.player.inventory.Item;
import honey.rendering.Rendering;
import honey.world.World;

public class Blueprint {

    public static ConfigManager config;

    //Static json data
    public static final Map<String, List<Map<String, Number>>> blueprintMats = new HashMap<>();
    public static final Map<String, Map<String, List<Number>>> blueprintParams = new HashMap<>();
    public static final Map<String, Map<String, String>> blueprintTextures = new HashMap<>();
    public static final Map<String, Integer> blueprintProducts = new HashMap<>();
    public static final Map<String, List<String>> blueprintTags = new HashMap<>();

    public final String type;

    public final List<Map<String, Number>> mats;
    public final Map<String, List<Number>> params;
    public final Map<String, String> texture;
    public final int product;
    public final List<String> tags;

    public final BufferedImage staticTexture;

    public Blueprint(String type) {
        this.type = type;
        product = blueprintProducts.get(type);

        mats = blueprintMats.get(type);
        params = blueprintParams.get(type);
        texture = blueprintTextures.get(type);
        tags = blueprintTags.get(type);

        staticTexture = getTexture();
    }

    private BufferedImage getTexture() {
        final String textureString = texture.get("texture");
        if (textureString != null) {
            return Rendering.texture(textureString, null);
        }
        return null;
    }

    //Check to see if player has materials
    public boolean hasMaterials(Inventory inventory) {
        //Go through all materials needed
        for (Map<String, Number> material : mats) {
            //If don't have, return false
            if (!inventory.hasMaterial(material)) {
                return false;
            }
        }
        //If makes it past check, return true
        return true;
    }

    //Checks if blueprint can be built
    public boolean checkCanPlace(World world, Player player, int[] cursor) {
        //Position trying to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / config.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / config.tileSize) + cursor[1])
        };

        //Checks if player has materials
        if (!hasMaterials(player.inventory)) {
            return false;
        }

        //Checks to ensure player can always place on selected tile
        if (tags.contains("placeAway") && cursor[0] == 0 && cursor[1] == 0) {
            return false;
        }

        if (index[0] < 0 || index[0] >= world.size[0] || index[1] < 0 || index[1] >= world.size[1]) {
            return false;
        }

        final List<Number> tileList = params.getOrDefault("tile", new ArrayList<>());
        final List<Number> objList = params.getOrDefault("obj", List.of(0));
        //If object doesnt exist, only check tile
        if (world.objGrid[index[0]][index[1]] == null) {
            return tileList.contains(world.grid[index[0]][index[1]].id) && objList.contains(0);
        }
        //Check object and tile
        return (tileList.contains(world.grid[index[0]][index[1]].id)
                && objList.contains(world.objGrid[index[0]][index[1]].id));
    }

    public void renderUiTile(Graphics2D g, int x, int y, Inventory inventory, boolean mirror) {
        //Verification color
        String textureColor = "#ff0000";
        //If have materials, display green verification
        if (hasMaterials(inventory)) {
            textureColor = "#00ff00";
        }
        //Render blueprint Scroll
        if (mirror) {
            g.drawImage(Rendering.texture("ui/hud/blueprint", textureColor), x + config.hudSize * 17 / 6, y, -config.hudSize, config.hudSize, null);
        } else {
            g.drawImage(Rendering.texture("ui/hud/blueprint", textureColor), x, y, config.hudSize, config.hudSize, null);
        }

        //Render blueprint Item
        if (staticTexture != null) {
            if (mirror) {
                g.drawImage(staticTexture, x + config.hudSize * 47 / 24, y + config.hudSize * 3 / 24, config.hudSize * 3 / 4, config.hudSize * 3 / 4, null);
            } else {
                g.drawImage(staticTexture, x + config.hudSize * 3 / 24, y + config.hudSize * 3 / 24, config.hudSize * 3 / 4, config.hudSize * 3 / 4, null);
            }
        }

        for (int i = 0; i < mats.size(); i++) {
            Map<String, Number> mat = mats.get(i);
            String itemId = Item.itemStringId.get(mat.get("id").intValue());
            String name = Item.itemNames.get(itemId);
            int itemCount = mat.getOrDefault("count", 1).intValue();

            g.setFont(new Font("Dialog", Font.PLAIN, 16));
            if (inventory.getItemCount(itemId) >= itemCount) {
                g.setColor(Color.GREEN);
                if (mirror) {
                    final int w = g.getFontMetrics().stringWidth(name + " x" + itemCount + " ✓");
                    g.drawString(name + " x" + itemCount + " ✓", x + config.hudSize * 5 / 3 - w, y + config.hudSize - i * 18);
                } else {
                    g.drawString("✓ " + name + " x" + itemCount, x + config.hudSize * 13 / 12, y + config.hudSize - i * 18);
                }
            } else {
                g.setColor(Color.RED);
                if (mirror) {
                    final int w = g.getFontMetrics().stringWidth(name + " x" + itemCount + " ✕");
                    g.drawString(name + " x" + itemCount + " ✕", x + config.hudSize * 5 / 3 - w, y + config.hudSize - i * 18);
                } else {
                    g.drawString("✕ " + name + " x" + itemCount, x + config.hudSize * 13 / 12, y + config.hudSize - i * 18);
                }
            }
        }
    }
}

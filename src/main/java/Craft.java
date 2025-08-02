
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Craft {

    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final Color DARK_GREEN = new Color(0, 160, 0);

    //Static json data
    public static final Map<String, List<Map<String, Number>>> recipeMats = new HashMap<>();
    public static final Map<String, List<Map<String, Number>>> recipeProducts = new HashMap<>();
    public static final Map<String, Map<String, String>> recipeTextures = new HashMap<>();
    public static final Map<String, String> recipeTypes = new HashMap<>();
    public static final Map<String, String> recipeNames = new HashMap<>();
    public static final Map<String, Map<String, Number>> recipeAttributes = new HashMap<>();

    public final Set<String> recipes = new LinkedHashSet<>();

    private double scroll = 0;
    private int hover = -1;

    public Craft(Set<String> recipes) {
        this.recipes.addAll(recipes);
    }

    public static void craft(Player player, String recipeKey) {
        for (Map<String, Number> product : recipeProducts.get(recipeKey)) {
            player.inventory.incrementItem(product, true);
        }

        //Material data
        List<Map<String, Number>> recipe = recipeMats.get(recipeKey);

        //Removes materials
        for (Map<String, Number> material : recipe) {
            player.inventory.incrementItem(material, false);
        }
    }

    public void update(Player player, InputHandler input) {
        if (!recipes.isEmpty()) {
            scroll = Math.clamp(scroll + input.mouseScroll, 0, recipes.size() - 1);
            hover = -1;
            if (Math.abs(input.mousePos[1] - GAME_HEIGHT / 2) <= 50) {
                double highlight = (input.mousePos[0] - (GAME_WIDTH / 2 - 50));
                if (highlight % 110 <= 100) {
                    hover = (int) Math.floor(highlight / 110);
                }
            }

            if (input.clickPressed(1)) {
                if (hover > -1 && hover < recipes.size()) {
                    final String recipe = (String) recipes.toArray()[hover];
                    if (hasMaterials(player, recipe)) {
                        craft(player, recipe);
                    }
                }
            }
        }
    }

    public void renderUi(Graphics2D g, Player player) {
        final String[] recipeArray = recipes.toArray(String[]::new);

        for (int i = 0; i < recipeArray.length; i++) {
            final String recipe = recipeArray[i];
            final Map<String, String> texture = recipeTextures.get(recipe);

            final int offset = 110 * i - ((int) Math.floor(scroll * 110));

            String slotColor = "#ff0000";

            if (hasMaterials(player, recipe)) {
                slotColor = "#00ff00";
            }

            if (hover == i) {
                g.drawImage(Rendering.texture("hud/recipe", slotColor), (int) (GAME_WIDTH / 2 - 55 + offset), (int) (GAME_HEIGHT / 2 - 55), 110, 110, null);
            } else {
                g.drawImage(Rendering.texture("hud/recipe", slotColor), (int) (GAME_WIDTH / 2 - 50 + offset), (int) (GAME_HEIGHT / 2 - 50), 100, 100, null);
            }

            final String recipeTexture = texture.get("texture");
            if (recipeTexture != null) {
                g.drawImage(Rendering.texture(recipeTexture, "#ffffff"), (int) (GAME_WIDTH / 2 - 40 + offset), (int) (GAME_HEIGHT / 2 - 40), 80, 80, null);
            }
        }

        int descIndex = hover;
        if (descIndex < 0 || descIndex >= recipes.size()) {
            descIndex = (int) Math.round(scroll);
        }

        if (!recipes.isEmpty()) {
            String recipe = recipeArray[descIndex];
            String name = recipeNames.get(recipe);

            g.setFont(new Font("VT323 Regular", Font.PLAIN, 36));
            int textSize = g.getFontMetrics().stringWidth(name);

            List<Map<String, Number>> mats = recipeMats.get(recipe);

            // Render Scroll
            int scrollWidth = Math.ceilDiv(Math.max(textSize, mats.size() * 60), 16);

            g.drawImage(Rendering.texture("hud/scroll_end", "#ffffff"), (int) (GAME_WIDTH - scrollWidth * 16) / 2 - 16, 40, 16, 128, null);
            for (int i = 0; i < scrollWidth; i++) {
                g.drawImage(Rendering.texture("hud/scroll_middle", "#ffffff"), (int) (GAME_WIDTH - scrollWidth * 16) / 2 + i * 16, 40, 16, 128, null);
            }
            g.drawImage(Rendering.texture("hud/scroll_end", "#ffffff"), (int) (GAME_WIDTH + scrollWidth * 16) / 2, 40, 16, 128, null);

            // Render Title
            if (hasMaterials(player, recipe)) {
                g.setColor(DARK_GREEN);
            } else {
                g.setColor(Color.RED);
            }

            g.drawString(name, (int) (GAME_WIDTH - textSize) / 2, 80);

            for (int i = 0; i < mats.size(); i++) {
                Map<String, Number> material = mats.get(i);
                int count = material.getOrDefault("count", 1).intValue();

                String texture = null;

                final int id = material.get("id").intValue();
                switch (material.getOrDefault("type", 0).intValue()) {
                    case 0 -> {
                        texture = Item.itemTextures.get(Item.itemStringId.get(id)).get("texture");
                    }
                    case 1 -> {
                        texture = Weapon.weaponTextures.get(Weapon.weaponStringId.get(id)).get("item_texture");
                    }
                }

                int x = (int) (GAME_WIDTH - mats.size() * 60) / 2 + i * 60;

                if (texture != null) {
                    g.drawImage(Rendering.texture(texture, "#ffffff"), x + 5, 90, 50, 50, null);
                }

                String label = "x" + count;
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 24));
                if(player.inventory.hasMaterial(material)){
                    g.setColor(DARK_GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                Rendering.centeredText(g, label, x + 30, 150);
            }
        }
    }

    public static boolean hasMaterials(Player player, String recipeKey) {
        //Material data
        List<Map<String, Number>> recipe = recipeMats.get(recipeKey);

        //Go through all materials needed
        for (Map<String, Number> material : recipe) {
            //If don't have, return false
            if (!player.inventory.hasMaterial(material)) {
                return false;
            }
        }
        //If makes it past check, return true
        return true;
    }
}

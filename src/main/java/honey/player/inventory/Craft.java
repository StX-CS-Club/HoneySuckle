package honey.player.inventory;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import honey.mechanics.ConfigManager;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.player.armory.Ammo;
import honey.player.armory.Armor;
import honey.player.armory.Weapon;
import honey.rendering.Rendering;

public class Craft {

    public static ConfigManager config;

    private static final Color DARK_GREEN = new Color(0, 160, 0);

    //Static json data
    public static final Map<String, List<Map<String, Number>>> recipeMats = new HashMap<>();
    public static final Map<String, List<Map<String, Number>>> recipeProducts = new HashMap<>();
    public static final Map<String, Map<String, String>> recipeTextures = new HashMap<>();
    public static final Map<String, String> recipeTypes = new HashMap<>();
    public static final Map<String, String> recipeNames = new HashMap<>();
    public static final Map<String, Map<String, Number>> recipeAttributes = new HashMap<>();

    public final Set<String> recipes = new LinkedHashSet<>();
    private List<String> orderedRecipes = new ArrayList<>();

    private double scroll = 0;
    private int hover = -1;

    private final Player player;

    public Craft(Player player, Set<String> recipes) {
        this.player = player;
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

    public void update(InputHandler input) {
        if (!recipes.isEmpty()) {
            if(orderedRecipes.size() != recipes.size()){
                orderedRecipes = buildOrderedRecipes();
            }
            scroll = Math.clamp(scroll + input.mouseScroll, 0, orderedRecipes.size() - 1);
            hover = -1;
            if (Math.abs(input.mousePos[1] - config.gameHeight / 2) <= 50) {
                double highlight = (input.mousePos[0] - (config.gameWidth / 2 - 50)) + scroll * 110;
                if (highlight % 110 <= 100) {
                    hover = (int) Math.floor(highlight / 110);
                }
            }

            if (input.clickPressed(1) || input.clickDown(3)) {
                if (hover > -1 && hover < orderedRecipes.size()) {
                    final String recipe = orderedRecipes.get(hover);
                    if (hasMaterials(player, recipe)) {
                        craft(player, recipe);
                        hover = -1;
                    }
                }
            }
        }
    }

    public void renderUi(Graphics2D g) {
        final String[] recipeArray = orderedRecipes.toArray(String[]::new);

        for (int i = 0; i < recipeArray.length; i++) {
            final String recipe = recipeArray[i];
            final Map<String, String> texture = recipeTextures.get(recipe);

            final int offset = 110 * i - ((int) Math.floor(scroll * 110));

            String slotColor = "#ff0000";

            if (hasMaterials(player, recipe)) {
                slotColor = "#00ff00";
            }

            if (hover == i) {
                g.drawImage(Rendering.texture("ui/slots/recipe", slotColor), (int) (config.gameWidth / 2 - 55 + offset), (int) (config.gameHeight / 2 - 55), 110, 110, null);
            } else {
                g.drawImage(Rendering.texture("ui/slots/recipe", slotColor), (int) (config.gameWidth / 2 - 50 + offset), (int) (config.gameHeight / 2 - 50), 100, 100, null);
            }

            final String recipeTexture = texture.get("texture");
            if (recipeTexture != null) {
                g.drawImage(Rendering.texture(recipeTexture, null), (int) (config.gameWidth / 2 - 40 + offset), (int) (config.gameHeight / 2 - 40), 80, 80, null);
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
            final int scale = (int) Math.floor(2.5 * config.hudSize / 32);
            int scrollWidth = Math.ceilDiv(Math.max(textSize, mats.size() * 60), 4 * scale);
            final int renderedW = (scrollWidth * 4 + 8) * scale;
            final int renderedH = 32 * scale;
            final int scrollTop = 20;
            final int scrollX = (config.gameWidth - renderedW) / 2;

            g.drawImage(Rendering.scroll(scrollWidth), scrollX, scrollTop, renderedW, renderedH, null);

            // Render Title
            if (hasMaterials(player, recipe)) {
                g.setColor(DARK_GREEN);
            } else {
                g.setColor(Color.RED);
            }

            g.drawString(name, (int) (config.gameWidth - textSize) / 2, scrollTop + 10 * scale);

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
                        texture = Weapon.weaponTextures.get(Weapon.weaponStringId.get(id)).get("itemTexture");
                    }
                    case 2 -> {
                        texture = Armor.armorTextures.get(Armor.armorStringId.get(id)).get("itemTexture");
                    }
                    case 3 -> {
                        texture = Ammo.ammoTextures.get(Ammo.ammoStringId.get(id)).get("texture");
                    }
                }

                int x = (int) (config.gameWidth - mats.size() * 60) / 2 + i * 60;

                if (texture != null) {
                    g.drawImage(Rendering.texture(texture, null), x + 5, scrollTop + scale * 50 / 4, 50, 50, null);
                }

                String label = "x" + count;
                g.setFont(new Font("VT323 Regular", Font.PLAIN, 24));
                if(player.inventory.hasMaterial(material)){
                    g.setColor(DARK_GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                Rendering.centeredText(g, label, x + 30, scrollTop + scale * 110 / 4);
            }
        }
    }

    private List<String> buildOrderedRecipes() {
        final List<String> result = new ArrayList<>();
        for (String type : config.recipeOrder) {
            for (String recipe : recipes) {
                if (recipeTypes.get(recipe).equals(type)) {
                    result.add(recipe);
                }
            }
        }
        for (String recipe : recipes) {
            if (!result.contains(recipe)) {
                result.add(recipe);
            }
        }
        return result;
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

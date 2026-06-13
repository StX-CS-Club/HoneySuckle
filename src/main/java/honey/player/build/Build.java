package honey.player.build;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import honey.mechanics.ConfigManager;
import honey.mechanics.InputHandler;
import honey.player.Player;
import honey.rendering.Rendering;
import honey.world.World;
import honey.world.WorldObject;

/*
 * Buold.java *
 - Class for managing player's building in world
 - Contains static json data
 */
public class Build {

    public static ConfigManager config;

    private final Player player;

    public Build(Player player, Set<String> blueprints) {
        this.player = player;
        //Assigns values to properties
        for (String blueprint : blueprints) {
            this.blueprints.add(new Blueprint(blueprint));
        }
    }

    //Build properties
    public final List<Blueprint> blueprints = new ArrayList<>();
    private int blueprintIndex = 0;

    //Index of cursor tile compared to player
    public int[] cursor = new int[2];

    public void addBlueprint(String type) {
        blueprints.add(new Blueprint(type));
    }

    public boolean hasBlueprint(String type) {
        for (Blueprint blueprint : blueprints) {
            if (blueprint.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    //Update Build
    public void update(World world, InputHandler input) {
        int[] gameSize = new int[]{config.gameWidth, config.gameHeight};
        for (int i = 0; i < 2; i++) {
            double mouseDiff = input.mousePos[i]
                    - (gameSize[i] / 2.0
                    + Math.floor(player.pos[i] / config.tileSize) * config.tileSize
                    - world.camera[i]);
            if (mouseDiff < 0) {
                cursor[i] = -1;
            } else if (mouseDiff > config.tileSize) {
                cursor[i] = 1;
            } else {
                cursor[i] = 0;
            }
        }
    }

    //Render Build
    public void render(Graphics2D g, World world) {
        //Tile position of player, with cursor pos added
        int[] index = new int[]{
            (int) Math.floor(player.pos[0] / config.tileSize + cursor[0]),
            (int) Math.floor(player.pos[1] / config.tileSize + cursor[1])
        };

        //Color of tile
        Color color = Color.red;

        //If can place on tile, be cyan
        if (blueprints.get(blueprintIndex).checkCanPlace(world, player, cursor)) {
            color = Color.cyan;
        }

        //Import camera from world
        double[] camera = World.worlds.get(World.level).camera;

        //Render Build Tile
        g.setColor(new Color(0, 0, 0, 0));
        Rendering.borderRect(g, 1, color,
                (int) (config.gameWidth / 2.0 + index[0] * config.tileSize - camera[0]),
                (int) (config.gameHeight / 2.0 + index[1] * config.tileSize - camera[1]),
                config.tileSize, config.tileSize);
    }

    //Render Build UI
    public void renderUi(Graphics2D g, World world) {
        if (player.screenPos[0] < config.hudSize * 3 + config.tileSize && player.screenPos[1] > config.gameHeight - config.hudSize * 25 / 12 - config.tileSize) {
            blueprints.get(blueprintIndex).renderUiTile(g, config.gameWidth - config.hudSize * 35 / 12, config.gameHeight - config.hudSize * 25 / 12, player.inventory, true);
        } else {
            blueprints.get(blueprintIndex).renderUiTile(g, config.hudSize / 12, config.gameHeight - config.hudSize * 25 / 12, player.inventory, false);
        }
    }

    //Change selected blueprint based on scroll wheel
    public void scrollBar(double mouseScroll) {
        if (Math.abs(mouseScroll) >= config.criticalMouseScroll) {
            blueprintIndex += Math.signum(mouseScroll);
            if (blueprintIndex < 0) {
                blueprintIndex = blueprints.size() - 1;
            }
            if (blueprintIndex >= blueprints.size()) {
                blueprintIndex = 0;
            }
        }
    }

    //Build something in the world
    public void build(World world) {
        //Current selected blueprint
        Blueprint blueprint = blueprints.get(blueprintIndex);

        //Position to build on
        int[] index = new int[]{
            (int) (Math.floor(player.pos[0] / config.tileSize) + cursor[0]),
            (int) (Math.floor(player.pos[1] / config.tileSize) + cursor[1])
        };

        //Checks if can place on tile
        if (blueprint.checkCanPlace(world, player, cursor)) {
            //Places tile
            world.objGrid[index[0]][index[1]] = new WorldObject(blueprint.product, index, world);
            //Removes materials
            for (Map<String, Number> material : blueprint.mats) {
                player.inventory.incrementItem(material, false);
            }
        }
    }
}

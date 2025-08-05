
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/*
 * Player.java *
 - Class for managing players
 */
public class Player {

    private static final int FPS = HoneySuckle.FPS;
    private static final int GAME_WIDTH = HoneySuckle.GAME_WIDTH;
    private static final int GAME_HEIGHT = HoneySuckle.GAME_HEIGHT;
    private static final int TILE_SIZE = HoneySuckle.TILE_SIZE;

    //Static list of all players
    public static List<Player> players = new ArrayList<>();

    //Player Constructor
    public Player(double[] pos, int size, List<String> tags) {
        //Assign values to properties
        this.pos = pos;
        this.size = size;
        build = new Build(new LinkedHashSet<>(Arrays.asList("wall", "raft")));
        craft = new Craft(new LinkedHashSet<>());
        armory = new Armory(
                new Weapon[]{new Weapon("sword"), new Weapon("bow"), new Weapon("shield")},
                new Armor("leather")
        );
        inventory = new Inventory(
                this,
                Arrays.asList(armory.weapons),
                Arrays.asList(new Armor[]{armory.armor}),
                Arrays.asList(new Item[]{new Item("wood", 4)}),
                null);
        inventory.incrementItem(Map.of("id", 9, "count", 12), true);
        
        attributes = armory.getAttributes();

        //Adds player to list of players
        players.add(this);
    }

    //Player properties
    public double[] pos;
    public double[] vel = new double[2];
    public double[] screenPos = new double[2];
    public double rotation;

    public Map<String, Number> attributes;

    public double health = 1;
    private double stamina = 1;

    public int size;

    public final Inventory inventory;
    public final Build build;
    public final Craft craft;
    public final Armory armory;

    //If scroll wheel goes to weapons or recipes
    private boolean weaponScroll = false;

    //Render Player
    public void render(Graphics2D g) {
        //If not dead, render
        if (health > 0) {
            //Render building and armory
            if (!inventory.isOpen) {
                build.render(g, World.worlds.get(World.level), this);
            }
            armory.render(g, this);
            inventory.renderItemSplashes(g, screenPos);

            //Original rotation
            AffineTransform originalTransform = g.getTransform();

            //Rotate to face mouse
            g.rotate(Math.toRadians(rotation), screenPos[0], screenPos[1]);

            //Render Player
            g.drawImage(
                    Rendering.texture("player/top", "#ffffff"),
                    (int) (screenPos[0] - size / 2.0),
                    (int) (screenPos[1] - size / 2.0),
                    size, size, null
            );
            //Render armor over player
            armory.renderArmor(g, this);

            //Reset rotation
            g.setTransform(originalTransform);
        }

        //Biome of current world
        Biome biome = World.worlds.get(World.level).biome;

        //If biome is foggy, add light source at player
        if (biome.tags.contains("fog")) {
            HoneySuckle.lights.add(Map.of(
                    "posX", screenPos[0],
                    "posY", screenPos[1],
                    "radius", attributes.getOrDefault("lightRadius", 6)
            ));
        }

        if (inventory.ideaFrames >= 0) {
            g.drawImage(Rendering.texture("hud/idea", inventory.ideaColor), (int) screenPos[0] - 15, (int) screenPos[1] - 15 - size, 30, 30, null);
            inventory.ideaFrames--;
        }
    }

    //Update Player
    public void update(InputHandler input) {
        //Player/Armor attributes
        attributes = armory.getAttributes();

        //Friction
        for (int i = 0; i < 2; i++) {
            vel[i] /= 2;
            if (Math.abs(vel[i]) <= 0.2) {
                vel[i] = 0;
            }
        }

        //AKA magnitude of acceleration
        double incriment = 30.0 / FPS * TILE_SIZE * attributes.getOrDefault("speed", 0.1).doubleValue();

        //Get current world camera
        World world = World.worlds.get(World.level);
        double[] camera = world.camera;

        if (!inventory.isOpen) {
            //Toggle weaponScroll on scroll wheel click
            if (input.clickPressed(2)) {
                weaponScroll = !weaponScroll;
            }

            //Decide where scroll wheel affects
            if (weaponScroll) {
                armory.scrollBar(input.mouseScroll);
            } else {
                build.scrollBar(input.mouseScroll);
            }

            //Update player build and armory
            build.update(this, world, input);
            armory.updateControls(input, this);

            //Build on right click
            if (input.clickDown(3)) {
                build.build(World.worlds.get(World.level), this);
            }
        }
        // Updates Inventory
        armory.updateWeapons(this);
        inventory.update(input);

        //If space pressed, reset acel to dash acel
        if (input.keyPressed(32)) {
            //If have stamina...
            if (stamina == 1) {
                vel[0] = 0;
                vel[1] = 0;
                incriment = TILE_SIZE * 1.25;
                stamina = 0;
            }
        } else {
            //Recover stamina
            stamina += 0.1 * 30.0 / FPS;
            if (stamina > 1) {
                stamina = 1;
            }
        }

        //If going diaganol, divide by sqrt 2
        if ((input.keyDown(83) || input.keyDown(87)) && (input.keyDown(65) || input.keyDown(68))) {
            incriment /= Math.sqrt(2);
        }

        //Add acel to vel if key down
        if (input.keyDown(83)) {
            vel[1] += incriment;
        }
        if (input.keyDown(87)) {
            vel[1] -= incriment;
        }
        if (input.keyDown(65)) {
            vel[0] -= incriment;
        }
        if (input.keyDown(68)) {
            vel[0] += incriment;
        }

        //Reset camera
        World.worlds.get(World.level).camera[0] = pos[0];
        World.worlds.get(World.level).camera[1] = pos[1];

        double[] newCamera = World.worlds.get(World.level).camera;
        int[] worldSize = World.worlds.get(World.level).size;

        if (newCamera[0] - GAME_WIDTH / 2.0 < 0) {
            World.worlds.get(World.level).camera[0] = GAME_WIDTH / 2.0;
        }
        if (newCamera[0] + GAME_WIDTH / 2.0 > worldSize[0] * TILE_SIZE) {
            World.worlds.get(World.level).camera[0] = worldSize[0] * TILE_SIZE - GAME_WIDTH / 2.0;
        }
        if (newCamera[1] - GAME_HEIGHT / 2 < 0.0) {
            World.worlds.get(World.level).camera[1] = GAME_HEIGHT / 2.0;
        }
        if (newCamera[1] + GAME_HEIGHT / 2.0 > worldSize[1] * TILE_SIZE) {
            World.worlds.get(World.level).camera[1] = worldSize[1] * TILE_SIZE - GAME_HEIGHT / 2.0;
        }

        //Bound player to world
        pos = world.bound(pos, vel, size / 2.0);

        //World interact with player
        world.playerEvent(this);
        //Update armor
        armory.updateArmor(this);

        //Regenerate health
        if (health > 0) {
            double regen = attributes.getOrDefault("regen", 0.001).doubleValue();
            health += regen * 30.0 / FPS;
            if (vel[0] == 0 && vel[1] == 0) {
                health += regen * 30.0 / FPS;
            }
        }
        //Cap health
        double maxHealth = attributes.getOrDefault("maxHealth", 1).doubleValue();
        if (health > maxHealth) {
            health = maxHealth;
        }

        //Reset position of player on screen
        screenPos = new double[]{
            GAME_WIDTH / 2.0 + pos[0] - camera[0],
            GAME_HEIGHT / 2.0 + pos[1] - camera[1]
        };

        //Difference between mouse and player pos on screen
        double[] mouseDiff = new double[]{input.mousePos[0] - screenPos[0], input.mousePos[1] - screenPos[1]};

        //Rotate player to face mouse
        rotation = Math.toDegrees(Math.atan(mouseDiff[0] / -mouseDiff[1]));
        if (mouseDiff[1] >= 0) {
            rotation += 180;
        } else if (mouseDiff[0] <= 0) {
            rotation += 360;
        }
    }

    //Damage Player
    public void damage(double damage, boolean defense) {
        //Divide damage by defense
        if(defense){
            damage /= attributes.getOrDefault("defense", 1).doubleValue();
        }
        health -= damage;
    }
}


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

    //Static list of all players
    public static List<Player> players = new ArrayList<>();

    //Player Constructor
    public Player(double[] pos, int size, List<String> tags) {
        //Assign values to properties
        this.pos = pos;
        this.size = size;
        build = new Build(new LinkedHashSet<>(Arrays.asList(-1, -2, -3)));
        armory = new Armory(
            new Weapon[]{new Weapon("sword"), new Weapon("bow"), new Weapon("shield")},
            new Armor("leather")
        );
        inventory = new Inventory(
                Arrays.asList(armory.weapons),
                Arrays.asList(new Armor[]{armory.armor}),
                Map.of("wood", 4),
                null);

        //Adds player to list of players
        players.add(this);
    }

    //Player properties
    public double[] pos;
    public double[] vel = new double[2];
    public double[] screenPos = new double[2];
    public double rotation;

    public double health = 1;
    private double stamina = 1;

    public int size;

    public final Inventory inventory;
    public final Build build;
    public final Armory armory;

    //If scroll wheel goes to weapons or recipes
    private boolean weaponScroll = false;

    //Render Player
    public void render(Graphics2D g, double[] mousePos) {
        //If not dead, render
        if (health > 0) {
            //Render building and armory
            build.render(g, World.worlds.get(World.level), this);
            armory.render(g, this);

            //Original rotation
            AffineTransform originalTransform = g.getTransform();

            //Rotate to face mouse
            g.rotate(Math.toRadians(rotation), screenPos[0], screenPos[1]);

            //Render Player
            g.drawImage(
                    Rendering.texture("player", "#ffffff"),
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
        String biome = World.worlds.get(World.level).biome;

        //If biome is foggy, add light source at player
        if (Biome.biomeTags.get(biome).contains("fog")) {
            HoneySuckle.lights.add(Map.of(
                    "posX", (int) screenPos[0],
                    "posY", (int) screenPos[1],
                    "radius", HoneySuckle.tileSize * 6
            ));
        }
    }

    //Update Player
    public void update(boolean[] keyDown, double[] mousePos, boolean[] click, double scroll) {
        //Player/Armor attributes
        Map<String, Double> attributes = armory.getAttributes();

        //Friction
        for (int i = 0; i < 2; i++) {
            vel[i] /= 2;
            if (Math.abs(vel[i]) <= 0.2) {
                vel[i] = 0;
            }
        }

        //AKA magnitude of acceleration
        double incriment = 30.0 / HoneySuckle.fps * HoneySuckle.tileSize * attributes.get("speed");

        //Get current world camera
        World world = World.worlds.get(World.level);
        double[] camera = world.camera;

        //Toggle weaponScroll on scroll wheel click
        if (click[2]) {
            weaponScroll = !weaponScroll;
        }

        //Decide where scroll wheel affects
        if (weaponScroll) {
            armory.scrollBar(scroll);
        } else {
            build.scrollBar(scroll);
        }

        //Update player build and armory
        build.update(this, world, mousePos);
        armory.update(click, keyDown, this);

        //Build on right click
        if (click[3]) {
            build.build(World.worlds.get(World.level), this);
        }

        //If space pressed, reset acel to dash acel
        if (keyDown[32]) {
            //If have stamina...
            if (stamina == 1) {
                vel[0] = 0;
                vel[1] = 0;
                incriment = HoneySuckle.tileSize * 1.25;
                stamina = 0;
            }
        } else {
            //Recover stamina
            stamina += 0.1 * 30.0 / HoneySuckle.fps;
            if (stamina > 1) {
                stamina = 1;
            }
        }

        //If going diaganol, divide by sqrt 2
        if ((keyDown[83] || keyDown[87]) && (keyDown[65] || keyDown[68])) {
            incriment /= Math.sqrt(2);
        }

        //Add acel to vel if key down
        if (keyDown[83]) {
            vel[1] += incriment;
        }
        if (keyDown[87]) {
            vel[1] -= incriment;
        }
        if (keyDown[65]) {
            vel[0] -= incriment;
        }
        if (keyDown[68]) {
            vel[0] += incriment;
        }

        //Bound player to world
        pos = world.bound(pos, vel, size / 2.0);

        //Reset camera
        World.worlds.get(World.level).camera[0] = pos[0];
        World.worlds.get(World.level).camera[1] = pos[1];

        double[] newCamera = World.worlds.get(World.level).camera;
        int[] screenSize = HoneySuckle.size;
        int[] worldSize = World.worlds.get(World.level).size;

        if (newCamera[0] - screenSize[0] / 2.0 < 0) {
            World.worlds.get(World.level).camera[0] = screenSize[0] / 2.0;
        }
        if (newCamera[0] + screenSize[0] / 2.0 > worldSize[0] * HoneySuckle.tileSize) {
            World.worlds.get(World.level).camera[0] = worldSize[0] * HoneySuckle.tileSize - screenSize[0] / 2.0;
        }
        if (newCamera[1] - screenSize[1] / 2 < 0.0) {
            World.worlds.get(World.level).camera[1] = screenSize[1] / 2.0;
        }
        if (newCamera[1] + screenSize[1] / 2.0 > worldSize[1] * HoneySuckle.tileSize) {
            World.worlds.get(World.level).camera[1] = worldSize[1] * HoneySuckle.tileSize - screenSize[1] / 2.0;
        }

        //World interact with player
        world.playerEvent(this);
        //Update armor
        armory.updateArmor(this);

        //Regenerate health
        if (health > 0) {
            health += attributes.get("regen") * 30.0 / HoneySuckle.fps;
            if (vel[0] == 0 && vel[1] == 0) {
                health += attributes.get("regen") * 30.0 / HoneySuckle.fps;
            }
        }
        //Cap health
        if (health > attributes.get("maxHealth")) {
            health = attributes.get("maxHealth");
        }

        //Reset position of player on screen
        screenPos = new double[]{
            HoneySuckle.size[0] / 2.0 + pos[0] - camera[0],
            HoneySuckle.size[1] / 2.0 + pos[1] - camera[1]
        };

        //Difference between mouse and player pos on screen
        double[] mouseDiff = new double[]{mousePos[0] - screenPos[0], mousePos[1] - screenPos[1]};

        //Rotate player to face mouse
        rotation = Math.toDegrees(Math.atan(mouseDiff[0] / -mouseDiff[1]));
        if (mouseDiff[1] >= 0) {
            rotation += 180;
        } else if (mouseDiff[0] <= 0) {
            rotation += 360;
        }
    }

    //Damage Player
    public void damage(double damage) {
        //Player/Armor attributes
        Map<String, Double> attributes = armory.getAttributes();
        //Divide damage by defense
        health -= damage / attributes.get("defense");
    }
}

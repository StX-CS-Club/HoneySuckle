
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Map;

/*
 * Armory.java *
 - Class for managing player's combat components (Armor, Weapons, etc.)
 */
public class Armory {
    //Basic armory components
    public int weaponIndex = 0;
    public Weapon[] weapons = new Weapon[3];
    public Armor armor;

    //Measures clickData from last frame
    private boolean[] lastClick = new boolean[5];

    //Armory Constructor
    public Armory(Weapon[] weapons, Armor armor) {
        //Provides default equipment
        for(int i = 0; i < 3; i++){
            if(weapons.length > i){
                this.weapons[i] = weapons[i];
            }
        }
        this.armor = armor;
    }

    //Update Armory
    public void update(boolean[] click, boolean[] keyDown, Player player) {
        //If left click and selected weapon exists, attack
        if (click[1]) {
            if (weapons[weaponIndex] != null) {
                if(weapons[weaponIndex].constClick || !lastClick[1]){
                    weapons[weaponIndex].attack(player);
                }
            }
        }
        //Selects weapon from number key
        for (int i = 0; i < 3; i++) {
            if (keyDown[49 + i]) {
                weaponIndex = i;
                break;
            }
        }
        //If selected weapon exists, update it
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].update(player);
        }
        //Set lastClick to click at end of frame
        lastClick = click.clone();
    }

    //Update Armory Armor
    public void updateArmor(Player player){
        //If armor exists, update it
        if(armor != null){
            armor.update(player);
        }
    }

    //If armor exists, returns armor attributes
    public Map<String, Double> getAttributes(){
        if(armor != null){
            return armor.attributes;
        }
        //Default "Naked" attributes
        return Armor.armorAttributes.get("default");
    }

    //Updates selected weapon from scroll wheel
    public void scrollBar(double scroll) {
        if (Math.abs(scroll) >= 1) {
            weaponIndex += Math.signum(scroll);
            if (weaponIndex < 0) {
                weaponIndex = 2;
            }
            if (weaponIndex >= 3) {
                weaponIndex = 0;
            }
        }
    }

    //Render Armory
    public void render(Graphics2D g, Player player) {
        //Original rotation
        AffineTransform originalTransform = g.getTransform();

        //Rotate to match player rotation
        g.rotate(Math.toRadians(player.rotation), player.screenPos[0], player.screenPos[1]);

        //If weapon exists, render it
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].render(g, player);
        }

        //Resets rotation
        g.setTransform(originalTransform);
    }

    //Render Armory Armor
    public void renderArmor(Graphics2D g, Player player) {
        //If armor exists, render it
        if(armor != null){
            armor.render(g, player);
        }
    }

    //Render Armory UI
    public void renderUi(Graphics2D g, Player player) {
        //Size of slot
        double size = HoneySuckle.size[0] / 12;
        double xMargin = 0;

        //If player in bottom left corner, display in right corner
        if(player.screenPos[0] < size*3+HoneySuckle.tileSize && player.screenPos[1] > HoneySuckle.size[1]-size*25/12-HoneySuckle.tileSize){
            xMargin = HoneySuckle.size[0]-size*3;
        }

        //Go through each slot
        for (int i = 0; i < 3; i++) {
            //Color of "flowers" of slot
            String color = "#000000";
            if (i == weaponIndex) {
                color = "#eeeeff";
            }

            //Render slot
            g.drawImage(Rendering.texture("hud/icon", color), (int) (size * i + xMargin), HoneySuckle.size[1] - (int) size, (int) size, (int) size, null);
            if(weapons[i] != null){
                //Render Weapon Item
                Map<String, String> texture = weapons[i].texture;
                if(texture.get("itemTexture") != null){            
            g.drawImage(Rendering.texture(texture.get("itemTexture"), "#ffffff"), (int) (size * i + size / 8 + xMargin), HoneySuckle.size[1] - (int) size*7/8, (int) size*3/4, (int) size*3/4, null);
                }
            }
        }
    }
}

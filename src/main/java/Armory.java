
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Map;

public class Armory {

    public int weaponIndex = 0;
    public final Weapon[] weapons;

    public Armory(int size) {
        weapons = new Weapon[size];
        weapons[0] = new Weapon("sword");
        weapons[1] = new Weapon("bow");
    }

    public void update(int click, boolean[] keyDown, Player player) {
        if (click == 1) {
            if (weapons[weaponIndex] != null) {
                weapons[weaponIndex].attack(player);
            }
        }
        for (int i = 0; i < 3; i++) {
            if (keyDown[49 + i]) {
                weaponIndex = i;
                break;
            }
        }
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].update(player);
        }
    }

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

    public void render(Graphics2D g, AffineTransform originalTransform, Player player) {
        if (weapons[weaponIndex] != null) {
            weapons[weaponIndex].render(g, player);
        }

        g.setTransform(originalTransform);
        double size = HoneySuckle.size[0] / 12;
        double xMargin = 0;
        if(player.screenPos[0] < size*3+HoneySuckle.tileSize && player.screenPos[1] > HoneySuckle.size[1]-size*25/12-HoneySuckle.tileSize){
            xMargin = HoneySuckle.size[0]-size*3;
        }

        for (int i = 0; i < 3; i++) {
            String color = "#000000";
            if (i == weaponIndex) {
                color = "#eeeeff";
            }
            g.drawImage(Rendering.texture("hud/icon", color), (int) (size * i + xMargin), HoneySuckle.size[1] - (int) size, (int) size, (int) size, null);
            if(weapons[i] != null){
                Map<String, String> texture = weapons[i].texture;
                if(texture.get("itemTexture") != null){            
            g.drawImage(Rendering.texture(texture.get("itemTexture"), "#ffffff"), (int) (size * i + size / 8 + xMargin), HoneySuckle.size[1] - (int) size*7/8, (int) size*3/4, (int) size*3/4, null);
                }
            }
        }
    }
}

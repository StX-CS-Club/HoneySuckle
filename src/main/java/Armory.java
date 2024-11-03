
import java.awt.Graphics2D;

public class Armory {
    public int weaponIndex = 0;
    public final Weapon[] weapons;

    public Armory(int size){
        weapons = new Weapon[size];
        weapons[0] = new Weapon("sword");
    }

    public void update(int click, Player player){
        if(click == 1){
            weapons[weaponIndex].attack();
        }
        weapons[weaponIndex].update(player);
    }

    public void render(Graphics2D g, Player player){
        weapons[weaponIndex].render(g, player);
    }
}

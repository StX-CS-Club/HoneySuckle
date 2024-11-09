
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Inventory.java *
 - Class for managing player inventories
 */
public class Inventory {

    //Inventory data
    public final List<Weapon> weapons = new ArrayList<>();
    public final List<Armor> armors = new ArrayList<>();
    public final Map<String, Integer> items = new HashMap<>();
    public final Map<String, Integer> ammo = new HashMap<>();

    //Inventory Constructor
    public Inventory(
            List<Weapon> weapons,
            List<Armor> armors,
            Map<String, Integer> items,
            Map<String, Integer> ammo) {
                //If specified, adds shit to inventory
                if(weapons != null){
                    this.weapons.addAll(weapons);
                }
                if(armors != null){
                    this.armors.addAll(armors);
                }
                if(items != null){
                    this.items.putAll(items);
                }
                if(ammo != null){
                    this.ammo.putAll(ammo);
                }
    }
}

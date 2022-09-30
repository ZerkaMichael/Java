import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import utils.Sleep;

@ScriptManifest(author = "Ryder", info = "Enchants jewerly", name = "rJewerly", version = 0.1, logo = "")

public class Main extends Script {
    private enum State {
        ENCHANT, BANK, WAIT
    }

    @Override
    public int onLoop() throws InterruptedException {
        switch(getState()){
            case ENCHANT:
                enchant();
                break;
            case BANK:
                bank();
                break;
            case WAIT:
                sleep(Sleep.weighted(50, 80, 120));
                break;

        }
        return Sleep.weighted(1,15,39);
    }

    private void enchant() throws InterruptedException{
        if(getTabs().isOpen(Tab.MAGIC)){
            sleep(Sleep.weighted(50, 80, 120));
            magic.castSpell(Spells.NormalSpells.LVL_1_ENCHANT);
            sleep(Sleep.weighted(15, 35, 125));
            getInventory().interact("Cast", "Opal bracelet");
            sleep(Sleep.weighted(50, 80, 120));
        }else{
            getTabs().open(Tab.MAGIC);
            sleep(Sleep.weighted(50, 80, 120));
        }
    }

    private void bank() throws InterruptedException{
        NPC banker = getNpcs().closest(1633);
        if(banker.exists()){
            if (banker.interact("Bank")) {
                Sleep.sleepUntil(() -> (myPlayer().isInteracting(banker) && !myPlayer().isAnimating()), 5000);
                sleep(Sleep.weighted(85, 175, 800));
                getBank().depositAll("Expeditious bracelet");
                sleep(Sleep.weighted(85, 175, 800));
                getBank().withdrawAll("Opal bracelet");
                sleep(Sleep.weighted(85, 175, 800));
                getBank().close();
                sleep(Sleep.weighted(85, 175, 800));
            }
        }
    }


    private State getState() {
        if(getTabs().isOpen(Tab.MAGIC) && !myPlayer().isMoving() && getInventory().contains("Opal bracelet", "Cosmic rune")){
            return State.ENCHANT;
        }else if(!getInventory().contains("Opal bracelet")){
                return State.BANK;
        }else if(getTabs().isOpen(Tab.INVENTORY) || myPlayer().isMoving() || myPlayer().isAnimating()){
                return State.WAIT;
        }return null;
    }
}

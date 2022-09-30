import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import utils.Sleep;

@ScriptManifest(author = "Ryder", info = "Fishes and cooks at barbarian village", name = "rFish&Cook", version = 0.1, logo = "")

public class Main extends Script {
    long lastAnimation, count = 0;
    private enum State {
        FISH, COOK, DROP, WAIT
    }

    @Override
    public int onLoop() throws InterruptedException {
        switch(getState()){
            case COOK:
                if(myPlayer().isAnimating()) {
                    lastAnimation = System.currentTimeMillis();
                }else if(System.currentTimeMillis() > (lastAnimation+3000)) {
                    cook();
                }
                break;
            case DROP:
                drop();
                break;
            case FISH:
                fish();
                break;
            case WAIT:
                sleep(Sleep.weighted(50,100,500));
                count++;
                if(count == 10){
                    stop();
                }
                break;

        }
        return Sleep.weighted(50,100,500);
    }

    private void fish() throws InterruptedException{
        NPC fishingSpot = getNpcs().closest(1526);
        Position fishPos = fishingSpot.getPosition();
        if(fishingSpot.exists()){
            if (fishingSpot.interact("Lure")) {
                Sleep.sleepUntil(() -> (myPlayer().isInteracting(fishingSpot) && myPlayer().isAnimating()), 5000);
                sleep(Sleep.weighted(85, 650, 8000));
                mouse.moveOutsideScreen();
                Sleep.sleepUntil(() -> (!(myPlayer().isAnimating()) || getDialogues().isPendingContinuation() || getInventory().isFull()) || !(fishPos.equals(fishingSpot.getPosition())), 480000);
                sleep(Sleep.weighted(85, 250, 1010));
            }
        }
    }

    private void drop() throws InterruptedException{
        getInventory().dropAllExcept("Fly fishing rod", "Feather");
    }

    private void cook() throws InterruptedException{
        RS2Object fire = getObjects().closest(43475);
        if(fire.exists()){
            if(fire.interact("Cook")) {
                Sleep.sleepUntil(() -> (getDialogues().isPendingContinuation()), 3000);
                getDialogues().selectOption(1);
                lastAnimation = System.currentTimeMillis();
            }
        }
    }

    private State getState() {
        if(!myPlayer().isAnimating() && !myPlayer().isMoving() && getInventory().contains("Fly fishing rod", "Feather") && !getInventory().isFull()){
            return State.FISH;
        }else if(getInventory().isFull()) {
            if(getInventory().contains("Raw trout") || getInventory().contains("Raw salmon")){
                return State.COOK;
            }else if (getInventory().contains("Cooked trout", "Cooked salmon", "Burnt Fish") && !getInventory().contains("Raw trout", "Raw salmon")) {
                return State.DROP;
            }
        }
        return State.WAIT;
    }
}

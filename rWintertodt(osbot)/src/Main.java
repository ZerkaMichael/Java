import org.osbot.rs07.api.Chatbox;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.listener.MessageListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import utils.Sleep;

@ScriptManifest(author = "Ryder", info = "Does Wintertodt what do you think it does?", name = "rWintertodt", version = 0.7, logo = "")

public class Main extends Script implements MessageListener{
    long lastFletchXP, lastFMXP;
    String axe;
    Area bankArea = new Area(1628,3963,1642,3942);
    Area waitArea = new Area(1627,3984,1633,3976);
    Area doorArea = new Area(1624,3971,1636,3968);

    private enum State {CHOPLOG, FLETCH, FEEDBRAZIER,  BANK, HEAL, WAIT, RETURN, WAITAREA}

    @Override
    public int onLoop() throws InterruptedException{
        switch(getState()){
            case CHOPLOG:
                chopLog();
                sleep(Sleep.weighted(50, 85, 125));
                break;
            case FEEDBRAZIER:
                if(getSkills().getExperience(Skill.FIREMAKING) > lastFMXP){
                    lastFMXP = getSkills().getExperience(Skill.FIREMAKING);
                    sleep(Sleep.weighted(1250, 1750, 3000));
                }else if(getSkills().getExperience(Skill.FIREMAKING) == lastFMXP){
                    lastFMXP = getSkills().getExperience(Skill.FIREMAKING);
                    feedBrazier();
                }
                break;
            case FLETCH:
                if(getSkills().getExperience(Skill.FLETCHING) > lastFletchXP){
                    lastFletchXP = getSkills().getExperience(Skill.FLETCHING);
                    sleep(Sleep.weighted(900, 1750, 2750));
                }else if(getSkills().getExperience(Skill.FLETCHING) == lastFletchXP){
                    lastFletchXP = getSkills().getExperience(Skill.FLETCHING);
                    fletch();
                }
                break;
            case BANK:
                bank();
                break;
            case HEAL:
                heal();
                break;
            case RETURN:
                enter();
                break;
            case WAITAREA:
                gotoWaitArea();
                break;
            case WAIT:
                Sleep.weighted(5,35,150);
                break;
        }
        return Sleep.weighted(50,100,250);
    }

    public void enter() throws InterruptedException{
        RS2Object door = getObjects().closest(29322);
        getCamera().moveYaw(Sleep.weighted(120, 180, 250));
        if(door.isVisible()){
            if(door.interact("Enter")){
                Sleep.sleepUntil(() -> (!myPlayer().isMoving() && !myPlayer().isAnimating()), 10000);
                sleep(Sleep.weighted(3500, 4000, 5000));
            }
        }else{
            if(getWalking().walk(door)){
                Sleep.sleepUntil(() -> (!myPlayer().isMoving() && !myPlayer().isAnimating()), 10000);
                sleep(Sleep.weighted(1000, 2000, 4000));
            }
        }
    }

    public void gotoWaitArea() throws InterruptedException{
        if(waitArea.contains(myPosition())){
            getMouse().moveOutsideScreen();
            Sleep.sleepUntil(() -> (getInventory().contains("Supply crate") || Integer.parseInt(getWidgets().getWidgetContainingText("Wintertodt's Energy:").getMessage().replaceAll("\\D+", "")) >= 90), 10000);
        }else{
            getWalking().walk(waitArea.getRandomPosition());
            Sleep.sleepUntil(() -> (!myPlayer().isMoving() && !myPlayer().isAnimating()), 10000);
        } sleep(Sleep.weighted(550, 1350, 2600));
    }

    public void chopLog() throws InterruptedException{
        RS2Object root = getObjects().closest(29311);
        if(root.exists()){
            if(root.interact("Chop")){
                Sleep.sleepUntil(() -> (!myPlayer().isMoving() & myPlayer().isAnimating()), 5000);
            }
        }
    }

    public void fletch() throws InterruptedException{
        if(getInventory().interact("Use", "Knife")){
            Sleep.weighted(15,20,145);
            getInventory().getItem("Bruma root").interact();
            Sleep.sleepUntil(() -> (!myPlayer().isMoving() & myPlayer().isAnimating()), 1500);
            //Sleep.sleepUntil(() -> (!myPlayer().isMoving() & !myPlayer().isAnimating()), 30000);
        }
    }

    public void feedBrazier() throws InterruptedException{
        if(getChatbox().contains(Chatbox.MessageType.GAME, "The brazier")){
            sleep(Sleep.weighted(3000, 4000, 5000));
        }
        RS2Object brazier = getObjects().closest(29314);
        if(brazier.exists()){
            if(brazier.interact("Feed")){
                sleep(Sleep.weighted(1500, 1600, 2500));
            }
        }
    }

    public void heal() throws InterruptedException{
        if(getInventory().contains(1895)){
            getInventory().interact("Eat", 1895);
            Sleep.weighted(150,300,500);
        }else if(getInventory().contains(1893)){
            getInventory().interact("Eat", 1893);
            Sleep.weighted(150,300,500);
        }else if(getInventory().contains(1891)){
            getInventory().interact("Eat", 1891);
            Sleep.weighted(150,300,500);
        }
    }

    public void bank() throws InterruptedException{
        RS2Object bank = getObjects().closest(29321);
        int axeSlot = getInventory().getSlotForNameThatContains(" axe");
        String axe = getInventory().getItemInSlot(axeSlot).getName();
        if(bank.exists() && bank.isVisible() && bankArea.contains(myPosition())){
            if(bank.interact("Bank")) {
                Sleep.sleepUntil(() -> (getBank().isOpen()), 10000);
                getBank().depositAllExcept(axe, "Knife", "Tinderbox", "Cake", "Supply crate");
                Sleep.weighted(75,225,400);
                getInventory().interact("Deposit-1", "Supply crate");
                Sleep.weighted(75,225,400);
                int cakeAmount = (int) getInventory().getAmount("Cake");
                int amountToBring = 5;
                if (cakeAmount != amountToBring) {
                    getBank().withdraw("Cake", (amountToBring - cakeAmount));
                    Sleep.weighted(75,225,400);
                }
            }
        }else if(bankArea.contains(myPosition())){
            getWalking().walk(bank);
            Sleep.sleepUntil(() -> (!myPlayer().isMoving() & !myPlayer().isAnimating()), 30000);
        }else{
            if(!waitArea.contains(myPosition()) && !doorArea.contains(myPosition())){
                getWalking().webWalk(waitArea);
                Sleep.sleepUntil(() -> (!myPlayer().isMoving() & !myPlayer().isAnimating()), 30000);
            }
            enter();
        }
    }


    public int getPoints(){
        if(getWidgets().getWidgetContainingText("Points") != null){
            return Integer.parseInt(getWidgets().getWidgetContainingText("Points").getMessage().replaceAll("\\D+", ""));
        }return 0;
    }

    public int getWtTime(){
        if(getWidgets().getWidgetContainingText("Wintertodt's Energy:") != null){
            return Integer.parseInt(getWidgets().getWidgetContainingText("Wintertodt's Energy:").getMessage().replaceAll("\\D+", ""));
        }return 0;
    }

    private State getState(){
        if(getSkills().getDynamic(Skill.HITPOINTS) <= 4){
            return State.HEAL;
        }else if(myPlayer().isMoving() || myPlayer().isAnimating()){
            return State.WAIT;
        }else if(getInventory().contains("Supply crate")){
            return State.BANK;
        }else if(getInventory().contains(axe, "Knife", "Tinderbox, Cake") && !getInventory().contains("Supply crate") && bankArea.contains(myPosition()) && !waitArea.contains(myPosition())) {
            return State.RETURN;
        }else if(getWtTime() <= 10 && getPoints() >= 500 && !getInventory().contains("Supply crate") && getInventory().contains("Bruma root")){
            return State.FEEDBRAZIER;
        }else if(getWtTime() >= 20 && getPoints() >= 500 && !getInventory().contains("Supply crate", "Bruma kindling", "Bruma root")){
            return State.CHOPLOG;
        }else if(!bankArea.contains(myPosition()) && getWtTime() == 0  || getPoints() >= 500 && !getInventory().contains("Supply crate", "Bruma kindling", "Bruma root")){
           return State.WAITAREA;
        }else if(getInventory().contains(axe, "Knife", "Tinderbox") && !getInventory().isFull() && !getInventory().contains("Bruma kindling") && !bankArea.contains(myPosition())){
            return State.CHOPLOG;
        }else if(getPoints() < 500 && getInventory().contains("Bruma root") && getInventory().contains("Bruma kindling") || getInventory().getAmount("Bruma root") >= 20){
            return State.FLETCH;
        }else if(getInventory().contains("Bruma kindling") && !getInventory().contains("Bruma root")){
            return State.FEEDBRAZIER;
        }return null;
    }
}
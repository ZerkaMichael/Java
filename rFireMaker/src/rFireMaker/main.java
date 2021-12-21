package rFireMaker;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import javax.swing.text.html.Map;
import java.util.List;
import java.util.Queue;

@ScriptManifest(name = "rFireMaker", info="Makes Fires dude what you think", logo= "", version = 0.1, author = "")



public class main extends Script {
    private int tinderBoxId = 590;
    Area fm_Area = new Area(3209,3430,3206,3427);
    private String[] groundObjects = {"Fire", "Daisies", "Fern", "Stones", "Thistle"};

    @Override
    public int onLoop() throws InterruptedException {
        switch(getState()) {
            case BANK:
               bank();
               break;
            case WALK_TO_LOCATION:
                walkToFM();
                break;
            case FIRE_MAKE:
                burnLogs();
                break;
            case QUIT:
                stop();
                break;
            case WAIT:
                new ConditionalSleep(random(250,7500)) {
                    @Override
                    public boolean condition() throws InterruptedException {
                        return myPlayer().isAnimating();
                    }
                }.sleep();
            case ERROR:
                stop();
                break;
        }

        return 100;
    }


    private enum State {
        BANK, FIRE_MAKE, WALK_TO_LOCATION, WAIT, QUIT, ERROR
    }

    private State getState(){
        if(inventory.isFull() && inventory.contains(tinderBoxId) && inventory.contains(getWoodType())) return State.WALK_TO_LOCATION;
        if(inventory.isEmptyExcept(tinderBoxId)) return State.BANK;
        if(fm_Area.contains(myPlayer())) return State.FIRE_MAKE;
        if(isFinished()) return State.QUIT;
        if(myPlayer().isAnimating() || myPlayer().isMoving() || myPlayer().exists()) return State.WAIT;

        return State.ERROR;
    }

    private String getWoodType() {
        if (getSkills().getDynamic(Skill.FIREMAKING) >= 15) {
            return "1521";
        } else{
            return "1511";
        }

    }

    private void bank() throws InterruptedException {
        if (!Banks.VARROCK_WEST.contains(myPlayer())) {
            getWalking().webWalk(Banks.VARROCK_WEST);
        } else {
            if (!getBank().isOpen()) {
                getBank().open();
            } else {
                getBank().withdrawAll(getWoodType());
            }
        }
    }

    private void walkToFM() throws InterruptedException {
        if (!fm_Area.contains(myPlayer())) {
            getWalking().webWalk((fm_Area));
        }
    }

    public boolean isFinished() {return getSkills().getDynamic(Skill.FIREMAKING) >= 50 || getBank().isOpen() && !getBank().contains(getWoodType());}

    public void burnLogs() throws InterruptedException {
        Position pol = myPlayer().getPosition().translate(14, -2);
        for (int i = 1; i <= 14; i++) {
            if (map.canReach(pol) && map.realDistance(pol) > 0 && map.realDistance(pol) < 16) {
                WalkingEvent newPosition = new WalkingEvent(new Position(pol));
                execute(newPosition);
                break;
            } else {
                pol = myPlayer().getPosition().translate(14 - i, 0);
                if (i == 14 && !map.canReach(pol)) {
                    pol = myPlayer().getPosition().translate(14 - i, 1);
                    i = 0;
                }
            }
        }

        private Queue<Position> getFMPositions() {
            List<Position> allPositions = location.getArea().getPositions();

            // Remove any position with an object (except ground decorations, as they can be walked on)
            for (RS2Object object : getObjects().getAll()) {
                if (object instanceof GroundDecoration) {
                    continue;
                }
                allPositions.removeIf(position -> object.getPosition().equals(position));
            }

            // Sort positions into rows
            HashMap<Integer, List<Position>> rows = new HashMap<>();
            for (Position position : allPositions) {
                rows.putIfAbsent(position.getY(), new ArrayList<>());
                rows.get(position.getY()).add(position);
            }

            if (rows.isEmpty()) {
                return new LinkedList<>();
            }

            // Find the longest consecutive row
            Queue<Position> longestConsecutiveRow = new LinkedList<>();
            for (List<Position> row : rows.values()) {

                row.sort((p1, p2) -> Integer.compare(p2.getX(), p1.getX()));

                ArrayDeque<Position> current = new ArrayDeque<>();

                for (Position position : row) {
                    if (current.isEmpty()) {
                        current.addLast(position);
                    } else if (position.getX() == current.getLast().getX() - 1) {
                        current.addLast(position);
                    } else if (current.size() > longestConsecutiveRow.size()) {
                        longestConsecutiveRow = new LinkedList<>(current);
                        current.clear();
                    } else {
                        current.clear();
                    }
                }
            }

            return longestConsecutiveRow;
        }
    }



}

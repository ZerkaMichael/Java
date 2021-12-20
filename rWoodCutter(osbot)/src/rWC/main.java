package rWC;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(name = "rWoodCutter", info="Cuts trees based on level for HCIM accounts outside of varrock west bank. Will bank logs.", logo= "", version = 0.1, author = "")

public class main extends Script {

    private int i = 0;

    @Override
    public void onStart() throws InterruptedException{
        super.onStart();
    }

    @Override
    public void onExit() throws InterruptedException{
        super.onExit();
    }

    @Override
    public int onLoop() throws InterruptedException{
        i = random(1,1000);
        if(i >=998){
            camera.moveYaw(random(-180,180));
            camera.movePitch(random(-180,180));
            i = 0;
        }

        if(shouldBank()){
            bank();
        }else{
            cutTree(getTreeName());
        }
        return 100;
    }

    private boolean shouldBank(){
        return getInventory().isFull();
    }

    private void bank() throws InterruptedException{
        if(!Banks.VARROCK_WEST.contains(myPlayer())){
            getWalking().webWalk(Banks.VARROCK_WEST);
        }else{
            if(!getBank().isOpen()){
                getBank().open();;
            }else{
                getBank().depositAllExcept(axes -> axes.getName().contains(" axe"));
            }
        }
    }

    private void cutTree(String treeName){
        if(!getTreeArea().contains(myPlayer())){
            getWalking().webWalk(getTreeArea());
        }else{
            RS2Object tree = getObjects().closest(treeName);
            if(!myPlayer().isAnimating() && tree != null && getTreeArea().contains(tree.getPosition())){
                if(!tree.isVisible()){
                    camera.toEntity(tree);
                    camera.moveYaw(random(-50,50));
                    camera.movePitch(random(-50,50));
                    if(tree.interact("Chop down")) {
                        new ConditionalSleep(5000) {
                            @Override
                            public boolean condition() throws InterruptedException {
                                return myPlayer().isAnimating();
                            }
                        }.sleep();
                    }
                }
            }
        }
    }

    private Area getTreeArea(){
        if(getSkills().getDynamic(Skill.WOODCUTTING) >= 60){
            return new Area(3203, 3506, 3225, 3497);
        }else if(getSkills().getDynamic(Skill.WOODCUTTING) >= 15){
            return new Area(3158, 3424, 3172, 3409);
        }else{
            return new Area(3145, 3465, 3172, 3450);
        }
    }

    private String getTreeName(){
        if(getSkills().getDynamic(Skill.WOODCUTTING) >= 60){
            return "Yew";
        }else if(getSkills().getDynamic(Skill.WOODCUTTING) >= 15){
            return "Oak";
        }else{
            return "Tree";
        }

    }


}

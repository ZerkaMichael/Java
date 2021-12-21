package net.runelite.client.plugins.rChickenSlayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.iutils.*;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;

import net.runelite.client.ui.overlay.OverlayManager;

@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "rChicken Slayer",
        enabledByDefault = false,
        description = "Kills Chickens with custom location support",
        tags = {"chicken", "feather", "combat","rchicken"}

)
@Slf4j
public class rChickenSlayerPlugin extends Plugin{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private rChickenSlayerOverlay overlay;

    @Inject
    private rChickenSlayerConfiguration config;

    @Inject
    private iUtils iUtils;

    @Inject
    private MouseUtils mouse;

    @Inject
    private PlayerUtils playerUtils;

    @Inject
    private InventoryUtils inventory;

    @Inject
    private InterfaceUtils interfaceUtils;

    @Inject
    private CalculationUtils calc;

    @Inject
    private MenuUtils menu;

    @Inject
    private NPCUtils npc;

    @Inject
    private WalkUtils walk;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ItemManager itemManager;

    Instant botTimer;
    Player player;
    MenuEntry targetMenu;
    String states;
    String status;
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    WorldPoint customLocation = new WorldPoint(3228,3297,0);
    long sleepLength;
    int tickLength;
    int timeout;
    boolean startBot;
    boolean walkToChicken;
    boolean waiting;

    @Provides
    rChickenSlayerConfiguration provideConfig(ConfigManager configManager) {
        return configManager.getConfig(rChickenSlayerConfiguration.class);
    }

    @Override
    protected void startUp() {
        iUtils.sendGameMessage("/||Plugin Started||\\");
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        botTimer = null;
        walkToChicken = false;
        startBot = false;
        waiting = false;
        overlayManager.remove(overlay);

    }

    @Subscribe
    public void onConfigButtonClicked(ConfigButtonClicked event) {
        if (!event.getGroup().equalsIgnoreCase("rChickenSlayer")) {
            return;
        }

        if (event.getKey().equals("startButton")) {
            iUtils.sendGameMessage("starting");
            botTimer = Instant.now();
            walkToChicken = true;
            startBot = true;
            waiting = false;
        } else if(event.getKey().equals("stopButton")) {
            iUtils.sendGameMessage("stopping");
            startBot = false;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!startBot){
            return;
        }
        player = client.getLocalPlayer();
        if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
            if(timeout > 0){
                timeout--;
            } else {
                states = getState();
                switch(states) {
                    case "TIMEOUT":
                        status = "Timeout";
                        timeout--;
                    break;
                    case "WAIT":
                        if(player.getWorldLocation().distanceTo(customLocation) > 20){
                            walk.webWalk(customLocation, 5, playerUtils.isMoving(beforeLoc), sleepDelay());
                            status = "Walking to chicken coop";
                        }else{
                            status = "At coop";
                            walkToChicken = false;
                            waiting = true;
                        }
                    break;
                    case "ATTACK":
                        if (!inCombat()) {
                            status = "Attacking";
                            NPC chickenNpc = npc.findNearestAttackableNpcWithin(customLocation, 20, "Chicken", true);
                            if (chickenNpc != null) {
                                targetMenu = new MenuEntry("", "", chickenNpc.getIndex(), MenuAction.NPC_SECOND_OPTION.getId(), 0, 0, false);
                                iUtils.doActionMsTime(targetMenu, chickenNpc.getConvexHull().getBounds(), sleepDelay());
                                timeout = tickDelay() + 1;
                            }
                        } else {
                            status = "Sleep";
                            timeout = tickDelay() + 1;
                        }
                    break;
                }
            }
            beforeLoc = player.getLocalLocation();
        }

    }

    private String getState() {
        if (walkToChicken) {
            return "WAIT";
        }
        if (timeout > 0) {
            playerUtils.handleRun(20, 20);
            return "TIMEOUT";
        }
        if (waiting) {
            return "ATTACK";
        } else {
            return null;
        }
    }

    private boolean inCombat() {
        NPC currentNPC = (NPC) player.getInteracting();
        if (currentNPC == null) {
            return false;
        } else {
            return true;
        }
    }

    private long sleepDelay() {
        sleepLength = calc.randomDelay(true, 55,525,50,150);
        return sleepLength;
    }

    private int tickDelay() {
        tickLength = (int) calc.randomDelay(false, 1,5,2,2);
        return tickLength;
    }
}

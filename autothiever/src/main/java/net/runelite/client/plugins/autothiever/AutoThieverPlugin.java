package net.runelite.client.plugins.autothiever;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import net.runelite.client.plugins.iutils.*;
import net.runelite.client.plugins.PluginDependency;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import static net.runelite.client.plugins.iutils.iUtils.iterating;


@Extension
@PluginDependency(iUtils.class)
@PluginDescriptor(
        name = "Auto Thiever",
        description = "Automatically thieves npcs",
        tags = {"auto", "thiever", "thieving", "skill", "skilling"},
        enabledByDefault = false
)
public class AutoThieverPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

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
    private BankUtils bank;

    @Inject
    private ObjectUtils object;

    @Inject
    private NPCUtils npc;

    @Inject
    private WalkUtils walk;

    @Inject
    private AutoThieverConfig config;

    @Inject
    private ItemManager itemManager;

    GameObject targetObject;
    private MenuEntry entry;
    private Random r = new Random();
    private int nextOpenPouchCount;
    private boolean emptyPouches = false;
    private boolean pluginStarted = false;
    long sleepLength;
    int tickLength;
    int timeout;
    private int count;
    WorldPoint customLocation = new WorldPoint(2653,3286,0);
    LocalPoint beforeLoc = new LocalPoint(0, 0);
    Player player;
    MenuEntry targetMenu;
    Set<Integer> NECK = Set.of(ItemID.DODGY_NECKLACE);

    @Provides
    AutoThieverConfig provideConfig(final ConfigManager configManager) {
        return configManager.getConfig(AutoThieverConfig.class);
    }

    @Override
    protected void startUp() {
        nextOpenPouchCount = getRandom(1, 28);
        iUtils.sendGameMessage("/||Plugin Started||\\");
    }

    @Override
    protected void shutDown() {
    }

    @Subscribe
    public void onConfigButtonClicked(ConfigButtonClicked event) {
        if (!event.getGroup().equals(AutoThieverConfig.class.getAnnotation(ConfigGroup.class).value())) {
            return;
        }

        if (event.getKey().equals("startButton")) {
            pluginStarted = true;
            nextOpenPouchCount = getRandom(1, 28);
        } else if (event.getKey().equals("stopButton")) {
            pluginStarted = false;
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        final String message = event.getMessage();
        if (event.getType() == ChatMessageType.SPAM) {
            if (message.startsWith("You pickpocket") || message.startsWith("You pick-pocket") || message.startsWith("You steal") || message.startsWith("You successfully pick-pocket") || message.startsWith("You successfully pick") || message.startsWith("You successfully steal") || message.startsWith("You pick the knight") || message.startsWith("You pick the Elf")) {
                timeout = 0;
            } else if (message.startsWith("You fail to pick") || message.startsWith("You fail to steal")) {
                timeout = getRandom(config.clickDelayMin(), config.clickDelayMax());
            } else if (message.startsWith("You open all of the pouches")) {
                emptyPouches = false;
                nextOpenPouchCount = getRandom(1, 28);
            } else if (event.getType() == ChatMessageType.GAMEMESSAGE) {
                if (message.startsWith("You need to empty your")){
                    emptyPouches = true;
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event){
        player = client.getLocalPlayer();
        beforeLoc = player.getLocalLocation();

        if (timeout > 0) {
            timeout--;
            return;
        }
        if(playerUtils.isInteracting() || playerUtils.isMoving() || playerUtils.isAnimating() || iterating){
            timeout = 1;
            return;
        }
        if (!pluginStarted){
            return;
        }

        if(bank.isOpen()){
            handleBanking();
            timeout = tickDelay()+ 3;
            return;
        }

        if(shouldBank()){
            openBank();
            timeout = tickDelay()+ 5;
            return;
        }

        if (shouldEat()) {
            eat();
            return;
        }

        if(shouldEquipNeck()){
            wearNeck();
            timeout = 5;
            return;
        }

        handleRandomPouchOpening();

        if (emptyPouches) {
            openPouches();
            return;
        }

        handleThieving();
    }

    public void handleThieving(){
        NPC thieveNPC = npc.findNearestNpc(config.npcId());
        if(thieveNPC == null){
            iUtils.sendGameMessage("npc null");
            return;
        }else{
            targetMenu = new MenuEntry("Pickpocket", "", thieveNPC.getIndex(), MenuAction.NPC_THIRD_OPTION.getId(), 0, 0, false);
            if(config.useInvoke()){
                iUtils.doInvokeMsTime(targetMenu, sleepDelay());
            }else{
                iUtils.doNpcActionMsTime(thieveNPC, MenuAction.NPC_THIRD_OPTION.getId(), sleepDelay());
            }
        }timeout = 1;
    }


    public void handleRandomPouchOpening() {
        WidgetItem item = getInventoryItem(ItemID.COIN_POUCH, ItemID.COIN_POUCH_22522, ItemID.COIN_POUCH_22523, ItemID.COIN_POUCH_22524,
                ItemID.COIN_POUCH_22525, ItemID.COIN_POUCH_22526, ItemID.COIN_POUCH_22527, ItemID.COIN_POUCH_22528,
                ItemID.COIN_POUCH_22529, ItemID.COIN_POUCH_22530, ItemID.COIN_POUCH_22531, ItemID.COIN_POUCH_22532,
                ItemID.COIN_POUCH_22533, ItemID.COIN_POUCH_22534, ItemID.COIN_POUCH_22535, ItemID.COIN_POUCH_22536,
                ItemID.COIN_POUCH_22537, ItemID.COIN_POUCH_22538);

        if (item == null) {
            return;
        }

        if (item.getQuantity() >= nextOpenPouchCount) {
            emptyPouches = true;
        }
    }

    public void openPouches() {
        WidgetItem item = getInventoryItem(ItemID.COIN_POUCH, ItemID.COIN_POUCH_22522, ItemID.COIN_POUCH_22523, ItemID.COIN_POUCH_22524,
                ItemID.COIN_POUCH_22525, ItemID.COIN_POUCH_22526, ItemID.COIN_POUCH_22527, ItemID.COIN_POUCH_22528,
                ItemID.COIN_POUCH_22529, ItemID.COIN_POUCH_22530, ItemID.COIN_POUCH_22531, ItemID.COIN_POUCH_22532,
                ItemID.COIN_POUCH_22533, ItemID.COIN_POUCH_22534, ItemID.COIN_POUCH_22535, ItemID.COIN_POUCH_22536,
                ItemID.COIN_POUCH_22537, ItemID.COIN_POUCH_22538);
        if (item == null) {
            iUtils.sendGameMessage("pouch null");
            return;
        }else{
            targetMenu = new MenuEntry("Open-all", "", item.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false);
            if(config.useInvoke()){
                iUtils.doInvokeMsTime(targetMenu, sleepDelay());
            }else{
                iUtils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepDelay());
            }
        }timeout = 1;
    }

    private boolean shouldEquipNeck() {
        return !playerUtils.isItemEquipped(NECK) && inventory.containsItem(NECK) && config.useNeck();
    }

    private boolean shouldBank() {
        return !inventory.containsItem(ItemID.JUG_OF_WINE);
    }

    public void wearNeck() {
        WidgetItem neck = getInventoryItem(ItemID.DODGY_NECKLACE);
        if (neck == null) {
            iUtils.sendGameMessage("null neck");
            return;
        }else{
            targetMenu = new MenuEntry("Wear", "", neck.getId(), MenuAction.ITEM_SECOND_OPTION.getId(), neck.getIndex(), WidgetInfo.INVENTORY.getId(), false);
            if(config.useInvoke()){
                iUtils.doInvokeMsTime(targetMenu, sleepDelay());
            }else{
                iUtils.doActionMsTime(targetMenu, neck.getCanvasBounds(), sleepDelay());
            }
        }
        timeout = 2;
    }

    public void eat() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return;
        }

        List<WidgetItem> list = inventoryWidget.getWidgetItems().stream().filter(item -> config.itemId() == item.getId()).collect(Collectors.toList());

        if (list.isEmpty()) {
            return;
        }

        WidgetItem item = list.get(0);

        if(item == null){
            return;
        }else{
            targetMenu = new MenuEntry("", "", item.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), item.getIndex(), WidgetInfo.INVENTORY.getId(), false);
            if(config.useInvoke()){
                iUtils.doInvokeMsTime(targetMenu, sleepDelay());
            }else{
                iUtils.doActionMsTime(targetMenu, item.getCanvasBounds(), sleepDelay());
            }
        }timeout = 4;
    }

    public boolean shouldEat() {
        switch (config.hpCheckStyle()) {
            case EXACT_HEALTH:
                return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpToEat();

            case PERCENTAGE:
                return (((float) client.getBoostedSkillLevel(Skill.HITPOINTS) / (float) client.getRealSkillLevel(Skill.HITPOINTS)) * 100.f) <= (float) config.hpToEat();
        }

        return false;
    }

    public int getRandom(int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (entry != null) {
            event.setMenuEntry(entry);
        }

        entry = null;
    }

    public void openBank() {
        GameObject bankTarget = object.findNearestBank();
        if (bankTarget != null) {
            iUtils.sendGameMessage("Bank found");
            targetMenu = new MenuEntry("", "", bankTarget.getId(),
                    bank.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(),
                    bankTarget.getSceneMinLocation().getY(), false);
            iUtils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
        } else {
            iUtils.sendGameMessage("Bank not found");
        }
    }

    public void handleBanking(){
        if(inventory.containsItemAmount(ItemID.JUG_OF_WINE, 22, false, true)&&inventory.containsItemAmount(ItemID.DODGY_NECKLACE, 4, false, true)){
            bank.close();
        }else{
            if(bank.contains(ItemID.DODGY_NECKLACE, 4)){
                count = 4 - (inventory.getItemCount(ItemID.DODGY_NECKLACE, false));
                if(count != 0){
                    bank.withdrawItemAmount(ItemID.DODGY_NECKLACE, count);
                    timeout = tickDelay()+2;
                }
            }
            if(inventory.containsItem(ItemID.JUG)){
                bank.depositAllOfItem(ItemID.JUG);
                timeout = tickDelay()+2;
            }
            if(bank.contains(ItemID.JUG_OF_WINE, 22)){
                count = 22 - (inventory.getItemCount(ItemID.JUG_OF_WINE, false));
                if(count != 0){
                    bank.withdrawItemAmount(ItemID.JUG_OF_WINE, count);
                    timeout = tickDelay()+2;
                }
            }
        }
    }

    public WidgetItem getInventoryItem(int... ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null) {
            return null;
        }

        for (WidgetItem item : inventoryWidget.getWidgetItems()) {
            if (Arrays.stream(ids).anyMatch(i -> i == item.getId())) {
                return item;
            }
        }

        return null;
    }
    private long sleepDelay(){
        sleepLength = calc.randomDelay(true, 55,525,50,150);
        return sleepLength;
    }

    private int tickDelay(){
        tickLength = (int) calc.randomDelay(true, 1,5,2,2);
        return tickLength;
    }
}
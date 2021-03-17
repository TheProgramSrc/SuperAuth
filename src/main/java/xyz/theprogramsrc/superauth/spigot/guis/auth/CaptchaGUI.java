package xyz.theprogramsrc.superauth.spigot.guis.auth;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.objects.GUIRows;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.xseries.XMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CaptchaGUI extends GUI {

    private boolean wrong;
    private final int generated;
    private final HashMap<Integer, Integer> numbers;
    private final boolean registering;
    private final int[] locs;
    private final List<Integer> used;

    public CaptchaGUI(Player player, boolean registering){
        super(player);
        this.registering = registering;
        this.numbers = new HashMap<>();
        this.generated = Utils.random(2, 64);
        this.locs = new int[]{11, 13, 15};
        this.numbers.put(locs[Utils.random(0, 2)], this.generated);
        this.used = Utils.toList(0,11,13,15);
        this.generateOthers();
        this.wrong = false;

        SuperAuth.spigot.runEvent(new SuperAuthBeforeCaptchaEvent(SuperAuth.spigot.getAuthSettings(), SuperAuth.spigot.getUserStorage(), player.getName()));
        this.open();

        if(!registering){
            UserStorage userStorage = SuperAuth.spigot.getUserStorage();
            new BukkitRunnable(){
                @Override
                public void run() {
                    User user = userStorage.get(player.getName());
                    if(user != null){
                        if(!user.isAuthorized()){
                            if(ForceLoginMemory.i.has(player.getName())){
                                SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(SuperAuth.spigot.getAuthSettings(), SuperAuth.spigot.getUserStorage(), player.getName()));
                                CaptchaGUI.this.close();
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(this.spigotPlugin, 0L, 5L);
        }
    }

    @Override
    public boolean canCloseGUI() {
        return ForceLoginMemory.i.has(this.getPlayer().getName());
    }

    @Override
    protected String getTitle() {
        return (this.wrong ? LBase.WRONG_CAPTCHA_GUI_TITLE : LBase.AUTH_CAPTCHA_GUI_TITLE.options().vars(this.generated+"")) + "";
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.THREE;
    }

    @Override
    protected GUIButton[] getButtons() {
        GUIButton[] numbers = this.numbers.entrySet().stream().map(e-> this.getNumberButton(e.getValue(), e.getKey())).toArray(GUIButton[]::new);
        List<GUIButton> list = new ArrayList<>(Utils.toList(numbers));
        list.add(this.getDisconnectButton());
        for(int i = 0; i < 27; ++i){
            if(!this.used.contains(i)){
                list.add(new GUIButton(i, this.getPreloadedItems().emptyItem()));
            }
        }
        GUIButton[] buttons = new GUIButton[list.size()];
        buttons = list.toArray(buttons);
        return buttons;
    }

    private GUIButton getNumberButton(int number, int slot){
        SimpleItem item = new SimpleItem(XMaterial.OAK_BUTTON)
                .setDisplayName("&a" + number)
                .setAmount(number);
        return new GUIButton(slot, item, a->{
            if(number != this.generated){
                this.wrong = true;
                this.open();
                this.getSpigotTasks().runTaskLater(45L, ()->{
                    this.wrong = false;
                    this.open();
                });
            }else{
                this.close();
                SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(SuperAuth.spigot.getAuthSettings(), SuperAuth.spigot.getUserStorage(), a.getPlayer().getName()));
                new ActionManager(a.getPlayer()).after(!registering);
            }
        });
    }

    private void generateOthers(){
        int[] locs = Arrays.stream(this.locs).filter(i-> !this.numbers.containsKey(i)).toArray();
        this.numbers.put(locs[Utils.random(0, locs.length-1)], Utils.random(2, 64));
        locs = Arrays.stream(this.locs).filter(i-> !this.numbers.containsKey(i)).toArray();
        this.numbers.put(locs[0], Utils.random(2, 64));
    }

    private GUIButton getDisconnectButton(){
        SimpleItem item = new SimpleItem(XMaterial.REDSTONE_BLOCK)
                .setDisplayName("&a" + LBase.AUTH_GUI_DISCONNECT_NAME)
                .setLore("&7")
                .addLoreLines(Utils.breakText(LBase.AUTH_GUI_DISCONNECT_DESCRIPTION.toString(), 25, "&7"));
        return new GUIButton(0, item, a-> getSpigotTasks().runTask(()-> {
            this.close();
            this.getSpigotTasks().runTask(()->a.getPlayer().kickPlayer("Disconnected"));
        }));
    }



}

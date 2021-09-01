package xyz.theprogramsrc.superauth.spigot.guis.auth;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.gui.Gui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiRows;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;

public class CaptchaGUI extends Gui{

    private boolean wrong;
    private final int generated;
    private final HashMap<Integer, Integer> numbers;
    private final boolean registering;
    private final int[] locs;

    public CaptchaGUI(Player player, boolean registering){
        super(player, false);
        this.registering = registering;
        this.numbers = new HashMap<>();
        this.generated = Utils.random(2, 64);
        this.locs = new int[]{11, 13, 15};
        this.numbers.put(locs[Utils.random(0, 2)], this.generated);
        this.generateOthers();
        this.wrong = false;

        SuperAuth.spigot.runEvent(new SuperAuthBeforeCaptchaEvent(SuperAuth.spigot.getAuthSettings(), SuperAuth.spigot.getUserStorage(), player.getName()));
        this.canCloseGui = ForceLoginMemory.i.has(this.player.getName());
        this.open();

        if(!registering){
            UserStorage userStorage = SuperAuth.spigot.getUserStorage();
            new BukkitRunnable(){
                @Override
                public void run() {
                    userStorage.get(player.getName(), user -> {
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
                    });
                }
            }.runTaskTimerAsynchronously(this.spigotPlugin, 0L, 5L);
        }
    }

    @Override
    public GuiTitle getTitle() {
        String captcha = this.generated+"";
        return GuiTitle.of((this.wrong ? LBase.WRONG_CAPTCHA_GUI_TITLE : LBase.AUTH_CAPTCHA_GUI_TITLE.options().placeholder("{Captcha}", captcha)) + "");
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.THREE;
    }

    @Override
    public void onBuild(GuiModel model) {
        model.setButton(0, this.getDisconnectButton());
        this.numbers.entrySet().stream().forEach(entry -> {
            model.setButton(entry.getKey(), this.getNumberButton(entry.getValue()));
        });
        model.fillEmptySlots();
    }

    private GuiEntry getNumberButton(int number){
        SimpleItem item = new SimpleItem(XMaterial.OAK_BUTTON)
                .setDisplayName("&a" + number)
                .setAmount(number);
        return new GuiEntry(item, a->{
            if(number != this.generated){
                this.wrong = true;
                this.open();
                this.getSpigotTasks().runTaskLater(45L, ()->{
                    this.wrong = false;
                    this.open();
                });
            }else{
                this.close();
                SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(SuperAuth.spigot.getAuthSettings(), SuperAuth.spigot.getUserStorage(), a.player.getName()));
                new ActionManager(a.player).after(!registering);
            }
        });
    }

    private void generateOthers(){
        int[] locs = Arrays.stream(this.locs).filter(i-> !this.numbers.containsKey(i)).toArray();
        this.numbers.put(locs[Utils.random(0, locs.length-1)], Utils.random(2, 64));
        locs = Arrays.stream(this.locs).filter(i-> !this.numbers.containsKey(i)).toArray();
        this.numbers.put(locs[0], Utils.random(2, 64));
    }

    private GuiEntry getDisconnectButton(){
        SimpleItem item = new SimpleItem(XMaterial.REDSTONE_BLOCK)
                .setDisplayName("&a" + LBase.AUTH_GUI_DISCONNECT_NAME)
                .setLore("&7")
                .addLoreLines(Utils.breakText(LBase.AUTH_GUI_DISCONNECT_DESCRIPTION.toString(), 25, "&7"));
        return new GuiEntry(item, a-> getSpigotTasks().runTask(()-> {
            this.close();
            this.getSpigotTasks().runTask(()->a.player.kickPlayer("Disconnected"));
        }));
    }



}

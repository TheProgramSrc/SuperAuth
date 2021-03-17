package xyz.theprogramsrc.superauth.spigot.guis.auth;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickAction;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickType;
import xyz.theprogramsrc.supercoreapi.spigot.guis.objects.GUIRows;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;
import xyz.theprogramsrc.supercoreapi.spigot.utils.xseries.XMaterial;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginGUI extends GUI {

    private final User user;
    private final int pinLength;
    private String input;

    private final AuthSettings authSettings;

    public LoginGUI(Player player, User user){
        super(player);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.pinLength = SuperAuth.spigot.getAuthSettings().getPinLength();
        this.input = "";
        this.user = user;
        this.open();


        UserStorage userStorage = SuperAuth.spigot.getUserStorage();
        new BukkitRunnable(){
            @Override
            public void run() {
                User user = userStorage.get(player.getName());
                if(user != null){
                    if(!user.isAuthorized()){
                        if(ForceLoginMemory.i.has(player.getName())){
                            LoginGUI.this.close();
                            this.cancel();
                        }
                    }else{
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(this.spigotPlugin, 0L, 5L);
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.SIX;
    }

    @Override
    protected String getTitle() {
        if(this.input == null){
            return LBase.AUTH_LOGIN_GUI_TITLE + " &9» " + LBase.NO_INPUT.options().upper().get();
        }else{
            return LBase.AUTH_LOGIN_GUI_TITLE + " &9» " + (this.input.isEmpty() ? LBase.NO_INPUT.options().upper().get() : this.input);
        }
    }

    @Override
    public boolean canCloseGUI() {
        return ForceLoginMemory.i.has(this.getPlayer().getName()) && !this.getPlayer().isOnline() && this.user.isAuthorized();
    }

    @Override
    protected GUIButton[] getButtons() {
        List<GUIButton> list = new ArrayList<>();
        list.add(this.createInputButton(40, 0, "http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27"));
        list.add(this.createInputButton(12, 1, "http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530"));
        list.add(this.createInputButton(13, 2, "http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847"));
        list.add(this.createInputButton(14, 3, "http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5"));
        list.add(this.createInputButton(21, 4, "http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5"));
        list.add(this.createInputButton(22, 5, "http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2"));
        list.add(this.createInputButton(23, 6, "http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab"));
        list.add(this.createInputButton(30, 7, "http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9"));
        list.add(this.createInputButton(31, 8, "http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5"));
        list.add(this.createInputButton(32, 9, "http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840"));
        list.add(this.getDoneButton());
        list.add(this.getDeleteButton());
        list.add(this.getDisconnectButton());
        List<Integer> used = Utils.toList(0,8,12,13,14,21,22,23,30,31,32,40,53);
        for(int i = 0; i < this.getRows().getSize(); ++i){
            if(!used.contains(i)){
                used.add(i);
                list.add(new GUIButton(i, this.getPreloadedItems().emptyItem()));
            }
        }
        GUIButton[] buttons = new GUIButton[list.size()];
        buttons = list.toArray(buttons);
        return buttons;
    }

    private GUIButton getDeleteButton(){
        SimpleItem item = new SimpleItem(XMaterial.BARRIER)
                .setDisplayName("&a" + LBase.AUTH_GUI_REMOVE_INPUT_NAME)
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.AUTH_GUI_REMOVE_INPUT_LEFT,
                        "&9" + Base.RIGHT_CLICK + "&7 " + LBase.AUTH_GUI_REMOVE_INPUT_RIGHT
                );
        return new GUIButton(8, item, a->{
            if(a.getAction() == ClickType.RIGHT_CLICK){
                this.input = "";
                this.open();
            }else{
                if(this.input.length() >= 1){
                    this.input = this.input.substring(0, this.input.length()-1);
                    this.open();
                }
            }
        });
    }

    private GUIButton getDoneButton(){
        SimpleItem item = new SimpleItem(XMaterial.EMERALD)
                .setDisplayName("&a"+ LBase.AUTH_GUI_CHECK_PASSWORD_NAME +" »")
                .setLore(
                        "&7",
                        "&7" + LBase.AUTH_GUI_CHECK_PASSWORD_DESCRIPTION
                );
        return new GUIButton(53, item, a->{
            try{
                if(this.user.isValid(this.input)){
                    this.finish(a);
                }else{
                    this.getSuperUtils().sendMessage(a.getPlayer(), LBase.WRONG_PASSWORD.toString());
                }
            }catch (NoSuchAlgorithmException ex){
                this.plugin.addError(ex);
                this.getSuperUtils().sendMessage(a.getPlayer(), this.getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING);
                this.log("&c" + LBase.ERROR_WHILE_HASHING_PASSWORD);
                ex.printStackTrace();
            }
        });
    }

    private GUIButton createInputButton(int slot, final int number, String url){
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setDisplayName("&a" + number)
                .setSkin(SkinTexture.fromURL(url));
        return new GUIButton(slot, item, a-> {
            if(this.input.length() != this.pinLength){
                this.input += number;
                this.open();
            }
        });
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

    private void finish(ClickAction a){
        if(this.authSettings.isCaptchaEnabled()){
            double random = Utils.random(0.0, 1.0);
            if(random <= this.authSettings.getCaptchaChance()){
                this.close();
                new CaptchaGUI(a.getPlayer(), false);
            }else{
                new ActionManager(a.getPlayer()).after(true);
            }
        }else{
            new ActionManager(a.getPlayer()).after(true);
        }
    }
}

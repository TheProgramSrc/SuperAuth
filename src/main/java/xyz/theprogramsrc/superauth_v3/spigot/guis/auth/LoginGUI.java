package xyz.theprogramsrc.superauth_v3.spigot.guis.auth;

import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth_v3.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth_v3.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.gui.Gui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction.ClickType;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiRows;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

public class LoginGUI extends Gui {

    private final User user;
    private final int pinLength;
    private String input;

    private final AuthSettings authSettings;

    public LoginGUI(Player player, User user){
        super(player, false);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.pinLength = SuperAuth.spigot.getAuthSettings().getPinLength();
        this.input = "";
        this.user = user;
        this.canCloseGui = ForceLoginMemory.i.has(this.player.getName()) && !this.player.isOnline() && this.user.isAuthorized();
        this.open();


        UserStorage userStorage = SuperAuth.spigot.getUserStorage();
        new BukkitRunnable(){
            @Override
            public void run() {
                userStorage.get(player.getName(), user -> {
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
                });
            }
        }.runTaskTimerAsynchronously(this.spigotPlugin, 0L, 10L);
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.SIX;
    }

    @Override
    public GuiTitle getTitle() {
        if(this.input == null){
            return GuiTitle.of(LBase.AUTH_LOGIN_GUI_TITLE + " &9» " + LBase.NO_INPUT.options().upper().get());
        }else{
            return GuiTitle.of(LBase.AUTH_LOGIN_GUI_TITLE + " &9» " + (this.input.isEmpty() ? LBase.NO_INPUT.options().upper().get() : this.input));
        }
    }

    @Override
    public void onBuild(GuiModel model) {
        model.setButton(0, this.getDisconnectButton());
        model.setButton(8, this.getDeleteButton());
        model.setButton(53, this.getDoneButton());
        model.setButton(12, this.createInputButton(1, "https://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530"));
        model.setButton(13, this.createInputButton(2, "https://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847"));
        model.setButton(14, this.createInputButton(3, "https://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5"));
        model.setButton(21, this.createInputButton(4, "https://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5"));
        model.setButton(22, this.createInputButton(5, "https://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2"));
        model.setButton(23, this.createInputButton(6, "https://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab"));
        model.setButton(30, this.createInputButton(7, "https://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9"));
        model.setButton(31, this.createInputButton(8, "https://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5"));
        model.setButton(32, this.createInputButton(9, "https://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840"));
        model.setButton(40, this.createInputButton(0, "https://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27"));
        model.fillEmptySlots();
    }

    private GuiEntry getDeleteButton(){
        SimpleItem item = new SimpleItem(XMaterial.BARRIER)
                .setDisplayName("&a" + LBase.AUTH_GUI_REMOVE_INPUT_NAME)
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.AUTH_GUI_REMOVE_INPUT_LEFT,
                        "&9" + Base.RIGHT_CLICK + "&7 " + LBase.AUTH_GUI_REMOVE_INPUT_RIGHT
                );
        return new GuiEntry(item, a->{
            if(a.clickType == ClickType.RIGHT_CLICK){
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

    private GuiEntry getDoneButton(){
        SimpleItem item = new SimpleItem(XMaterial.EMERALD)
                .setDisplayName("&a"+ LBase.AUTH_GUI_CHECK_PASSWORD_NAME +" »")
                .setLore(
                        "&7",
                        "&7" + LBase.AUTH_GUI_CHECK_PASSWORD_DESCRIPTION
                );
        return new GuiEntry(item, a->{
            try{
                if(this.user.isValid(this.input)){
                    this.finish(a);
                }else{
                    this.getSuperUtils().sendMessage(a.player, LBase.WRONG_PASSWORD.toString());
                }
            }catch (NoSuchAlgorithmException ex){
                this.plugin.addError(ex);
                this.getSuperUtils().sendMessage(a.player, this.getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING);
                this.log("&c" + LBase.ERROR_WHILE_HASHING_PASSWORD);
                ex.printStackTrace();
            }
        });
    }

    private GuiEntry createInputButton(final int number, String url){
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setDisplayName("&a" + number)
                .setSkin(SkinTexture.fromURL(url));
        return new GuiEntry(item, a-> {
            if(this.input.length() != this.pinLength){
                this.input += number;
                this.open();
            }
        });
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

    private void finish(GuiAction a){
        if(this.authSettings.isCaptchaEnabled()){
            double random = Utils.random(0.0, 1.0);
            if(random <= this.authSettings.getCaptchaChance()){
                this.close();
                new CaptchaGUI(a.player, false);
            }else{
                new ActionManager(a.player).after(true);
            }
        }else{
            new ActionManager(a.player).after(true);
        }
    }
}

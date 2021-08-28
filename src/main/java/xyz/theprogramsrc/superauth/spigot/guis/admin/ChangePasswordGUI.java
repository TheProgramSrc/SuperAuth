package xyz.theprogramsrc.superauth.spigot.guis.admin;

import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.gui.Gui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction.ClickType;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiRows;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;

public abstract class ChangePasswordGUI extends Gui {

    private String input;
    private final int pinLength;
    private final User user;

    public ChangePasswordGUI(Player player, User user) {
        super(player, false);
        this.pinLength = SuperAuth.spigot.getAuthSettings().getPinLength();
        this.user = user;
        this.input = "";
        this.open();
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.SIX;
    }

    @Override
    public GuiTitle getTitle() {
        return GuiTitle.of(LBase.MANAGE_USER_CHANGE_PASSWORD_GUI_TITLE + "&9» &a" + (this.input.isEmpty() ? LBase.NO_INPUT : this.input));
    }

    public abstract void onBack(GuiAction action);

    @Override
    public void onBuild(GuiModel model) {
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
        model.setButton(8, this.getDeleteButton());
        model.setButton(53, this.getDoneButton());
        model.fillEmptySlots();
    }

    private GuiEntry getDeleteButton(){
        SimpleItem item = new SimpleItem(XMaterial.BARRIER)
                .setDisplayName("&a" + LBase.MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_NAME)
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_LEFT,
                        "&9" + Base.RIGHT_CLICK + "&7 " + LBase.MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT
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
                .setDisplayName("&a"+ LBase.MANAGE_USER_CHANGE_PASSWORD_SAVE_NAME + " »")
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_CHANGE_PASSWORD_SAVE_DESCRIPTION
                );
        return new GuiEntry(item, a->{
            try{
                String password = Hashing.hash(SuperAuth.spigot.getAuthSettings().getHashingMethod(), this.input);
                this.user.setPassword(password);
                SuperAuth.spigot.getUserStorage().save(this.user, () -> {
                    this.getSpigotTasks().runTask(() -> this.onBack(a));
                });
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
                .setSkin(this.spigotPlugin.getSkinManager().getSkin(url));
        return new GuiEntry(item, a-> {
            if(this.input.length() != this.pinLength){
                this.input += number;
                this.open();
            }
        });
    }
}

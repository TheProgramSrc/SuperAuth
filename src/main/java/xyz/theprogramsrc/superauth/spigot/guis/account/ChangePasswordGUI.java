package xyz.theprogramsrc.superauth.spigot.guis.account;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
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

public abstract class ChangePasswordGUI extends GUI {

    private String input;
    private final int pinLength;
    private final User user;

    public ChangePasswordGUI(Player player, User user) {
        super(player);
        this.pinLength = SuperAuth.spigot.getAuthSettings().getPinLength();
        this.user = user;
        this.input = "";
        this.open();
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.SIX;
    }

    @Override
    protected String getTitle() {
        return LBase.MY_ACCOUNT_CHANGE_PASSWORD_GUI_TITLE.toString() + "&9» &a" + (this.input.isEmpty() ? LBase.NO_INPUT : this.input);
    }

    public abstract void onBack(ClickAction action);

    @Override
    public boolean isTitleCentered() {
        return false;
    }

    @Override
    protected GUIButton[] getButtons() {
        List<GUIButton> list = new ArrayList<>();
        list.add(this.createInputButton(12, 1, "http://textures.minecraft.net/texture/71bc2bcfb2bd3759e6b1e86fc7a79585e1127dd357fc202893f9de241bc9e530"));
        list.add(this.createInputButton(13, 2, "http://textures.minecraft.net/texture/4cd9eeee883468881d83848a46bf3012485c23f75753b8fbe8487341419847"));
        list.add(this.createInputButton(14, 3, "http://textures.minecraft.net/texture/1d4eae13933860a6df5e8e955693b95a8c3b15c36b8b587532ac0996bc37e5"));
        list.add(this.createInputButton(21, 4, "http://textures.minecraft.net/texture/d2e78fb22424232dc27b81fbcb47fd24c1acf76098753f2d9c28598287db5"));
        list.add(this.createInputButton(22, 5, "http://textures.minecraft.net/texture/6d57e3bc88a65730e31a14e3f41e038a5ecf0891a6c243643b8e5476ae2"));
        list.add(this.createInputButton(23, 6, "http://textures.minecraft.net/texture/334b36de7d679b8bbc725499adaef24dc518f5ae23e716981e1dcc6b2720ab"));
        list.add(this.createInputButton(30, 7, "http://textures.minecraft.net/texture/6db6eb25d1faabe30cf444dc633b5832475e38096b7e2402a3ec476dd7b9"));
        list.add(this.createInputButton(31, 8, "http://textures.minecraft.net/texture/59194973a3f17bda9978ed6273383997222774b454386c8319c04f1f4f74c2b5"));
        list.add(this.createInputButton(32, 9, "http://textures.minecraft.net/texture/e67caf7591b38e125a8017d58cfc6433bfaf84cd499d794f41d10bff2e5b840"));
        list.add(this.createInputButton(40, 0, "http://textures.minecraft.net/texture/0ebe7e5215169a699acc6cefa7b73fdb108db87bb6dae2849fbe24714b27"));
        list.add(this.getDoneButton());
        list.add(this.getDeleteButton());
        List<Integer> used = Utils.toList(8,12,13,14,21,22,23,30,31,32,40,53);
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
                .setDisplayName("&a" + LBase.MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_NAME)
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_LEFT,
                        "&9" + Base.RIGHT_CLICK + "&7 " + LBase.MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT
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
                .setDisplayName("&a"+ LBase.MY_ACCOUNT_CHANGE_PASSWORD_SAVE_NAME + " »")
                .setLore(
                        "&7",
                        "&7" + LBase.MY_ACCOUNT_CHANGE_PASSWORD_SAVE_DESCRIPTION
                );
        return new GUIButton(53, item, a->{
            try{
                String password = Hashing.hash(SuperAuth.spigot.getAuthSettings().getHashingMethod(), this.input);
                this.user.setPassword(password);
                SuperAuth.spigot.getUserStorage().save(this.user);
                this.onBack(a);
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
}

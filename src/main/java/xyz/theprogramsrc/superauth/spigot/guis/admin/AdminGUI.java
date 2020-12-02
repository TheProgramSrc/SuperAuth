package xyz.theprogramsrc.superauth.spigot.guis.admin;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickAction;
import xyz.theprogramsrc.supercoreapi.spigot.guis.objects.GUIRows;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;
import xyz.theprogramsrc.supercoreapi.spigot.utils.xseries.XMaterial;

public class AdminGUI extends GUI {

    public AdminGUI(Player player){
        super(player);
        this.open();
    }

    @Override
    protected String getTitle() {
        return "SuperAuth v" + this.getPluginVersion();
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.FOUR;
    }

    @Override
    protected GUIButton[] getButtons() {
        return new GUIButton[]{
                this.getManageUsersButton(),
                this.getSettingsButton(),
                this.getCloseGUIButton()
        };
    }

    private GUIButton getManageUsersButton(){
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setSkin(new SkinTexture(SuperAuth.spigot.getUserStorage().getRandomUserWithSkin().getSkinTexture()))
                .setDisplayName("&a" + LBase.ADMIN_GUI_USERS_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.ADMIN_GUI_USERS_DESCRIPTION);
        return new GUIButton(11, item, a-> new UserBrowser(a.getPlayer()){
            @Override
            public void onBack(ClickAction clickAction) {
                AdminGUI.this.open();
            }
        });
    }

    private GUIButton getSettingsButton(){
        SimpleItem item = new SimpleItem(XMaterial.COMMAND_BLOCK)
                .setDisplayName("&c" + Base.SETTINGS)
                .setLore(
                        "&7",
                        "&c&l" + LBase.COMING_SOON.options().upper().get()
                );
        return new GUIButton(13, item, null);
    }

    private GUIButton getCloseGUIButton(){
        SimpleItem item = new SimpleItem(XMaterial.REDSTONE_BLOCK)
                .setDisplayName("&4" + LBase.ADMIN_GUI_CLOSE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.ADMIN_GUI_CLOSE_DESCRIPTION
                );
        return new GUIButton(35, item, a-> this.close());
    }
}

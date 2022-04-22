package xyz.theprogramsrc.superauth_v3.spigot.guis.admin;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.gui.Gui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiRows;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.gui.precreated.settings.SettingPane;
import xyz.theprogramsrc.supercoreapi.spigot.gui.precreated.settings.SettingsGui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.precreated.settings.precreated.GeneralConfigurationSettingPane;
import xyz.theprogramsrc.supercoreapi.spigot.gui.precreated.settings.precreated.LanguageSelectionSettingPane;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

public class AdminGUI extends Gui {

    private User lastUser;
    private long lastTime;

    public AdminGUI(Player player){
        super(player, false);
        this.open();
    }

    @Override
    public GuiTitle getTitle() {
        return GuiTitle.of("SuperAuth v" + this.getPluginVersion(), true);
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.FOUR;
    }

    @Override
    public void onBuild(GuiModel model) {
        model.setButton(12, this.getManageUsersButton());
        model.setButton(14, this.getSettingsButton());
        model.setButton(this.getRows().size-1, this.getCloseGUIButton());
    }

    private GuiEntry getManageUsersButton(){
        if(this.lastUser == null || this.lastTime == 0L || (this.lastTime - System.currentTimeMillis() > 5000L)){
            SuperAuth.spigot.getUserStorage().getRandomUserWithSkin(user-> this.lastUser = user);
            this.lastTime = System.currentTimeMillis();
        }
        SkinTexture skin = new SkinTexture(lastUser.getSkinTexture());
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setSkin(skin)
                .setDisplayName("&a" + LBase.ADMIN_GUI_USERS_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.ADMIN_GUI_USERS_DESCRIPTION);
        return new GuiEntry(item, a-> new UserBrowser(a.player){
            @Override
            public void onBack(GuiAction clickAction) {
                AdminGUI.this.open();
            }
        });
    }

    private GuiEntry getSettingsButton(){
        SimpleItem item = new SimpleItem(XMaterial.COMMAND_BLOCK)
                .setDisplayName("&c" + Base.SETTINGS_EDITOR_NAME)
                .setLore(
                        "&7",
                        "&7" + Base.SETTINGS_EDITOR_DESCRIPTION
                );
        return new GuiEntry(item, a-> new SettingsGui(a.player){

            @Override
            public SettingPane[] getSettingPanes() {
                return new SettingPane[]{
                    new GeneralConfigurationSettingPane(),
                    new LanguageSelectionSettingPane(),
                };
            }

            @Override
            public void onBack(GuiAction clickAction) {
                AdminGUI.this.open();
            }

        });
    }

    private GuiEntry getCloseGUIButton(){
        SimpleItem item = new SimpleItem(XMaterial.REDSTONE_BLOCK)
                .setDisplayName("&4" + LBase.ADMIN_GUI_CLOSE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.ADMIN_GUI_CLOSE_DESCRIPTION
                );
        return new GuiEntry(item, a-> this.close());
    }
}

package xyz.theprogramsrc.superauth.spigot.guis.admin;

import java.util.Arrays;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.gui.BrowserGui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction.ClickType;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

public abstract class UserBrowser extends BrowserGui<User> {

    private final UserStorage userStorage;
    private User[] users = new User[0];

    public UserBrowser(Player player){
        super(player, false);
        this.backEnabled = true;
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.userStorage.requestUsers(users-> this.users = users);
        this.open();
    }

    @Override
    public String[] getSearchTags(User user) {
        return new String[]{user.getIp(), user.getUsername()};
    }

    @Override
    public User[] getObjects() {
        return Arrays.stream(this.users).filter(Utils::nonNull).toArray(User[]::new);
    }

    @Override
    public GuiEntry getEntry(final User user) {
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setDisplayName("&a" + LBase.USER_BROWSER_BUTTON_NAME.options().placeholder("{User}", user.getUsername()))
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.USER_BROWSER_BUTTON_LEFT,
                        "&9Q" + "&7 " + LBase.USER_BROWSER_BUTTON_Q
                );
        if(user.hasSkin()) item.setSkin(new SkinTexture(user.getSkinTexture()));
        return new GuiEntry(item, a->{
            if(a.clickType == ClickType.Q){
                this.getSpigotTasks().runAsyncTask(()->{
                    this.userStorage.remove(user, () -> {
                        this.userStorage.requestUsers(true, users -> {
                            this.users = users;
                            this.getSpigotTasks().runTask(this::open);
                        });
                    });
                });
            }else{
                new ManageUser(a.player, user){
                    @Override
                    public void onBack(GuiAction a) {
                        UserBrowser.this.open();
                    }
                };
            }
        });
    }

    @Override
    public void onBuild(GuiModel model) {
        super.onBuild(model);
        model.setButton(47, this.getRefreshCacheButton());
    }

    @Override
    public GuiTitle getTitle() {
        return GuiTitle.of(LBase.USER_BROWSER_GUI_TITLE.toString());
    }

    private GuiEntry getRefreshCacheButton(){
        SimpleItem item = new SimpleItem(XMaterial.EMERALD)
                .setDisplayName("&a" + LBase.USER_BROWSER_REFRESH_CACHE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.USER_BROWSER_REFRESH_CACHE_DESCRIPTION
                );

        return new GuiEntry(item, a->{
            this.userStorage.requestUsers(true, users -> {
                this.users = users;
                this.getSpigotTasks().runTask(this::open);
            });
        });
    }
}

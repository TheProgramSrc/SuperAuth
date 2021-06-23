package xyz.theprogramsrc.superauth.spigot.guis.admin;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.guis.BrowserGUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickAction;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickType;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class UserBrowser extends BrowserGUI<User> {

    private final UserStorage userStorage;
    private User[] users;

    public UserBrowser(Player player){
        super(player);
        this.backEnabled = true;
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.users = this.userStorage.requestUsers();
        this.open();
    }

    @Override
    public User[] getObjects() {
        return Arrays.stream(this.users).filter(Utils::nonNull).toArray(User[]::new);
    }

    @Override
    public GUIButton getButton(final User user) {
        SimpleItem item = new SimpleItem(XMaterial.PLAYER_HEAD)
                .setDisplayName("&a" + user.getUsername())
                .setLore(
                        "&7",
                        "&9" + Base.LEFT_CLICK + "&7 " + LBase.USER_BROWSER_BUTTON_LEFT,
                        "&9Q" + "&7 " + LBase.USER_BROWSER_BUTTON_Q
                );
        if(user.hasSkin()) item.setSkin(new SkinTexture(user.getSkinTexture()));
        return new GUIButton(item).setAction(a->{
            if(a.getAction() == ClickType.Q){
                this.userStorage.remove(user);
                this.open();
            }else{
                new ManageUser(a.getPlayer(), user){
                    @Override
                    public void onBack(ClickAction a) {
                        UserBrowser.this.open();
                    }
                };
            }
        });
    }

    @Override
    protected GUIButton[] getButtons() {
        LinkedList<GUIButton> buttons = new LinkedList<>(Utils.toList(super.getButtons()));
        buttons.add(this.getRefreshCacheButton());
        return buttons.toArray(new GUIButton[0]);
    }

    @Override
    protected String getTitle() {
        return LBase.USER_BROWSER_GUI_TITLE.toString();
    }

    private GUIButton getRefreshCacheButton(){
        SimpleItem item = new SimpleItem(XMaterial.EMERALD)
                .setDisplayName("&a" + LBase.USER_BROWSER_REFRESH_CACHE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.USER_BROWSER_REFRESH_CACHE_DESCRIPTION
                );

        return new GUIButton(47, item, a->{
            this.users = this.userStorage.requestUsers(true);
            this.open();
        });
    }
}

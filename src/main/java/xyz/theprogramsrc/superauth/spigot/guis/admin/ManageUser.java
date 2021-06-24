package xyz.theprogramsrc.superauth.spigot.guis.admin;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickAction;
import xyz.theprogramsrc.supercoreapi.spigot.guis.objects.GUIRows;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;

import java.security.NoSuchAlgorithmException;

public abstract class ManageUser extends GUI {

    private final UserStorage userStorage;
    private final AuthSettings authSettings;
    private User user;

    public ManageUser(Player player, User user){
        super(player);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.user = user;
        this.open();
    }

    @Override
    public void open() {
        this.user = this.userStorage.get(this.user.getUsername());
        super.open();
    }

    @Override
    protected String getTitle() {
        // Remove var in v3.17
        return LBase.MANAGE_USER_GUI_TITLE.options().vars(this.user.getUsername()).placeholder("{User}", this.user.getUsername()).get();
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.FOUR;
    }

    @Override
    protected GUIButton[] getButtons() {
        return new GUIButton[]{
                new GUIButton(this.getRows().getSize()-1, this.getPreloadedItems().getBackItem(), this::onBack),
                this.getChangePasswordButton(),
                this.getTogglePremiumMode(),
        };
    }

    public abstract void onBack(ClickAction a);

    private GUIButton getTogglePremiumMode(){
        boolean premium = this.userStorage.get(this.getPlayer().getName()).isPremium();
        SimpleItem item = new SimpleItem(premium ? XMaterial.GOLD_BLOCK : XMaterial.DIAMOND_BLOCK)
                .setDisplayName("&a" + LBase.MANAGE_USER_TOGGLE_MODE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_TOGGLE_MODE_DESCRIPTION
                ).addPlaceholder("{Mode}", (premium ? ("&a&l" + LBase.CRACKED) : ("&6&l" + LBase.PREMIUM)) + "&7");
        return new GUIButton(14, item, a-> new Thread(()->{
            User user = this.userStorage.get(a.getPlayer().getName());
            user.setPremium(!user.isPremium());
            this.userStorage.save(user);
            this.open();
        }).start());
    }

    private GUIButton getChangePasswordButton(){
        SimpleItem item = new SimpleItem(XMaterial.NAME_TAG)
                .setDisplayName("&a" + LBase.MANAGE_USER_CHANGE_PASSWORD_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION
                );
        return new GUIButton(12, item, a-> {
            String auth = this.user.getAuthMethod();
            if(auth.equals("DIALOG") || auth.equals("COMMANDS")){
                new Dialog(a.getPlayer()){
                    @Override
                    public String getTitle() {
                        return LBase.DIALOG_CHANGE_PASSWORD_TITLE.toString();
                    }

                    @Override
                    public String getSubtitle() {
                        return LBase.DIALOG_CHANGE_PASSWORD_SUBTITLE.toString();
                    }

                    @Override
                    public String getActionbar() {
                        return LBase.DIALOG_CHANGE_PASSWORD_ACTIONBAR.toString();
                    }

                    @Override
                    public boolean onResult(String s) {
                        try{
                            ManageUser self = ManageUser.this;
                            HashingMethod method = self.authSettings.getHashingMethod();
                            String hash = Hashing.hash(method, s);
                            User user = self.userStorage.get(getPlayer().getName());
                            self.userStorage.save(user.setPassword(hash));
                            this.getSpigotTasks().runTask(self::open);
                            return true;
                        }catch (NoSuchAlgorithmException ex){
                            this.plugin.addError(ex);
                            ManageUser.this.getSuperUtils().sendMessage(getPlayer(), getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING);
                            this.log("&c" + LBase.ERROR_WHILE_HASHING_PASSWORD);
                            ex.printStackTrace();
                            return false;
                        }
                    }
                };
            }else{
                new ChangePasswordGUI(a.getPlayer(), this.user){
                    @Override
                    public void onBack(ClickAction action) {
                        ManageUser.this.open();
                    }
                };
            }
        });
    }
}

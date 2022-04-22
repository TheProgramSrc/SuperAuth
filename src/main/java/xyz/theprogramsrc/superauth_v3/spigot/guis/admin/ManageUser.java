package xyz.theprogramsrc.superauth_v3.spigot.guis.admin;

import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth_v3.global.hashing.Hashing;
import xyz.theprogramsrc.superauth_v3.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.gui.Gui;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiAction;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiEntry;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiModel;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiRows;
import xyz.theprogramsrc.supercoreapi.spigot.gui.objets.GuiTitle;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;

public abstract class ManageUser extends Gui {

    private final UserStorage userStorage;
    private final AuthSettings authSettings;
    private User user;

    public ManageUser(Player player, User user){
        super(player, false);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.user = user;
        this.open();
    }

    @Override
    public void open() {
        this.getSpigotTasks().runAsyncTask(() -> this.userStorage.get(this.user.getUsername(), u -> {
            this.user = u;
            super.open();
        }));
    }

    @Override
    public GuiTitle getTitle() {
        return GuiTitle.of(LBase.MANAGE_USER_GUI_TITLE.options().placeholder("{User}", this.user.getUsername()).get());
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.FOUR;
    }

    @Override
    public void onBuild(GuiModel model) {
        model.setButton(this.getRows().size-1, new GuiEntry(this.getPreloadedItems().getBackItem(), this::onBack));
        model.setButton(12, this.getChangePasswordButton());
        this.getSpigotTasks().runAsyncTask(() -> this.userStorage.get(this.user.getUsername(), u-> this.getSpigotTasks().runTask(() -> {
            model.setButton(14, this.getTogglePremiumMode(u));
            model.fillEmptySlots();
        })));
    }

    public abstract void onBack(GuiAction a);

    private GuiEntry getTogglePremiumMode(User u){
        SimpleItem item = new SimpleItem(u.isPremium() ? XMaterial.GOLD_BLOCK : XMaterial.DIAMOND_BLOCK)
                .setDisplayName("&a" + LBase.MANAGE_USER_TOGGLE_MODE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_TOGGLE_MODE_DESCRIPTION
                ).addPlaceholder("{Mode}", (u.isPremium() ? ("&a&l" + LBase.CRACKED) : ("&6&l" + LBase.PREMIUM)) + "&7");
        return new GuiEntry(item, a-> this.getSpigotTasks().runAsyncTask(() -> this.getSpigotTasks().runAsyncTask(() -> this.userStorage.get(u.getUsername(), u1 -> {
            u1.setPremium(!u1.isPremium());
            this.userStorage.save(user);
            this.open();
        }))));
    }

    private GuiEntry getChangePasswordButton(){
        SimpleItem item = new SimpleItem(XMaterial.NAME_TAG)
                .setDisplayName("&a" + LBase.MANAGE_USER_CHANGE_PASSWORD_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION
                );
        return new GuiEntry(item, a-> {
            String auth = this.user.getAuthMethod();
            if(auth.equals("DIALOG") || auth.equals("COMMANDS")){
                new Dialog(a.player){
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
                            this.getSpigotTasks().runAsyncTask(() -> self.userStorage.get(user.getUsername(), u1 -> {
                                self.userStorage.save(u1.setPassword(hash));
                                this.getSuperUtils().sendMessage(a.player, this.getSettings().getPrefix() + LBase.PASSWORD_UPDATED);
                            }));
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
                new ChangePasswordGUI(a.player, this.user){
                    @Override
                    public void onBack(GuiAction a) {
                        ManageUser.this.close();
                        this.getSuperUtils().sendMessage(a.player, this.getSettings().getPrefix() + LBase.PASSWORD_UPDATED);
                    }
                };
            }
        });
    }
}

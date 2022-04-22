package xyz.theprogramsrc.superauth_v3.spigot.guis.account;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth_v3.global.hashing.Hashing;
import xyz.theprogramsrc.superauth_v3.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
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

public class MyAccountGUI extends Gui {

    private final AuthSettings authSettings;
    private final UserStorage userStorage;

    public MyAccountGUI(Player player) {
        super(player, false);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.open();
    }

    @Override
    public GuiRows getRows() {
        return GuiRows.THREE;
    }

    @Override
    public GuiTitle getTitle() {
        return GuiTitle.of(LBase.MY_ACCOUNT_GUI_TITLE.options().placeholder("{Player}", this.player.getName()).placeholder("{DisplayName}", this.player.getDisplayName()).get());
    }

    @Override
    public void onBuild(GuiModel model) {
        model.setButton(12, this.getChangePassword());
        model.setButton(14, this.getTogglePremiumMode());
        model.setButton(this.getRows().size-1, new GuiEntry(this.getPreloadedItems().getBackItem(), a-> this.close()));
        model.fillEmptySlots();
    }

    private GuiEntry getTogglePremiumMode(){
        AtomicBoolean premium = new AtomicBoolean();
        this.userStorage.get(this.player.getName(), user -> premium.set(user.isPremium()));
        SimpleItem item = new SimpleItem(premium.get() ? XMaterial.GOLD_BLOCK : XMaterial.DIAMOND_BLOCK)
                .setDisplayName("&a" + LBase.MY_ACCOUNT_GUI_TOGGLE_MODE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MY_ACCOUNT_GUI_TOGGLE_MODE_DESCRIPTION
                ).addPlaceholder("{Mode}", (premium.get() ? ("&a&l" + LBase.CRACKED) : ("&6&l" + LBase.PREMIUM)) + "&7");
        return new GuiEntry(item, a->{
            this.userStorage.get(a.player.getName(), user -> {
                user.setPremium(!user.isPremium());
                this.userStorage.save(user);
                this.open();
            });
        });
    }

    private GuiEntry getChangePassword(){
        SimpleItem item = new SimpleItem(XMaterial.NAME_TAG)
                .setDisplayName("&a" + LBase.MANAGE_USER_CHANGE_PASSWORD_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION
                );
        return new GuiEntry(item, a-> this.getSpigotTasks().runAsyncTask(() ->{
            this.userStorage.get(a.player.getName(), user -> {
                String auth = user.getAuthMethod();
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
                                MyAccountGUI self = MyAccountGUI.this;
                                HashingMethod method = self.authSettings.getHashingMethod();
                                String hash = Hashing.hash(method, s);
                                self.userStorage.get(getPlayer().getName(), user -> {
                                    self.userStorage.save(user.setPassword(hash));
                                    this.getSuperUtils().sendMessage(this.getPlayer(), this.getSettings().getPrefix() + LBase.PASSWORD_UPDATED);
                                });
                                return true;
                            }catch (NoSuchAlgorithmException ex){
                                this.plugin.addError(ex);
                                MyAccountGUI.this.getSuperUtils().sendMessage(getPlayer(), getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING);
                                this.log("&c" + LBase.ERROR_WHILE_HASHING_PASSWORD);
                                ex.printStackTrace();
                                return false;
                            }
                        }
                    };
                }else{
                    this.getSpigotTasks().runAsyncTask(() -> {
                        this.userStorage.get(a.player.getName(), u -> {
                            new ChangePasswordGUI(a.player, u){
                                @Override
                                public void onBack(GuiAction action) {
                                    MyAccountGUI.this.close();
                                    this.getSuperUtils().sendMessage(action.player, this.getSettings().getPrefix() + LBase.PASSWORD_UPDATED);
                                }
                            }; 
                        });
                    });
                }
            });
        }));
    }
}

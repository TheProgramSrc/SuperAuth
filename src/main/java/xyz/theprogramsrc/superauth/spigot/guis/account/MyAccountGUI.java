package xyz.theprogramsrc.superauth.spigot.guis.account;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.xseries.XMaterial;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUI;
import xyz.theprogramsrc.supercoreapi.spigot.guis.GUIButton;
import xyz.theprogramsrc.supercoreapi.spigot.guis.action.ClickAction;
import xyz.theprogramsrc.supercoreapi.spigot.guis.objects.GUIRows;
import xyz.theprogramsrc.supercoreapi.spigot.items.SimpleItem;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class MyAccountGUI extends GUI {

    private final AuthSettings authSettings;
    private final UserStorage userStorage;

    public MyAccountGUI(Player player) {
        super(player);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.open();
    }

    @Override
    protected GUIRows getRows() {
        return GUIRows.THREE;
    }

    @Override
    protected String getTitle() {
        return LBase.MY_ACCOUNT_GUI_TITLE.options().placeholder("{Player}", this.getPlayer().getName()).placeholder("{DisplayName}", this.getPlayer().getDisplayName()).get();
    }

    @Override
    protected GUIButton[] getButtons() {
        LinkedList<GUIButton> buttons = new LinkedList<>();
        List<Integer> used = Utils.toList(12, 14, this.getRows().getSize()-1);
        for(int i = 0; i < this.getRows().getSize(); ++i){
            if(!used.contains(i)){
                buttons.add(new GUIButton(i, this.getPreloadedItems().emptyItem()));
            }
        }

        buttons.add(this.getTogglePremiumMode());
        buttons.add(this.getChangePassword());
        buttons.add(new GUIButton(this.getRows().getSize()-1, this.getPreloadedItems().getBackItem(), a-> this.close()));

        return buttons.toArray(new GUIButton[0]);
    }

    private GUIButton getTogglePremiumMode(){
        boolean premium = this.userStorage.get(this.getPlayer().getName()).isPremium();
        SimpleItem item = new SimpleItem(premium ? XMaterial.GOLD_BLOCK : XMaterial.DIAMOND_BLOCK)
                .setDisplayName("&a" + LBase.MY_ACCOUNT_GUI_TOGGLE_MODE_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MY_ACCOUNT_GUI_TOGGLE_MODE_DESCRIPTION
                ).addPlaceholder("{Mode}", (premium ? ("&a&l" + LBase.CRACKED) : ("&6&l" + LBase.PREMIUM)) + "&7");
        return new GUIButton(14, item, a->{
            User user = this.userStorage.get(a.getPlayer().getName());
            user.setPremium(!user.isPremium());
            this.userStorage.save(user);
            this.open();
        });
    }

    private GUIButton getChangePassword(){
        SimpleItem item = new SimpleItem(XMaterial.NAME_TAG)
                .setDisplayName("&a" + LBase.MANAGE_USER_CHANGE_PASSWORD_NAME)
                .setLore(
                        "&7",
                        "&7" + LBase.MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION
                );
        return new GUIButton(12, item, a->{
            String auth = this.userStorage.get(a.getPlayer().getName()).getAuthMethod();
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
                            MyAccountGUI self = MyAccountGUI.this;
                            HashingMethod method = self.authSettings.getHashingMethod();
                            String hash = Hashing.hash(method, s);
                            User user = self.userStorage.get(getPlayer().getName());
                            self.userStorage.save(user.setPassword(hash));
                            this.getSpigotTasks().runTask(self::open);
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
                new ChangePasswordGUI(a.getPlayer(), this.userStorage.get(a.getPlayer().getName())){
                    @Override
                    public void onBack(ClickAction action) {
                        MyAccountGUI.this.open();
                    }
                };
            }
        });
    }
}

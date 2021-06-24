package xyz.theprogramsrc.superauth.global.languages;

import xyz.theprogramsrc.supercoreapi.global.translations.Translation;
import xyz.theprogramsrc.supercoreapi.global.translations.TranslationManager;
import xyz.theprogramsrc.supercoreapi.global.translations.TranslationPack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum LBase implements TranslationPack {

    /* Short Words */
    PREMIUM("General.Premium","Premium"),
    CRACKED("General.Cracked", "Cracked"),
    NO_INPUT("General.NoInput", "No Input"),

    /* Messages */
    CONFIG_RELOADED("Messages.ConfigurationReloaded", "&aThe configuration files were reloaded"),
    PASSWORD_TOO_SHORT("Errors.PasswordTooShort","&cError. Please use a password with at least {Length} characters."),
    PASSWORD_TOO_LONG("Errors.PasswordTooLong","&cError. Please use a password with no more than {Length} characters."),
    DIALOG_HOW_TO_USE("HowToUse.Dialog","&aTo authenticate yourself just write in the chat your password. Don't worry because it wont be showed publicly."),
    GUI_HOW_TO_USE("HowToUse.GUI","&aTo authenticate yourself an GUI will be opened, if you cannot see the GUI use the command /{Command} and enter your pin."),
    COMMAND_HOW_TO_USE("HowToUse.Command","&aTo register use the command /{RegisterCommand} <Password>. To login use the command /{LoginCommand} <Password>"),
    REGISTER_COMMAND_USAGE("HowToUse.Register","&cUse &e/{Command} <Password> &cto register."),
    LOGIN_COMMAND_USAGE("HowToUse.Login","&cUse &e/{Command} <Password> &cto login."),

    ERROR_WHILE_HASHING("Errors.Hashing","&cThe hashing util is not working right now, try again later."),
    WRONG_CAPTCHA("Errors.Captcha","&cWrong captcha! &aEnter this captcha: &b{Captcha}"),
    WRONG_PASSWORD("Errors.Password","&cWrong password!"),
    STILL_IN_AUTH("Errors.StillInAuth","&cYou can't do this! You need to authenticate yourself!"),
    ERROR_FETCHING_DATA("Errors.FetchingData","&cError while fetching data. Please contact an administrator"),
    ALREADY_IDENTIFIED("Errors.AlreadyIdentified","&cYou're already identified!"),

    ALREADY_PREMIUM("Errors.AlreadyPremium","&cYou're already a premium user!"),
    ALREADY_CRACKED("Errors.AlreadyCracked","&cYou're already a cracked user!"),

    CONFIRMATION_MESSAGE("Messages.ChangeModeConfirmation","&eIn order to confirm you need to run the command &7{Command}"),
    CHANGE_MODE_WARNING("Messages.ChangeModeWarning","&cWARNING: IF YOU CHANGE YOUR MODE YOU MAY LOSE ALL THE DATA AND PROGRESS"),


    ALREADY_ADMIN("Messages.AlreadyAdmin","&cThe user &7{User}&c is already an admin!"),
    ADDED_ADMIN("Messages.AddedAdmin","&aNow the user &7{User}&a is an admin."),
    ALREADY_NON_ADMIN("Messages.NotAdmin","&cThe user &7{User}&c is not an admin!"),
    REMOVED_ADMIN("Messages.RemovedAdmin","&aThe user &7{User} &ais no longer an admin."),

    USER_NOT_REGISTERED("Errors.UserNotRegistered","The user must be registered!"),
    USER_NOT_EXISTS("Errors.UserNotExists","That user doesn't exists!"),
    USER_ALREADY_IDENTIFIED("Errors.UserAlreadyIdentified","That user is already identified!"),
    ERROR_WHILE_FETCHING_PLAYER("Errors.FetchingPlayer","Error while fetching player from User DataBase, maybe the user is not online"),

    FORCED_LOGIN("Messages.ForcedLogin","&aForced login for user &c{User}"),
    REMOVE_REQUEST_SENT("Messages.RemoveRequestSent","The remove request was sent"),

    USE_LOGIN_COMMAND("Errors.AlreadyRegistered","&cYou're already registered! Use the command &e/{Command} <Password>"),
    USE_REGISTER_COMMAND("Errors.NotRegistered","&cYou're not registered! Please register yourself using the command &e/{Command} <Password>"),

    WIKI_INFORMATION("Messages.Wiki","You can view all the information regarding the plugin in our wiki: https://wiki.theprogramsrc.xyz/"),
    FORCE_LOGIN_NOT_SUPPORTED("Errors.ForceLoginNotSupported","You can't use force login with the auth method DIALOG"),

    CONSOLE_UPDATED_USER_IP_ADDRESS("Messages.UpdatedIPAddress","&aSent request to update &7{UserName}'s&a account IP Address to: &b{NewIPAddress}"),
    CONSOLE_UPDATED_USER_MODE("Messages.UpdatedMode","&aSent request to update &7{UserName}'s&a account mode to: &b{NewMode}"),

    /* DIALOGS */
    DIALOG_REGISTER_TITLE("Dialogs.Register.Title","&bRegister"),
    DIALOG_REGISTER_SUBTITLE("Dialogs.Register.Subtitle","&7Write in the chat your password"),
    DIALOG_REGISTER_ACTIONBAR("Dialogs.Register.Actionbar","&aDon't use commands, just write your password"),
    DIALOG_LOGIN_TITLE("Dialogs.Login.Title","&bLogin"),
    DIALOG_LOGIN_SUBTITLE("Dialogs.Login.Subtitle","&7Write in the chat your password"),
    DIALOG_LOGIN_ACTIONBAR("Dialogs.Login.Actionbar","&aDon't use commands, just write your password"),
    DIALOG_CAPTCHA_TITLE("Dialogs.Captcha.Title","&bCaptcha"),
    DIALOG_CAPTCHA_SUBTITLE("Dialogs.Captcha.Subtitle","&7Write in the chat: &9&l{Captcha}"),
    DIALOG_CAPTCHA_ACTIONBAR("Dialogs.Captcha.Actionbar","&aWrite '&9&l{Captcha}&a' in the chat"),
    DIALOG_AUTH_COMMAND_USED("Errors.DialogNoCommand","&cDon't use commands! Just write your password in the chat"),

    DIALOG_CHANGE_PASSWORD_TITLE("Dialogs.ChangePassword.Title","&9Password"),
    DIALOG_CHANGE_PASSWORD_SUBTITLE("Dialogs.ChangePassword.Subtitle","&7Change Password"),
    DIALOG_CHANGE_PASSWORD_ACTIONBAR("Dialogs.ChangePassword.Actionbar","&aCreate a new password"),

    /* GUIs */
    AUTH_REGISTER_GUI_TITLE("GUIs.Register.Title","&cRegister"),
    AUTH_LOGIN_GUI_TITLE("GUIs.Login.Title","&cLogin"),
    AUTH_CAPTCHA_GUI_TITLE("GUIs.Captcha.Title","&bCaptcha &9> &7Choose &4&l{Captcha}"),
    WRONG_CAPTCHA_GUI_TITLE("GUIs.WrongCaptcha.Title","&cWRONG CAPTCHA!"),

    AUTH_GUI_DISCONNECT_NAME("GUIs.Auth.Disconnect.Name","Disconnect"),
    AUTH_GUI_DISCONNECT_DESCRIPTION("GUIs.Auth.Disconnect.Lore","Because the plugin blocks the inventory closing if you want to disconnect you may click this button."),
    AUTH_GUI_SAVE_PASSWORD_NAME("GUIs.Auth.Save.Name","Save"),
    AUTH_GUI_SAVE_PASSWORD_DESCRIPTION("GUIs.Auth.Save.Lore","Click to save your password"),
    AUTH_GUI_CHECK_PASSWORD_NAME("GUIs.Auth.Check.Name","Check"),
    AUTH_GUI_CHECK_PASSWORD_DESCRIPTION("GUIs.Auth.Check.Lore","Click to check your password"),
    AUTH_GUI_REMOVE_INPUT_NAME("GUIs.Auth.Delete.Name","Delete"),
    AUTH_GUI_REMOVE_INPUT_LEFT("GUIs.Auth.Delete.LeftAction","Delete last digit"),
    AUTH_GUI_REMOVE_INPUT_RIGHT("GUIs.Auth.Delete.RightAction","Clear Input"),

    ADMIN_GUI_USERS_NAME("GUIs.Admin.Users.Name","Users"),
    ADMIN_GUI_USERS_DESCRIPTION("GUIs.Admin.Users.Lore","Manage your users"),
    ADMIN_GUI_CLOSE_NAME("GUIs.Admin.Close.Name","Close GUI"),
    ADMIN_GUI_CLOSE_DESCRIPTION("GUIs.Items.Close.Lore","Click to close the GUI"),

    USER_BROWSER_GUI_TITLE("GUIs.Admin-UserBrowser.Title","&9> &cUsers"),
    USER_BROWSER_BUTTON_NAME("GUIs.Admin-UserBrowser.Manage.Name","&a{User}"),
    USER_BROWSER_BUTTON_LEFT("GUIs.Admin-UserBrowser.Manage.ActionLeft","Manage"),
    USER_BROWSER_BUTTON_Q("GUIs.Admin-UserBrowser.Manage.ActionQ","Unregister/Delete User"),
    USER_BROWSER_REFRESH_CACHE_NAME("GUIs.Admin-UserBrowser.RefreshCache.Name","Refresh Cache"),
    USER_BROWSER_REFRESH_CACHE_DESCRIPTION("GUIs.Admin-UserBrowser.RefreshCache.Lore","Click to refresh the cache"),

    MANAGE_USER_GUI_TITLE("GUIs.Admin-UserManager.Title","&cUsers &9> &5{User}"),
    MANAGE_USER_CHANGE_PASSWORD_NAME("GUIs.Admin-UserManager.ChangePassword.Name","Change Password"),
    MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION("GUIs.Admin-UserManager.ChangePassword.Lore","Click to change the password"),
    MANAGE_USER_TOGGLE_MODE_NAME("GUIs.Admin-UserManager.ToggleMode.Name","Toggle Premium Mode"),
    MANAGE_USER_TOGGLE_MODE_DESCRIPTION("GUIs.Admin-UserManager.ToggleMode.Lore","Click to change to {Mode} mode"),
    MANAGE_USER_CHANGE_PASSWORD_GUI_TITLE("GUIs.Admin-UserPasswordChange.Title","Password Change"),
    MANAGE_USER_CHANGE_PASSWORD_SAVE_NAME("GUIs.Admin-UserPasswordChange.Save.Name","&aSave"),
    MANAGE_USER_CHANGE_PASSWORD_SAVE_DESCRIPTION("GUIs.Admin-UserPasswordChange.Save.Lore","&7Click to save the new password"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_NAME("GUIs.Admin-UserPasswordChange.Delete.Name","Delete"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_LEFT("GUIs.Admin-UserPasswordChange.Delete.LeftAction","Delete last digit"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT("GUIs.Admin-UserPasswordChange.Delete.RightAction","Clear Input"),


    MY_ACCOUNT_GUI_TITLE("GUIs.MyAccount.Title","&cMy Account &9> &a{Player}"),
    MY_ACCOUNT_GUI_CHANGE_PASSWORD_NAME("GUIs.MyAccount.ChangePassword.Name","Change Password"),
    MY_ACCOUNT_GUI_CHANGE_PASSWORD_DESCRIPTION("GUIs.MyAccount.ChangePassword.Lore","Click to change your password"),
    MY_ACCOUNT_GUI_TOGGLE_MODE_NAME("GUIs.MyAccount.ToggleMode.Name","Toggle Premium Mode"),
    MY_ACCOUNT_GUI_TOGGLE_MODE_DESCRIPTION("GUIs.MyAccount.ToggleMode.Lore","Click to change to {Mode} mode"),
    MY_ACCOUNT_CHANGE_PASSWORD_GUI_TITLE("GUIs.ChangePassword.Title","Password Change"),
    MY_ACCOUNT_CHANGE_PASSWORD_SAVE_NAME("GUIs.ChangePassword.Save.Name","&aSave"),
    MY_ACCOUNT_CHANGE_PASSWORD_SAVE_DESCRIPTION("GUIs.ChangePassword.Save.Lore","&7Click to save your new password"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_NAME("GUIs.ChangePassword.Delete.Name","Delete"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_LEFT("GUIs.ChangePassword.Delete.LeftAction","Delete last digit"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT("GUIs.ChangePassword.Delete.RightAction","Clear Input"),


    /* Kick Messages */
    VPN_KICK("Messages.VpnKick","It seems like you're using a VPN. Please turn it off and enter again."),
    CHANGE_MODE_KICK("Messages.ChangeModeKick","&7To apply your settings please enter again."),
    TOOK_TOO_LONG("Messages.TookTooLongKick","&cPlease identify yourself in {Time} seconds or less!"),
    YOUR_IP_HAS_CHANGED("Messages.IpChangedKick","&cWe detected that your IP Address has changed. If you're the owner of the account please contact an admin and share your new IP Address: &b{NewIPAddress}"),

    /* Error Messages */
    SERVER_ERROR_WHILE_CHECKING_IP("Errors.CannotCheckServerIp","Cannot check IP, server returned the following message:"),
    CLIENT_ERROR_WHILE_CHECKING_IP("Errors.CannotCheckUserIp","Error while checking IP:"),
    NULL_CONTENT_RETURNED("Errors.NullContent","Error while reading content: Server returned Null"),
    ERROR_ON_DATA_REQUEST("Errors.FailedRequest","Couldn't request data:"),
    ERROR_WHILE_CREATING_TABLES("Errors.CreateTable","Cannot create tables:"),
    ERROR_WHILE_UPDATING_TABLES("Errors.UpdateTable","Cannot update tables:"),
    ERROR_WHILE_DELETING_USER("Errors.DeleteUser","Couldn't delete user:"),
    ERROR_WHILE_SAVING_USER_DATA("Errors.SaveUser","Error while saving {UserName}'s data:"),
    ERROR_WHILE_HASHING_PASSWORD("Errors.HashPassword","Error while hashing password:"),


    ;

    private TranslationManager manager;
    private final String content, path;

    LBase(String path, String content){
        this.content = content;
        this.path = path;
    }

    @Override
    public String getLanguage() {
        return "en";
    }

    @Override
    public Translation get() {
        return new Translation(this, this.path, this.content);
    }

    @Override
    public List<Translation> translations() {
        return Arrays.stream(values()).map(LBase::get).collect(Collectors.toList());
    }

    @Override
    public void setManager(TranslationManager translationManager) {
        this.manager = translationManager;
    }

    @Override
    public TranslationManager getManager() {
        return manager;
    }

    @Override
    public String toString() {
        return this.get().translate();
    }
}

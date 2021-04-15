package xyz.theprogramsrc.superauth.global.languages;

import xyz.theprogramsrc.supercoreapi.global.translations.Translation;
import xyz.theprogramsrc.supercoreapi.global.translations.TranslationManager;
import xyz.theprogramsrc.supercoreapi.global.translations.TranslationPack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum LBase implements TranslationPack {

    /* Short Words */
    PREMIUM("Premium"),
    CRACKED("Cracked"),
    NO_INPUT("No Input"),

    /* Messages */
    PASSWORD_TOO_SHORT("&cError. Please use a password with at least {Length} characters."),
    PASSWORD_TOO_LONG("&cError. Please use a password with no more than {Length} characters."),
    DIALOG_HOW_TO_USE("&aTo authenticate yourself just write in the chat your password. Don't worry because it wont be showed publicly."),
    GUI_HOW_TO_USE("&aTo authenticate yourself an GUI will be opened, if you cannot see the GUI use the command /{Command} and enter your pin."),
    COMMAND_HOW_TO_USE("&aTo register use the command /{RegisterCommand} <Password>. To login use the command /{LoginCommand} <Password>"),
    REGISTER_COMMAND_USAGE("&cUse &e/{Command} <Password> &cto register."),
    LOGIN_COMMAND_USAGE("&cUse &e/{Command} <Password> &cto login."),

    ERROR_WHILE_HASHING("&cThe hashing util is not working right now, try again later."),
    WRONG_CAPTCHA("&cWrong captcha! &aEnter this captcha: &b{Captcha}"),
    WRONG_PASSWORD("&cWrong password!"),
    STILL_IN_AUTH("&cYou can't do this! You need to authenticate yourself!"),
    ERROR_FETCHING_DATA("&cError while fetching data. Please contact an administrator"),
    ALREADY_IDENTIFIED("&cYou're already identified!"),

    ALREADY_PREMIUM("&cYou're already a premium user!"),
    ALREADY_CRACKED("&cYou're already a cracked user!"),

    CONFIRMATION_MESSAGE("&eIn order to confirm you need to run the command &7$1"),
    CHANGE_MODE_WARNING("&cWARNING: IF YOU CHANGE YOUR MODE YOU MAY LOSE ALL THE DATA AND PROGRESS"),

    ALREADY_ADMIN("&cThe user &a$1&c is already an admin!"),
    ADDED_ADMIN("&aNow the user &c$1 &ais an admin."),
    ALREADY_NON_ADMIN("&cThe user &a$1&c is not an admin!"),
    REMOVED_ADMIN("&aThe user &c$1 &ais no longer an admin."),

    USER_NOT_REGISTERED("The user must be registered!"),
    USER_NOT_EXISTS("That user doesn't exists!"),
    USER_ALREADY_IDENTIFIED("That user is already identified!"),
    ERROR_WHILE_FETCHING_PLAYER("Error while fetching player from User DataBase, maybe the user is not online"),

    FORCED_LOGIN("&aForced login for user &c$1"),
    REMOVE_REQUEST_SENT("The remove request was sent"),

    USE_LOGIN_COMMAND("&cYou're already registered! Use the command &e/$1 <Password>"),
    USE_REGISTER_COMMAND("&cYou're not registered! Please register yourself using the command &e/$1 <Password>"),

    WIKI_INFORMATION("You can view all the commands at the wiki: https://wiki.theprogramsrc.xyz/"),
    FORCE_LOGIN_NOT_SUPPORTED("You can't use force login with the auth method DIALOG"),

    CONSOLE_UPDATED_USER_IP_ADDRESS("&aSent request to update &7{UserName}'s&a account IP Address to: &b{NewIPAddress}"),
    CONSOLE_UPDATED_USER_MODE("&aSent request to update &7{UserName}'s&a account mode to: &b{NewMode}"),

    /* DIALOGS */
    DIALOG_REGISTER_TITLE("&bRegister"),
    DIALOG_REGISTER_SUBTITLE("&7Write in the chat your password"),
    DIALOG_REGISTER_ACTIONBAR("&aDon't use commands, just write your password"),
    DIALOG_LOGIN_TITLE("&bLogin"),
    DIALOG_LOGIN_SUBTITLE("&7Write in the chat your password"),
    DIALOG_LOGIN_ACTIONBAR("&aDon't use commands, just write your password"),
    DIALOG_CAPTCHA_TITLE("&bCaptcha"),
    DIALOG_CAPTCHA_SUBTITLE("&7Write in the chat: &9&l$1"),
    DIALOG_CAPTCHA_ACTIONBAR("&aWrite '&9&l$1&a' in the chat"),
    DIALOG_AUTH_COMMAND_USED("&cDon't use commands! Just write your password in the chat"),

    DIALOG_CHANGE_PASSWORD_TITLE("&9Password"),
    DIALOG_CHANGE_PASSWORD_SUBTITLE("&7Change Password"),
    DIALOG_CHANGE_PASSWORD_ACTIONBAR("&aCreate a new password"),

    /* GUIs */
    AUTH_REGISTER_GUI_TITLE("&cRegister"),
    AUTH_LOGIN_GUI_TITLE("&cLogin"),
    AUTH_CAPTCHA_GUI_TITLE("&bCaptcha &9> &7Choose &4&l$1"),
    WRONG_CAPTCHA_GUI_TITLE("&cWRONG CAPTCHA!"),

    AUTH_GUI_DISCONNECT_NAME("Disconnect"),
    AUTH_GUI_DISCONNECT_DESCRIPTION("Because the plugin blocks the inventory closing if you want to disconnect you may click this button."),
    AUTH_GUI_SAVE_PASSWORD_NAME("Save"),
    AUTH_GUI_SAVE_PASSWORD_DESCRIPTION("Click to save your password"),
    AUTH_GUI_CHECK_PASSWORD_NAME("Check"),
    AUTH_GUI_CHECK_PASSWORD_DESCRIPTION("Click to check your password"),
    AUTH_GUI_REMOVE_INPUT_NAME("Delete"),
    AUTH_GUI_REMOVE_INPUT_LEFT("Delete last digit"),
    AUTH_GUI_REMOVE_INPUT_RIGHT("Clear Input"),

    ADMIN_GUI_USERS_NAME("Users"),
    ADMIN_GUI_USERS_DESCRIPTION("Manage your users"),
    ADMIN_GUI_CLOSE_NAME("Close GUI"),
    ADMIN_GUI_CLOSE_DESCRIPTION("Click to close the GUI"),

    USER_BROWSER_GUI_TITLE("&9> &cUsers"),
    USER_BROWSER_BUTTON_LEFT("Manage"),
    USER_BROWSER_BUTTON_Q("Unregister/Delete User"),
    USER_BROWSER_REFRESH_CACHE_NAME("Refresh Cache"),
    USER_BROWSER_REFRESH_CACHE_DESCRIPTION("Click to refresh the cache"),

    MANAGE_USER_GUI_TITLE("&cUsers &9> &5$1"),
    MANAGE_USER_CHANGE_PASSWORD_NAME("Change Password"),
    MANAGE_USER_CHANGE_PASSWORD_DESCRIPTION("Click to change the password"),
    MANAGE_USER_TOGGLE_MODE_NAME("Toggle Premium Mode"),
    MANAGE_USER_TOGGLE_MODE_DESCRIPTION("Click to change to {Mode} mode"),
    MANAGE_USER_CHANGE_PASSWORD_GUI_TITLE("Password Change"),
    MANAGE_USER_CHANGE_PASSWORD_SAVE_NAME("&aSave"),
    MANAGE_USER_CHANGE_PASSWORD_SAVE_DESCRIPTION("&7Click to save the new password"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_NAME("Delete"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_LEFT("Delete last digit"),
    MANAGE_USER_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT("Clear Input"),


    MY_ACCOUNT_GUI_TITLE("&cMy Account &9> &a{Player}"),
    MY_ACCOUNT_GUI_CHANGE_PASSWORD_NAME("Change Password"),
    MY_ACCOUNT_GUI_CHANGE_PASSWORD_DESCRIPTION("Click to change your password"),
    MY_ACCOUNT_GUI_TOGGLE_MODE_NAME("Toggle Premium Mode"),
    MY_ACCOUNT_GUI_TOGGLE_MODE_DESCRIPTION("Click to change to {Mode} mode"),
    MY_ACCOUNT_CHANGE_PASSWORD_GUI_TITLE("Password Change"),
    MY_ACCOUNT_CHANGE_PASSWORD_SAVE_NAME("&aSave"),
    MY_ACCOUNT_CHANGE_PASSWORD_SAVE_DESCRIPTION("&7Click to save your new password"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_NAME("Delete"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_LEFT("Delete last digit"),
    MY_ACCOUNT_CHANGE_PASSWORD_REMOVE_INPUT_RIGHT("Clear Input"),


    /* Kick Messages */
    VPN_KICK("It seems like you're using a VPN. Please turn it off and enter again."),
    CHANGE_MODE_KICK("&7To apply your settings please enter again."),
    TOOK_TOO_LONG("&cPlease identify yourself in {Seconds} seconds or less!"),
    YOUR_IP_HAS_CHANGED("&cWe detected that your IP Address has changed. If you're the owner of the account please contact an admin and share your new IP Address: &b{NewIPAddress}"),

    /* Error Messages */
    SERVER_ERROR_WHILE_CHECKING_IP("Cannot check IP, server returned the following message:"),
    CLIENT_ERROR_WHILE_CHECKING_IP("Error while checking IP:"),
    NULL_CONTENT_RETURNED("Error while reading content: Server returned Null"),
    ERROR_ON_DATA_REQUEST("Couldn't request data:"),
    ERROR_WHILE_CREATING_TABLES("Cannot create tables:"),
    ERROR_WHILE_UPDATING_TABLES("Cannot update tables:"),
    ERROR_WHILE_DELETING_USER("Couldn't delete user:"),
    ERROR_WHILE_SAVING_USER_DATA("Error while saving {UserName}'s data:"),
    ERROR_WHILE_HASHING_PASSWORD("Error while hashing password:"),


    ;

    private TranslationManager manager;
    private final String content;

    LBase(String content){
        this.content = content;
    }

    @Override
    public Locale getLanguage() {
        return new Locale("en","US");
    }

    @Override
    public Translation get() {
        return new Translation(this, this.name(), this.content);
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

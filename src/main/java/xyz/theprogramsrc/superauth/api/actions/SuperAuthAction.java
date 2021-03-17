package xyz.theprogramsrc.superauth.api.actions;

import org.bukkit.entity.Player;

public interface SuperAuthAction {

    /**
     * The prefix should be without ':'
     * @return the prefix
     */
    String getPrefix();

    /**
     * Executed when the action is triggered
     * @param player the player who executed the action
     * @param argument the provided argument in the action
     * @param before true if it's before auth, false otherwise
     * @param register true if it's registering, false otherwise
     */
    void onExecute(Player player, String argument, boolean before, boolean register);
}

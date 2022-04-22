package xyz.theprogramsrc.superauth.global

/**
 * Representation of an authentication method
 */
enum class AuthMethod {
    /**
     * Authentication with PIN GUI
     */
    GUI,

    /**
     * Authentication with Chat Password
     */
    DIALOG,

    /**
     * Authentication with Command Password
     */
    COMMAND,
}
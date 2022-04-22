package xyz.theprogramsrc.superauth.global

import java.security.SecureRandom

object Hashing {

    private val secureRandom = SecureRandom()

    private val hexChars = "0123456789ABCDEF".toCharArray()
    private val numberChars = "0123456789".toCharArray()
    private val alphaNumChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()

    /**
     * Generates a random hexadecimal string of the given length.
     *
     * @param length The length of the string to generate
     * @return The generated string
     */
    fun generateHex(length: Int = 32): String {
        check(length > 0) { "Length must be greater than 0, but it was $length" }
        return (0 until length).map {
            hexChars[secureRandom.nextInt(hexChars.size)]
        }.joinToString("")
    }

    /**
     * Generates a random number string of the given length.
     *
     * @param length The length of the string to generate
     * @return The generated string
     */
    fun generateNumbers(length: Int = 32): String {
        check(length > 0) { "Length must be greater than 0, but it was $length" }
        return (0 until length).map {
            numberChars[secureRandom.nextInt(numberChars.size)]
        }.joinToString("")
    }

    /**
     * Generates a random alphanumeric string of the given length.
     *
     * @param length The length of the string to generate
     * @return The generated string
     */
    fun generateAlphaNumeric(length: Int = 32): String {
        check(length > 0) { "Length must be greater than 0, but it was $length" }
        return (0 until length).map {
            alphaNumChars[secureRandom.nextInt(alphaNumChars.size)]
        }.joinToString("")
    }

}
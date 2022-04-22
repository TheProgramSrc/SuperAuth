package xyz.theprogramsrc.superauth.global.hashing

interface HashingAlgorithm {

    /**
     * Checks if this algorithm is loaded and ready
     * to use.
     *
     * @return true if loaded, false otherwise
     */
    fun isLoaded(): Boolean = true

    /**
     * Generates a hash from the given [input]
     *
     * @param input the input to hash
     * @return the hash
     */
    fun hash(input: String): String

    /**
     * Checks if the given [password] matches the given [hash].
     *
     * @param password the first string
     * @param hash the second string
     * @return true if the password matches the hash, false otherwise
     */
    fun check(password: String, hash: String): Boolean
}
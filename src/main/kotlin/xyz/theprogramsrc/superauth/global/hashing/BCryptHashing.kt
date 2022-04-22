package xyz.theprogramsrc.superauth.global.hashing

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Representation of a BCrypt Hashing Algorithm
 *
 * @param version The version of the hashing algorithm
 */
open class BCryptHashing(version: BCrypt.Version = BCrypt.Version.VERSION_2A): HashingAlgorithm {

    private val hasher = BCrypt.with(version)
    private val verifyer = BCrypt.verifyer(version)

    override fun hash(input: String): String =
        hasher.hashToString(12, input.toCharArray())

    override fun check(password: String, hash: String): Boolean =
        verifyer.verifyStrict(password.toCharArray(), hash.toCharArray()).verified


}

/**
 * Representation of the BCrypt Hashing Algorithm with Version 2A
 */
class BCrypt2AHashing: BCryptHashing(BCrypt.Version.VERSION_2A)

/**
 * Representation of the BCrypt Hashing Algorithm with Version 2B
 */
class BCrypt2BHashing: BCryptHashing(BCrypt.Version.VERSION_2B)

/**
 * Representation of the BCrypt Hashing Algorithm with Version 2X
 */
class BCrypt2XHashing: BCryptHashing(BCrypt.Version.VERSION_2X)

/**
 * Representation of the BCrypt Hashing Algorithm with Version 2Y
 */
class BCrypt2YHashing: BCryptHashing(BCrypt.Version.VERSION_2Y)

/**
 * Representation of the BCrypt Hashing Algorithm with Version 2Y No Null Terminator
 */
class BCrypt2YNoNullTerminatorHashing: BCryptHashing(BCrypt.Version.VERSION_2Y_NO_NULL_TERMINATOR)
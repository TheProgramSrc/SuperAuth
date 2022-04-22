package xyz.theprogramsrc.superauth.global.hashing

import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Representation of the SHA Based Hashing Algorithm
 *
 * @param version The version of the hashing algorithm. Defaults to 'SHA-512'
 */
open class ShaHashing(private val version: String = "SHA-512"): HashingAlgorithm {

    override fun hash(input: String): String = MessageDigest.getInstance(version).let { digest ->
        digest.reset()
        digest.update(input.toByteArray(Charset.defaultCharset()))
        digest.digest().let { bytes ->
            String.format("%0${bytes.size shl 1}x", BigInteger(1, bytes))
        }
    }

    override fun check(password: String, hash: String): Boolean =
        MessageDigest.isEqual(
            hash(password).toByteArray(Charset.defaultCharset()),
            hash.toByteArray(Charset.defaultCharset())
        )
}

/**
 * Representation of the Sha1 Hashing Algorithm
 */
class SHA1: ShaHashing("SHA-1")

/**
 * Representation of the Sha256 Hashing Algorithm
 */
class SHA256: ShaHashing("SHA-256")

/**
 * Representation of the Sha384 Hashing Algorithm
 */
class SHA384: ShaHashing("SHA-384")

/**
 * Representation of the Sha512 Hashing Algorithm
 */
class SHA512: ShaHashing()
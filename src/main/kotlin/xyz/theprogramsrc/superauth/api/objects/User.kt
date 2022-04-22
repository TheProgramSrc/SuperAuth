package xyz.theprogramsrc.superauth.api.objects

import xyz.theprogramsrc.superauth.global.AuthMethod
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture

/**
 * Representation of a user
 * @param username The username of the user
 * @param password The password of the user
 * @param ip The ip of the user
 * @param premium Whether the user is premium or not
 * @param admin Whether the user is admin or not
 * @param authorized Whether the user is authorized or not
 * @param registered Whether the user is registered or not
 * @param authMethod The auth method of the user
 * @param skinTexture The skin texture of the user
 */
data class User(
    val username: String,
    val password: String,
    val ip: String?,
    val premium: Boolean,
    val admin: Boolean,
    val authorized: Boolean,
    val registered: Boolean,
    val authMethod: AuthMethod,
    val skinTexture: SkinTexture?,
)

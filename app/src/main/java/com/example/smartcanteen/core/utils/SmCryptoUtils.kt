package com.example.smartcanteen.core.utils

import android.util.Base64
import android.util.Log
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SmCryptoUtils {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    /**
     * RSA2 签名：强制 NO_WRAP
     */
    fun signRSA2(content: String, privateKeyString: String): String {
        return try {
            val cleanKey = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")

            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            val privateKey: PrivateKey = try {
                KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
            } catch (e: Exception) {
                val rsaPrivKey = RSAPrivateKey.getInstance(keyBytes)
                val rsaSpec = RSAPrivateCrtKeySpec(
                    rsaPrivKey.modulus, rsaPrivKey.publicExponent, rsaPrivKey.privateExponent,
                    rsaPrivKey.prime1, rsaPrivKey.prime2, rsaPrivKey.exponent1, rsaPrivKey.exponent2,
                    rsaPrivKey.coefficient
                )
                KeyFactory.getInstance("RSA", "BC").generatePrivate(rsaSpec)
            }

            val signature = Signature.getInstance("SHA256WithRSA")
            signature.initSign(privateKey)
            signature.update(content.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("SmCryptoUtils", "RSA签名失败: ${e.message}")
            ""
        }
    }

    /**
     * AES 加密：强制 NO_WRAP, IV 为 16 字节 0
     */
    fun encryptAES(content: String, aesKeyBase64: String): String {
        return try {
            val keyBytes = Base64.decode(aesKeyBase64, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val iv = IvParameterSpec(ByteArray(16))
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
            val encryptedBytes = cipher.doFinal(content.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            content
        }
    }
}

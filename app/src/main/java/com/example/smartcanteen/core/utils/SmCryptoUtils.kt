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

/**
 * 签名与加密工具类 - 深度适配工行网关
 * 支持 RSA2 签名与 AES 报文加密
 */
object SmCryptoUtils {

    init {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    /**
     * 生成请求报文的 RSA2 签名
     */
    fun signRSA2(content: String, privateKeyString: String): String {
        if (privateKeyString.contains("TEMP_PRIVATE_KEY") || privateKeyString.isBlank()) {
            Log.e("SmCryptoUtils", "警告：私钥尚未配置！")
            return ""
        }

        return try {
            val cleanKey = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")

            val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
            
            val privateKey: PrivateKey = try {
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyBytes))
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
            Log.e("SmCryptoUtils", "签名生成失败: ${e.message}")
            ""
        }
    }

    /**
     * 工行标准 AES 加密 (AES/CBC/PKCS5Padding, IV为16个0)
     * @param content 待加密的明文 (JSON)
     * @param aesKeyBase64 Base64 格式的 AES 密钥
     */
    fun encryptAES(content: String, aesKeyBase64: String): String {
        return try {
            val keyBytes = Base64.decode(aesKeyBase64, Base64.DEFAULT)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            
            // 工行规范：IV 为 16 字节的 0
            val iv = IvParameterSpec(ByteArray(16))
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
            
            val encryptedBytes = cipher.doFinal(content.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("SmCryptoUtils", "AES 加密失败: ${e.message}")
            content // 失败则返回原内容
        }
    }
}

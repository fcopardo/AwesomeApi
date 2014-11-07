package com.awesomeapi.libs

// Based on Play! 2 Framework implementation
// https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/libs/Crypto.scala

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

/**
 * Cryptographic utilities.
 */

object Crypt {

  private val hexSize = 16
  private val tokenSize = 52
  private val encoding = "utf-8"
  private lazy val conf = com.typesafe.config.ConfigFactory.load("secrets")
  private lazy val provider: Option[String] = None
  private lazy val transformation: String = "AES"
  private val random = new SecureRandom()
  val cipher = provider.map(p => Cipher.getInstance(transformation, p)).getOrElse(Cipher.getInstance(transformation))

  private lazy val secret: String = {
    conf.getString(com.awesomeapi.libs.Environment.env + ".secret_key") match {
      case "SET A PRODUCTION KEY" | "" => throw new NotImplementedError("Must implement a secret key for production environment.")
      case key: String => {
        if (key.length < hexSize) throw new IllegalArgumentException("Secret key must have 16 bytes at least.")
        key
      }
    }
  }

  /**
   * Generate a cryptographically secure token
   * @return An Base64 encrypted string.
   */
  def generateToken: String = {
    val bytes = new Array[Byte](tokenSize)
    random.nextBytes(bytes)
    Codecs.toBase64(bytes).
      replace("+","-").
      replace("/",".").
      replace("=","").trim
  }


  /**
   * Encrypt a String with the AES encryption standard using the application's secret key.
   *
   * @param value The String to encrypt.
   * @return An hexadecimal encrypted string.
   */
  def encryptAES(value: String): String = {
    encryptAES(value, secret.substring(0, hexSize))
  }

  /**
   * Encrypt a String with the AES encryption standard and the supplied private key.
   *
   * @param value The String to encrypt.
   * @param privateKey The key used to encrypt.
   * @return An hexadecimal encrypted string.
   */
  def encryptAES(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes(encoding)
    val skeySpec = new SecretKeySpec(raw, transformation)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    Codecs.toHexString(cipher.doFinal(value.getBytes(encoding)))
  }

  /**
   * Decrypt a String with the AES encryption standard using the application's secret key.
   *
   * @param value An hexadecimal encrypted string.
   * @return The decrypted String.
   */
  def decryptAES(value: String): String = {
    decryptAES(value, secret.substring(0, hexSize))
  }

  /**
   * Decrypt a String with the AES encryption standard.
   *
   * The private key must have a length of 16 bytes.
   *
   * @param value An hexadecimal encrypted string.
   * @param privateKey The key used to encrypt.
   * @return The decrypted String.
   */
  def decryptAES(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes(encoding)
    val skeySpec = new SecretKeySpec(raw, transformation)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(
      cipher.doFinal(
        Codecs.hexStringToByte(
          Codecs.base64Padding(value)
        )
      )
    )
  }

  /**
   * Encrypt a String with the AES encryption standard using the application's secret key to Base 62.
   *
   * @param value The String to encrypt.
   * @return An hexadecimal encrypted string.
   */
  def encrypt(value: String): String = {
    encrypt(value, secret.substring(0, hexSize))
  }

  /**
   * Encrypt a String with the AES encryption standard and the supplied private key to Base 62.
   *
   * @param value The String to encrypt.
   * @param privateKey The key used to encrypt.
   * @return An hexadecimal encrypted string.
   */
  def encrypt(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes(encoding)
    val skeySpec = new SecretKeySpec(raw, transformation)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    Codecs.toBase64(cipher.doFinal(value.getBytes(encoding))).
      replace("+","-").
      replace("/",".").
      replace("=","").trim
  }

  /**
   * Decrypt a String with the AES encryption standard using the application's secret key from Base 62.
   *
   * @param value An hexadecimal encrypted string.
   * @return The decrypted String.
   */
  def decrypt(value: String): String = {
    decrypt(value, secret.substring(0, hexSize))
  }

  /**
   * Decrypt a String with the AES encryption standard from Base 62.
   *
   * The private key must have a length of 16 bytes.
   *
   * @param value An hexadecimal encrypted string.
   * @param privateKey The key used to encrypt.
   * @return The decrypted String.
   */
  def decrypt(value: String, privateKey: String): String = {
    val raw = privateKey.getBytes(encoding)
    val skeySpec = new SecretKeySpec(raw, transformation)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    new String(
      cipher.doFinal(
        Codecs.fromBase64ToByte(
          Codecs.base64Padding(
            value.replace("-","+").replace(".","/")
          )
        )
      ), encoding
    )
  }
}

package com.awesomeapi.libs

// Based on Play! 2 Framework implementation
// https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/play/api/libs/Codecs.scala

object Codecs {

  import java.security.MessageDigest

  lazy val mdSha1 = MessageDigest.getInstance("SHA-1")
  lazy val mdMD5 = MessageDigest.getInstance("MD5")

  /**
   * Computes the SHA-1 digest for a byte array.
   *
   * @param bytes the data to hash
   * @return the SHA-1 digest, encoded as a hex string
   */
  def sha1(bytes: Array[Byte]): String = {
    mdSha1.reset()
    mdSha1.update(bytes)
    mdSha1.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }

  /**
   * Compute the SHA-1 digest for a `String`.
   *
   * @param text the text to hash
   * @return the SHA-1 digest, encoded as a hex string
   */
  def sha1(text: String): String = sha1(text.getBytes)

  /**
   * Computes the MD5 digest for a byte array.
   *
   * @param bytes the data to hash
   * @return the MD5 digest, encoded as a hex string
   */
  def md5(bytes: Array[Byte]): String = {
    mdMD5.reset()
    mdMD5.update(bytes)
    mdMD5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }

  def md5(text: String): String = md5(text.getBytes)

  // --

  private val hexChars = "0123456789abcdef".toList

  /**
   * Converts a byte array into an array of characters that denotes a hexadecimal representation.
   */
  def toHex(array: Array[Byte]): Array[Char] = {
    val result = new Array[Char](array.length * 2)
    for (i <- 0 until array.length) {
      val b = array(i) & 0xff
      result(2 * i) = hexChars(b >> 4)
      result(2 * i + 1) = hexChars(b & 0xf)
    }
    result
  }

  /**
   * Converts a byte array into a `String` that denotes a hexadecimal representation.
   */
  def toHexString(array: Array[Byte]): String = {
    new String(toHex(array))
  }

  /**
   * Transform an hexadecimal String to a byte array.
   */
  def hexStringToByte(hexString: String): Array[Byte] = {
    import org.apache.commons.codec.binary.Hex
    Hex.decodeHex(hexString.toCharArray)
  }

  val base64encoder = new sun.misc.BASE64Encoder()
  val base64decoder = new sun.misc.BASE64Decoder()

  def toBase64(str: String): String = {
    toBase64(str.getBytes)
  }

  def toBase64(array: Array[Byte]): String = {
    base64encoder.encodeBuffer(array)
  }

  def fromBase64(str: String): String = {
    new String(fromBase64ToByte(str))
  }

  def fromBase64ToByte(str: String): Array[Byte] = {
    base64decoder.decodeBuffer(str)
  }

  def base64Padding(str: String): String = {
    if (str.length % 4 == 0) str
    else base64Padding(str + "=")
  }
}

package com.faforever.uid;

import com.google.gson.JsonObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UID {

  private static final OperatingSystem OPERATING_SYSTEM = OperatingSystem.current();
  private final SecureRandom secureRandom;

  private BigInteger publicKey = new BigInteger("13731707816857396218511477189051880183926672022487649441793167544537");
  private Base64.Encoder base64Encoder;

  public UID() {
    secureRandom = new SecureRandom();
    base64Encoder = Base64.getEncoder();
  }

  public String generateUid(String session) {
    try {
      // Step 1: Load public key
      RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicKey, new BigInteger("65537"));
      KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = rsaKeyFactory.generatePublic(keySpec);

      // Step 2: Generate 16 bytes initialization vector, encode base 64
      byte[] initVector = new byte[16];
      secureRandom.nextBytes(initVector);
      byte[] base64InitVector = base64Encoder.encode(initVector);
      if(base64InitVector.length != 24) {
        throw new IllegalStateException("Initialization vector base64 was not 24 bytes");
      }

      // Step 3: Generate AES key 16 bytes, encrypt it, encode base 64
      KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
      aesKeyGen.init(16);
      SecretKey aesKey = aesKeyGen.generateKey();

      Cipher rsaCipher = Cipher.getInstance("RSA");
      rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
      byte[] aesKeyEncrypted = rsaCipher.doFinal(aesKey.getEncoded());

      byte[] aesKeyEncrytpedBase64 = base64Encoder.encode(aesKeyEncrypted);
      if(aesKeyEncrytpedBase64.length != 40) {
        throw new IllegalStateException("AES key base64 was not 40 bytes");
      }

      AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
      aesParams.init(initVector);
      Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, aesParams);

      // Step 4: AES-encrypt the json string, encode base 64
      // Prefix the JSON string with char '2' to indicate the new uid data format for the server
      String jsonString = "2" + getSystemInfoJson(session);
      // Insert trailing bytes to make len(json_string) a multiple of 16
      int jsonLength = jsonString.length();
      int trailLength = ((((jsonLength / 16) + 1) * 16) - jsonLength);
      jsonString = jsonString + new String(new char[trailLength]).replace("\0", "x");

      byte[] encryptedString = aesCipher.doFinal(jsonString.getBytes(UTF_8));
      byte[] encryptedStringBase64 = base64Encoder.encode(encryptedString);

      // Step 5: put message together
      byte[] messageBytes = append(new byte[]{(byte) trailLength}, base64InitVector, encryptedStringBase64, aesKeyEncrytpedBase64);
      return base64Encoder.encodeToString(messageBytes);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  String getSystemInfoJson(String session) {
    JsonObject info = new JsonObject();
    info.addProperty("session", session);

    JsonObject machine = new JsonObject();
    JsonObject display = new JsonObject();

    DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

    display.addProperty("width", dm.getHeight());
    display.addProperty("height", dm.getWidth());

    info.add("desktop", display);

    switch (OPERATING_SYSTEM) {
      case WINDOWS:
        readValues(machine, new WindowsValueProvider());
        break;
      case UNIX:
        readValues(machine, new UnixValueProvider());
        break;
      default:
        throw new RuntimeException("Operating system is not supported");
    }

    info.add("machine", machine);
    return info.toString();
  }

  private void readValues(JsonObject machine, ValueProvider provider) {
    JsonObject motherboard = new JsonObject();
    motherboard.addProperty("vendor", provider.getMotherboardVendor());
    motherboard.addProperty("name", provider.getMotherboardName());
    machine.add("motherboard", motherboard);

    JsonObject model = new JsonObject();
    model.addProperty("name", provider.getModel());
    model.addProperty("manufacturer", provider.getManufacturer());
    machine.add("model", model);

    JsonObject processor = new JsonObject();
    processor.addProperty("name", provider.getProcessorName());
    processor.addProperty("id", provider.getProcessorId());
    machine.add("processor", processor);

    JsonObject memory = new JsonObject();
    memory.addProperty("serial0", provider.getMemSerial());
    machine.add("memory", memory);

    machine.addProperty("uuid", provider.getUuid());

    JsonObject os = new JsonObject();
    os.addProperty("version", provider.getOsVersion());
    os.addProperty("type", provider.getOsName());
    machine.add("os", os);

    JsonObject bios = new JsonObject();
    bios.addProperty("manufacturer", provider.getBiosManufacturer());
    bios.addProperty("version", provider.getBiosVersion());
    bios.addProperty("date", provider.getBiosReleaseDate());
    bios.addProperty("serial", provider.getBiosSerial());
    bios.addProperty("description", provider.getBiosDescription());
    bios.addProperty("smbbversion", provider.getBiosSmbVersion());
    machine.add("bios", bios);

    JsonObject disks = new JsonObject();
    disks.addProperty("vserial", provider.getDiskSerial());
    disks.addProperty("controller_id", provider.getDiskControllerId());
    machine.add("disks", disks);
  }

  public static byte[] append(final byte[]... arrays) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
      for (final byte[] array : arrays) {
          out.write(array, 0, array.length);
    }
    return out.toByteArray();
  }

  public static void main(String[] args) {
    UID uid = new UID();
    System.out.println(uid.getSystemInfoJson(""));
    System.out.println(uid.generateUid(""));
  }
}

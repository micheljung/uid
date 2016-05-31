package com.faforever.uid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Pattern.MULTILINE;

public class UnixValueProvider implements ValueProvider {

  private static final String INVALID = "invalid";
  private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("model name\\s*: (.*)");
  private static final Pattern LSPCI_SATA_PATTERN = Pattern.compile(".{7}0106: (....:....)", MULTILINE);
  private static final Pattern LSPCI_IDE_PATTERN = Pattern.compile(".{7}0601: (....:....)", MULTILINE);
  private static final Pattern LSBLK_SERIAL_MATCHER = Pattern.compile("/          (.*)", MULTILINE);

  private final Runtime runtime;

  public UnixValueProvider() {
    runtime = Runtime.getRuntime();
  }

  @Override
  public String getMotherboardVendor() {
    try {
      return readFile("/sys/class/dmi/id/board_vendor");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getMotherboardName() {
    try {
      return readFile("/sys/class/dmi/id/board_name");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getProcessorName() {
    try {
      Matcher matcher = MODEL_NAME_PATTERN.matcher(readFile("/proc/cpuinfo"));
      return matcher.find() ? matcher.group(1) : INVALID;
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getProcessorId() {
    return INVALID;
  }

  @Override
  public String getMemSerial() {
    return INVALID;
  }

  @Override
  public String getUuid() {
    try {
      return readFile("/var/lib/dbus/machine-id");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getOsVersion() {
    try {
      return exec("uname -r");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getOsName() {
    try {
      return exec("uname -s");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getBiosManufacturer() {
    return readBiosValue("vendor");
  }

  @Override
  public String getBiosVersion() {
    return readBiosValue("version");
  }

  @Override
  public String getBiosReleaseDate() {
    return readBiosValue("date");
  }

  @Override
  public String getBiosSerial() {
    return INVALID;
  }

  @Override
  public String getBiosDescription() {
    return INVALID;
  }

  @Override
  public String getBiosSmbVersion() {
    return getBiosVersion();
  }

  @Override
  public String getDiskSerial() {
    try {
      Matcher matcher = LSBLK_SERIAL_MATCHER.matcher(exec("lsblk -n -o mountpoint,serial"));
      return matcher.find() ? matcher.group(1) : INVALID;
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getDiskControllerId() {
    try {
      Matcher matcher = LSPCI_SATA_PATTERN.matcher(exec("lspci -n"));
      if (matcher.find()) {
        return matcher.group(1);
      }

      matcher = LSPCI_IDE_PATTERN.matcher(exec("lspci -n"));
      return matcher.find() ? matcher.group(1) : INVALID;
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getModel() {
    try {
      return readFile("/sys/class/dmi/id/sys_vendor");
    } catch (IOException e) {
      return INVALID;
    }
  }

  @Override
  public String getManufacturer() {
    try {
      return readFile("/sys/class/dmi/id/product_name");
    } catch (IOException e) {
      return INVALID;
    }
  }

  private String readBiosValue(String key) {
    try {
      String dmiFilename = String.format("/sys/class/dmi/id/bios_%s", key);
      if (Files.exists(Paths.get(dmiFilename))) {
        return readFile(dmiFilename);
      } else {
        return readFile(String.format("/sys/class/virtual/id/bios_%s", key));
      }
    } catch (IOException e) {
      return INVALID;
    }
  }

  private static String readFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)), UTF_8).trim();
  }

  private String exec(String cmd) throws IOException {
    Scanner scanner = new Scanner(runtime.exec(cmd).getInputStream(), UTF_8.name()).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next().trim() : "";
  }
}

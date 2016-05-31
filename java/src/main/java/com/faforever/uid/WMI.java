package com.faforever.uid;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class WMI {

  public static final Pattern PATTERN = Pattern.compile("wmic:root\\\\cli>");
  private static final String INVALID = "invalid";

  private Process wmic;
  private OutputStreamWriter writer;
  private Scanner scanner;

  public WMI() {
    try {
      wmic = new ProcessBuilder("wmic").start();

      writer = new OutputStreamWriter(wmic.getOutputStream());
      scanner = new Scanner(wmic.getInputStream());
      scanner.useDelimiter(PATTERN);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String queryValue(String mode, String value) {
    try {
      writer.write(String.format("%s get %s\n", mode, value));
      writer.flush();
      String[] result = scanner.next().replaceAll("\\r", "").split("\\n");
      if (result.length >= 2 && result[0].trim().toLowerCase().equals(value.toLowerCase())) {
        String re = result[1].trim();
        if (!re.equals("")) {
          return re;
        }
        return null;
      }
      return INVALID;
    } catch (IOException e) {
      return INVALID;
    }
  }

  public void terminate() {
    try {
      writer.write("exit\n");
      writer.flush();
      wmic.waitFor();
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}

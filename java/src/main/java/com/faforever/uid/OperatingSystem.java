package com.faforever.uid;

public enum OperatingSystem {
  WINDOWS,
  UNIX,
  OTHER;

  public static OperatingSystem current() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return WINDOWS;
    }
    if (osName.contains("mac") || osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
      return UNIX;
    }

    return OTHER;
  }
}

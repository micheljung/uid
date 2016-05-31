package com.faforever.uid;

public interface ValueProvider {

  String getMotherboardVendor();

  String getMotherboardName();

  String getProcessorName();

  String getProcessorId();

  String getMemSerial();

  String getUuid();

  String getOsVersion();

  String getOsName();

  String getBiosManufacturer();

  String getBiosVersion();

  String getBiosReleaseDate();

  String getBiosSerial();

  String getBiosDescription();

  String getBiosSmbVersion();

  String getDiskSerial();

  String getDiskControllerId();

  String getModel();

  String getManufacturer();
}

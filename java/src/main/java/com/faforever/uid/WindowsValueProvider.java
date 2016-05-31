package com.faforever.uid;

public class WindowsValueProvider implements ValueProvider, AutoCloseable {

  private final WMI wmi;

  public WindowsValueProvider() {
    wmi = new WMI();
  }

  @Override
  public String getMotherboardVendor() {
    return wmi.queryValue("BaseBoard", "Manufacturer");
  }

  @Override
  public String getMotherboardName() {
    return wmi.queryValue("BaseBoard", "Product");
  }

  @Override
  public String getProcessorName() {
    return wmi.queryValue("CPU", "Name");
  }

  @Override
  public String getProcessorId() {
    return wmi.queryValue("CPU", "ProcessorId");
  }

  @Override
  public String getMemSerial() {
    return wmi.queryValue("MemPhysical", "SerialNumber");
  }

  @Override
  public String getUuid() {
    return wmi.queryValue("CSProduct", "UUID");
  }

  @Override
  public String getOsVersion() {
    return wmi.queryValue("OS", "Version");
  }

  @Override
  public String getOsName() {
    return wmi.queryValue("BIOS", "Manufacturer");
  }

  @Override
  public String getBiosManufacturer() {
    return null;
  }

  @Override
  public String getBiosVersion() {
    return wmi.queryValue("BIOS", "Version");
  }

  @Override
  public String getBiosReleaseDate() {
    return wmi.queryValue("BIOS", "ReleaseDate");
  }

  @Override
  public String getBiosSerial() {
    return wmi.queryValue("BIOS", "SerialNumber");
  }

  @Override
  public String getBiosDescription() {
    return wmi.queryValue("BIOS", "Description");
  }

  @Override
  public String getBiosSmbVersion() {
    return wmi.queryValue("BIOS", "SMBIOSBIOSVersion");
  }

  @Override
  public String getDiskSerial() {
    return wmi.queryValue("LogicalDisk C:", "VolumeSerialNumber");
  }

  @Override
  public String getDiskControllerId() {
    return wmi.queryValue("IDEController", "DeviceID");
  }

  @Override
  public String getModel() {
    return wmi.queryValue("ComputerSystem", "Model");
  }

  @Override
  public String getManufacturer() {
    return wmi.queryValue("ComputerSystem", "Manufacturer");
  }

  @Override
  public void close() throws Exception {
    wmi.terminate();
  }
}

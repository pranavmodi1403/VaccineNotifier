package com.modip.vaccinenotifier;

public class VaccineDataModel {
    int centerId;
    String address;
    String name;
    int availableCapacity;
    int dose1Capacity;
    int dose2Capacity;
    int ageLimit;
    String vaccineName;
    int pincode;
    String date;

    public VaccineDataModel(int centerId, String address, String name, int availableCapacity, int dose1Capacity, int dose2Capacity, int ageLimit, String vaccineName, int pincode, String date) {
        this.centerId = centerId;
        this.address = address;
        this.name = name;
        this.availableCapacity = availableCapacity;
        this.dose1Capacity = dose1Capacity;
        this.dose2Capacity = dose2Capacity;
        this.ageLimit = ageLimit;
        this.vaccineName = vaccineName;
        this.pincode = pincode;
        this.date = date;
    }

    public int getCenterId() {
        return centerId;
    }

    public void setCenterId(int centerId) {
        this.centerId = centerId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDose1AvailableCount() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public int getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        this.ageLimit = ageLimit;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public int getDose1Capacity() {
        return dose1Capacity;
    }

    public void setDose1Capacity(int dose1Capacity) {
        this.dose1Capacity = dose1Capacity;
    }

    public int getDose2Capacity() {
        return dose2Capacity;
    }

    public void setDose2Capacity(int dose2Capacity) {
        this.dose2Capacity = dose2Capacity;
    }

    public int getPincode() {
        return pincode;
    }

    public void setPincode(int pincode) {
        this.pincode = pincode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

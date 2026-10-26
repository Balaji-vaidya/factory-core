package com.factory.product.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

public class ProductSpecDto implements Serializable {

    @NotBlank(message = "Processing capacity is required")
    private String capacity;

    @NotBlank(message = "Motor power is required")
    private String motorPower;

    private String motorRating;

    @NotBlank(message = "Machine dimensions are required")
    private String dimensions;

    @NotBlank(message = "Net machine weight is required")
    private String weight;

    private String airVolume;
    private String screenArea;
    private List<String> seedCompatibility;

    public ProductSpecDto() {}

    public ProductSpecDto(String capacity, String motorPower, String motorRating, String dimensions,
                          String weight, String airVolume, String screenArea, List<String> seedCompatibility) {
        this.capacity = capacity;
        this.motorPower = motorPower;
        this.motorRating = motorRating;
        this.dimensions = dimensions;
        this.weight = weight;
        this.airVolume = airVolume;
        this.screenArea = screenArea;
        this.seedCompatibility = seedCompatibility;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getMotorPower() {
        return motorPower;
    }

    public void setMotorPower(String motorPower) {
        this.motorPower = motorPower;
    }

    public String getMotorRating() {
        return motorRating;
    }

    public void setMotorRating(String motorRating) {
        this.motorRating = motorRating;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getAirVolume() {
        return airVolume;
    }

    public void setAirVolume(String airVolume) {
        this.airVolume = airVolume;
    }

    public String getScreenArea() {
        return screenArea;
    }

    public void setScreenArea(String screenArea) {
        this.screenArea = screenArea;
    }

    public List<String> getSeedCompatibility() {
        return seedCompatibility;
    }

    public void setSeedCompatibility(List<String> seedCompatibility) {
        this.seedCompatibility = seedCompatibility;
    }
}

package com.factory.product.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_specs")
public class ProductSpec {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false, length = 100)
    private String capacity;

    @Column(name = "motor_power", nullable = false, length = 100)
    private String motorPower;

    @Column(name = "motor_rating", length = 100)
    private String motorRating;

    @Column(nullable = false, length = 100)
    private String dimensions;

    @Column(nullable = false, length = 50)
    private String weight;

    @Column(name = "air_volume", length = 100)
    private String airVolume;

    @Column(name = "screen_area", length = 100)
    private String screenArea;

    @Column(name = "seed_compatibility", columnDefinition = "TEXT")
    private String seedCompatibility;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ProductSpec() {
        this.id = UUID.randomUUID().toString();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public String getSeedCompatibility() {
        return seedCompatibility;
    }

    public void setSeedCompatibility(String seedCompatibility) {
        this.seedCompatibility = seedCompatibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

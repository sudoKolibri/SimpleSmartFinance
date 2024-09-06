package myProject.model;

import java.util.UUID;

public class Category {
    private String id;
    private String name;
    private String color;
    private boolean isStandard;
    private boolean isCustom;
    private double budget;

    // Constructor for custom categories
    public Category(String id, String name, String color, boolean isStandard, boolean isCustom, double budget) {
        this.id = (id == null) ? UUID.randomUUID().toString() : id;  // Assign a UUID if id is null
        this.name = name;
        this.color = color;
        this.isStandard = isStandard;
        this.isCustom = isCustom;
        this.budget = budget;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isStandard() { return isStandard; }
    public void setStandard(boolean isStandard) { this.isStandard = isStandard; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean isCustom) { this.isCustom = isCustom; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
}

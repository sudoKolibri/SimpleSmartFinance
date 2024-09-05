package myProject.model;

public class Category {
    private String id;
    private String name;
    private String color;  // Color for display
    private boolean isStandard;  // Default category
    private boolean isCustom;  // Custom category
    private double budget;  // Budget for this category

    // Constructor for custom categories
    public Category(String id, String name, String color, boolean isStandard, boolean isCustom, double budget) {
        this.id = id;
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
    public void setStandard(boolean standard) { isStandard = standard; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
}

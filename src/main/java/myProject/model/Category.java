package myProject.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class Category {
    private StringProperty id;
    private StringProperty name;
    private StringProperty color;
    private boolean isStandard;
    private boolean isCustom;
    private DoubleProperty budget;

    // Constructor for custom categories
    public Category(String id, String name, String color, boolean isStandard, boolean isCustom, double budget) {
        this.id = new SimpleStringProperty((id == null) ? UUID.randomUUID().toString() : id);
        this.name = new SimpleStringProperty(name);
        this.color = new SimpleStringProperty(color);
        this.isStandard = isStandard;
        this.isCustom = isCustom;
        this.budget = new SimpleDoubleProperty(budget);
    }

    // Constructor for loading category by ID only
    public Category(String id) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty();
        this.color = new SimpleStringProperty();
        this.isStandard = false;
        this.isCustom = false;
        this.budget = new SimpleDoubleProperty(0.0);
    }

    // Getters and setters
    public StringProperty idProperty() { return id; }
    public String getId() { return id.get(); }
    public void setId(String id) { this.id.set(id); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public StringProperty colorProperty() { return color; }
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }

    public boolean isStandard() { return isStandard; }
    public void setStandard(boolean isStandard) { this.isStandard = isStandard; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean isCustom) { this.isCustom = isCustom; }

    public DoubleProperty budgetProperty() { return budget; }
    public double getBudget() { return budget.get(); }
    public void setBudget(double budget) { this.budget.set(budget); }
}

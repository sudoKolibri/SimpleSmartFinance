package myProject.model;

import javafx.beans.property.*;

import java.util.Objects;
import java.util.UUID;

public class Category {
    private final StringProperty id;
    private final StringProperty name;
    private boolean isStandard;
    private boolean isCustom;
    private ObjectProperty<Double> budget;

    // Constructor for custom categories
    public Category(String id, String name, boolean isStandard, boolean isCustom, Double budget) {
        this.id = new SimpleStringProperty((id == null) ? UUID.randomUUID().toString() : id);
        this.name = new SimpleStringProperty(name);
        this.isStandard = isStandard;
        this.isCustom = isCustom;
        this.budget = new SimpleObjectProperty<>(budget);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }


    public boolean isStandard() {
        return isStandard;
    }

    public void setStandard(boolean standard) {
        isStandard = standard;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public Double getBudget() {
        return budget.get();
    }

    public ObjectProperty<Double> budgetProperty() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget.set(budget);
    }

    public boolean hasBudget() {
        return budget.get() != null && budget.get() > 0;
    }


    // equals() and hashCode() based on the 'id' property

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id.get(), category.id.get());  // Compare based on 'id'
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());  // Hash based on 'id'
    }
}

package myProject.model;

import javafx.beans.property.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Die Category-Klasse repräsentiert eine Kategorie, die entweder als Standard oder benutzerdefiniert
 * existieren kann. Jede Kategorie hat eine eindeutige ID, einen Namen und optional ein Budget.
 */
public class Category {

    private final StringProperty id;  // Eindeutige ID der Kategorie
    private final StringProperty name;  // Name der Kategorie
    private boolean isStandard;  // Gibt an, ob es sich um eine Standardkategorie handelt
    private boolean isCustom;  // Gibt an, ob es sich um eine benutzerdefinierte Kategorie handelt
    private ObjectProperty<Double> budget;  // Optionales Budget der Kategorie

    /**
     * Konstruktor zur Erstellung einer benutzerdefinierten Kategorie.
     *
     * @param id Eindeutige ID der Kategorie. Wenn null, wird eine UUID generiert.
     * @param name Name der Kategorie.
     * @param isStandard Gibt an, ob die Kategorie eine Standardkategorie ist.
     * @param isCustom Gibt an, ob die Kategorie eine benutzerdefinierte Kategorie ist.
     * @param budget Optionales Budget der Kategorie. Kann null sein.
     */
    public Category(String id, String name, boolean isStandard, boolean isCustom, Double budget) {
        this.id = new SimpleStringProperty((id == null) ? UUID.randomUUID().toString() : id);
        this.name = new SimpleStringProperty(name);
        this.isStandard = isStandard;
        this.isCustom = isCustom;
        this.budget = new SimpleObjectProperty<>(budget);
    }

    // Getter und Setter für die Eigenschaften
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

    /**
     * Überprüft, ob die Kategorie ein gültiges Budget hat.
     *
     * @return true, wenn die Kategorie ein Budget größer als 0 hat, sonst false.
     */
    public boolean hasBudget() {
        return budget.get() != null && budget.get() > 0;
    }

    // equals() und hashCode() basierend auf der 'id'-Eigenschaft
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id.get(), category.id.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }
}

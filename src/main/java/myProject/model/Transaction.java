package myProject.model;

import javafx.beans.property.*;

import java.util.Date;
import java.util.UUID;

public class Transaction {

    private final StringProperty id;
    private final StringProperty description; // Visible to the user, acts as the title of the transaction
    private final ObjectProperty<Date> date;
    private final DoubleProperty amount;
    private final StringProperty type; // "income" or "expense"

    // Relationships
    private final ObjectProperty<User> user; // Foreign key linking to the user
    private final ObjectProperty<Account> account; // Foreign key linking to the account
    private final ObjectProperty<Category> category; // Foreign key linking to the category

    // Recurring transactions
    private final BooleanProperty isRecurring;
    private StringProperty recurrenceInterval; // "daily", "weekly", "monthly"
    private ObjectProperty<Date> endDate; // Optional end date for recurring transactions

    private StringProperty currency; // Optional, for multi-currency support
    private StringProperty status; // "pending", "completed", etc.

    // Timestamps
    private final ObjectProperty<Date> createdAt;
    private final ObjectProperty<Date> updatedAt;

    // Constructor
    public Transaction(String description, double amount, String type, User user, Account account, Category category, Date date) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString()); // Generate a UUID, hidden from users
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.type = new SimpleStringProperty(type);
        this.user = new SimpleObjectProperty<>(user);
        this.account = new SimpleObjectProperty<>(account);
        this.category = new SimpleObjectProperty<>(category);
        this.date = new SimpleObjectProperty<>(date);
        this.isRecurring = new SimpleBooleanProperty(false); // Default to non-recurring
        this.status = new SimpleStringProperty("completed"); // Default status
        this.createdAt = new SimpleObjectProperty<>(new Date()); // Current time as createdAt
        this.updatedAt = new SimpleObjectProperty<>(new Date()); // Current time as updatedAt
    }

    // Properties (Getters for JavaFX binding)
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<Date> dateProperty() {
        return date;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public ObjectProperty<Account> accountProperty() {
        return account;
    }

    public ObjectProperty<Category> categoryProperty() {
        return category;
    }

    public BooleanProperty isRecurringProperty() {
        return isRecurring;
    }

    public StringProperty recurrenceIntervalProperty() {
        return recurrenceInterval;
    }

    public ObjectProperty<Date> endDateProperty() {
        return endDate;
    }

    public StringProperty currencyProperty() {
        return currency;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public ObjectProperty<Date> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<Date> updatedAtProperty() {
        return updatedAt;
    }

    // Standard Getters and Setters for regular use
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public Date getDate() {
        return date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public Account getAccount() {
        return account.get();
    }

    public void setAccount(Account account) {
        this.account.set(account);
    }

    public Category getCategory() {
        return category.get();
    }

    public void setCategory(Category category) {
        this.category.set(category);
    }

    public boolean isRecurring() {
        return isRecurring.get();
    }

    public void setRecurring(boolean isRecurring) {
        this.isRecurring.set(isRecurring);
    }

    public String getRecurrenceInterval() {
        return recurrenceInterval.get();
    }

    public void setRecurrenceInterval(String recurrenceInterval) {
        this.recurrenceInterval.set(recurrenceInterval);
    }

    public Date getEndDate() {
        return endDate.get();
    }

    public void setEndDate(Date endDate) {
        this.endDate.set(endDate);
    }

    public String getCurrency() {
        return currency.get();
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public Date getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt.set(createdAt);
    }

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    // Method to handle recurring transactions
    public void markAsRecurring(String interval, Date endDate) {
        this.isRecurring.set(true);
        this.recurrenceInterval.set(interval);
        this.endDate.set(endDate);
    }
}

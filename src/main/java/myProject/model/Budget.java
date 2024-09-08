package myProject.model;

import java.util.Date;
import java.util.List;

public class Budget {
    private String id;
    private String userId;
    private double amount;
    private Date startDate;
    private Date endDate;
    private List<String> categoryIds;  // List of category IDs linked to this budget

    // Constructor
    public Budget(String id, String userId, double amount, Date startDate, Date endDate, List<String> categoryIds) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryIds = categoryIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }
}

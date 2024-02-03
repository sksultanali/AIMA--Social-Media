package com.developerali.aima.Models;

public class WithdrawModel {
    String details;
    int amount;

    public WithdrawModel(String details, int amount) {
        this.details = details;
        this.amount = amount;
    }

    public WithdrawModel() {
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

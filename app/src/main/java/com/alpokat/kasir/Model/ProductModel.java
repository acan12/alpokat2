package com.alpokat.kasir.Model;

public class ProductModel {
    private String barcode;
    private String description;
    private String price;

    public ProductModel(String barcode, String description, String price) {
        this.barcode = barcode;
        this.description = description;
        this.price = price;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}

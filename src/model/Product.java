package src.model;

public class Product {
    private String sku;
    private String name;
    private String clothes_name;
    private String color;
    private String size;
    private String decal_name;
    private String file_decal_name;
    private long quantity;

    public Product(String sku, String name, String clothes_name,String color, String size,long quantity) {
        this.sku = sku;
        this.name = name;
        this.clothes_name = clothes_name;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClothes_name() {
        return clothes_name;
    }

    public void setClothes_name(String clothes_name) {
        this.clothes_name = clothes_name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDecal_name() {
        return decal_name;
    }

    public void setDecal_name(String decal_name) {
        this.decal_name = decal_name;
    }

    public String getFile_decal_name() {
        return file_decal_name;
    }

    public void setFile_decal_name(String file_decal_name) {
        this.file_decal_name = file_decal_name;
    }
}

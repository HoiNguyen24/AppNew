package src.model;

public class Product implements Cloneable{
    private String sku;
    private String name;
    private String clothes_name;
    private String color;
    private String size;
    private String decal_name;
    private String file_decal_name;
    private long quantity;
    private String quantity_in;

    public String getQuantity_in() {
        return quantity_in;
    }

    public void setQuantity_in(String quantity_in) {
        this.quantity_in = quantity_in;
    }

    public Product(String sku, String name, String clothes_name, String color, String size, long quantity) {
        this.sku = sku;
        this.name = name;
        this.clothes_name = clothes_name;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        quantity_in = String.valueOf(quantity);
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
        this.quantity_in= String.valueOf(this.quantity);
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

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return clothes_name+","+color+","+size+","+quantity;
    }
}

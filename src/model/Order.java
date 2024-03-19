package src.model;

import java.sql.Array;
import java.util.ArrayList;

public class Order {
    private String sku;
    private ArrayList<Product> products;

    private String date;

    private String products_name;

    public Order(String sku, ArrayList<Product> products, String date) {
        this.sku = sku;
        this.products = products;
        this.date = date;
        this.products_name = getNames();
    }

    public String getProducts_name() {
        return getNames();
    }

    public void setProducts_name(String products_name) {
        this.products_name = products_name;
    }

    public Order(String sku,String date) {
        this.sku = sku;
        this.date = date;
        products = new ArrayList<>();
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public String getNames(){
        StringBuffer stringBuffer = new StringBuffer();
        for (Product product: products){
            stringBuffer.append(product.getName()+","+product.getQuantity()+"\n");
        }
        return stringBuffer.toString();
    }
    public void add(Product product){
        products.add(product);
    }

    @Override
    public String toString() {
        return "Order{" +
                "sku='" + sku + '\'' +
                ", products=" + products +
                ", date='" + date + '\'' +
                ", products_name='" + products_name + '\'' +
                '}';
    }
}

package src.model;

public class FileProductList {
    public static int stt_st = 1;
    private int stt;
    private String sku;

    private String color;

    private String name;

    private String size;

    public FileProductList(String sku,String name, String color, String size) {
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.name = name;
        this.stt = stt_st;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "STT:" + getStt() +" sku:"+ getSku() +" name:"+ getName() +" color:"+ getColor()+" size:"+ getSize();
    }
}

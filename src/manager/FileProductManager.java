package src.manager;

import src.model.FileProductList;

import java.io.File;
import java.util.ArrayList;

public class FileProductManager {
    ArrayList<FileProductList> productLists = new ArrayList<>();

    public ArrayList<FileProductList> getProductLists() {
        return productLists;
    }

    public void setProductLists(ArrayList<FileProductList> productLists) {
        this.productLists = productLists;
    }

    public boolean check(FileProductList fileProductList){
        for (FileProductList productList: productLists){
            if(productList.getName().equals(fileProductList.getName()) && productList.getColor().equals(fileProductList.getColor()) && productList.getSize().equals(fileProductList.getSize())) return false;
        }
        return true;
    }
    public void display(){
        for (FileProductList fileProductList: productLists){
            System.out.println(fileProductList);
        }
    }
    public void add(FileProductList fileProductList){
        if(check(fileProductList)) {
            productLists.add(fileProductList);
            FileProductList.stt_st++;
        }
    }

}

package src.model;

import javax.xml.transform.Result;
import java.sql.*;

public class Decal {
    private int id ;
    private String name;

    public Decal(String name){
        this.name = name;
    }

    public Decal(String name,int id) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

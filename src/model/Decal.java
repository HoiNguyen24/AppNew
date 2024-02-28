package src.model;

import javax.xml.transform.Result;
import java.sql.*;

public class Decal {
    public static long id_st;
    static {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(id) from decals");
            if(rs.next())
            id_st = Long.parseLong(rs.getString(1))+1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private long id ;
    private String name;

    public Decal(String name) {
        this.id = id_st;
        this.name = name;
        id_st++;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

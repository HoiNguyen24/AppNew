package src;

import src.model.Decal;

import java.io.File;
import java.sql.*;


public class Main {
    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            statement = connection.createStatement();
            String a = "một";
            String str = "INSERT INTO product values (?,?,?,?,?)";
            PreparedStatement stmt = connection.prepareStatement(str);
            stmt.setString(1,a);
            stmt.setString(2,a);
            stmt.setString(3,a);
            stmt.setString(4,a);
            stmt.setString(5,a);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package src;

import javafx.scene.Parent;
import src.model.Decal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;


public class Main {
    public static void main(String[] args) throws IOException {
        Connection connection = null;
        Statement statement = null;
        File folder = new File("image/1");
        for (File file: folder.listFiles()){
            System.out.println(file.getPath());
        }
    }
}
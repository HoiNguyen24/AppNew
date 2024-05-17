package src;

import javafx.scene.Parent;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import src.control.Base;
import src.manager.FileProductManager;
import src.model.Decal;
import src.model.FileProductList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Base.main(args);
//        XSSFWorkbook wb = null;
//        try {
//            wb = new XSSFWorkbook(new FileInputStream("Đang giao đơn hàng-2024-03-30-09_18 (1).xlsx"));
//            XSSFSheet sheet = wb.getSheetAt(0);
//            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
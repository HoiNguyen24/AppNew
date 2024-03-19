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
    public static void main(String[] args) throws IOException, SQLException {
        FileProductManager fileProductManager = new FileProductManager();
        String on_file = "tiktok";
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream("323232 (2).xlsx"));
        XSSFSheet sheet = wb.getSheetAt(0);
        for (int  i = 0 ; i < sheet.getLastRowNum();i++){
             for (int j = 0 ; j < sheet.getRow(i).getLastCellNum();j++){
                 System.out.println(j+":"+sheet.getRow(i).getCell(j).toString());
             }
        }
    }
}
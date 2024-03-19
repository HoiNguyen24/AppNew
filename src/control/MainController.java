package src.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import src.func.Resize;
import src.manager.FileProductManager;


import src.manager.OrderManager;
import src.model.Decal;
import src.model.FileProductList;
import src.model.Order;
import src.model.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;

public class MainController extends Application implements Initializable,Cloneable {

    private String on_file = "a";
    private File file;
    FileProductManager fileProductManager = new FileProductManager();
     @FXML
    TableView<Product> tableView = new TableView<>();
     @FXML
    TableColumn<Product,String> sku;
    @FXML TableColumn<Product,String> name;
    @FXML TableColumn<Product,String> color;
    @FXML TableColumn<Product,String> size;

    @FXML TableColumn<Product,Long> quantity;

    @FXML TableColumn<Product,String> quantity_in = new TableColumn<>();
    @FXML ObservableList<Product> observableList;
    public void refresh(ArrayList<Product> products){
        observableList = FXCollections.observableArrayList(products);
        sku.setCellValueFactory(new PropertyValueFactory<Product,String>("sku"));
        name.setCellValueFactory(new PropertyValueFactory<Product,String>("name"));
        color.setCellValueFactory(new PropertyValueFactory<Product,String>("color"));
        size.setCellValueFactory(new PropertyValueFactory<Product,String>("size"));
        quantity.setCellValueFactory(new PropertyValueFactory<Product,Long>("quantity"));
        quantity_in.setCellValueFactory(new PropertyValueFactory<Product,String>("quantity_in"));
        tableView.setEditable(true);
        quantity_in.setCellFactory(TextFieldTableCell.<Product>forTableColumn());
        tableView.setItems(observableList);
    }

    @FXML TableView<FileProductList> tableFile = new TableView<>();
    @FXML TableColumn<FileProductList,String> sku_file = new TableColumn<>();
    @FXML TableColumn<FileProductList,String> name_file = new TableColumn<>();
    @FXML TableColumn<FileProductList,String> color_file = new TableColumn<>();
    @FXML TableColumn<FileProductList,String> size_file = new TableColumn<>();

    @FXML TableColumn<FileProductList,Integer> stt_file = new TableColumn<>();
    ObservableList<FileProductList> fileProductLists;

    public void refresh_input(ArrayList<FileProductList> fileProductList){
            fileProductLists = FXCollections.observableArrayList(fileProductList);
            sku_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("sku"));
            color_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("color"));
            size_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("size"));
            name_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("name"));
            stt_file.setCellValueFactory(new PropertyValueFactory<FileProductList, Integer>("stt"));
            tableFile.setItems(fileProductLists);
    }
    public void dealdata(File file) throws Exception{
        FileInputStream fileInputStream = new FileInputStream(file.getPath());
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fileInputStream);
        XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
        for (int i = 0 ; i < 50;i++) {
            xssfSheet.setColumnWidth(i, 20000);
        }
        Row row = xssfSheet.createRow(xssfSheet.getLastRowNum()+1);
        fileInputStream.close();
        FileOutputStream outputStream = new FileOutputStream("work.xlsx");
        this.file = new File("work.xlsx");
        xssfWorkbook.write(outputStream);
        xssfWorkbook.close();
        outputStream.close();
    }
    public void input_product(File file) throws Exception {
        dealdata(file);
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = wb.getSheetAt(0);
        int rowc = 2;
        if(!on_file.equals("tiktok"))
            rowc = 1;
        for (int  i = rowc ; i < sheet.getLastRowNum();i++){
            if(sheet.getRow(i).getCell(0) != null){
            if(on_file.equals("tiktok")){
                    String[] variant = sheet.getRow(i).getCell(8).toString().split(",");
                    if(variant.length==2)
                        fileProductManager.add(new FileProductList(sheet.getRow(i).getCell(5).toString(),
                                sheet.getRow(i).getCell(7).toString(),
                                variant[0],
                                variant[1]));
                    else
                        fileProductManager.add(new FileProductList(sheet.getRow(i).getCell(5).toString(),
                                sheet.getRow(i).getCell(7).toString(),
                                variant[0],
                                "default"));
                }
            else{
                String[] variant = sheet.getRow(i).getCell(18).toString().split(",");
                if(variant.length==2)
                    fileProductManager.add(new FileProductList(sheet.getRow(i).getCell(13).toString(),
                            sheet.getRow(i).getCell(14).toString(),
                            variant[0],
                            variant[1]));
                else
                    fileProductManager.add(new FileProductList(sheet.getRow(i).getCell(13).toString(),
                            sheet.getRow(i).getCell(14).toString(),
                            variant[0],
                            "default"));
             }
            }
            else
                break;
        }
        fileProductManager.display();
    }
    public void choosing_file() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file đơn hàng: ");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel file","*.xlsx");
        fileChooser.getExtensionFilters().add(filter);
        this.file = fileChooser.showOpenDialog(new Stage());
        System.out.println(file.getAbsolutePath());
        if(file != null){
            input_product(file);
        }
    }
    private ArrayList<FileProductList> check_product(ArrayList<FileProductList> fileProductLists) throws SQLException {
        ArrayList<FileProductList> back = new ArrayList<>();
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        Statement statement = connection.createStatement();
        for (int  i = 0 ; i < fileProductLists.size();i++){
            try{
                ResultSet resultSet = null;
                if(on_file.equals("tiktok"))
                   resultSet = statement.executeQuery("SELECT * FROM product p join products_sku ps on ps.id = p.id where ps.tiktok_sku =" + fileProductLists.get(i).getSku());
                else {
                    resultSet = statement.executeQuery("SELECT * FROM product p join products_sku ps on ps.id = p.id where ps.shoppe_sku =" + fileProductLists.get(i).getSku());
                }
                if(!resultSet.next())
                 back.add(fileProductLists.get(i));
            }catch (Exception e){
            }
        }
        return back;
    }
    @FXML Label on_add = new Label();

    @FXML TextField product_shoppe_sku = new TextField();
    @FXML TextField product_tiktok_sku = new TextField();

    @FXML TextField product_name = new TextField();
    @FXML TextField cloth_name = new TextField();
    @FXML TextField cloth_color = new TextField();
    @FXML TextField cloth_size = new TextField();
    @FXML TextArea decal_str = new TextArea();

    @FXML TextField decal_textfield = new TextField();

    @FXML TextArea img_part = new TextArea();

    @FXML ImageView image1 = new ImageView();
    @FXML ImageView image2 = new ImageView();
    @FXML ImageView image3 = new ImageView();

    @FXML ChoiceBox<String> decals_name = new ChoiceBox<>();
    private static int onCount = 0;
    ArrayList<File> file_list = new ArrayList<>();

    ArrayList<Decal> decal_list = new ArrayList<>();
    public void import_image() throws SQLException, IOException{
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        PreparedStatement ps_img = connection.prepareStatement("SELECT id from decals where name = ?");
        ps_img.setString(1,decal_textfield.getText());
        ResultSet rs = ps_img.executeQuery();
        if (rs.next()){
            File folder = new File("image/" + rs.getString(1));
            for (File file: folder.listFiles()){
                import_img(file);
            }
        }
        else {
            delete_imgs();
        }
    }
    public void choice(ActionEvent event){
        String choice = decals_name.getValue();
        decal_textfield.setText(choice);
        try {
            import_image();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void auto_import() throws SQLException, IOException{
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        ArrayList<String> name = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs1 = statement.executeQuery("SELECT name from decals order by name desc");
        ArrayList<String> name_list = new ArrayList<>();
        while (rs1.next()){
            name_list.add(rs1.getString(1));
        }
        int[] index = new int[name_list.size()];
        String[] now = decal_textfield.getText().toUpperCase().split(" ");
        for (int  i = 0 ; i < index.length;i++){
            for (int j = 0 ; j < now.length;j++){
                if(name_list.get(i).contains(now[j]))
                    index[i]++;
            }
        }
        for (int i = 1; i < index.length; ++i) {
            int key = index[i];
            String temp = name_list.get(i);
            int j = i - 1;
            while (j >= 0 && index[j] < key) {
                index[j + 1] = index[j];
                name_list.set(j+1,name_list.get(j));
                j = j - 1;
            }
            index[j + 1] = key;
            name_list.set(j+1,temp);
        }
        for (int  i = 0 ; i < index.length;i++){
            if (index[i] == 0 ){
                name_list.remove(i);
                i--;
            }
        }
        ObservableList<String> ObserName = FXCollections.observableArrayList(name_list);
        decals_name.setItems(ObserName);
    }
    public void delete_imgs(){
        delete_img1();
        delete_img2();
        delete_img3();
    }
    public void delete_img1(){
        image1.setImage(null);
    }
    public void delete_img2(){
        image2.setImage(null);
    }
    public void delete_img3(){
        image3.setImage(null);
    }
    public void import_img(File file) throws IOException{
        decal_str.setText(decal_str.getText() + "," + file.getName());
        if(image1.getImage() == null){
            Image image = new Image(file.toURL().toString(),130,125,false,false);
            image1.setImage(image);
        }
        else if(image2.getImage() == null){
            Image image = new Image(file.toURL().toString(),130,125,false,false);
            image2.setImage(image);
        }
        else if(image3.getImage() == null){
            Image image = new Image(file.toURL().toString(),130,125,false,false);
            image3.setImage(image);
        }
    }
    public void add_image() throws IOException{
         FileChooser fileChooser = new FileChooser();
         FileChooser.ExtensionFilter filter =  new FileChooser.ExtensionFilter("image","*.png","*.svg","*.jpg");
         fileChooser.getExtensionFilters().add(filter);
         File file = fileChooser.showOpenDialog(new Stage());
         file_list.add(file);
         import_img(file);
    }
    public boolean check_dup_decal(String name) throws SQLException{
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        PreparedStatement ps_img = connection.prepareStatement("SELECT id from decals where name = ?");
        ps_img.setString(1,name);
        ResultSet rs = ps_img.executeQuery();
        if(rs.next())
            return true;
        else
            return false;
    }
    public void add_decal() throws Exception{
        Dialog<Decal> decalDialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Decal.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
        decalDialog.setDialogPane(dialogPane);
        decals_name.setOnAction(this::choice);
        decalDialog.setResultConverter(buttonType -> {
            if(buttonType == deleteButton)
                return new Decal(decal_textfield.getText());
            else {
                file_list.clear();
                return null;
            }
        });
        Optional<Decal> decal = decalDialog.showAndWait();
        decal.ifPresent(decal1 -> {
            try {
                if(!check_dup_decal(decal_textfield.getText())) {
                    Connection connection = null;
                    Statement statement = null;
                    File file = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp", "root", "admin");
                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO decals (name) values (?)");
                        preparedStatement.setString(1, decal.get().getName().toUpperCase());
                        preparedStatement.executeUpdate();
                        statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery("SELECT max(id) from decals ");
                        if(rs.next()){
                            file = new File("image/" + rs.getString(1));
                            decal1.setId(rs.getInt(1));
                        }
                        if (file.mkdirs()) System.out.println("crerate");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    decal_list.add(decal1);
                    for (File file1 : file_list) {
                        try {
                            FileUtils.copyFile(file1.getAbsoluteFile(), new File(file.getAbsolutePath() + "/" + file1.getName()));
                            FileInputStream fileInputStream = new FileInputStream(new File(file.getAbsolutePath() + "/" + file1.getName()));
                            Path path = Paths.get(file.getAbsolutePath() + "/" + file1.getName());
                            Resize.resize(fileInputStream, path, 50, 50);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }else{
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
                    PreparedStatement ps_img = connection.prepareStatement("SELECT * from decals where name = ?");
                    ps_img.setString(1,decal_textfield.getText());
                    ResultSet rs = ps_img.executeQuery();
                    if (rs.next())
                    decal_list.add(new Decal(rs.getString(2),rs.getInt(1)));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        file_list.clear();
    }
    public void add_product() throws Exception{
//        while(onCount < fileProductManager.getProductLists().size()){
            fileProductManager.setProductLists(check_product(fileProductManager.getProductLists()));
            FileProductList product = fileProductManager.getProductLists().get(onCount);
            Dialog<Boolean> dialog = new Dialog<>();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Product.fxml"));
            fxmlLoader.setController(this);
            ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
            DialogPane dialogPane = (DialogPane) fxmlLoader.load();
            dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
            on_add.setText(product.toString());
            if(on_file.equals("tiktok"))
               product_tiktok_sku.setText(product.getSku());
            else
                product_shoppe_sku.setText(product.getSku());
            product_name.setText(product.getName());
            cloth_color.setText(product.getColor());
            cloth_size.setText(product.getSize());
            dialog.setDialogPane(dialogPane);
            dialog.setResultConverter(buttonType -> {
                if(buttonType == deleteButton)
                    return true;
                else{
                    decal_list.clear();
                    return false;
                }
            });
            Optional<Boolean> result = dialog.showAndWait();
            result.ifPresent(bool ->{
                if(bool){
                    Connection connection = null;
                    Statement statement = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
                        PreparedStatement st = connection.prepareStatement("INSERT INTO product(name,clothes_name,size,color) values (?,?,?,?)");
                        st.setString(1,product_name.getText().toUpperCase());
                        st.setString(2,cloth_name.getText().toUpperCase());
                        st.setString(3,cloth_size.getText().toUpperCase());
                        st.setString(4,cloth_color.getText().toUpperCase());
                        st.executeUpdate();
                        Statement statement2 = connection.createStatement();
                        ResultSet rs = statement2.executeQuery("SELECT max(id) from product");rs.next();
                        PreparedStatement st_id = connection.prepareStatement("INSERT INTO products_sku values (?,?,?)");
                        st_id.setString(1,String.valueOf(rs.getInt(1)));
                        st_id.setString(2,product_tiktok_sku.getText());
                        st_id.setString(3,product_shoppe_sku.getText());
                        st_id.executeUpdate();
                        for (Decal decal: decal_list){
                            System.out.println(decal.getId());
                            PreparedStatement statement1 = connection.prepareStatement("INSERT INTO product_detail values (?,?)");
                            statement1.setString(1,rs.getString(1));
                            statement1.setString(2, String.valueOf(decal.getId()));
                            statement1.executeUpdate();
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    onCount++;
                }
            });
//        }
        if(onCount == fileProductManager.getProductLists().size()){
            onCount = 0;
        }
        decal_list.clear();
    }
    private ArrayList<Product> products = new ArrayList<>();
    public void create_listProduct() throws IOException{
        Workbook workbook;
        FileInputStream fins = null;
        try {
            fins = new FileInputStream(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        workbook = WorkbookFactory.create(fins);
        Sheet sheet = workbook.getSheetAt(0);
        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        int uncheck = 0;
        for (Row row: sheet){
            if(uncheck < 2) uncheck++;
            else {
                String order_sku = null;
                String sku = null;
                String quantity = null;
                String date = null;
                int count = 1;
                for (Cell cell : row) {
                    CellValue cellValue = formulaEvaluator.evaluate(cell);
                    String value = cellValue.getStringValue();
                    if(count == 1){
                        order_sku = value;
                    }
                    else if(count == 3) {
                        date = value;
                        date+= ":00";
                    }
                    else if(count == 5 && on_file.equals("tiktok")){
                        sku = value;
                    }
                    else if(count == 8 && on_file.equals("tiktok")){
                        quantity = value;
                    }
                    else if(count == 10 && !on_file.equals("tiktok")){
                        sku = value;
                    }
                    else if(count == 21 && on_file.equals("tiktok")){
                        date = value;
                        String[] date_time = date.split(" ");
                        String[] day = date_time[0].split("/");
                        date = day[2]+"-"+day[1]+"-"+day[0] +" "+date_time[1];
                    }
                    else if(count == 23){
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
                            Statement deleteOrder = connection.createStatement();
                            deleteOrder.executeUpdate("DELETE from orders");
                            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO orders values (?,?,?,"+Long.parseLong(quantity)+")");
                            PreparedStatement statement = connection.prepareStatement("SELECT id from products_sku where tiktok_sku=? OR shoppe_sku =?;");
                            statement.setString(1,sku.toString());
                            statement.setString(2,sku.toString());
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()){
                                preparedStatement.setString(1,order_sku);
                                preparedStatement.setString(2,date);
                                preparedStatement.setString(3,rs.getString(1));
                            }
                            preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    count++;
                }
            }
        }
        try {
            products.clear();
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement = connection.createStatement();
            Statement statement1 = connection.createStatement();
            ResultSet order = statement.executeQuery("SELECT product_sku,SUM(quantity) FROM orders group by product_sku");
            while(order.next()){
                ResultSet rs =null;
                if(on_file.equals("tiktok"))
                    rs = statement1.executeQuery("SELECT ps.tiktok_sku,p.name,p.clothes_name,p.color,p.size FROM product p join products_sku ps on ps.id = p.id where p.id = " + order.getString(1)+";");
                else
                    rs = statement1.executeQuery("SELECT ps.shoppe_sku,p.name,p.clothes_name,p.color,p.size FROM product p join products_sku ps on ps.id = p.id where p.id = " + order.getString(1)+";");
                if(rs.next())
                  products.add(new Product(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),order.getLong(2)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println(products.toString());
        refresh(products);
    }
    public void file_tiktok(ActionEvent event) throws Exception{
        on_file = "tiktok";
        choosing_file();
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/input_Product.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
        dialog.setDialogPane(dialogPane);
        refresh_input(check_product(fileProductManager.getProductLists()));
        dialog.setResultConverter(buttonType -> {
            if(buttonType == deleteButton)
                return true;
            return false;
        });
        Optional<Boolean> result= dialog.showAndWait();
        result.ifPresent(bool ->{
            if(bool){

            }
        });
    }
    public void file_shoppe(ActionEvent event) throws Exception{
        choosing_file();
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/input_Product.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
        dialog.setDialogPane(dialogPane);
        refresh_input(check_product(fileProductManager.getProductLists()));
        dialog.setResultConverter(buttonType -> {
            if(buttonType == deleteButton)
                return true;
            return false;
        });
        Optional<Boolean> result= dialog.showAndWait();
        result.ifPresent(bool ->{
            if(bool){

            }
        });
    }
    public void out_cloth() throws IOException {
           products.clear();
           create_listProduct();
           try {
               Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
               Statement statement = connection.createStatement();
               statement.executeUpdate("DELETE from orders");
           }catch (Exception e){

           }
           String filePath = "cloth"+file.getName();
           File file = new File(filePath);
           if(file.exists())
               System.out.println("create");
           Workbook workbook = new XSSFWorkbook();
           Sheet sheet = workbook.createSheet("Danh sách áo trơn");
           Row row = null;
           Cell cell = null;
        for (int i = 0; i < products.size(); i++) {
            row = sheet.createRow(i);
            String[] arr = products.get(i).toString().split(",");
            for (int  j = 0 ; j < arr.length;j++){
                cell = row.createCell(j);
                cell.setCellValue(arr[j]);
            }
        }
        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        fout.close();
    }
    public void outDecal() throws IOException{
        products.clear();
        create_listProduct();
        String filePath = "decal"+ file.getName();
        File file = new File(filePath);
        if(file.exists())
            System.out.println("create");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Danh sách hình in");
        Row row = null;
        Cell cell = null;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select d.id,d.name,sum(o.quantity) as quantity from product_detail join (SELECT product_sku,SUM(orders.quantity) as quantity from orders group by product_sku) o on o.product_sku = product_detail.product_sku join decals d on d.id = product_detail.decals_id group by d.id;");
            int count = 0;
            Statement statement1 = connection.createStatement();
            ResultSet rs1 = statement1.executeQuery("SELECT * FROM orders");
            while (rs.next()){
                System.out.println(rs.getString(1) + rs.getString(2));
                row = sheet.createRow(count);
                short height = 760;
                row.setHeight(height);
                cell = row.createCell(0);
                cell.setCellValue(rs.getString(2));
                cell = row.createCell(1);
                cell.setCellValue(rs.getString(3));
                String file_path ="image/"+rs.getString(1);
                File image = new File(file_path);
                for (int i = 0; i < image.listFiles().length ; i++) {
                    System.out.println("ok");
                    String image_path = ((image.listFiles())[i]).toString().replace("\\","/");
                    System.out.println(image_path);
                    FileInputStream fileInputStream = new FileInputStream(image_path);
                    byte[] bytes = IOUtils.toByteArray(fileInputStream);
                    int pictureIdx = workbook.addPicture(bytes,Workbook.PICTURE_TYPE_PNG);
                    fileInputStream.close();
                    XSSFCreationHelper helper = (XSSFCreationHelper) workbook.getCreationHelper();
                    Drawing drawing = sheet.createDrawingPatriarch();
                    XSSFClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(i+2);
                    anchor.setRow1(count);
                    anchor.setCol2(i+3);
                    anchor.setCol2(count+1);
                    Picture pict = drawing.createPicture(anchor,pictureIdx);
                    pict.resize();
                }
                count++;
            }
        }catch (Exception e){
            System.out.println("not ok");
        }
        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        workbook.close();
        fout.close();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement1 = connection.createStatement();
            statement1.executeQuery("DELETE from orders");
        } catch (Exception e){

        }
    }
    OrderManager orderManager = new OrderManager();
    public ArrayList<Product> createClone(){
        ArrayList<Product> pclone = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement = connection.createStatement();
            Statement statement1 = connection.createStatement();
            ResultSet order = statement.executeQuery("SELECT product_sku,SUM(quantity) FROM orders group by product_sku");
            while(order.next()){
                ResultSet rs =null;
                if(on_file.equals("tiktok"))
                    rs = statement1.executeQuery("SELECT ps.tiktok_sku,p.name,p.clothes_name,p.color,p.size FROM product p join products_sku ps on ps.id = p.id where p.id = " + order.getString(1)+";");
                else
                    rs = statement1.executeQuery("SELECT ps.shoppe_sku,p.name,p.clothes_name,p.color,p.size FROM product p join products_sku ps on ps.id = p.id where p.id = " + order.getString(1)+";");
                if(rs.next())
                    pclone.add(new Product(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),0));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pclone;
    }
//    Orders
    @FXML TableView<Order> orderView ;
    @FXML TableColumn<Order,String> order_sku;
    @FXML TableColumn<Order,String> order_product;
    @FXML TableColumn<Order,String> date_order;
    ObservableList<Order> observableOrder ;
    public void printOrders(ArrayList<Product> products) throws IOException, SQLException {
        ArrayList<Order> possible = new ArrayList<>();
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/print_orders.fxml"));
        fxmlLoader.setController(this);
        orderManager.create(on_file);
        System.out.println(orderManager.getOrders().toString());
        observableOrder  = FXCollections.observableArrayList(orderManager.getOrders());
        order_sku.setCellValueFactory(new PropertyValueFactory<Order,String>("sku"));
        order_product.setCellValueFactory(new PropertyValueFactory<Order,String>("products_name"));
        date_order.setCellValueFactory(new PropertyValueFactory<Order,String>("date"));
        orderView.setItems(observableOrder);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        ButtonType deleteButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(deleteButton);
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton){
                return true;
            }
            return false;
        });
        Optional<Boolean> rs = dialog.showAndWait();
    }
    public void create_orders() throws Exception{
        ArrayList<Product> rsarr = new ArrayList<>();
        create_listProduct();
        Dialog<ArrayList<Product>> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/quantity_in.fxml"));
        fxmlLoader.setController(this);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        refresh(createClone());
        ButtonType deleteButton = new ButtonType("Xuất đơn có thể đi", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(deleteButton);
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(buttonType -> {
            if(buttonType == deleteButton){
             for (int i = 0 ; i < tableView.getItems().size();i++){
                 rsarr.add(tableView.getItems().get(i));
             }
             return rsarr;
            }
            return null;
        });
        Optional<ArrayList<Product>> rs = dialog.showAndWait();
        rs.ifPresent(result ->{
            try {
                printOrders(rsarr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        products.clear();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        quantity_in.setOnEditCommit(productStringCellEditEvent -> {
            Product product = productStringCellEditEvent.getTableView().getItems().get(productStringCellEditEvent.getTablePosition().getRow());
            product.setQuantity(Long.parseLong(productStringCellEditEvent.getNewValue()));
            System.out.println(product.toString());
        });
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}

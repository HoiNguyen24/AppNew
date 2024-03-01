package src.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import src.manager.FileProductManager;


import src.model.Decal;
import src.model.FileProductList;
import src.model.Product;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController extends Application implements Initializable {
    private File file;
    FileProductManager fileProductManager = new FileProductManager();
     @FXML
    TableView<Product> tableView ;
     @FXML
    TableColumn<Product,String> sku;
    @FXML TableColumn<Product,String> name;
    @FXML TableColumn<Product,String> color;
    @FXML TableColumn<Product,String> size;

    @FXML TableColumn<Product,Long> quantity;
    @FXML ObservableList<Product> observableList;

    public void refresh(ArrayList<Product> products){
        observableList = FXCollections.observableArrayList(products);
        sku.setCellValueFactory(new PropertyValueFactory<Product,String>("sku"));
        name.setCellValueFactory(new PropertyValueFactory<Product,String>("name"));
        color.setCellValueFactory(new PropertyValueFactory<Product,String>("color"));
        size.setCellValueFactory(new PropertyValueFactory<Product,String>("size"));
        quantity.setCellValueFactory(new PropertyValueFactory<Product,Long>("quantity"));
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
        sku_file.setCellValueFactory(new PropertyValueFactory<FileProductList,String>("sku"));
        color_file.setCellValueFactory(new PropertyValueFactory<FileProductList,String>("color"));
        size_file.setCellValueFactory(new PropertyValueFactory<FileProductList,String>("size"));
        name_file.setCellValueFactory(new PropertyValueFactory<FileProductList,String>("name"));
        stt_file.setCellValueFactory(new PropertyValueFactory<FileProductList,Integer>("stt"));
        tableFile.setItems(fileProductLists);
    }
    public void input_product(File file) throws IOException {
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
                String sku = null;
                String name = null;
                String var = null;
                int count = 1;
                for (Cell cell : row) {
                    CellValue cellValue = formulaEvaluator.evaluate(cell);
                    String value = cellValue.getStringValue();
                    if(count == 5){
                        sku = value;
                    }
                    else if(count == 6){
                        name = value;
                    }
                    else if(count == 7){
                        var = value;
                    }
                    else if(count == 8){
                        String[] arr = var.split(",");
                        System.out.println(sku + name + var);
                        if(arr.length == 2)
                            fileProductManager.add(new FileProductList(sku,name,arr[0],arr[1]));
                        else
                            fileProductManager.add(new FileProductList(sku,name,arr[0],"default"));
                    }
                    count++;
                }
            }
        }
    }
    public void choosing_file() throws IOException{
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
                ResultSet resultSet = statement.executeQuery("SELECT * FROM product where sku =" + fileProductLists.get(i).getSku());
                if(!resultSet.next())
                 back.add(fileProductLists.get(i));
            }catch (Exception e){
            }
        }
        return back;
    }
    @FXML Label on_add = new Label();
    @FXML TextField product_sku = new TextField();
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
    private static int onCount = 0;
    ArrayList<File> file_list = new ArrayList<>();

    ArrayList<Decal> decal_list = new ArrayList<>();
    public void add_image() throws IOException{
         FileChooser fileChooser = new FileChooser();
         FileChooser.ExtensionFilter filter =  new FileChooser.ExtensionFilter("image","*.png","*.svg","*.jpg");
         fileChooser.getExtensionFilters().add(filter);
         File file = fileChooser.showOpenDialog(new Stage());
         file_list.add(file);
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

    public void add_decal() throws Exception{
        Dialog<Decal> decalDialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Decal.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
        decalDialog.setDialogPane(dialogPane);
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
            decal_list.add(decal1);
            File file = new File("image/"+decal.get().getId());
            System.out.println(file.getAbsolutePath());
            System.out.println(file.getPath());
            for (File file1: file_list){
                try {
                    FileUtils.copyFile(file1.getAbsoluteFile(),new File(file.getAbsolutePath()+"/"+ file1.getName()) );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(file.mkdirs()) System.out.println("crerate");
            Connection connection = null;
            Statement statement = null;
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO decals values (?,?)");
                preparedStatement.setString(1,String.valueOf(decal.get().getId()));
                preparedStatement.setString(2,decal.get().getName());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        file_list.clear();
    }
    public void add_product() throws IOException{
//        while(onCount < fileProductManager.getProductLists().size()){
            FileProductList product = fileProductManager.getProductLists().get(onCount);
            Dialog<Boolean> dialog = new Dialog<>();
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Product.fxml"));
            fxmlLoader.setController(this);
            ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
            DialogPane dialogPane = (DialogPane) fxmlLoader.load();
            dialogPane.getButtonTypes().addAll(deleteButton,ButtonType.CANCEL);
            on_add.setText(product.toString());
            product_sku.setText(product.getSku());
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
                        PreparedStatement st = connection.prepareStatement("INSERT INTO product values (?,?,?,?,?)");
                        st.setString(1,product_sku.getText());
                        st.setString(2,product_name.getText());
                        st.setString(3,cloth_name.getText());
                        st.setString(4,cloth_size.getText());
                        st.setString(5,cloth_color.getText());
                        st.executeUpdate();
                        for (Decal decal: decal_list){
                            PreparedStatement statement1 = connection.prepareStatement("INSERT INTO product_detail values (?,?)");
                            statement1.setString(1,product_sku.getText());
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
                String sku = null;
                String quantity = null;
                int count = 1;
                for (Cell cell : row) {
                    CellValue cellValue = formulaEvaluator.evaluate(cell);
                    String value = cellValue.getStringValue();
                    if(count == 5){
                        sku = value;
                    }
                    else if(count == 8){
                        quantity = value;
                        try {
                            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
                            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO orders values (?,"+Long.parseLong(quantity)+")");
                            preparedStatement.setString(1,sku);
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
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT p.*,SUM(o.quantity) FROM orders o join product p on o.product_sku = p.sku group by o.product_sku");
            while(rs.next()){
                products.add(new Product(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getLong(6)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        refresh(products);
    }
    public void file_tiktok(ActionEvent event) throws Exception{
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
           create_listProduct();
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
    public void create_decal(File file) throws Exception{
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT d.id,d.name,p.quantity from product_detail pd join (SELECT p.*,SUM(o.quantity) as quantity FROM orders o join product p on o.product_sku = p.sku group by o.product_sku) p on p.sku = pd.product_sku join decals d on pd.decals_id = d.id;");
        while (rs.next()){
            Path dir = Path.of("/image"+rs.getString(1));
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir,"*.{png,svg,jpg}");
        }
    }
    public void outDecal(){
        String filePath = "decal"+ file.getName();
        File file = new File(filePath);

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}

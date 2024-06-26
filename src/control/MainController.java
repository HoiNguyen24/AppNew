package src.control;

import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.SaveFormat;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.input.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.derby.diag.StatementCache;
import org.apache.derby.iapi.jdbc.BrokeredPreparedStatement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
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

import javax.print.DocFlavor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.scene.control.cell.TextFieldTableCell.forTableColumn;

public class MainController extends Application implements Initializable, Cloneable {

    Connection connection;

    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp", "root", "admin");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String on_file = "a";
    private File file;
    FileProductManager fileProductManager = new FileProductManager();
    @FXML
    TableView<Product> tableView = new TableView<>();
    @FXML
    TableColumn<Product, String> sku;
    @FXML
    TableColumn<Product, String> name;
    @FXML
    TableColumn<Product, String> color;
    @FXML
    TableColumn<Product, String> size;

    @FXML
    TableColumn<Product, Long> quantity;

    @FXML
    TableColumn<Product, String> quantity_in = new TableColumn<>();
    @FXML
    ObservableList<Product> observableList;
    @FXML
    TableColumn<Product, String> color_in;

    public void refresh(ArrayList<Product> products) {
        observableList = FXCollections.observableArrayList(products);
        sku.setCellValueFactory(new PropertyValueFactory<Product, String>("sku"));
        name.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
        color.setCellValueFactory(new PropertyValueFactory<Product, String>("color"));
        size.setCellValueFactory(new PropertyValueFactory<Product, String>("size"));
        quantity.setCellValueFactory(new PropertyValueFactory<Product, Long>("quantity"));
        quantity_in.setCellValueFactory(new PropertyValueFactory<Product, String>("quantity_in"));
        tableView.setEditable(true);
        quantity_in.setCellFactory(TextFieldTableCell.<Product>forTableColumn());
        tableView.setItems(observableList);
    }

    @FXML
    TableView<FileProductList> tableFile = new TableView<>();
    @FXML
    TableColumn<FileProductList, String> name_file = new TableColumn<>();
    @FXML
    TableColumn<FileProductList, String> color_file = new TableColumn<>();
    @FXML
    TableColumn<FileProductList, String> size_file = new TableColumn<>();

    @FXML
    TableColumn<FileProductList, Integer> stt_file = new TableColumn<>();
    ObservableList<FileProductList> fileProductLists;

    public void refresh_input(ArrayList<FileProductList> fileProductList) {
        fileProductLists = FXCollections.observableArrayList(fileProductList);
        color_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("color"));
        size_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("size"));
        name_file.setCellValueFactory(new PropertyValueFactory<FileProductList, String>("name"));
        stt_file.setCellValueFactory(new PropertyValueFactory<FileProductList, Integer>("stt"));
        tableFile.setItems(fileProductLists);
    }
    public boolean useRegex(final String input) {
        if (input.trim().toUpperCase().equals("L") || input.trim().toUpperCase().equals("M") || input.trim().toUpperCase().equals("SIZE L") || input.trim().toUpperCase().equals("SIZE M") || input.trim().toUpperCase().equals("SIZE 1") || input.trim().toUpperCase().equals("SIZE 2") || input.trim().toUpperCase().equals("1") || input.trim().toUpperCase().equals("2") || input.trim().toUpperCase().equals("SZ 1") || input.trim().toUpperCase().equals("SZ 2"))
            return true;
        else
            return false;
    }

    public void setOnfile(File file) {

    }

    public void  readFile(File file) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = wb.getSheetAt(0);
        try {
            int nameC = 0 ,variantC = 0;
            Row r = sheet.getRow(0);
            for (int i = 0 ; i < r.getLastCellNum();i++){
                String n = r.getCell(i).toString().toUpperCase().replaceAll("\\s\\s+", " ").trim();
                System.out.println(n);
                if(n.equals("PRODUCT NAME") || n.equals("TÊN SẢN PHẨM")){
                    nameC = i;
                }
                else if(n.equals("VARIATION") || n.equals("TÊN PHÂN LOẠI HÀNG")){
                    variantC = i;
                }
            }
            PreparedStatement ps = connection.prepareStatement("insert into product (size,color,status,name) values (?,?,false,?)");
            PreparedStatement check = connection.prepareStatement("SELECT * FROM product where color = ? and size = ? and name = ?");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                if (sheet.getRow(i).getCell(0) != null) {
                        String[] variant = sheet.getRow(i).getCell(variantC).toString().split(",");
                        if (variant.length == 2) {
                            if (variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("L") || variant[0].replaceAll("\\s\\s+", " ").trim().equals("M") || variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("SIZE L") || variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("SIZE M")|| variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("SZ M") || variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("SZ L")) {
                                String temp = variant[0];
                                variant[0] = variant[1];
                                variant[1] = temp;
                            }
                            ps.setString(1, variant[1].replaceAll("\\s\\s+", " ").trim());
                            check.setString(2, variant[1].replaceAll("\\s\\s+", " ").trim());
                            ps.setString(2, variant[0].replaceAll("\\s\\s+", " ").trim());
                            check.setString(1, variant[0].replaceAll("\\s\\s+", " ").trim());
                        } else if (variant.length == 1) {
                            if (useRegex(variant[0])) {
                                ps.setString(1, variant[0].replaceAll("\\s\\s+", " ").trim());
                                ps.setString(2, "default");
                                check.setString(1, "default");
                                check.setString(2, variant[0].replaceAll("\\s\\s+", " ").trim());
                            } else if(variant[0].replaceAll("\\s\\s+", " ").trim().toUpperCase().equals("DEFAULT")){
                                ps.setString(1, "default");
                                check.setString(2, "default");
                                ps.setString(2, "default");
                                check.setString(1, "default");
                            } else if (!useRegex(variant[0])) {
                                ps.setString(1, "default");
                                ps.setString(2, variant[0].replaceAll("\\s\\s+", " ").trim());
                                check.setString(1, variant[0].replaceAll("\\s\\s+", " ").trim());
                                check.setString(2, "default");
                            }
                        } else if (variant.length == 0) {
                            ps.setString(1, "default");
                            check.setString(2, "default");
                            ps.setString(2, "default");
                            check.setString(1, "default");
                        }
                        check.setString(3, sheet.getRow(i).getCell(nameC).toString());
                        ResultSet rs1 = check.executeQuery();
                        if (!rs1.next()) {
                            ps.setString(3,sheet.getRow(i).getCell(nameC).toString());
                            ps.executeUpdate();
                        }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void input_product() throws Exception {
        fileProductManager.getProductLists().clear();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id,name,color,size FROM product where status = false");
        while (rs.next()) {
            fileProductManager.add(new FileProductList(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)));
        }
        refresh_input(fileProductManager.getProductLists());
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/input_Product.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton, ButtonType.CANCEL);
        dialog.setDialogPane(dialogPane);
        refresh_input(check_product(fileProductManager.getProductLists()));
        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton)
                return true;
            return false;
        });
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(bool -> {
            if (bool) {

            }
        });
        fileProductManager.display();
    }

    public void choosing_file() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file đơn hàng: ");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Excel file", "*.xlsx", "*.csv");
        fileChooser.getExtensionFilters().add(filter);
        this.file = fileChooser.showOpenDialog(new Stage());
        if (file.getPath().contains(".csv")) {
            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(file.getPath(), loadOptions);
            String path = file.getPath().replace(".csv", ".xlsx");
            workbook.save(file.getPath().replace(".csv", ".xlsx"), SaveFormat.XLSX);
            file = new File(path);
        }
        if (!file.equals(null)) {
            readFile(file);
            read_order(file);
        }
    }

    private ArrayList<FileProductList> check_product(ArrayList<FileProductList> fileProductLists) throws SQLException {
        List<FileProductList> back = new ArrayList<>();
        Statement statement = connection.createStatement();
        Collections.sort(fileProductLists, new Comparator<FileProductList>() {
            @Override
            public int compare(FileProductList o1, FileProductList o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i = 0; i < fileProductLists.size(); i++) {
            fileProductLists.get(i).setStt(i + 1);
        }
        return (ArrayList<FileProductList>) fileProductLists;
    }

    @FXML
    Label on_add = new Label();
    @FXML
    TextField linked_name = new TextField();
    @FXML
    TextField product_name = new TextField();
    @FXML
    TextField cloth_name = new TextField();
    @FXML
    TextField cloth_color = new TextField();
    @FXML
    TextField cloth_size = new TextField();
    @FXML
    TextArea decal_str = new TextArea();

    @FXML
    TextField decal_textfield = new TextField();
    @FXML
    Label warning_text = new Label();

    @FXML
    ChoiceBox<String> linked_choice = new ChoiceBox<>();
    @FXML
    TextArea img_part = new TextArea();
    @FXML
    ImageView image1 = new ImageView();
    @FXML
    ImageView image2 = new ImageView();
    @FXML
    ImageView image3 = new ImageView();
    @FXML
    ChoiceBox<String> decals_name = new ChoiceBox<>();
    private static int onCount = 0;
    ArrayList<File> file_list = new ArrayList<>();

    ArrayList<Decal> decal_list = new ArrayList<>();

    public void import_image() throws SQLException, IOException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp", "root", "admin");
        PreparedStatement ps_img = connection.prepareStatement("SELECT id from decals where name = ?");
        ps_img.setString(1, decal_textfield.getText());
        ResultSet rs = ps_img.executeQuery();
        if (rs.next()) {
            File folder = new File("image/" + rs.getString(1));
            for (File file : folder.listFiles()) {
                import_img(file);
            }
        } else {
            delete_imgs();
        }
    }

    public void choice(ActionEvent event) {
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

    public void auto_import() throws SQLException, IOException {
        ArrayList<String> name = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs1 = statement.executeQuery("SELECT name from decals order by name desc");
        ArrayList<String> name_list = new ArrayList<>();
        name_list.clear();
        while (rs1.next()) {
            name_list.add(rs1.getString(1));
        }
        int[] index = new int[name_list.size()];
        String[] now = decal_textfield.getText().toUpperCase().split(" ");
        for (int i = 0; i < index.length; i++) {
            for (int j = 0; j < now.length; j++) {
                if (name_list.get(i).contains(now[j]))
                    index[i]++;
            }
        }
        for (int i = 1; i < index.length; ++i) {
            int key = index[i];
            String temp = name_list.get(i);
            int j = i - 1;
            while (j >= 0 && index[j] < key) {
                index[j + 1] = index[j];
                name_list.set(j + 1, name_list.get(j));
                j = j - 1;
            }
            index[j + 1] = key;
            name_list.set(j + 1, temp);
        }
        int count = 0;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 0) {
                name_list.remove(count);
            } else {
                count++;
            }
        }
        ObservableList<String> ObserName = FXCollections.observableArrayList(name_list);
        decals_name.setItems(ObserName);
    }

    public int index_img(Image image, ArrayList<File> files) throws MalformedURLException {
        for (int i = 0; i < files.size(); i++) {
            System.out.println(files.get(i).toURL());
            System.out.println(image.getUrl());
            if ((files.get(i).toURL()).toString().equals(image.getUrl().toString()))
                return i;
        }
        return -1;
    }

    public void delete_imgs() {
        delete_img1();
        delete_img2();
        delete_img3();
    }

    public void delete_img1() {
        System.out.println(image1.getImage().getUrl());
        try {
            file_list.remove(index_img(image1.getImage(), file_list));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        image1.setImage(null);
    }

    public void delete_img2() {
        try {
            file_list.remove(index_img(image1.getImage(), file_list));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        image2.setImage(null);
    }

    public void delete_img3() {
        try {
            file_list.remove(index_img(image1.getImage(), file_list));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        image3.setImage(null);
    }

    public void import_img(File file) throws IOException {
        if (image1.getImage() == null) {
            Image image = new Image(file.toURL().toString(), 130, 125, false, false);
            image1.setImage(image);
        } else if (image2.getImage() == null) {
            Image image = new Image(file.toURL().toString(), 130, 125, false, false);
            image2.setImage(image);
        } else if (image3.getImage() == null) {
            Image image = new Image(file.toURL().toString(), 130, 125, false, false);
            image3.setImage(image);
        }
    }

    public void add_image() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("image", "*.png", "*.svg", "*.jpg");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(new Stage());
        file_list.add(file);
        import_img(file);
    }

    public boolean check_dup_decal(String name) throws SQLException {
        PreparedStatement ps_img = connection.prepareStatement("SELECT id from decals where name = ?");
        ps_img.setString(1, name);
        ResultSet rs = ps_img.executeQuery();
        if (rs.next())
            return true;
        else
            return false;
    }

    public void add_decal() throws Exception {
        Dialog<Decal> decalDialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Decal.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton, ButtonType.CANCEL);
        decalDialog.setDialogPane(dialogPane);
        decals_name.setOnAction(this::choice);
        decalDialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton)
                return new Decal(decal_textfield.getText());
            else {
                file_list.clear();
                return null;
            }
        });
        Optional<Decal> decal = decalDialog.showAndWait();
        decal.ifPresent(decal1 -> {
            try {
                if (!check_dup_decal(decal_textfield.getText())) {
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
                        if (rs.next()) {
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
                } else {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp", "root", "admin");
                    PreparedStatement ps_img = connection.prepareStatement("SELECT * from decals where name = ?");
                    ps_img.setString(1, decal_textfield.getText());
                    ResultSet rs = ps_img.executeQuery();
                    if (rs.next())
                        decal_list.add(new Decal(rs.getString(2), rs.getInt(1)));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        for (int i = 0; i < decal_list.size(); i++) {
            decal_str.setText(decal_list.get(i).getName() + "\n");
        }
        file_list.clear();
    }

    public void choice_linked(ActionEvent event) throws SQLException {
        String choice = linked_choice.getValue();
        linked_name.setText(choice);
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,clothes_name FROM product where color = ? and name = ?");
        preparedStatement.setString(1, cloth_color.getText());
        preparedStatement.setString(2, choice);
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            warning_text.setStyle("-fx-text-fill: green;");
            warning_text.setText("Màu sắc và kích thước giống ");
            cloth_name.setText(rs.getString(2));
        } else {
            warning_text.setStyle("-fx-text-fill: red;");
            warning_text.setText("Không cùng kích thước và màu sắc");
        }
    }
    public void choice_color(ActionEvent event) throws SQLException {
        String name = product_name.getText();
        String color_af = cloth_color.getText();
        String choice = color_link.getValue();
        for(int i = 0 ; i < fileProductManager.getProductLists().size();i++){
            if(fileProductManager.getProductLists().get(i).getName().equals(name) && fileProductManager.getProductLists().get(i).getColor().equals(choice)){
                System.out.println("chuyen o day");
                FileProductList temp = fileProductManager.getProductLists().get(i);
                fileProductManager.getProductLists().set(i,fileProductManager.getProductLists().get(onCount));
                fileProductManager.getProductLists().set(onCount,temp);
                return;
            }
        }
        cloth_color.setText(choice);
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,clothes_name FROM product where color = ? and name = ?");
        preparedStatement.setString(1, cloth_color.getText());
        preparedStatement.setString(2, linked_name.getText());
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) {
            warning_text.setStyle("-fx-text-fill: green;");
            warning_text.setText("Màu sắc và kích thước giống ");
            cloth_name.setText(rs.getString(2));
        } else {
            warning_text.setStyle("-fx-text-fill: red;");
            warning_text.setText("Không cùng kích thước và màu sắc");
        }
        for(int i = 0 ; i < fileProductManager.getProductLists().size();i++){
            if(fileProductManager.getProductLists().get(i).getColor().equals(color_af))
                fileProductManager.getProductLists().get(i).setColor(choice);
        }
    }

    public void import_linked() throws SQLException {
        ArrayList<String> name = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs1 = statement.executeQuery("SELECT name from product");
        ArrayList<String> name_list = new ArrayList<>();
        name_list.clear();
        while (rs1.next()) {
            name_list.add(rs1.getString(1));
        }
        name_list = new ArrayList<>(new HashSet<>(name_list));
        int[] index = new int[name_list.size()];
        String[] now = product_name.getText().toUpperCase().split(" ");
        for (int i = 0; i < index.length; i++) {
            for (int j = 0; j < now.length; j++) {
                if (name_list.get(i).contains(now[j]))
                    index[i]++;
            }
        }
        for (int i = 1; i < index.length; ++i) {
            int key = index[i];
            String temp = name_list.get(i);
            int j = i - 1;
            while (j >= 0 && index[j] < key) {
                index[j + 1] = index[j];
                name_list.set(j + 1, name_list.get(j));
                j = j - 1;
            }
            index[j + 1] = key;
            name_list.set(j + 1, temp);
        }
        int count = 0;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 0 || index[i] < now.length * 0.3) {
                name_list.remove(count);
            } else {
                count++;
            }
        }
        ObservableList<String> ObserName = FXCollections.observableArrayList(name_list);
        linked_choice.setItems(ObserName);
    }
    @FXML ChoiceBox<String> color_link = new ChoiceBox<>();
    public void import_color() throws SQLException {
        ArrayList<String> name = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet rs1 = statement.executeQuery("SELECT color from product");
        ArrayList<String> name_list = new ArrayList<>();
        name_list.clear();
        while (rs1.next()) {
            name_list.add(rs1.getString(1));
        }
        name_list = new ArrayList<>(new HashSet<>(name_list));
        int[] index = new int[name_list.size()];
        String[] now = cloth_color.getText().toUpperCase().split(" ");
        for (int i = 0; i < index.length; i++) {
            for (int j = 0; j < now.length; j++) {
                if (name_list.get(i).contains(now[j]))
                    index[i]++;
            }
        }
        for (int i = 1; i < index.length; ++i) {
            int key = index[i];
            String temp = name_list.get(i);
            int j = i - 1;
            while (j >= 0 && index[j] < key) {
                index[j + 1] = index[j];
                name_list.set(j + 1, name_list.get(j));
                j = j - 1;
            }
            index[j + 1] = key;
            name_list.set(j + 1, temp);
        }
        int count = 0;
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 0) {
                name_list.remove(count);
            } else {
                count++;
            }
        }
        name_list.add("DEFAULT");
        ObservableList<String> ObserName = FXCollections.observableArrayList(name_list);
        color_link.setItems(ObserName);
    }
    public String getName(String id){
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT clothes_name from product where id =?");
            ps.setString(1,id);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getString(1);
        }catch (SQLException e){
             e.printStackTrace();
        }
        return null;
    }
    public void linked(){
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * from products_link");
            PreparedStatement update = connection.prepareStatement("UPDATE product set clothes_name = ? where id = ?");
            PreparedStatement link = connection.prepareStatement("UPDATE orders set product_sku = ? where product_sku = ?");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                link.setString(1,rs.getString(1));
                link.setString(2,rs.getString(2));
                link.executeUpdate();
                update.setString(1,rs.getString(1));
                update.setString(2,getName(rs.getString(2)));
                update.executeUpdate();
            }
        }catch (SQLException e){
              e.printStackTrace();
        }
    }
    public void insertLinked(String id1, String id2) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * from products_link where tiktok_id = ? or shoppe_id = ? or tiktok_id = ? or shoppe_id = ?");
            ps.setString(1,id1);
            ps.setString(2,id2);
            ps.setString(3,id2);
            ps.setString(4,id1);
            PreparedStatement insert = connection.prepareStatement("INSERT INTO  products_link values (?,?)");
            PreparedStatement update = connection.prepareStatement("UPDATE products_link set tiktok_id = ?,shoppe_id = ?");
            PreparedStatement status = connection.prepareStatement("UPDATE product set status = 1,clothes_name = ? where id = ?");
            ResultSet rs  = ps.executeQuery();
            if(rs.next()){
                System.out.println(rs.getString(1)+","+rs.getString(2));
                System.out.println("vao day 1");
                System.out.println(id1 +"," + id2);
                if(rs.getString(1).equals(id1)){
                    update.setString(1,id1);
                    update.setString(2,id2);
                    status.setString(1,getName(id2));
                    status.setString(2,id1);
                    status.executeUpdate();
                }else{
                    update.setString(1,id2);
                    update.setString(2,id1);
                    status.setString(1,getName(id1));
                    status.setString(2,id2);
                    status.executeUpdate();
                }
                update.executeUpdate();
            }else{
                System.out.println("vao day 2");
                System.out.println(id1 +"," + id2);
                insert.setString(1,id1);
                insert.setString(2,id2);
                insert.executeUpdate();
                System.out.println("Tên là" + getName(id2));
                status.setString(1,getName(id2));
                status.setString(2,id1);
                status.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void chooseOncount(){
          FileProductList fileProductList = tableFile.getFocusModel().getFocusedItem();
        System.out.println(fileProductList);
          int count = tableFile.getFocusModel().getFocusedIndex();
          for (int i = count ; i > 0;i--){
              fileProductManager.getProductLists().set(i,fileProductManager.getProductLists().get(i-1));
          }
          fileProductManager.getProductLists().set(onCount,fileProductList);
        System.out.println(fileProductManager.getProductLists().get(0));
          refresh_input(fileProductManager.getProductLists());
    }
    public void add_product() throws Exception {
//        while(onCount < fileProductManager.getProductLists().size()){
        fileProductManager.setProductLists(check_product(fileProductManager.getProductLists()));
        FileProductList product = fileProductManager.getProductLists().get(onCount);
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/Add_Product.fxml"));
        fxmlLoader.setController(this);
        ButtonType deleteButton = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        dialogPane.getButtonTypes().addAll(deleteButton, ButtonType.CANCEL);
        System.out.println(product.toString());
        on_add.setText(product.toString());
        on_add.setWrapText(true);
        linked_choice.setOnAction(event -> {
            try {
                choice_linked(event);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        color_link.setOnAction(event -> {
            try {
                choice_color(event);
            }catch (SQLException e){

            }
        });
        product_name.setText(product.getName());
        cloth_color.setText(product.getColor());
        cloth_size.setText(product.getSize());
        import_color();
        import_linked();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id,clothes_name,color from product where name = ?");
            preparedStatement.setString(1, product.getName());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                cloth_name.setText(rs.getString("clothes_name"));
                if (product.getColor().equals(rs.getString("color"))) {
                    PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT pd.decals_id,d.name from product_detail pd join decals d on d.id = pd.decals_id join product p on p.id  = pd.product_sku where p.name = ? and p.color = ?");
                    preparedStatement1.setString(1, product.getName());
                    preparedStatement1.setString(2, rs.getString("color"));
                    ResultSet rsd = preparedStatement1.executeQuery();
                    while (rsd.next()) {
                        System.out.println(rsd.getString(1) + " " + rsd.getString(2));
                        decal_list.add(new Decal(rsd.getString(2), rsd.getInt(1)));
                        decal_str.setText(decal_str.getText() + "\n" + rsd.getString(2));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setDialogPane(dialogPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton)
                return true;
            else {
                decal_list.clear();
                return false;
            }
        });
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(bool -> {
            if (bool) {
                try {
                    System.out.println(fileProductManager.getProductLists().get(onCount));
                    if ( !linked_name.getText().strip().equals("")) {
                        LinkedList<FileProductList> list = new LinkedList<>();
                        for (int i = onCount; i < fileProductManager.getProductLists().size(); i++) {
                            FileProductList productList = fileProductManager.getProductLists().get(i);
                            if (productList.getName().strip().equals(product_name.getText().trim())) {
                                list.add(productList);
                                fileProductManager.getProductLists().remove(productList);
                                i--;
                                System.out.println(i);
                            }
                        }
                        System.out.println(list);
                        PreparedStatement linked = connection.prepareStatement("SELECT id from product where color like ? and  size in (?,?,?,?,?,?) and name = ?");
                        for (int i = 0; i < list.size(); i++) {
                            String[]  arr = list.get(i).getSize().split(" ");
                            linked.setString(1, "%"+list.get(i).getColor()+"%");
                            linked.setString(2, list.get(i).getSize().replace(" ","").trim());
                            linked.setString(3,"SIZE " +list.get(i).getSize().replace(" ","").trim());
                            linked.setString(4,"SZ " + list.get(i).getSize().replace(" ","").trim());
                            if(arr.length  == 2){
                                linked.setString(5,arr[1]);
                                linked.setString(6,"SIZE "+ arr[1]);
                                linked.setString(7,"SZ "+ arr[1]);
                            }
                            else{
                                linked.setString(5,arr[0]);
                                linked.setString(6,arr[0]);
                                linked.setString(7,arr[0]);
                            }
                            linked.setString(8, linked_name.getText());
                            ResultSet rs = linked.executeQuery();
                            if (rs.next()) {
                                insertLinked(list.get(i).getSku(),rs.getString(1));
                            }
                        }
                    }
                    if (!decal_str.getText().equals("")) {
                        ArrayList<FileProductList> list = new ArrayList<>();
                        for (int i = 0; i < fileProductManager.getProductLists().size(); i++) {
                            FileProductList productList = fileProductManager.getProductLists().get(i);
                            if (productList.getName().strip().equals(product_name.getText().strip()) && productList.getColor().strip().equals(cloth_color.getText().strip())) {
                                list.add(productList);
                            }
                        }
                        System.out.println(Arrays.toString(list.toArray()));
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE product set clothes_name = ?,status = true where id = ?");
                        for (int i = 0; i < list.size(); i++) {
                            preparedStatement.setString(1, cloth_name.getText());
                            preparedStatement.setString(2, list.get(i).getSku());
                            preparedStatement.executeUpdate();
                            for (Decal decal : decal_list) {
                                System.out.println(decal.getId());
                                PreparedStatement statement1 = connection.prepareStatement("INSERT INTO product_detail values (?,?)");
                                statement1.setString(1, list.get(i).getSku());
                                statement1.setString(2, String.valueOf(decal.getId()));
                                statement1.executeUpdate();
                            }
                            fileProductManager.getProductLists().remove(list.get(i));
                        }
                    } else {
                        ArrayList<FileProductList> list = new ArrayList<>();
                        System.out.println("oncount" + onCount);
                        for (int i = 0; i < fileProductManager.getProductLists().size(); i++) {
                            FileProductList productList = fileProductManager.getProductLists().get(i);
                            if (productList.getName().strip().equals(product_name.getText().strip())) {
                                list.add(productList);
                                fileProductManager.getProductLists().remove(productList);
                                i--;
                                System.out.println(i);
                            }
                        }
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE product set clothes_name = ?,status = true where id = ?");
                        for (int i = 0; i < list.size(); i++) {
                            System.out.println(list.get(i).getSku());
                            preparedStatement.setString(1, cloth_name.getText());
                            preparedStatement.setString(2, list.get(i).getSku());
                            preparedStatement.executeUpdate();
                        }
                    }
                    fileProductManager.getProductLists().clear();
                    Statement st = connection.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id,name,color,size FROM product where status = false");
                    while (rs.next()) {
                        fileProductManager.add(new FileProductList(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)));
                    }

                    refresh_input( check_product(fileProductManager.getProductLists()));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        decal_list.clear();
        decal_str.setText("");
    }

    public boolean checkdoneOrder(String order_sku) {
        try {
            PreparedStatement checkorder = connection.prepareStatement("select * from done_order where order_sku = ?");
            checkorder.setString(1, order_sku);
            if (checkorder.executeQuery().next())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ArrayList<Product> products = new ArrayList<>();

    public void read_order(File file) throws Exception {
        Workbook workbook;
        FileInputStream fins = null;
        fins = new FileInputStream(file);
        workbook = WorkbookFactory.create(fins);
        Sheet sheet = workbook.getSheetAt(0);
        int nameC = 0 ,variantC = 0,orderC = 0,quantityC = 0,dateC = 0 ;
        Row r = sheet.getRow(0);
        for (int i = 0 ; i < r.getLastCellNum();i++){
            String n = r.getCell(i).toString().toUpperCase().replaceAll("\\s\\s+", " ").trim();
            System.out.println(n);
            if(n.equals("PRODUCT NAME") || n.equals("TÊN SẢN PHẨM")){
                nameC = i;
            }
            else if(n.equals("VARIATION") || n.equals("TÊN PHÂN LOẠI HÀNG")){
                variantC = i;
            }else if(n.equals("ORDER ID") || n.equals("MÃ ĐƠN HÀNG")){
                orderC = i;
            }else if(n.equals("QUANTITY") || n.equals("SỐ LƯỢNG")){
                quantityC = i;
            }else if(n.equals("CREATED TIME") || n.equals("NGÀY ĐẶT HÀNG")){
                dateC = i;
            }
        }
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO orders values (?,?,?,?)");
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            if (!checkdoneOrder(sheet.getRow(i).getCell(0).toString())) {
                String[] variant = sheet.getRow(i).getCell(variantC).toString().split(",");
                String name = sheet.getRow(i).getCell(nameC).toString();
                System.out.println(name);
                System.out.println(Arrays.toString(variant));
                PreparedStatement ps = connection.prepareStatement("SELECT id from product where name = ? and name = ? and color = ? and size = ?");
                if (variant.length == 2) {
                    if (useRegex(variant[0])) {
                        String temp = variant[0];
                        variant[0] = variant[1];
                        variant[1] = temp;
                    }
                    ps.setString(4,variant[1].replaceAll("\\s\\s+", " ").trim());
                    ps.setString(1,name);
                    ps.setString(2,name);
                    ps.setString(3,variant[0].replaceAll("\\s\\s+", " ").trim());
                } else {
                    System.out.println(useRegex(variant[0]));
                    if (!useRegex(variant[0])) {
                        ps.setString(1, name);
                        ps.setString(2,name);
                        ps.setString(3, variant[0].replaceAll("\\s\\s+", " ").trim());
                        ps.setString(4, "default");
                    } else if (useRegex(variant[0])) {
                        ps.setString(1,name);
                        ps.setString(2,name);
                        ps.setString(4,variant[0].replaceAll("\\s\\s+", " ").trim());
                        ps.setString(3,"default");
                    } else {
                        ps.setString(1,name);
                        ps.setString(2,name);
                        ps.setString(4,"default");
                        ps.setString(3,"default");
                    }
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) preparedStatement.setString(3, rs.getString(1));
                preparedStatement.setString(1, sheet.getRow(i).getCell(orderC).toString());
                String date;
                if(!on_file.equals("tiktok")){
                    date = sheet.getRow(i).getCell(dateC).toString();
                    date += ":00";
                }else{
                    date = sheet.getRow(i).getCell(dateC).toString();
                    String[] date_time = date.split(" ");
                    String[] day = date_time[0].split("/");
                    date = day[2] + "-" + day[1] + "-" + day[0] + " " + date_time[1];
                }

                preparedStatement.setString(2, date);
                preparedStatement.setString(4, sheet.getRow(i).getCell(quantityC).toString());
                preparedStatement.executeUpdate();
            }
        }
    }

    public void create_listProduct() {
       try {
           products.clear();
           Statement statement = connection.createStatement();
           Statement statement1 = connection.createStatement();
           ResultSet order = statement.executeQuery("SELECT product_sku,SUM(quantity) FROM orders group by product_sku");
           while (order.next()) {
               ResultSet rs = null;
               rs = statement1.executeQuery("SELECT id,name,clothes_name,color,size FROM product where id = " + order.getString(1) + ";");
               if (rs.next()){
                   products.add(new Product(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), order.getLong(2)));
               }
           }
           System.out.println(products.toString());
           refresh(products);
       }catch (SQLException e){

       }
    }

    public void file_tiktok(ActionEvent event) throws Exception {
        on_file = "tiktok";
        choosing_file();
    }

    public void file_shoppe(ActionEvent event) throws Exception {
        on_file = "cac";
        choosing_file();
    }
    public void out() throws IOException,SQLException{
        linked();
        create_listProduct();
        out_cloth();
        outDecal();
        PreparedStatement ps = connection.prepareStatement("INSERT INTO done_order values (?)");
        PreparedStatement ps1 = connection.prepareStatement("DELETE from orders where id = ?");
        PreparedStatement ps2 = connection.prepareStatement("SELECT * from orders");
        ResultSet rs = ps2.executeQuery();
        while (rs.next()){
            ps.setString(1,rs.getString(1));
            ps.executeUpdate();
            ps1.setString(1,rs.getString(1));
            ps1.executeUpdate();
        }
    }
    public void out_cloth() throws IOException, SQLException {
        Date date = new Date(System.currentTimeMillis());
        String[] dates = date.toString().split(" ");
        System.out.println(Arrays.toString(dates));
        String hour = dates[3].replace(":", "-");
        String filePath = "cloth_" + hour + "_d" + dates[2] + "_y" + dates[5] + ".xlsx";
        File file = new File(filePath);
        if (file.exists())
            System.out.println("create");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách áo trơn");
        Row row = null;
        Cell cell = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT p.clothes_name,p.color,p.size,sum(o.quantity) from orders o join product p on p.id = o.product_sku group by p.clothes_name,p.color,p.size order by clothes_name desc;");
            Row frow = sheet.createRow(0);
            Cell cell1 = frow.createCell(0);
            cell1.setCellValue("Tên áo");
            cell1 = frow.createCell(1);
            cell1.setCellValue("Màu");
            cell1 = frow.createCell(2);
            cell1.setCellValue("Size");
            cell1 = frow.createCell(3);
            cell1.setCellValue("Số lượng");
            int count = 1;
            while (rs.next()) {
                row = sheet.createRow(count);
                for (int i = 0; i < 4; i++) {
                    cell = row.createCell(i);
                    cell.setCellValue(rs.getString(i + 1));
                }
                count++;
            }
        } catch (Exception e) {
        }
        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        fout.close();
    }

    public void outDecal() throws IOException, SQLException {
        Date date = new Date(System.currentTimeMillis());
        String[] dates = date.toString().split(" ");
        System.out.println(Arrays.toString(dates));
        String hour = dates[3].replace(":", "-");
        String filePath = "decal_" + hour + "_d" + dates[2] + "_y" + dates[5] + ".xlsx";
        File file = new File(filePath);
        if (file.exists())
            System.out.println("create");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Danh sách hình in");
        Row row = null;
        Cell cell = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select d.id,d.name,sum(o.quantity) as quantity from product_detail join (SELECT product_sku,SUM(orders.quantity) as quantity from orders group by product_sku) o on o.product_sku = product_detail.product_sku join decals d on d.id = product_detail.decals_id group by d.id;");
            int count = 0;
            Statement statement1 = connection.createStatement();
            ResultSet rs1 = statement1.executeQuery("SELECT * FROM orders");
            while (rs.next()) {
                System.out.println(rs.getString(1) + rs.getString(2));
                row = sheet.createRow(count);
                short height = 760;
                row.setHeight(height);
                cell = row.createCell(0);
                cell.setCellValue(rs.getString(2));
                cell = row.createCell(1);
                cell.setCellValue(rs.getString(3));
                String file_path = "image/" + rs.getString(1);
                File image = new File(file_path);
                for (int i = 0; i < image.listFiles().length; i++) {
                    System.out.println("ok");
                    String image_path = ((image.listFiles())[i]).toString().replace("\\", "/");
                    System.out.println(image_path);
                    FileInputStream fileInputStream = new FileInputStream(image_path);
                    byte[] bytes = IOUtils.toByteArray(fileInputStream);
                    int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                    fileInputStream.close();
                    XSSFCreationHelper helper = (XSSFCreationHelper) workbook.getCreationHelper();
                    Drawing drawing = sheet.createDrawingPatriarch();
                    XSSFClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(i + 2);
                    anchor.setRow1(count);
                    anchor.setCol2(i + 3);
                    anchor.setCol2(count + 1);
                    Picture pict = drawing.createPicture(anchor, pictureIdx);
                    pict.resize();
                }
                count++;
            }
        } catch (Exception e) {
            System.out.println("not ok");
        }
        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        workbook.close();
        fout.close();
    }

    OrderManager orderManager = new OrderManager();

    public ArrayList<Product> createClone() {
        ArrayList<Product> pclone = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            Statement statement1 = connection.createStatement();
            ResultSet order = statement.executeQuery("SELECT product_sku,SUM(quantity) FROM orders group by product_sku");
            while (order.next()) {
                ResultSet rs = null;
                    rs = statement1.executeQuery("SELECT id,name,clothes_name,color,size FROM product where id = " + order.getString(1) + ";");
                    if(rs.next())
                    pclone.add(new Product(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), 0));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pclone;
    }

    //    Orders
    @FXML
    TableView<Order> orderView = new TableView<>();
    @FXML
    TableColumn<Order, String> order_sku;
    @FXML
    TableColumn<Order, String> order_product;
    @FXML
    TableColumn<Order, String> date_order;
    ObservableList<Order> observableOrder;

    public void refresh_order(ArrayList<Order> orders) {
        observableOrder = FXCollections.observableArrayList(orders);
        order_sku.setCellValueFactory(new PropertyValueFactory<Order, String>("sku"));
        date_order.setCellValueFactory(new PropertyValueFactory<Order, String>("dates"));
        order_product.setCellValueFactory(new PropertyValueFactory<Order, String>("products_name"));
        order_product.setCellFactory(param -> {
            return new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        Text text = new Text(item);
                        text.setStyle("-fx-text-alignment:justify;");
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                        setGraphic(text);
                    }
                }
            };
        });
        orderView.setItems(observableOrder);
    }

    public boolean checkRemain(ArrayList<Product> products, Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (product.getSku().equals(products.get(i).getSku())) {
//                    System.out.println("tim duoc san pham");
//                    System.out.println(product.getQuantity());
//                    System.out.println(products.get(i).getQuantity());
                if (product.getQuantity() <= products.get(i).getQuantity()) {
//                        System.out.println("tra lai san pham");
                    return true;
                }
            }
        }
        return false;
    }

    public void minusQuantity(ArrayList<Product> products, Order order) {
        for (int i = 0; i < order.getProducts().size(); i++) {
            for (int j = 0; j < products.size(); j++) {
                if (order.getProducts().get(i).getSku().equals(products.get(j).getSku())) {
                    if (order.getProducts().get(i).getQuantity() <= products.get(j).getQuantity()) {
                        products.get(j).setQuantity(products.get(j).getQuantity() - order.getProducts().get(i).getQuantity());
                    }
                }
            }
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE from orders where order_sku = ?");
            preparedStatement.setString(1, order.getSku());
            preparedStatement.executeUpdate();
            PreparedStatement preparedStatement1 = connection.prepareStatement("INSERT INTO done_order values (?)");
            preparedStatement1.setString(1, order.getSku());
            preparedStatement1.executeUpdate();
        } catch (Exception e) {

        }
    }

    public ArrayList<Order> solve(ArrayList<Product> products) {
        System.out.println(products.toString());
        ArrayList<Order> orders = new ArrayList<>();
        for (int i = 0; i < orderManager.getOrders().size(); i++) {
            Order order = orderManager.getOrders().get(i);
            boolean check = true;
            for (int j = 0; j < order.getProducts().size(); j++) {
                System.out.println(checkRemain(products, order.getProducts().get(j)));
                if (!checkRemain(products, order.getProducts().get(j))) {
                    check = false;
                }
            }
            if (check) {
                minusQuantity(products, order);
                orders.add(order);
            }
        }
//        System.out.println("so don duoc chon la" + orders.toString());
        return orders;
    }

    public void out_posOrder() throws IOException {
        Date date = new Date(System.currentTimeMillis());
        String[] dates = date.toString().split(" ");
        System.out.println(Arrays.toString(dates));
        String hour = dates[3].replace(":", "-");
        String filePath = "order_" + hour + "_d" + dates[2] + "_y" + dates[5] + ".xlsx";
        File file = new File(filePath);
        if (file.exists())
            System.out.println("create");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách áo trơn");
        Row row = null;
        Cell cell = null;
        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("Mã đơn hàng");
        cell = row.createCell(1);
        cell.setCellValue("Ngày");
        cell = row.createCell(2);
        cell.setCellValue("Tên sản phẩm");
        cell = row.createCell(3);
        cell.setCellValue("Màu sắc");
        cell = row.createCell(4);
        cell.setCellValue("Size");
        cell = row.createCell(5);
        cell.setCellValue("Số lượng");
        int count = 1;
        for (int i = 0; i < orderView.getItems().size(); i++) {
            Order order = orderView.getItems().get(i);
            row = sheet.createRow(count);
            for (int j = 0; j < 3; j++) {
                cell = row.createCell(j);
                switch (j) {
                    case 0:
                        cell.setCellValue(orderView.getItems().get(i).getSku());
                        break;
                    case 1:
                        cell.setCellValue(orderView.getItems().get(i).getDates());
                        break;
                    case 2:
                        int index = 0;
                        for (int k = count; k < count + order.getProducts().size(); k++) {
                            Product product = order.getProducts().get(index);
                            if (index == 0) {
                                cell = row.createCell(2);
                                cell.setCellValue(product.getName());
                                cell = row.createCell(3);
                                cell.setCellValue(product.getColor());
                                cell = row.createCell(4);
                                cell.setCellValue(product.getSize());
                                cell = row.createCell(5);
                                cell.setCellValue(product.getQuantity());
                            } else {
                                row = sheet.createRow(k);
                                cell = row.createCell(2);
                                cell.setCellValue(product.getName());
                                cell = row.createCell(3);
                                cell.setCellValue(product.getColor());
                                cell = row.createCell(4);
                                cell.setCellValue(product.getSize());
                                cell = row.createCell(5);
                                cell.setCellValue(product.getQuantity());
                            }
                            index++;
                        }
                        break;
                }
            }
            if (order.getProducts().size() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(count, count + order.getProducts().size() - 1, 0, 0));
                sheet.addMergedRegion(new CellRangeAddress(count, count + order.getProducts().size() - 1, 1, 1));
            }
//            System.out.println("count:"+count);
//            System.out.println("order: "+ order.getProducts().size());
//            System.out.println("count+ order:"+(count + order.getProducts().size()));
            count += order.getProducts().size();
//            System.out.println("count sau khi cong" + count);
        }
        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        fout.close();
    }

    public void printOrders(ArrayList<Product> products) throws IOException, SQLException {
        Dialog<Boolean> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/src/screen/print_orders.fxml"));
        fxmlLoader.setController(this);
        orderManager.create(on_file);
        DialogPane dialogPane = (DialogPane) fxmlLoader.load();
        ButtonType deleteButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(deleteButton);
        dialog.setDialogPane(dialogPane);
        refresh_order(solve(products));
        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButton) {
                return true;
            }
            return false;
        });
        Optional<Boolean> rs = dialog.showAndWait();
        rs.ifPresent(result -> {
            rsarr.clear();
            orderManager.getOrders().clear();
        });
    }

    private ArrayList<Product> rsarr = new ArrayList<>();

    public void create_orders() throws Exception {
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
            if (buttonType == deleteButton) {
//                System.out.println("day so: " + tableView.getItems());
                for (int i = 0; i < tableView.getItems().size(); i++) {
                    rsarr.add(tableView.getItems().get(i));
                }
                return rsarr;
            }
            return null;
        });
        Optional<ArrayList<Product>> rs = dialog.showAndWait();
        rs.ifPresent(result -> {
            try {
//                System.out.println("day duoc tao ra " + rsarr + "," + result.toString());
                printOrders(rsarr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        products.clear();
    }

    @FXML
    public void press(KeyEvent event) {
        Product product = tableView.getFocusModel().getFocusedItem();
        switch (event.getCode()) {
            case DIGIT0 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "0"));
            case DIGIT1 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "1"));
            case DIGIT2 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "2"));
            case DIGIT3 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "3"));
            case DIGIT4 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "4"));
            case DIGIT5 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "5"));
            case DIGIT6 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "6"));
            case DIGIT7 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "7"));
            case DIGIT8 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "8"));
            case DIGIT9 -> product.setQuantity(Long.parseLong(String.valueOf(product.getQuantity()) + "9"));
            case BACK_SPACE -> product.setQuantity(product.getQuantity() / 10);
        }
        System.out.println(product.getQuantity());
        ArrayList<Product> products1 = new ArrayList<>();
        for (int i = 0; i < tableView.getItems().size(); i++) {
            products1.add(tableView.getItems().get(i));
        }
        refresh(products1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        quantity_in.setOnEditCommit(productStringCellEditEvent -> {
//            Product product = productStringCellEditEvent.getTableView().getItems().get(productStringCellEditEvent.getTablePosition().getRow());
//            product.setQuantity(Long.parseLong(productStringCellEditEvent.getNewValue()));
//            productStringCellEditEvent.getTableView().requestFocus();
//            productStringCellEditEvent.getTableView().getSelectionModel().select(productStringCellEditEvent.getTablePosition().getRow()+1);
//            System.out.println(productStringCellEditEvent.getTablePosition().getRow());
//            TablePosition<Product,String> pos = new TablePosition<>(tableView,productStringCellEditEvent.getTablePosition().getRow()+1,quantity_in);
//            productStringCellEditEvent.getTableView().getFocusModel().focus(pos);
//        });
    }

    @Override
    public void start(Stage stage) throws Exception {

    }
}

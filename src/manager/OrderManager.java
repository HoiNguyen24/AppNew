package src.manager;

import src.model.Order;
import src.model.Product;

import java.sql.*;
import java.util.ArrayList;

public class OrderManager {
    ArrayList<Order> orders = new ArrayList<>();

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public void create(String onfile) throws SQLException{
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lumiapp","root","admin");
        Statement statement = connection.createStatement();
        ResultSet sku_list = statement.executeQuery("SELECT order_sku,dates from orders group by order_sku");
        while (sku_list.next()){
            Statement target = connection.createStatement();
            ResultSet product = null;
            ArrayList<Product> products = new ArrayList<>();
            if(onfile.equals("tiktok")){
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT ps.tiktok_sku,p.name,p.clothes_name,p.color,p.size,o.quantity  from orders o join product p on p.id = o.product_sku join products_sku ps on ps.id = p.id where o.order_sku = ? ");
                preparedStatement.setString(1,sku_list.getString(1));
                product = preparedStatement.executeQuery();
            }
            else{
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT ps.shoppe_sku,p.name,p.clothes_name,p.color,p.size,o.quantity  from orders o join product p on p.id = o.product_sku join products_sku ps on ps.id = p.id where o.order_sku = ?");
                preparedStatement.setString(1,sku_list.getString(1));
                product = preparedStatement.executeQuery();
            }
            while (product.next()){
                System.out.println("thêm sản phẩm");
                products.add(new Product(product.getString(1),product.getString(2),product.getString(3),product.getString(4),product.getString(5),Long.parseLong(product.getString(6))));
            }
            Order order = new Order(sku_list.getString(1),products,sku_list.getString(2));
            orders.add(order);
        }
    }
}

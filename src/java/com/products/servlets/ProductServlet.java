/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.products.servlets;

import com.products.database.DatabaseConnection;
import java.awt.Event;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author c0644881
 */
@Path("/products")
public class ProductServlet {

    @GET
    @Produces("application/")
    public String doGet() {
        String result = getResults("SELECT * FROM products");
        return result;
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public String doGet(@PathParam("id") String id) {
        String result = getProduct("SELECT * FROM products WHERE product_id = ?", id);
        return result;
    }

    private String getResults(String query) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ ");
        int count = 0;
        //String jsonArray = null;      
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (count > 0) {
                    stringBuilder.append(",\n");
                }
                stringBuilder.append(String.format("{ \"productId\" : %s, \"name\" : \"%s\", \"description\" : \"%s\", \"quantity\" : %s }", rs.getInt("product_id"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
                count = count + 1;
                System.out.println(count);
            }
            stringBuilder.append(" ]");
        } catch (SQLException ex) {
            System.out.println("Exception in getting database connection: " + ex.getMessage());
        }
        return stringBuilder.toString();
    }

    private String getProduct(String query, String... params) {
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        //String jsonArray = null;      
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (count > 0) {
                    stringBuilder.append(",\n");
                }
                stringBuilder.append(String.format("{ \"productId\" : %s, \"name\" : \"%s\", \"description\" : \"%s\", \"quantity\" : %s }", rs.getInt("product_id"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
                count = count + 1;
                System.out.println(count);
            }

        } catch (SQLException ex) {
            System.out.println("Exception in getting database connection: " + ex.getMessage());
        }
        return stringBuilder.toString();
    }

    @POST
    @Consumes("application/json")
    public String doPost(String str) {
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> map = new HashMap<>();
        String name = "", value;
        while (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            switch (evt) {
                case KEY_NAME:
                    name = parser.getString();
                    break;
                case VALUE_STRING:
                    value = parser.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(parser.getInt());
                    map.put(name, value);
                    break;
            }
        }

        int changes = 0;

        String product_name = map.get("name");
        String description = map.get("description");
        String quantity = map.get("quantity");
        changes = doInsert("INSERT INTO products (name, description, quantity) VALUES (?, ?, ?)", product_name, description, quantity);
        if (changes > 0) {
            int id = getId("select max(product_id) from products");
            String res = "http://localhost:8080/Assignment3/products?id=" + id;
            return res;
        } else {
            String res = "Status(500)";
            return res;
        }
    }

    private int doInsert(String query, String name, String description, String quantity) {
        int numChanges = 0;
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, quantity);
            numChanges = pstmt.executeUpdate();
            return numChanges;
        } catch (SQLException ex) {
            System.out.println("Sql Exception: " + ex.getMessage());
            return numChanges;
        }

    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    protected String doPut(@PathParam("id") String id, String str) {
        int changes = 0;
        JsonParser parser = Json.createParser(new StringReader(str));
        Map<String, String> map = new HashMap<>();
        String name = "", value;
        while (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            switch (evt) {
                case KEY_NAME:
                    name = parser.getString();
                    break;
                case VALUE_STRING:
                    value = parser.getString();
                    map.put(name, value);
                    break;
                case VALUE_NUMBER:
                    value = Integer.toString(parser.getInt());
                    map.put(name, value);
                    break;
            }
        }

        String product_name = map.get("name");
        String description = map.get("description");
        String quantity = map.get("quantity");

        changes = doUpdate("update products set product_id = ?, name = ?, description = ?, quantity = ? where product_id = ?", id, product_name, description, quantity, id);
        if (changes > 0) {
            String res = "http://localhost:8080/Assignment3/products?id=" + id;
            return res;
        } else {
            String res = "Status(500)";
            return res;
        }

    }

    private int doUpdate(String query, String id, String name, String description, String quantity, String pid) {
        int numChanges = 0;
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, quantity);
            pstmt.setString(5, pid);
            numChanges = pstmt.executeUpdate();
            return numChanges;
        } catch (SQLException ex) {
            System.out.println("Sql Exception: " + ex.getMessage());
            return numChanges;
        }

    }

    @PUT
    @Path("{id}")
    protected String doDelete(@PathParam("id") String id) {
        int changes = 0;

        changes = doRemove("delete from products where product_id = ?", id);
        if (changes > 0) {
            String res = "Status(200)";
            return res;
        } else {
            String res = "Status(500)";
            return res;
        }

    }

    private int getId(String query) {
        int id = 0;
        //String jsonArray = null;      
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
            }

        } catch (SQLException ex) {
            System.out.println("Exception in getting database connection: " + ex.getMessage());
        }
        return id;
    }

    private int doRemove(String query, String id) {
        int numChanges = 0;
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, id);
            numChanges = pstmt.executeUpdate();
            return numChanges;
        } catch (SQLException ex) {
            System.out.println("Sql Exception: " + ex.getMessage());
            return numChanges;
        }

    }

}

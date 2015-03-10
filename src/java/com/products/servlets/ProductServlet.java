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
    public String doGet(@PathParam("id") String id){
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
                changes = doUpdate("INSERT INTO products (name, description, quantity) VALUES (?, ?, ?)", product_name, description, quantity);
                if (changes > 0) {
                    int id = getId("select max(product_id) from products");
                    String res = "http://localhost:8080/Assignment3/products?id=" + id;
                    return res;
                } else {
                    String res = "Status(500)";
                    return res;
                }
    }
            

    

    private int doUpdate(String query, String name, String description , String quantity) {
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

    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        int changes = 0;
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("id") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String id = request.getParameter("id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                changes = doUpdate("update products set product_id = ?, name = ?, description = ?, quantity = ? where product_id = ?", id, name, description, quantity, id);
                if (changes > 0) {
                    response.sendRedirect("http://localhost:8080/Assignment3/products?id=" + id);
                } else {
                    response.setStatus(500);
                }
            } else {
                //response.setStatus(500);
                out.println("Error: Not enough data to input. Please use a URL of the form /products?id=xx&name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            System.out.println("Error in writing output: " + ex.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        int changes = 0;
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("id")) {
                String id = request.getParameter("id");
                changes = doUpdate("delete from products where product_id = ?", id);
                if (changes > 0) {
                    response.setStatus(200);
                } else {
                    response.setStatus(500);
                }
            } else {
                //response.setStatus(500);
                out.println("Error: Not enough data to input. Please use a URL of the form /products?id");
            }
        } catch (IOException ex) {
            System.out.println("Error in writing output: " + ex.getMessage());
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

}

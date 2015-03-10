/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.products.servlets;

import com.products.database.DatabaseConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
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
    protected String doGet() {
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        int changes = 0;
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            if (keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                //String id = request.getParameter("id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                changes = doUpdate("INSERT INTO products (name, description, quantity) VALUES (?, ?, ?)", name, description, quantity);
                if (changes > 0) {
                    int id = getId("select max(product_id) from products");
                    response.sendRedirect("http://localhost:8080/Assignment3/products?id=" + id);
                } else {
                    response.setStatus(500);
                }
            } else {
                //response.setStatus(500);
                out.println("Error: Not enough data to input. Please use a URL of the form /products?name=XXX&description=XXX&quantity=xx");
            }
        } catch (IOException ex) {
            System.out.println("Error in writing output: " + ex.getMessage());
        }
    }

    private int doUpdate(String query, String... params) {
        int numChanges = 0;

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
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

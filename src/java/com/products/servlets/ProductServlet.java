/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.products.servlets;

import com.products.database.DatabaseConnection;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 *
 * @author c0644881
 */
@Path("/products")
public class ProductServlet {

    @GET
    @Produces("application/json")
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
        /*StringBuilder stringBuilder = new StringBuilder();
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
        return stringBuilder.toString();*/
        StringWriter out = new StringWriter();
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {  
                JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
                JsonGenerator gen = factory.createGenerator(out);
                gen.writeStartObject()
                        .write("productId", rs.getInt("product_id"))
                        .write("name", rs.getString("name"))
                        .write("description", rs.getString("description"))
                        .write("quantity", rs.getInt("quantity"))
                      .writeEnd();
                gen.close();
            }
            
        } catch (SQLException ex) {
            System.out.println("Exception in getting database connection: " + ex.getMessage());
        }
        return out.toString();

    }

    private String getProduct(String query, String... params) {
        StringBuilder stringBuilder = new StringBuilder();
        StringWriter out = new StringWriter();
        int count = 0;
        //String jsonArray = null;      
        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {  
                JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
                JsonGenerator gen = factory.createGenerator(out);
                gen.writeStartObject()
                        .write("productId", rs.getInt("product_id"))
                        .write("name", rs.getString("name"))
                        .write("description", rs.getString("description"))
                        .write("quantity", rs.getInt("quantity"))
                      .writeEnd();
                gen.close();
            }
            
        } catch (SQLException ex) {
            System.out.println("Exception in getting database connection: " + ex.getMessage());
        }
        return out.toString();
    }

    @POST
    @Consumes("application/json")
    public Response doPost(@Context UriInfo uri, String str) {
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
            //String res = "http://localhost:8080/CPD4414-Assignment5/webresources/products/" + id;
            return Response.ok(uri.getAbsolutePath().toString() + "/" + id).build();
        } else {
            return Response.status(500).build();
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
    public Response doPut(@Context UriInfo uri, @PathParam("id") String id, String str) {
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
            //String res = "http://localhost:8080/CPD4414-Assignment5/webresources/products/" + id;
            return Response.ok(uri.getAbsolutePath().toString() + "/" + id).build();
        } else {
            return Response.status(500).build();
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

    @DELETE
    @Path("{id}")
    public Response doDelete(@Context UriInfo uri, @PathParam("id") String id) {
        int changes = 0;

        changes = doRemove("delete from products where product_id = ?", id);
        if (changes > 0) {
            return Response.ok().build();
        } else {
            return Response.status(500).build();
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

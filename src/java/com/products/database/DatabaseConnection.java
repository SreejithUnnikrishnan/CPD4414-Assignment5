/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.products.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author c0644881
 */
public class DatabaseConnection {
    
    public static Connection getConnection() throws SQLException{
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/inventory";
            String user = "root";
            String pass = "";
            connection = DriverManager.getConnection(jdbc, user, pass);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Exception in getting connection: " + ex.getMessage());
        }
        return connection;
        
    }
    
}

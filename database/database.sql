--create INVENTORY database
create database inventory;

use inventory;

-- create PRODUCTS table
create table products(
	product_id INTEGER(3) not null auto_increment,
	name VARCHAR(30) not null,
	description VARCHAR(50) not null,
	quantity INTEGER(3) not null,
	CONSTRAINT products_product_id_pk PRIMARY KEY (product_id)
);

-- Auto increment for product_id
ALTER TABLE products AUTO_INCREMENT = 1;

-- Insert data into PRODUCTS table
INSERT INTO products (name,description,quantity) VALUES ('lorem','lorem ipsum',4);
INSERT INTO products (name,description,quantity) VALUES ('dolor','dolor sit',12);


select * from products;


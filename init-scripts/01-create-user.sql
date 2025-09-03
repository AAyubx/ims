-- Create user with proper authentication for MySQL 8.0
-- This script runs when the MySQL container first initializes

-- Create the user with mysql_native_password authentication
CREATE USER IF NOT EXISTS 'inventory_user'@'%' IDENTIFIED WITH mysql_native_password BY 'inventory_pass';

-- Grant all privileges on the inventory_saas database
GRANT ALL PRIVILEGES ON inventory_saas.* TO 'inventory_user'@'%';

-- Grant SELECT on mysql.user to help with debugging
GRANT SELECT ON mysql.user TO 'inventory_user'@'%';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify the user was created correctly
SELECT User, Host, plugin FROM mysql.user WHERE User = 'inventory_user';
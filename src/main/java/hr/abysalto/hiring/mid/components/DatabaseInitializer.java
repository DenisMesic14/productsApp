package hr.abysalto.hiring.mid.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private boolean dataInitialized = false;

    public boolean isDataInitialized() {
        return this.dataInitialized;
    }

    public void initialize() {
        initTables();
        initData();
        this.dataInitialized = true;
    }

    private void initTables() {

        this.jdbcTemplate.execute("""
                 CREATE TABLE IF NOT EXISTS USERS (
                	 ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                	 USERNAME VARCHAR(50) UNIQUE NOT NULL,
                	 PASSWORD VARCHAR(255) NOT NULL,
                	 EMAIL VARCHAR(100) UNIQUE NOT NULL,
                	 FIRST_NAME VARCHAR(100),
                	 LAST_NAME VARCHAR(100),
                	 CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                	 ENABLED BOOLEAN DEFAULT TRUE
                 );
                """);

        this.jdbcTemplate.execute("""
                 CREATE TABLE IF NOT EXISTS CART_ITEMS (
                	 ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                	 USER_ID BIGINT NOT NULL,
                	 PRODUCT_ID BIGINT NOT NULL,
                	 PRODUCT_TITLE VARCHAR(255),
                	 PRODUCT_PRICE DECIMAL(10,2),
                	 QUANTITY INT DEFAULT 1,
                	 ADDED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                	 FOREIGN KEY (USER_ID) REFERENCES USERS(ID) ON DELETE CASCADE
                 );
                """);

        this.jdbcTemplate.execute("""
                 CREATE TABLE IF NOT EXISTS FAVORITES (
                	 ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                	 USER_ID BIGINT NOT NULL,
                	 PRODUCT_ID BIGINT NOT NULL,
                	 ADDED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                	 FOREIGN KEY (USER_ID) REFERENCES USERS(ID) ON DELETE CASCADE,
                	 UNIQUE(USER_ID, PRODUCT_ID)
                 );
                """);

        this.jdbcTemplate.execute("""
                 CREATE TABLE IF NOT EXISTS BUYER (
                	 BUYER_ID INT AUTO_INCREMENT PRIMARY KEY,
                	 FIRST_NAME VARCHAR(100) NOT NULL,
                	 LAST_NAME VARCHAR(100) NOT NULL,
                	 TITLE VARCHAR(100) NULL
                 );
                """);
    }

    private void initData() {
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE USERNAME = 'testuser'",
                Integer.class
        );

        if (userCount == null || userCount == 0) {
            this.jdbcTemplate.execute("""
                    	INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, FIRST_NAME, LAST_NAME, CREATED_AT, ENABLED) 
                    	VALUES ('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
                    			'test@example.com', 'Test', 'User', CURRENT_TIMESTAMP, true)
                    """);
        }

        Integer buyerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BUYER",
                Integer.class
        );
        if (buyerCount == null || buyerCount == 0) {
            this.jdbcTemplate.execute("INSERT INTO BUYER (FIRST_NAME, LAST_NAME, TITLE) VALUES ('Jabba', 'Hutt', 'the')");
            this.jdbcTemplate.execute("INSERT INTO BUYER (FIRST_NAME, LAST_NAME, TITLE) VALUES ('Anakin', 'Skywalker', NULL)");
            this.jdbcTemplate.execute("INSERT INTO BUYER (FIRST_NAME, LAST_NAME, TITLE) VALUES ('Jar Jar', 'Binks', NULL)");
            this.jdbcTemplate.execute("INSERT INTO BUYER (FIRST_NAME, LAST_NAME, TITLE) VALUES ('Han', 'Solo', NULL)");
            this.jdbcTemplate.execute("INSERT INTO BUYER (FIRST_NAME, LAST_NAME, TITLE) VALUES ('Leia', 'Organa', 'Princess')");
        }
    }
}
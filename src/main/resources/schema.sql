CREATE TABLE IF NOT EXISTS device_categories (id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              category_name VARCHAR(255) NOT NULL,
                                              description VARCHAR(500));

CREATE TABLE IF NOT EXISTS device_commands (id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            model_id VARCHAR(255) NOT NULL,
                                            command_name VARCHAR(255) NOT NULL,
                                            command_template TEXT,
                                            description VARCHAR(500));

CREATE TABLE IF NOT EXISTS device_rules (id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(255) NOT NULL,
                                         description VARCHAR(500),
                                         condition_json TEXT NOT NULL,
                                         action_json TEXT NOT NULL,
                                         is_active BOOLEAN DEFAULT TRUE);

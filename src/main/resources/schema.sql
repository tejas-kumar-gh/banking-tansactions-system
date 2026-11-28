CREATE TABLE customer (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_account_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id),

    CONSTRAINT chk_account_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_account_status
        CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
) ENGINE=InnoDB;


--source_account_id & destination_account_id are both nullable because a DEPOSIT only uses a destination account, a WITHDRAWAL only uses a source account, and a TRANSFER uses both.
CREATE TABLE banking_transaction (
    transaction_record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    source_account_id BIGINT,
    destination_account_id BIGINT,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_source
        FOREIGN KEY (source_account_id)
        REFERENCES account(account_id),

    CONSTRAINT fk_transaction_destination
        FOREIGN KEY (destination_account_id)
        REFERENCES account(account_id),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transaction_type
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),

    CONSTRAINT chk_transaction_status
        CHECK (transaction_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REJECTED'))
) ENGINE=InnoDB;

CREATE TABLE transaction_audit (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES banking_transaction(transaction_id)
) ENGINE=InnoDB;

CREATE TABLE failed_transaction (
    failure_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    failure_reason VARCHAR(255) NOT NULL,
    error_category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_failed_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES banking_transaction(transaction_id)
) ENGINE=InnoDB;

CREATE INDEX idx_account_customer
ON account(customer_id);

CREATE INDEX idx_transaction_source
ON banking_transaction(source_account_id);

CREATE INDEX idx_transaction_destination
ON banking_transaction(destination_account_id);

CREATE INDEX idx_transaction_status
ON banking_transaction(transaction_status);

CREATE INDEX idx_transaction_created
ON banking_transaction(created_at);

CREATE INDEX idx_audit_transaction
ON transaction_audit(transaction_id);


CREATE TABLE performance_test_run (
    run_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_name VARCHAR(100),
    worker_count INT NOT NULL,
    total_transactions INT NOT NULL,
    test_status VARCHAR(20) NOT NULL,
    started_at DATETIME(3),
    completed_at DATETIME(3),
    duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE performance_test_metrics (
    metric_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id BIGINT NOT NULL UNIQUE,

    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    rejected_count INT DEFAULT 0,
    technical_error_count INT DEFAULT 0,

    average_duration_ms DECIMAL(12,3),
    minimum_duration_ms BIGINT,
    maximum_duration_ms BIGINT,

    throughput_tps DECIMAL(12,3),
    success_rate DECIMAL(7,3),
    failure_rate DECIMAL(7,3),

    peak_active_threads INT,
    queue_capacity INT,
    db_max_pool_size INT,
    peak_db_active_connections INT,

    FOREIGN KEY (run_id)
        REFERENCES performance_test_run(run_id)
);

use banking_db;

CREATE TABLE performance_test_run (
    run_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_name VARCHAR(100),
    worker_count INT NOT NULL,
    total_transactions INT NOT NULL,
    test_status VARCHAR(20) NOT NULL,
    started_at DATETIME(3),
    completed_at DATETIME(3),
    duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


CREATE TABLE performance_test_metrics (
    metric_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    run_id BIGINT NOT NULL UNIQUE,

    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    rejected_count INT DEFAULT 0,
    technical_error_count INT DEFAULT 0,

    average_duration_ms DECIMAL(12,3),
    minimum_duration_ms BIGINT,
    maximum_duration_ms BIGINT,

    throughput_tps DECIMAL(12,3),
    success_rate DECIMAL(7,3),
    failure_rate DECIMAL(7,3),

    peak_active_threads INT,
    queue_capacity INT,
    db_max_pool_size INT,
    peak_db_active_connections INT,

    FOREIGN KEY (run_id)
        REFERENCES performance_test_run(run_id)
) ENGINE=InnoDB;



ALTER TABLE performance_test_metrics
ADD COLUMN peak_db_idle_connections INT DEFAULT 0,
ADD COLUMN peak_db_pending_threads INT DEFAULT 0;
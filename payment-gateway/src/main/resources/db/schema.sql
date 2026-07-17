CREATE DATABASE IF NOT EXISTS payment_gateway
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE payment_gateway;

-- Payment orders table
CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT 'Merchant order number',
    trade_no VARCHAR(64) DEFAULT NULL COMMENT 'Channel transaction number',
    channel VARCHAR(20) NOT NULL COMMENT 'Payment channel: WECHAT/ALIPAY/UNIONPAY',
    scene VARCHAR(20) NOT NULL COMMENT 'Payment scene: NATIVE/H5/APP',
    amount DECIMAL(12,2) NOT NULL COMMENT 'Amount in yuan',
    subject VARCHAR(256) DEFAULT NULL COMMENT 'Order subject',
    body TEXT DEFAULT NULL COMMENT 'Order description',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Order status: PENDING/SUCCESS/FAILED/CLOSED/REFUNDED',
    channel_fee DECIMAL(12,4) DEFAULT 0 COMMENT 'Channel fee rate used',
    response_time_ms INT DEFAULT 0 COMMENT 'Channel response time in ms',
    extra_data JSON DEFAULT NULL COMMENT 'Additional channel response data',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_trade_no (trade_no),
    INDEX idx_channel (channel),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Payment orders';

-- Channel fee configuration
CREATE TABLE IF NOT EXISTS channel_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel VARCHAR(20) NOT NULL COMMENT 'Payment channel code',
    channel_name VARCHAR(64) NOT NULL COMMENT 'Channel display name',
    fee_rate DECIMAL(6,4) NOT NULL COMMENT 'Fee rate (e.g. 0.0060 = 0.6%)',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT 'Enabled: 1=yes, 0=no',
    priority INT NOT NULL DEFAULT 100 COMMENT 'Manual priority (lower = higher)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Channel fee configuration';

-- Reconciliation records
CREATE TABLE IF NOT EXISTS reconciliation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no VARCHAR(32) NOT NULL COMMENT 'Reconciliation batch number',
    channel VARCHAR(20) NOT NULL COMMENT 'Payment channel',
    bill_date VARCHAR(10) NOT NULL COMMENT 'Bill date (yyyy-MM-dd)',
    local_order_no VARCHAR(32) DEFAULT NULL COMMENT 'Local order number',
    channel_trade_no VARCHAR(64) DEFAULT NULL COMMENT 'Channel transaction number',
    channel_amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Amount from channel bill',
    local_amount DECIMAL(12,2) DEFAULT NULL COMMENT 'Amount in local order',
    diff_type VARCHAR(20) NOT NULL COMMENT 'Difference type: MATCH/LOCAL_ONLY/CHANNEL_ONLY/AMOUNT_MISMATCH',
    diff_detail VARCHAR(512) DEFAULT NULL COMMENT 'Difference detail',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Handle status: PENDING/HANDLED/IGNORED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_batch_no (batch_no),
    INDEX idx_channel (channel),
    INDEX idx_bill_date (bill_date),
    INDEX idx_diff_type (diff_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Reconciliation records';

-- Insert default channel configuration
INSERT INTO channel_config (channel, channel_name, fee_rate, priority) VALUES
    ('WECHAT', 'WeChat Pay', 0.0060, 1),
    ('ALIPAY', 'Alipay', 0.0055, 2),
    ('UNIONPAY', 'UnionPay', 0.0050, 3)
ON DUPLICATE KEY UPDATE channel_name = VALUES(channel_name);

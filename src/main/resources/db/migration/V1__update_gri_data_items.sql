-- 테이블이 없는 경우 생성
DO $$
BEGIN
    -- 테이블이 없는 경우 생성
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'gri_data_items'
    ) THEN
        CREATE TABLE gri_data_items (
            id BIGSERIAL PRIMARY KEY,
            standard_code VARCHAR(255) NOT NULL,
            disclosure_code VARCHAR(255) NOT NULL,
            disclosure_title VARCHAR(255) NOT NULL,
            disclosure_value TEXT,
            description TEXT,
            numeric_value DOUBLE PRECISION,
            unit VARCHAR(255),
            reporting_period_start DATE,
            reporting_period_end DATE,
            verification_status VARCHAR(255),
            verification_provider VARCHAR(255),
            category VARCHAR(255) NOT NULL,
            company_id BIGINT,
            created_at TIMESTAMP NOT NULL,
            updated_at TIMESTAMP,
            data_type VARCHAR(255) NOT NULL CHECK (data_type IN ('TIMESERIES','TEXT','NUMERIC'))
        );
        
        -- 인덱스 생성
        CREATE INDEX idx_gri_company_id ON gri_data_items(company_id);
        CREATE INDEX idx_gri_standard_code ON gri_data_items(standard_code);
        CREATE INDEX idx_gri_disclosure_code ON gri_data_items(disclosure_code);
        CREATE INDEX idx_gri_category ON gri_data_items(category);
        CREATE INDEX idx_gri_reporting_period ON gri_data_items(reporting_period_start, reporting_period_end);
        CREATE INDEX idx_gri_verification_status ON gri_data_items(verification_status);
        CREATE INDEX idx_gri_company_category ON gri_data_items(company_id, category);
    ELSE
        -- 테이블은 있지만 data_type 컬럼이 없는 경우 추가
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'gri_data_items' AND column_name = 'data_type'
        ) THEN
            -- 먼저 NULL을 허용하도록 추가
            ALTER TABLE gri_data_items ADD COLUMN data_type VARCHAR(255);
            
            -- 기존 데이터를 기본값 'TEXT'로 업데이트
            UPDATE gri_data_items SET data_type = 'TEXT' WHERE data_type IS NULL;
            
            -- 이제 NOT NULL 제약조건과 CHECK 제약조건 추가
            ALTER TABLE gri_data_items ALTER COLUMN data_type SET NOT NULL;
            ALTER TABLE gri_data_items ADD CONSTRAINT check_data_type CHECK (data_type IN ('TIMESERIES','TEXT','NUMERIC'));
        ELSE
            -- 이미 존재하는 경우 NULL 값만 'TEXT'로 업데이트
            UPDATE gri_data_items SET data_type = 'TEXT' WHERE data_type IS NULL;
        END IF;
    END IF;
END $$; 
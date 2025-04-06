-- gri_data_items 테이블에 data_type 컬럼이 이미 존재하는지 확인
DO $$
BEGIN
    -- 데이터 타입 컬럼이 존재하지 않으면 추가
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
END $$; 
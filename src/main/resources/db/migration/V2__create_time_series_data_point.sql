-- TimeSeriesDataPoint 테이블 생성
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_name = 'time_series_data_points'
    ) THEN
        CREATE TABLE time_series_data_points (
            id BIGSERIAL PRIMARY KEY,
            gri_data_item_id BIGINT NOT NULL,
            year INTEGER NOT NULL,
            value VARCHAR(255) NOT NULL,
            unit VARCHAR(255),
            created_at TIMESTAMP,
            updated_at TIMESTAMP,
            CONSTRAINT fk_time_series_gri_data_item 
                FOREIGN KEY (gri_data_item_id) 
                REFERENCES gri_data_items(id) 
                ON DELETE CASCADE
        );
        
        -- 인덱스 생성
        CREATE INDEX idx_time_series_gri_data_item ON time_series_data_points(gri_data_item_id);
        CREATE INDEX idx_time_series_year ON time_series_data_points(year);
    END IF;
END $$; 
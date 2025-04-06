package dev.gyeoul.esginsightboard.controller;

import dev.gyeoul.esginsightboard.dto.GriDataItemDto;
import dev.gyeoul.esginsightboard.dto.TimeSeriesDataPointDto;
import dev.gyeoul.esginsightboard.entity.GriDataItem;
import dev.gyeoul.esginsightboard.service.GriDataItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class CompanyGriControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private GriDataItemService griDataItemService;
    
    @InjectMocks
    private CompanyGriController companyGriController;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(companyGriController).build();
    }
    
    @Test
    @DisplayName("회사 GRI 데이터를 조회한다")
    void testGetCompanyGriData() throws Exception {
        // Given
        Long companyId = 1L;
        Map<String, GriDataItemDto> mockDataMap = createMockGriDataMap();
        when(griDataItemService.getGriDataMapByCompanyId(companyId)).thenReturn(mockDataMap);
        
        // When & Then
        mockMvc.perform(get("/api/company/{companyId}/gri", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['305-1'].standardCode").value("GRI 305"))
                .andExpect(jsonPath("$.['305-1'].disclosureTitle").value("직접 온실가스 배출량 (Scope 1)"))
                .andExpect(jsonPath("$.['305-1'].dataType").value("TIMESERIES"));
    }
    
    @Test
    @DisplayName("회사 GRI 데이터를 업데이트한다")
    void testUpdateCompanyGriData() throws Exception {
        // Given
        Long companyId = 1L;
        Map<String, GriDataItemDto> mockDataMap = createMockGriDataMap();
        Map<String, GriDataItemDto> updatedDataMap = createMockGriDataMap(); // 실제로는 업데이트된 데이터
        
        when(griDataItemService.updateGriDataForCompany(eq(companyId), any())).thenReturn(updatedDataMap);
        
        // When & Then
        mockMvc.perform(put("/api/company/{companyId}/gri", companyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockDataMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['305-1'].standardCode").value("GRI 305"))
                .andExpect(jsonPath("$.['305-1'].disclosureTitle").value("직접 온실가스 배출량 (Scope 1)"))
                .andExpect(jsonPath("$.['305-1'].dataType").value("TIMESERIES"));
    }
    
    @Test
    @DisplayName("회사의 특정 카테고리 GRI 데이터를 업데이트한다")
    void testUpdateCompanyGriDataByCategory() throws Exception {
        // Given
        Long companyId = 1L;
        String category = "E";
        Map<String, GriDataItemDto> mockDataMap = createMockGriDataMap();
        Map<String, GriDataItemDto> updatedDataMap = createMockGriDataMap(); // 실제로는 업데이트된 데이터
        
        when(griDataItemService.updateGriDataForCompanyByCategory(eq(companyId), eq(category), any())).thenReturn(updatedDataMap);
        
        // When & Then
        mockMvc.perform(put("/api/company/{companyId}/gri/{category}", companyId, category)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockDataMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['305-1'].standardCode").value("GRI 305"))
                .andExpect(jsonPath("$.['305-1'].disclosureTitle").value("직접 온실가스 배출량 (Scope 1)"))
                .andExpect(jsonPath("$.['305-1'].dataType").value("TIMESERIES"));
    }
    
    // 테스트용 목 데이터 생성
    private Map<String, GriDataItemDto> createMockGriDataMap() {
        Map<String, GriDataItemDto> dataMap = new HashMap<>();
        
        // 시계열 데이터 포인트 생성
        List<TimeSeriesDataPointDto> timeSeriesData = new ArrayList<>();
        timeSeriesData.add(TimeSeriesDataPointDto.builder()
                .year(2020)
                .value("15000")
                .unit("tCO2eq")
                .notes("2020년 배출량")
                .build());
        timeSeriesData.add(TimeSeriesDataPointDto.builder()
                .year(2021)
                .value("14500")
                .unit("tCO2eq")
                .notes("2021년 배출량")
                .build());
        
        // 시계열 데이터 항목 생성
        GriDataItemDto timeseriesItem = GriDataItemDto.builder()
                .id(1L)
                .standardCode("GRI 305")
                .disclosureCode("305-1")
                .disclosureTitle("직접 온실가스 배출량 (Scope 1)")
                .disclosureValue("시계열 데이터 참조")
                .category("E")
                .dataType("TIMESERIES")
                .timeSeriesData(timeSeriesData)
                .build();
        
        // 텍스트 데이터 항목 생성
        GriDataItemDto textItem = GriDataItemDto.builder()
                .id(2L)
                .standardCode("GRI 305")
                .disclosureCode("305-5")
                .disclosureTitle("온실가스 배출량 감축")
                .disclosureValue("당사는 2030년까지 온실가스 배출량을 2020년 대비 50% 감축하는 목표를 설정하였습니다.")
                .category("E")
                .dataType("TEXT")
                .build();
        
        // 숫자 데이터 항목 생성
        GriDataItemDto numericItem = GriDataItemDto.builder()
                .id(3L)
                .standardCode("GRI 302")
                .disclosureCode("302-1")
                .disclosureTitle("조직 내 에너지 소비")
                .numericValue(120000.0)
                .unit("MWh")
                .category("E")
                .dataType("NUMERIC")
                .build();
        
        dataMap.put("305-1", timeseriesItem);
        dataMap.put("305-5", textItem);
        dataMap.put("302-1", numericItem);
        
        return dataMap;
    }
} 
package dev.gyeoul.esginsightboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequest {
    private String companyName;
    private String ceoName;
    private String companyCode;
    private String companyPhoneNumber;
}

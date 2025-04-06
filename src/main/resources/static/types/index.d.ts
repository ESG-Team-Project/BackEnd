/**
 * ESG Insight Board API TypeScript Interface Definitions
 * 
 * 이 파일은 백엔드와 프론트엔드 간의 타입 정의를 공유하기 위한 목적으로 작성되었습니다.
 * API 응답과 요청에 사용되는 주요 인터페이스 정의를 포함합니다.
 */

/**
 * 페이지네이션 응답 인터페이스
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * 오류 응답 인터페이스
 */
export interface ErrorResponse {
  status: number;
  code: string;
  message: string;
  timestamp: string;
  path: string;
  errors?: ValidationError[];
}

/**
 * 유효성 검증 오류 인터페이스
 */
export interface ValidationError {
  field: string;
  rejectedValue: string;
  message: string;
}

/**
 * 감사 로그 DTO 인터페이스
 */
export interface AuditLogDto {
  id: number;
  entityType: string;
  entityId: string;
  action: string;
  details: string;
  username: string;
  ipAddress: string;
  createdAt: string;
}

/**
 * GRI 데이터 항목 DTO 인터페이스
 */
export interface GriDataItemDto {
  id?: number;
  standardCode: string;
  disclosureCode: string;
  disclosureTitle?: string;
  disclosureValue: string;
  numericValue?: number;
  unit?: string;
  reportingPeriodStart?: string;
  reportingPeriodEnd?: string;
  verificationStatus?: string;
  verificationProvider?: string;
  category?: string;
  companyId: number;
  companyName?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  valid?: boolean;
  timeSeriesData?: TimeSeriesDataPointDto[];
}

/**
 * 시계열 데이터 포인트 DTO 인터페이스
 */
export interface TimeSeriesDataPointDto {
  id?: string;
  year: number;
  value: number;
  unit?: string;
}

/**
 * GRI 데이터 검색 조건 인터페이스
 */
export interface GriDataSearchCriteria {
  category?: string;
  standardCode?: string;
  disclosureCode?: string;
  reportingPeriodStart?: string;
  reportingPeriodEnd?: string;
  verificationStatus?: string;
  companyId?: number;
  keyword?: string;
  sort?: string;
} 
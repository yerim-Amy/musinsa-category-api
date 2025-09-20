# 무신사 카테고리 관리 API

온라인 쇼핑몰의 상품 카테고리 관리 시스템입니다. 패션 브랜드 무신사의 복잡한 상품 분류 체계를 모델링하여, 성별 구분과 트리 구조를 지원합니다.
JWT 토큰 기반 인증을 통해 관리자만 카테고리를 생성/수정/삭제할 수 있고, 일반 사용자는 카테고리 조회만 가능합니다. RESTful API와 실시간 API 문서(Swagger)를 제공합니다.

## 실행 방법

실행 방법은 2가지가 있습니다.

### 방법 1: Docker Compose 사용

#### 사전 요구사항
- Docker Desktop (Windows/Mac)

#### 실행 단계
```bash
# Docker Compose로 한 번에 빌드 및 실행
docker-compose up

# 백그라운드 실행
docker-compose up -d

# 중지
docker-compose down
```

### 방법 2: 직접 JAR 실행

#### 사전 요구사항
- Java 11
- Maven 3.3 이상

#### 실행 단계
```bash
# JAR 파일 빌드
mvn clean package -DskipTests

# 애플리케이션 실행
java -jar target/category-api-0.0.1-SNAPSHOT.jar
```

### 애플리케이션 접속
- **API 서버**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **H2 콘솔**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## Database 명세

### H2 Database 설정
- **접속 URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:categorydb`
- **Username**: `sa`
- **Password**: (공백)
- **특징**: 인메모리 데이터베이스로 애플리케이션 종료시 데이터 초기화
- **초기 데이터**: `data.sql` 파일을 통해 자동 로드

### categories 테이블
| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 카테고리 고유 ID |
| name | VARCHAR(100) | NOT NULL | 카테고리 이름 |
| description | VARCHAR(1000) | NULL | 카테고리 설명 |
| gender | CHAR(1) | DEFAULT 'A' | 성별 구분 (M/F/A) |
| parent_id | BIGINT | FK | 부모 카테고리 ID |
| display_order | INT | NOT NULL, DEFAULT 1 | 정렬 순서 |
| depth | INT | NOT NULL, DEFAULT 0 | 카테고리 깊이 |
| path | VARCHAR(1000) | NULL | 카테고리 경로 (/1/2/3) |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 활성화 상태 |
| created_at | TIMESTAMP | NOT NULL | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | 수정일시 |
| created_by | VARCHAR(50) | NULL | 생성자 |
| updated_by | VARCHAR(50) | NULL | 수정자 |

### 제약조건
- **UK_category_parent_display_order**: (parent_id, display_order) 유니크 제약
- **FK_category_parent**: parent_id → categories(id) 외래키 제약

## API 명세

### 인증 API

#### 관리자 토큰 발급
```http
POST /auth/token
Content-Type: application/json

{
  "adminId": "admin",
  "password": "musinsa2025!"
}
```

**응답**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "adminId": "admin",
    "expiresIn": 3600,
    "expiresAt": "2025-09-20T15:30:00"
  },
  "message": "토큰이 성공적으로 발급되었습니다"
}
```

#### 토큰 검증
```http
GET /auth/verify
Authorization: Bearer {token}
```

### 카테고리 API

**인증이 필요한 API**: 카테고리 생성, 수정, 삭제, 활성화
**인증이 불필요한 API**: 카테고리 조회, 검색

```http
Authorization: Bearer {your-jwt-token}
```

#### 주요 API 목록

| HTTP Method | Endpoint | 설명 | 인증 필요 |
|-------------|----------|------|-----------|
| POST | `/api/categories` | 카테고리 생성 | ✅ |
| PUT | `/api/categories/{id}` | 카테고리 수정 | ✅ |
| DELETE | `/api/categories/{id}` | 카테고리 삭제(비활성화) | ✅ |
| DELETE | `/api/categories/{id}/real?confirm=true` | 카테고리 완전 삭제 | ✅ |
| PATCH | `/api/categories/{id}/activate` | 카테고리 활성화 | ✅ |
| GET | `/api/categories/{id}` | 단일 카테고리 조회 | ❌ |
| GET | `/api/categories/{id}/children` | 하위 카테고리 조회 | ❌ |
| GET | `/api/categories/tree` | 카테고리 트리 조회 | ❌ |
| GET | `/api/categories/roots` | 루트 카테고리 조회 | ❌ |
| GET | `/api/categories` | 전체 카테고리 조회 | ❌ |
| GET | `/api/categories/search?keyword={keyword}` | 카테고리 검색 | ❌ |

#### 카테고리 생성 예시
```http
POST /api/categories
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "상의",
  "description": "상의 카테고리",
  "parentId": null,
  "displayOrder": 1,
  "gender": "A"
}
```

#### Gender 파라미터
- `M`: 남성
- `F`: 여성  
- `A`: 전체 (기본값)

### 응답 형식
```json
{
  "success": true,
  "data": { ... },
  "message": "응답 메시지",
  "timestamp": "2025-09-20T14:30:00"
}
```
```json
{
  "success": false,
  "error": "C001", 
  "message": "카테고리를 찾을 수 없습니다",
  "timestamp": "2025-09-20T14:30:00"
}
```

## Swagger 사용법

1. **접속**: http://localhost:8080/swagger-ui/index.html
2. **토큰 발급**: `/auth/token` API 실행 (adminId: `admin`, password: `musinsa2025!`)
3. **인증 설정**: 우상단 "Authorize" 버튼 → `Bearer {토큰}` 입력
4. **API 테스트**: 인증이 필요한 API들을 테스트

## 주요 기능

- **계층형 카테고리 관리**: 트리 구조 지원 (최대 4단계)
- **성별 구분**: 남성/여성/전체 카테고리 분류
- **정렬 순서 관리**: 같은 레벨 내 카테고리 순서 조정
- **소프트 삭제**: 카테고리 비활성화/활성화
- **JWT 인증**: 관리자 전용 API 보안
- **API 문서화**: SpringDoc OpenAPI 지원

## 기술 스택

- **Framework**: Spring Boot 2.7.18
- **Security**: JWT (jjwt 0.11.5)
- **Database**: JPA/Hibernate + H2
- **Documentation**: SpringDoc OpenAPI 1.7.0
- **Build Tool**: Maven
- **Java**: 11
- **Container**: Docker & Docker Compose

## 참고사항

- 테스트용 계정은 application.yml에 작성했음
- 카테고리 이름은 같은 부모 하위에서 중복 불가
- displayOrder는 같은 부모 하위에서 중복 불가
- 하위 카테고리가 있는 카테고리는 삭제 불가
- 부모가 비활성화된 경우 하위 카테고리 활성화 불가
- 키워드로 검색 시, 2글자 이상 입력해야 검색 가능
- JWT 토큰 만료시간: 1시간
- H2 데이터베이스는 애플리케이션 종료시 초기화

### 로그 확인

- **로그 파일**: `logs/musinsa-category-api.log`
- **Docker 로그**: `docker-compose logs -f`

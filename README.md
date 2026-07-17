# pharmacyservice

약품 재고 관리 서비스 1차 스프린트: 약품 재고 조회(HL2-5), 약품 입고 관리(HL2-6), 처방전 조회(HL2-17).

## 실행 전 환경변수 설정

Oracle DB(스키마 `PHARMACY`)가 이미 생성되어 있어야 하며, 다음 환경변수를 설정해야 애플리케이션과 통합 테스트(`PharmacyserviceApplicationTests`)가 정상 기동합니다.

| 환경변수 | 설명 | 예시 |
|---|---|---|
| `PHARMACY_DB_URL` | Oracle JDBC 접속 URL | `jdbc:oracle:thin:@localhost:1521/XEPDB1` |
| `PHARMACY_DB_USERNAME` | DB 계정 | `pharmacy` |
| `PHARMACY_DB_PASSWORD` | DB 비밀번호 | `********` |

Windows PowerShell 예시:

```powershell
$env:PHARMACY_DB_URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1"
$env:PHARMACY_DB_USERNAME = "pharmacy"
$env:PHARMACY_DB_PASSWORD = "********"
./gradlew bootRun
```

`spring.jpa.hibernate.ddl-auto=validate`로 설정되어 있어, DB에 `MEDICATION_LOT` / `MEDICATION_STOCK` / `MEDICATION_RECEIPT` / `MEDICATION_RECEIPT_ITEM` / `INVENTORY_MOVEMENT` / `PRESCRIPTION_LINK` / `PRESCRIPTION_ITEM_LINK` 테이블이 엔티티와 일치하는 컬럼으로 미리 존재해야 기동됩니다. 테이블/컬럼을 새로 만들거나 변경하지 않습니다.

## 테스트 실행

```bash
./gradlew clean test
```

- `PharmacyserviceApplicationTests`(전체 컨텍스트 로딩)는 위 DB 환경변수가 유효한 Oracle 인스턴스를 가리켜야 통과합니다.
- 나머지 `*ServiceTest`(Mockito 단위 테스트), `*ControllerTest`(`@WebMvcTest` 슬라이스 테스트)는 DB 없이도 실행됩니다.

## API 요약

### 약품 재고 조회 (HL2-5)

- `GET /api/pharmacy/inventories` — medicationId, lotNo, storageLocationId, expirationFrom, expirationTo, page, size (모두 선택). 기본 정렬: 유효기한 오름차순.
- `GET /api/pharmacy/inventories/{medicationStockId}` — 없으면 404.

```bash
curl "http://localhost:8080/api/pharmacy/inventories?medicationId=MEDICATION-001&page=0&size=20"
curl "http://localhost:8080/api/pharmacy/inventories/{medicationStockId}"
```

### 약품 입고 관리 (HL2-6)

- `POST /api/pharmacy/receipts` — 헤더(supplierId, storageLocationId, receiptDt, receivedById) + items 배열. 단일 트랜잭션으로 입고 저장 → 로트 조회/생성 → 재고 조회/생성 → 재고 증가 → 이동이력 저장까지 처리하며, 실패 시 전체 롤백됩니다. DELETE/취소/수정 API는 제공하지 않습니다.

```bash
curl -X POST "http://localhost:8080/api/pharmacy/receipts" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierId": "SUPPLIER-001",
    "storageLocationId": "LOCATION-001",
    "receiptDt": "2026-07-16",
    "receivedById": "STAFF-001",
    "items": [
      {
        "medicationId": "MEDICATION-001",
        "lotNo": "LOT-2026-001",
        "expirationDt": "2027-12-31",
        "manufactureDt": "2026-06-01",
        "unitCd": "EA",
        "receiptQty": 100,
        "unitPrice": 500
      }
    ]
  }'
```

### 처방전 조회 (HL2-17)

- `GET /api/pharmacy/prescriptions` — prescriptionId, patientId, physicianId, departmentId, page, size (모두 선택).
- `GET /api/pharmacy/prescriptions/{prescriptionLinkId}` — items 포함 상세, 없으면 404.

처방/약품/환자/직원 서비스는 아직 실제 API 주소가 확정되지 않아, 이번 스프린트에서는 로컬 연계 테이블(`PRESCRIPTION_LINK`, `PRESCRIPTION_ITEM_LINK`) 조회만 구현했습니다. 이후 연동 시 `PrescriptionClient`, `MedicationInfoClient`, `PatientClient`, `StaffClient`를 추가할 예정입니다(TODO).

```bash
curl "http://localhost:8080/api/pharmacy/prescriptions?patientId=PATIENT-001&page=0&size=20"
curl "http://localhost:8080/api/pharmacy/prescriptions/{prescriptionLinkId}"
```

## 공통 응답

```json
{
  "code": 200,
  "message": "SUCCESS",
  "data": {}
}
```

## 이번 스프린트에서 다루지 않는 것

- 조정, 불출, 반품, 폐기, 마약류 기능
- hisfrontend / features / components / AppRouter / Redux Store / Root Reducer
- 외부 서비스(진료, 인사, 임금) 실제 API 연동

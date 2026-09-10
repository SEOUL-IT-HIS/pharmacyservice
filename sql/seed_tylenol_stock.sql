-- ============================================================================
-- 타이레놀정500mg 전용 시드 스크립트
-- ============================================================================
-- 배경: seed_inventory_receipt.sql은 MEDICATION 테이블에서 "아무 5건"만 골라
--       로트/재고를 만들기 때문에, 타이레놀이 그 5건에 우연히 포함되지 않으면
--       (1) 약품 마스터에 아예 없거나 (2) 마스터엔 있어도 재고가 없어서
--       재고조회 화면과 외래 검색(GET /api/pharmacy/medications?name=)에 안 보였다.
--       이 스크립트는 타이레놀정500mg을 이름으로 직접 지정해서
--       마스터 등록(없으면) + 로트 + 재고 + 입고이력까지 한 번에 만든다.
--
-- 실행 전제조건: seed_inventory_receipt.sql과 동일(MEDICATION_LOT 등 테이블이
-- ddl-auto=update로 이미 생성된 상태여야 함).
--
-- EDI_CODE 안내: 실제 건강보험 EDI/약가코드가 아니라 개발/테스트용으로 임의 지정한
-- 값이다(EDI-TYLENOL-500). 외래 쪽에서 처방 이벤트를 보낼 때는 이 값을 하드코딩
-- 하지 말고, 반드시 GET /api/pharmacy/medications?name=타이레놀 응답에 실려오는
-- ediCode 필드를 그대로 사용하면 된다(검색 API의 원래 설계 의도).
--
-- 실행 방법: SQLGate 등에서 PHARMACY 스키마로 접속(pharmacy 계정) 후 전체 실행(F5).
-- 여러 번 실행해도 안전하다 — 마스터는 있으면 재사용, 로트/재고는 실행할 때마다
-- 새로 추가된다(중복 방지 로직 없음, 학습/테스트용).
-- ============================================================================

DECLARE
  v_medication_id    NUMBER;
  v_lot_id           VARCHAR2(36);
  v_stock_id         VARCHAR2(36);
  v_receipt_id       VARCHAR2(36);
  v_receipt_item_id  VARCHAR2(36);
  v_movement_id      VARCHAR2(36);
  v_qty              NUMBER := 100;
  v_lot_cnt          NUMBER;

  FUNCTION new_uuid RETURN VARCHAR2 IS
  BEGIN
    RETURN LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()),
      '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'));
  END;

BEGIN
  -- 1) 마스터에 타이레놀정500mg이 없으면 새로 등록, 있으면 그 ID를 재사용
  BEGIN
    SELECT MEDICATION_ID INTO v_medication_id
    FROM PHARMACY.MEDICATION
    WHERE MEDICATION_NAME LIKE '%타이레놀%'
      AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      v_medication_id := PHARMACY.MEDICATION_SEQ.NEXTVAL;
      -- CREATED_AT/UPDATED_AT은 평소엔 Hibernate(@CreationTimestamp/@UpdateTimestamp)가 채워주지만
      -- 이 스크립트는 JPA를 거치지 않는 순수 SQL이라 직접 넣어야 한다(NOT NULL 제약 있음).
      INSERT INTO PHARMACY.MEDICATION
        (MEDICATION_ID, MEDICATION_NAME, EDI_CODE, FORM_CODE_NAME, ENTP_NAME, ETC_OTC_NAME,
         CREATED_AT, UPDATED_AT)
      VALUES
        (v_medication_id, '타이레놀정500mg', 'EDI-TYLENOL-500', '정제', '한국얀센', '일반의약품',
         SYSTIMESTAMP, SYSTIMESTAMP);
  END;

  -- 2) 그 약품에 이미 재고(로트)가 있으면 다시 만들지 않고 종료
  -- (Oracle PL/SQL은 SQL Server처럼 IF EXISTS(subquery)를 바로 못 써서 COUNT로 확인한다)
  SELECT COUNT(*) INTO v_lot_cnt
  FROM PHARMACY.MEDICATION_LOT
  WHERE MEDICATION_ID = TO_CHAR(v_medication_id);

  IF v_lot_cnt > 0 THEN
    DBMS_OUTPUT.PUT_LINE('타이레놀정500mg(MEDICATION_ID=' || v_medication_id || ')은 이미 재고가 있어 건너뜀.');
  ELSE
    -- 입고 헤더
    v_receipt_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_RECEIPT
      (MEDICATION_RECEIPT_ID, SUPPLIER_ID, STORAGE_LOCATION_ID, RECEIPT_DT, RECEIVED_BY_ID)
    VALUES
      (v_receipt_id, 'SUPPLIER-SEED', 'LOC-A', TRUNC(SYSDATE), 'SEED');

    -- 로트
    v_lot_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_LOT
      (MEDICATION_LOT_ID, MEDICATION_ID, LOT_NO, EXPIRATION_DT, MANUFACTURE_DT, UNIT_CD)
    VALUES
      (v_lot_id, TO_CHAR(v_medication_id), 'LOT-' || TO_CHAR(v_medication_id) || '-01',
       ADD_MONTHS(TRUNC(SYSDATE), 12), TRUNC(SYSDATE), 'EA');

    -- 재고
    v_stock_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_STOCK
      (MEDICATION_STOCK_ID, MEDICATION_LOT_ID, STORAGE_LOCATION_ID, CURRENT_QTY, LAST_MOVEMENT_AT)
    VALUES
      (v_stock_id, v_lot_id, 'LOC-A', v_qty, SYSTIMESTAMP);

    -- 입고 항목
    v_receipt_item_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_RECEIPT_ITEM
      (MEDICATION_RECEIPT_ITEM_ID, MEDICATION_RECEIPT_ID, MEDICATION_LOT_ID, RECEIPT_QTY, UNIT_PRICE)
    VALUES
      (v_receipt_item_id, v_receipt_id, v_lot_id, v_qty, 1000);

    -- 입출고 이력
    v_movement_id := new_uuid;
    INSERT INTO PHARMACY.INVENTORY_MOVEMENT
      (INVENTORY_MOVEMENT_ID, MEDICATION_STOCK_ID, STOCK_TX_TYPE_CD, MOVEMENT_QTY,
       BEFORE_QTY, AFTER_QTY, SOURCE_FORM_ID, SOURCE_FORM_TYPE_CD, MOVEMENT_AT, MOVED_BY_ID)
    VALUES
      (v_movement_id, v_stock_id, '01', v_qty, 0, v_qty, v_receipt_id, 'RECEIPT', SYSTIMESTAMP, 'SEED');

    DBMS_OUTPUT.PUT_LINE('타이레놀정500mg(MEDICATION_ID=' || v_medication_id || ') 재고 ' || v_qty || '건 등록 완료.');
  END IF;

  COMMIT;
END;
/

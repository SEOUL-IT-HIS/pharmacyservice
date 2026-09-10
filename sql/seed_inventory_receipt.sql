-- ============================================================================
-- PHM 재고/입고 샘플 데이터 시드 스크립트
-- ============================================================================
-- 목적: 공공API로 이미 등록된 약품(PHARMACY.MEDICATION) 중 최대 5개를 골라
--       로트(MEDICATION_LOT) + 재고(MEDICATION_STOCK) + 입고(MEDICATION_RECEIPT/
--       MEDICATION_RECEIPT_ITEM) + 입출고이력(INVENTORY_MOVEMENT)을 한 번에 채운다.
--       -> /pharmacy/stock(재고조회), /pharmacy/receipt/list(입고조회) 화면에
--          바로 데이터가 보이게 하기 위한 스크립트.
--
-- 실행 전제조건 (반드시 순서대로):
--   1) pharmacyservice 백엔드를 최소 한 번 기동해서 ddl-auto=update로
--      MEDICATION_LOT / MEDICATION_STOCK / MEDICATION_RECEIPT /
--      MEDICATION_RECEIPT_ITEM / INVENTORY_MOVEMENT 테이블이 자동 생성된 상태여야 함
--   2) 약품 리스트 화면에서 "공공API에서 가져오기" 버튼을 최소 1회 눌러
--      PHARMACY.MEDICATION 테이블에 약품이 1건 이상 있어야 함 (없으면 이 스크립트는
--      아무것도 넣지 않고 조용히 끝남)
--
-- 실행 방법: SQLGate 등에서 PHARMACY 스키마로 접속(pharmacy 계정) 후 전체 실행(F5).
-- 여러 번 실행하면 그때마다 새 로트/재고/입고 건이 추가된다 (중복 방지 로직 없음 —
-- 학습/테스트용 스크립트이므로 여러 번 돌리고 싶으면 그냥 다시 실행하면 됨).
-- ============================================================================

DECLARE
  v_lot_id           VARCHAR2(36);
  v_stock_id         VARCHAR2(36);
  v_receipt_id       VARCHAR2(36);
  v_receipt_item_id  VARCHAR2(36);
  v_movement_id      VARCHAR2(36);
  v_qty              NUMBER;
  v_cnt              NUMBER := 0;

  -- Oracle SYS_GUID()(32자리 hex, 하이픈 없음)을 Java UUID.toString() 형식(36자리)으로 변환
  FUNCTION new_uuid RETURN VARCHAR2 IS
  BEGIN
    RETURN LOWER(REGEXP_REPLACE(RAWTOHEX(SYS_GUID()),
      '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5'));
  END;

BEGIN
  -- 입고 헤더 1건 생성 (공급업체/입고일/담당자는 임시값)
  v_receipt_id := new_uuid;
  INSERT INTO PHARMACY.MEDICATION_RECEIPT
    (MEDICATION_RECEIPT_ID, SUPPLIER_ID, STORAGE_LOCATION_ID, RECEIPT_DT, RECEIVED_BY_ID)
  VALUES
    (v_receipt_id, 'SUPPLIER-SEED', 'LOC-A', TRUNC(SYSDATE), 'SEED');

  -- 공공API로 이미 등록된 약품 중 최대 5개
  FOR r IN (
    SELECT MEDICATION_ID, MEDICATION_NAME
    FROM PHARMACY.MEDICATION
    WHERE ROWNUM <= 5
    ORDER BY MEDICATION_ID
  ) LOOP
    v_cnt := v_cnt + 1;
    v_qty := 50 + v_cnt * 10;  -- 60, 70, 80, 90, 100 식으로 약품마다 다르게

    -- 1) 로트 생성 (유효기간 1년 후, 오늘 제조)
    v_lot_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_LOT
      (MEDICATION_LOT_ID, MEDICATION_ID, LOT_NO, EXPIRATION_DT, MANUFACTURE_DT, UNIT_CD)
    VALUES
      (v_lot_id, TO_CHAR(r.MEDICATION_ID), 'LOT-' || TO_CHAR(r.MEDICATION_ID) || '-01',
       ADD_MONTHS(TRUNC(SYSDATE), 12), TRUNC(SYSDATE), 'EA');

    -- 2) 재고 생성 (해당 로트/보관위치 기준 현재수량)
    v_stock_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_STOCK
      (MEDICATION_STOCK_ID, MEDICATION_LOT_ID, STORAGE_LOCATION_ID, CURRENT_QTY, LAST_MOVEMENT_AT)
    VALUES
      (v_stock_id, v_lot_id, 'LOC-A', v_qty, SYSTIMESTAMP);

    -- 3) 입고 항목 생성 (위 입고 헤더에 연결)
    v_receipt_item_id := new_uuid;
    INSERT INTO PHARMACY.MEDICATION_RECEIPT_ITEM
      (MEDICATION_RECEIPT_ITEM_ID, MEDICATION_RECEIPT_ID, MEDICATION_LOT_ID, RECEIPT_QTY, UNIT_PRICE)
    VALUES
      (v_receipt_item_id, v_receipt_id, v_lot_id, v_qty, 1000);

    -- 4) 입출고 이력 생성 (입고로 인한 수량 증가 기록)
    v_movement_id := new_uuid;
    INSERT INTO PHARMACY.INVENTORY_MOVEMENT
      (INVENTORY_MOVEMENT_ID, MEDICATION_STOCK_ID, STOCK_TX_TYPE_CD, MOVEMENT_QTY,
       BEFORE_QTY, AFTER_QTY, SOURCE_FORM_ID, SOURCE_FORM_TYPE_CD, MOVEMENT_AT, MOVED_BY_ID)
    VALUES
      (v_movement_id, v_stock_id, '01', v_qty, 0, v_qty, v_receipt_id, 'RECEIPT', SYSTIMESTAMP, 'SEED');

    DBMS_OUTPUT.PUT_LINE('약품 ' || r.MEDICATION_ID || ' (' || r.MEDICATION_NAME || ') 재고 ' || v_qty || '건 등록');
  END LOOP;

  IF v_cnt = 0 THEN
    DBMS_OUTPUT.PUT_LINE('PHARMACY.MEDICATION에 약품이 없습니다. 먼저 약품 리스트 화면에서 "공공API에서 가져오기"를 눌러주세요.');
  END IF;

  COMMIT;
END;
/

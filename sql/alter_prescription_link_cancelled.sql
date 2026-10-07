-- 처방코어 취소 통보(CANCELLED 상태) 반영용 스키마 변경.
-- 배경: ddl-auto=update는 새 컬럼은 추가하지만, 이미 만들어진 테이블의 CHECK 제약은 고치지 않는다.
--       Hibernate가 enum(STRING) 컬럼에 STATUS IN ('RECEIVED','DISPENSED','REJECTED') 제약을 만들어 둔 DB라면
--       CANCELLED 저장 시 ORA-02290이 난다. 아래 스크립트는 그 제약을 찾아서 CANCELLED를 포함하도록 다시 만든다.
-- 실행: PHARMACY 스키마 소유 계정(또는 ALTER 권한이 있는 계정)으로 실행. 이미 반영돼 있으면 아무 일도 안 한다.

-- 1) STATUS CHECK 제약을 찾아 교체
DECLARE
    v_cond VARCHAR2(4000);
BEGIN
    FOR c IN (SELECT constraint_name, search_condition
              FROM all_constraints
              WHERE owner = 'PHARMACY'
                AND table_name = 'PRESCRIPTION_LINK'
                AND constraint_type = 'C') LOOP
        v_cond := c.search_condition;
        IF UPPER(v_cond) LIKE '%STATUS%' AND UPPER(v_cond) LIKE '%RECEIVED%' THEN
            IF UPPER(v_cond) LIKE '%CANCELLED%' THEN
                DBMS_OUTPUT.PUT_LINE('이미 CANCELLED가 포함된 제약: ' || c.constraint_name);
            ELSE
                EXECUTE IMMEDIATE 'ALTER TABLE PHARMACY.PRESCRIPTION_LINK DROP CONSTRAINT ' || c.constraint_name;
                EXECUTE IMMEDIATE 'ALTER TABLE PHARMACY.PRESCRIPTION_LINK ADD CONSTRAINT CK_PRESCRIPTION_LINK_STATUS '
                    || 'CHECK (STATUS IN (''RECEIVED'',''DISPENSED'',''REJECTED'',''CANCELLED''))';
                DBMS_OUTPUT.PUT_LINE('제약 교체: ' || c.constraint_name || ' -> CK_PRESCRIPTION_LINK_STATUS');
            END IF;
        END IF;
    END LOOP;
END;
/

-- 2) 취소 사유/취소자 컬럼. 앱이 ddl-auto=update로 자동 추가하므로 보통은 필요 없고,
--    ddl-auto를 끈 환경에서만 직접 실행한다.
-- ALTER TABLE PHARMACY.PRESCRIPTION_LINK ADD (CANCEL_REASON VARCHAR2(200 CHAR), CANCELLED_BY_ID VARCHAR2(100 CHAR), CANCEL_REQUESTED_AT TIMESTAMP, CANCEL_OUTCOME VARCHAR2(20 CHAR),
--   ENCOUNTER_TYPE VARCHAR2(10 CHAR), PRIORITY_CODE VARCHAR2(10 CHAR), VERBAL_YN VARCHAR2(1 CHAR));
-- COMMENT ON COLUMN PHARMACY.PRESCRIPTION_LINK.CANCEL_REASON IS '처방코어 취소사유';
-- COMMENT ON COLUMN PHARMACY.PRESCRIPTION_LINK.CANCELLED_BY_ID IS '처방코어 취소자';
-- COMMENT ON COLUMN PHARMACY.PRESCRIPTION_LINK.CANCEL_REQUESTED_AT IS '처방코어 취소통보 수신일시';
-- COMMENT ON COLUMN PHARMACY.PRESCRIPTION_LINK.CANCEL_OUTCOME IS '취소통보 처리결과(APPLIED 반영/REFUSED 불출 이후라 미반영)';

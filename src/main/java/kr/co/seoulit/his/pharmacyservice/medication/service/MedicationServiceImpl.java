package kr.co.seoulit.his.pharmacyservice.medication.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import kr.co.seoulit.his.pharmacyservice.common.BusinessException;
import kr.co.seoulit.his.pharmacyservice.common.ErrorCode;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationDto;
import kr.co.seoulit.his.pharmacyservice.medication.dto.MedicationRegisterRequest;
import kr.co.seoulit.his.pharmacyservice.medication.entity.Medication;
import kr.co.seoulit.his.pharmacyservice.medication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    /** 공공API(의약품 낱알식별정보) 한 페이지당 조회 건수 */
    private static final int IMPORT_PAGE_SIZE = 100;
    /** 우선 1페이지만 적재 (필요해지면 늘림) */
    private static final int IMPORT_MAX_PAGE = 1;
    private static final DateTimeFormatter ITEM_PERMIT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MedicationRepository medicationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Value("${public-data.medication.base-url}")
    private String publicDataBaseUrl;

    @Value("${public-data.medication.service-key}")
    private String publicDataServiceKey;

    @Override
    public List<MedicationDto> getMedicationList() {
        return medicationRepository.findAll()
                .stream()
                .map(this::toMedicationDto)
                .toList();
    }

    @Override
    @Transactional
    public void registerMedication(MedicationRegisterRequest request) {
        if (!StringUtils.hasText(request.getMedicationName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Medication medication = new Medication();
        medication.setMedicationName(request.getMedicationName().trim());
        medication.setItemSeq(blankToNull(request.getItemSeq()));
        medication.setItemEngName(blankToNull(request.getItemEngName()));
        medication.setEntpName(blankToNull(request.getEntpName()));
        medication.setEtcOtcName(blankToNull(request.getEtcOtcName()));
        medication.setClassNo(blankToNull(request.getClassNo()));
        medication.setClassName(blankToNull(request.getClassName()));
        medication.setFormCodeName(blankToNull(request.getFormCodeName()));
        medication.setChart(blankToNull(request.getChart()));
        medication.setItemPermitDate(request.getItemPermitDate());
        medication.setEdiCode(blankToNull(request.getEdiCode()));
        medication.setStdCd(blankToNull(request.getStdCd()));
        medicationRepository.save(medication);
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    @Override
    @Transactional
    public int importFromPublicApi() {
        int savedCount = 0;

        for (int pageNo = 1; pageNo <= IMPORT_MAX_PAGE; pageNo++) {
            List<JsonNode> items = fetchPublicApiItems(pageNo);
            if (items.isEmpty()) {
                break;
            }

            for (JsonNode item : items) {
                upsertFromPublicApiItem(item);
                savedCount++;
            }

            if (items.size() < IMPORT_PAGE_SIZE) {
                break;
            }
        }

        return savedCount;
    }

    /**
     * body.items는 보통 배열로 바로 내려오지만, 결과가 1건이거나 items.item으로 감싸져 오는
     * 경우도 있어 세 가지 형태를 모두 방어적으로 처리한다.
     */
    private List<JsonNode> fetchPublicApiItems(int pageNo) {
        String response;
        try {
            response = restClient.get()
                    .uri(publicDataBaseUrl
                            + "?serviceKey={serviceKey}&pageNo={pageNo}&numOfRows={numOfRows}&type=json",
                            publicDataServiceKey, pageNo, IMPORT_PAGE_SIZE)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            // 서버까지 요청이 아예 안 갔거나(타임아웃/연결거부) 4xx/5xx로 거절된 경우 — "연결 자체가 불안정"으로 분류
            log.error("공공API(의약품 낱알식별정보) 연결 실패", e);
            throw new BusinessException(ErrorCode.PUBLIC_API_CONNECTION_ERROR);
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response);
        } catch (Exception e) {
            // 연결/응답은 정상으로 왔는데 내용물이 JSON으로 못 읽는 경우 — "응답 파싱 오류"로 분류
            log.error("공공API(의약품 낱알식별정보) 응답 파싱 실패", e);
            throw new BusinessException(ErrorCode.PUBLIC_API_RESPONSE_ERROR);
        }

        String resultCode = root.path("header").path("resultCode").asText();
        if (StringUtils.hasText(resultCode) && !"00".equals(resultCode)) {
            // 연결은 됐지만 공공API 쪽이 에러코드를 내려준 경우 — 이것도 "연결 불안정" 쪽으로 묶어서 처리
            log.error("공공API(의약품 낱알식별정보) 오류 응답: {}", root.path("header").path("resultMsg").asText());
            throw new BusinessException(ErrorCode.PUBLIC_API_CONNECTION_ERROR);
        }

        JsonNode itemsNode = root.path("body").path("items");
        List<JsonNode> items = new ArrayList<>();
        if (itemsNode.isArray()) {
            itemsNode.forEach(items::add);
        } else if (itemsNode.isObject()) {
            JsonNode itemNode = itemsNode.path("item");
            if (itemNode.isArray()) {
                itemNode.forEach(items::add);
            } else if (itemNode.isObject()) {
                items.add(itemNode);
            } else if (itemsNode.has("ITEM_SEQ")) {
                items.add(itemsNode);
            }
        }
        return items;
    }

    private void upsertFromPublicApiItem(JsonNode item) {
        String itemSeq = textOrNull(item, "ITEM_SEQ");
        if (!StringUtils.hasText(itemSeq)) {
            return;
        }

        Medication medication = medicationRepository.findByItemSeq(itemSeq)
                .orElseGet(Medication::new);

        medication.setItemSeq(itemSeq);
        medication.setMedicationName(textOrNull(item, "ITEM_NAME"));
        medication.setItemEngName(textOrNull(item, "ITEM_ENG_NAME"));
        medication.setEntpName(textOrNull(item, "ENTP_NAME"));
        medication.setEtcOtcName(textOrNull(item, "ETC_OTC_NAME"));
        medication.setClassNo(textOrNull(item, "CLASS_NO"));
        medication.setClassName(textOrNull(item, "CLASS_NAME"));
        medication.setFormCodeName(textOrNull(item, "FORM_CODE_NAME"));
        medication.setChart(textOrNull(item, "CHART"));
        medication.setItemPermitDate(parseItemPermitDate(textOrNull(item, "ITEM_PERMIT_DATE")));
        medication.setEdiCode(textOrNull(item, "EDI_CODE"));
        medication.setStdCd(textOrNull(item, "STD_CD"));

        medicationRepository.save(medication);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    private LocalDate parseItemPermitDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, ITEM_PERMIT_DATE_FORMAT);
        } catch (Exception e) {
            log.warn("ITEM_PERMIT_DATE 파싱 실패: {}", value);
            return null;
        }
    }

    private MedicationDto toMedicationDto(Medication medication) {
        MedicationDto dto = new MedicationDto();
        dto.setMedicationId(medication.getMedicationId());
        dto.setMedicationName(medication.getMedicationName());
        dto.setItemSeq(medication.getItemSeq());
        dto.setItemEngName(medication.getItemEngName());
        dto.setEntpName(medication.getEntpName());
        dto.setEtcOtcName(medication.getEtcOtcName());
        dto.setClassNo(medication.getClassNo());
        dto.setClassName(medication.getClassName());
        dto.setFormCodeName(medication.getFormCodeName());
        dto.setChart(medication.getChart());
        dto.setItemPermitDate(medication.getItemPermitDate());
        dto.setEdiCode(medication.getEdiCode());
        dto.setStdCd(medication.getStdCd());
        return dto;
    }
}

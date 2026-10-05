package kr.co.seoulit.his.pharmacyservice.common.commoncode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * admin-service 공통코드를 서버 기동 시 전부 가져와 메모리에 들고 있는 로컬 캐시
 * (프로젝트 규칙: 서버 실행 시 공통코드 로컬캐시 적재).
 *
 * 적재 시점에 admin-service를 못 부르면(로컬 개발 환경에서 admin을 안 띄워둔 경우 등)
 * 캐시를 빈 상태로 두고 서버는 정상 기동한다 — 공통코드 적재 실패로 PHM 전체가 안 뜨면
 * 안 되기 때문. 이 캐시를 참조하는 쪽은 값이 없을 수 있다는 걸 감안해야 한다.
 *
 * 조회 전용이며, 기존에 짜둔 자바 enum 검증(DosageFormCode, RecipientType 등)을
 * 대체하는 게 아니다 — 그 enum들은 "등록된 값과 어긋나지 않는지 한 번 더 방어적으로
 * 검증"하는 용도로 이미 의도적으로 분리돼 있다(DosageFormCode.java 주석 참고). 이 캐시는
 * 코드값 -> 표시명 조회, 그리고 "서버가 공통코드를 알고 있어야 한다"는 규칙 자체를 만족시키는
 * 용도다.
 */
@Slf4j
@Component
public class CommonCodeCache {

    private static final String GROUP_LIST_PATH = "/api/admin/commonCodeGroup/list";
    private static final String ITEM_LIST_PATH = "/api/admin/commonCodeItem/list";
    private static final String USE_Y = "Y";

    private final RestClient restClient;

    /** groupCode -> (codeValue -> codeName). 교체는 통째로(캐시 적재 중엔 이전 값을 그대로 보여줌). */
    private volatile Map<String, Map<String, String>> codesByGroup = Map.of();

    public CommonCodeCache(@Value("${admin.api.base-url}") String adminApiBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(adminApiBaseUrl).build();
    }

    /** admin-service에서 전체 공통코드 그룹+항목을 다시 읽어와 캐시를 통째로 교체한다. */
    public void reload() {
        try {
            List<CommonCodeGroupDto> groups = fetchGroups();
            Map<String, Map<String, String>> next = new HashMap<>();
            for (CommonCodeGroupDto group : groups) {
                if (!USE_Y.equals(group.useYn())) {
                    continue;
                }
                Map<String, String> byValue = new LinkedHashMap<>();
                for (CommonCodeItemDto item : fetchItems(group.groupId())) {
                    if (USE_Y.equals(item.useYn())) {
                        byValue.put(item.codeValue(), item.codeName());
                    }
                }
                next.put(group.groupCode(), byValue);
            }
            this.codesByGroup = next;
            log.info("공통코드 캐시 적재 완료 — {}개 그룹", next.size());
        } catch (Exception e) {
            log.warn("공통코드 캐시 적재 실패(admin-service 연결 불가로 추정) — 캐시를 비워둔 채 계속합니다.", e);
        }
    }

    /** groupCode의 codeValue -> codeName 한 건. 캐시에 없으면(미등록/적재 실패) 빈 값. */
    public Optional<String> getCodeName(String groupCode, String codeValue) {
        return Optional.ofNullable(codesByGroup.getOrDefault(groupCode, Map.of()).get(codeValue));
    }

    /** groupCode에 등록된 전체 코드를 {value, label} 리스트로. 드롭다운 채울 때 그대로 쓴다. */
    public List<CommonCodeOption> getOptions(String groupCode) {
        return codesByGroup.getOrDefault(groupCode, Map.of()).entrySet().stream()
                .map(e -> new CommonCodeOption(e.getKey(), e.getValue()))
                .toList();
    }

    /** groupCode에 codeValue가 등록돼 있는지. 캐시가 비어 있으면(적재 실패) 항상 false라 주의. */
    public boolean contains(String groupCode, String codeValue) {
        return codesByGroup.getOrDefault(groupCode, Map.of()).containsKey(codeValue);
    }

    public boolean isLoaded() {
        return !codesByGroup.isEmpty();
    }

    private List<CommonCodeGroupDto> fetchGroups() {
        AdminApiEnvelope<List<CommonCodeGroupDto>> response = restClient.get()
                .uri(GROUP_LIST_PATH)
                .retrieve()
                .body(new ParameterizedTypeReference<AdminApiEnvelope<List<CommonCodeGroupDto>>>() {
                });
        return response == null || response.data() == null ? List.of() : response.data();
    }

    private List<CommonCodeItemDto> fetchItems(String groupId) {
        AdminApiEnvelope<List<CommonCodeItemDto>> response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path(ITEM_LIST_PATH).queryParam("groupId", groupId).build())
                .retrieve()
                .body(new ParameterizedTypeReference<AdminApiEnvelope<List<CommonCodeItemDto>>>() {
                });
        return response == null || response.data() == null ? List.of() : response.data();
    }
}

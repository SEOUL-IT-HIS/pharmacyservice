package kr.co.seoulit.his.pharmacyservice.common.commoncode;

import kr.co.seoulit.his.pharmacyservice.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 로컬캐시 운영용 엔드포인트.
 * reload는 프로젝트 규칙(서버 실행 시 적재)에는 없는 보너스 기능이다 — admin에 새 코드를
 * 등록했는데 PHM을 당장 재기동하기 어려운 경우(이번에 PHM_RECIPIENT_TYPE 등록할 때처럼)를
 * 위해 추가했다. 재기동 없이도 POST로 다시 적재할 수 있다.
 */
@RestController
@RequestMapping("/api/pharmacy/common-codes")
public class CommonCodeCacheController {

    private final CommonCodeCache commonCodeCache;

    public CommonCodeCacheController(CommonCodeCache commonCodeCache) {
        this.commonCodeCache = commonCodeCache;
    }

    @GetMapping
    public ApiResponse<List<CommonCodeOption>> list(@RequestParam String groupCode) {
        return ApiResponse.success(commonCodeCache.getOptions(groupCode));
    }

    @PostMapping("/reload")
    public ApiResponse<Map<String, Boolean>> reload() {
        commonCodeCache.reload();
        return ApiResponse.success(Map.of("loaded", commonCodeCache.isLoaded()));
    }
}

package kr.co.seoulit.his.pharmacyservice.common.commoncode;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 서버 기동 직후 공통코드를 한 번 적재한다(프로젝트 규칙: 서버 실행 시 공통코드 로컬캐시 적재). */
@Component
public class CommonCodeCacheInitializer implements ApplicationRunner {

    private final CommonCodeCache commonCodeCache;

    public CommonCodeCacheInitializer(CommonCodeCache commonCodeCache) {
        this.commonCodeCache = commonCodeCache;
    }

    @Override
    public void run(ApplicationArguments args) {
        commonCodeCache.reload();
    }
}

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 100,                 // 동시 100 가상 사용자
  duration: '3m',           // 3분간 지속
  tags: {
    project: 'garabu',
    env: 'docker-local',
  },
  thresholds: {             // 실패 임계치
    http_req_failed: ['rate<0.01'],            // 1% 미만 오류
    http_req_duration: ['p(95)<400'],          // 95%가 400ms 이하
  },
};

export default function () {
  const res = http.get(__ENV.TARGET_URL);      // 환경변수로 대상 URL 주입
  check(res, { 'status is 200': r => r.status === 200 });
  sleep(1);                                    // 사용자 think-time
}
/**

실행 방법

부하 시작 (백그라운드)
docker compose -f docker-compose.k6.yml up -d

진행 상태 실시간 확인
docker compose -f docker-compose.k6.yml logs -f k6

마무리
docker compose -f docker-compose.k6.yml down -v

*/
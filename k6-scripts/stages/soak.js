import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30m', target: 100 },  // 점진 ramp-up
    { duration: '11h', target: 200 },  // 안정 상태
    { duration: '30m', target: 0 },    // ramp-down
  ],
  thresholds: { http_req_failed: ['rate<0.01'] },
};
export default () => { http.get(__ENV.TARGET_URL); sleep(1); };
//12시간 soak + ramp-up
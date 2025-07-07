import http from 'k6/http';
import { sleep } from 'k6';
export const options = { vus: 5, duration: '30s' };
export default () => { http.get(__ENV.TARGET_URL); sleep(1); };
//30초만 빠르게 체크
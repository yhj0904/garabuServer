import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
  vus: 50, // 동시 사용자 수
  duration: '30s', // 테스트 시간
};

export default function () {
  http.get('http://localhost:8080//api/v2/book/mybooks');
  sleep(1);
}

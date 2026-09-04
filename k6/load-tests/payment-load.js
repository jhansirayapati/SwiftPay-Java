import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const baseUrl = __ENV.BASE_URL || 'http://localhost:3001';
const vus = Number(__ENV.VUS || 10);
const duration = __ENV.DURATION || '30s';
const targetTps = Number(__ENV.TARGET_TPS || 0);

export const options = {
	vus,
	duration,
	thresholds: {
		http_req_failed: ['rate<0.01'],
		http_req_duration: ['p(95)<500', 'p(99)<1000'],
		...(targetTps > 0 ? { http_reqs: [`rate>=${targetTps}`] } : {}),
	},
};

export default function () {
	const response = http.post(
		`${baseUrl}/v1/payments`,
		JSON.stringify({
			transaction_id: `txn_${uuidv4()}`,
			sender_id: 'user_001',
			receiver_id: 'user_002',
			amount: '1.00',
			currency: 'INR',
		}),
		{ headers: { 'Content-Type': 'application/json' } },
	);

	check(response, {
		'payment accepted': (result) => result.status === 202,
	});
}
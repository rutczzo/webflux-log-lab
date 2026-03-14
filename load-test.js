import http from 'k6/http';

export const options = {
    vus: 100,
    duration: '20s',
};

export default function () {
    http.get('http://43.200.255.156:8080/test');
}
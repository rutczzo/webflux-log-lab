import http from 'k6/http';

const url = 'Your IP'

export const options = {
    vus: 100,
    duration: '20s',
};

export default function () {
    http.get('http://${url}:8080/test');
}

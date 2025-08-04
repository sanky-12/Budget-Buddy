import axios from 'axios';

// Create Axios instance for Budget Service
const budgetApi = axios.create({
  baseURL: 'http://localhost:8084', // Change this if your budget service runs on another port
});

// Attach token automatically
budgetApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle 401 errors globally
budgetApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      alert('Session expired. Please login again.');
      localStorage.removeItem('token');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default budgetApi;

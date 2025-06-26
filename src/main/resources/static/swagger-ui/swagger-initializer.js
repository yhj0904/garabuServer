<!-- src/main/resources/static/swagger-initializer.js -->
window.onload = () => {
  window.ui = SwaggerUIBundle({
    // disable-swagger-default-url: true 로 껐으니 직접 지정
    url: "/v3/api-docs",
    dom_id: '#swagger-ui',
    requestInterceptor: (req) => {
      const token = localStorage.getItem('swagger_access_token');
      if (token) {
        req.headers['Authorization'] = 'Bearer ' + token;
      }
      return req;
    },
    presets: [SwaggerUIBundle.presets.apis],
  });
};

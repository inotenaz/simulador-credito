package com.hackathon2025.simulador_credito.interceptor;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.hackathon2025.simulador_credito.model.Telemetria;
import com.hackathon2025.simulador_credito.repository.TelemetriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TelemetriaInterceptor implements HandlerInterceptor {

    @Autowired
    private TelemetriaRepository telemetriaRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        request.setAttribute("endpoint", request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        String endpoint = (String) request.getAttribute("endpoint");
        int status = response.getStatus();

        Telemetria t = new Telemetria();
        t.setNomeApi(endpoint);
        t.setTempoExecucao(duration);
        t.setDataReferencia(LocalDate.now());
        t.setStatusHttp(status);

        telemetriaRepository.save(t);
    }
}

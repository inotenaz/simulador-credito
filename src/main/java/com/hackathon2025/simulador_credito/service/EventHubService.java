package com.hackathon2025.simulador_credito.service;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Service
public class EventHubService {

    private final EventHubProducerClient producerClient;

    public EventHubService() {
        String connectionString = "Endpoint=sb://eventhack.servicebus.windows.net/;SharedAccessKeyName=hack;SharedAccessKey=HeHeVaVqyVkntO2FnjQcs2Ilh/4MUDo4y+AEhKp8z+g=;EntityPath=simulacoes"; 

        this.producerClient = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildProducerClient();
    }

    public void sendMessage(String message) {
        try {
            EventData eventData = new EventData(message.getBytes(StandardCharsets.UTF_8));
            producerClient.send(Collections.singleton(eventData));
            System.out.println("Mensagem enviada para o Event Hub.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


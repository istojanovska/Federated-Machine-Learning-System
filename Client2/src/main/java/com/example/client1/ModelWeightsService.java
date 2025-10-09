package com.example.client1;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ModelWeightsService {
    private final String pythonUrl = "http://localhost:8003/train-local-model";
    private final WebClient webClient;
    private RabbitTemplate rabbitTemplate;
    private final String clientId;


    public ModelWeightsService(RabbitTemplate rabbitTemplate, @Value("${spring.application.name}") String clientId) {
        this.webClient = WebClient.builder().build();
        this.rabbitTemplate = rabbitTemplate;
        this.clientId = clientId;
    }

    @RabbitListener(queues = "aggregated_weights_diabetes_queue_2")
    public void receiveDiabetesModel(AggregatedModel model) {
        recieveAggregatedWeights(model);
    }

    @RabbitListener(queues = "aggregated_weights_anemia_queue_2")
    public void receiveAnemiaModel(AggregatedModel model) {
        recieveAggregatedWeights(model);
    }

    @RabbitListener(queues = "aggregated_weights_heart_attack_queue_2")
    public void receiveHeartAttackModel(AggregatedModel model) {
        recieveAggregatedWeights(model);
    }



    public void recieveAggregatedWeights(AggregatedModel aggregatedModel) {
        System.out.println("[" + clientId + "] Recieved initial model: " + aggregatedModel);

        ModelWeights report = new ModelWeights();
        report.setCoef(aggregatedModel.getCoef());
        report.setIntercept(aggregatedModel.getIntercept());
        report.setModelTypeId(aggregatedModel.getModelType().getCode());
        report.setRound(aggregatedModel.getRound());

        report = webClient.post()
                .uri(pythonUrl)
                .bodyValue(report)
                .retrieve()
                .bodyToMono(ModelWeights.class)
                .block();
        System.out.println("Received coef: " + report.getCoef());
        System.out.println("Recieved intercept: " + report.getIntercept());
        System.out.println("Accuracy:" + report.getAccuracy());

        report.setModelType(aggregatedModel.getModelType());

        sendTrainedModelToFlServer(report);
    }


    public void sendTrainedModelToFlServer(ModelWeights trainedModel) {
        trainedModel.setClientId(clientId);
        String queueName = switch (trainedModel.getModelTypeId()){
            case 0 -> "received_weights_diabetes_queue_2";
            case 1 -> "received_weights_anemia_queue_2";
            case 2 -> "received_weights_heart_attack_queue_2";
            default -> throw new IllegalArgumentException("Unknown model type");
        };
        rabbitTemplate.convertAndSend(queueName, trainedModel);
        System.out.println("Updated model sent to FL server");
    }
}

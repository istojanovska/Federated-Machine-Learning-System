package com.example.flserver;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FLService {
    private final AggregatedModelRepository aggregatedModelRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, List<ModelWeights>> updatesByRoundAndType = new HashMap<>();
    private final Map<String, Long> waitingStartTimes = new HashMap<>();
    private final long WAIT_LIMIT_MS = 5 * 60 * 1000;
    int expectedClients = 2;
    private Queue<ModelType> pendingBroadcasts = new LinkedList<>();
    private boolean broadcastInProgress = false;


    public FLService(AggregatedModelRepository aggregatedModelRepository, RabbitTemplate rabbitTemplate) {
        this.aggregatedModelRepository = aggregatedModelRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void saveModelWeights(AggregatedModel aggregatedModel){
        aggregatedModelRepository.save(aggregatedModel);
    }


    public AggregatedModel initialize(int numFeatures, ModelType modelType){
        List<Double> initWeights = new ArrayList<>(Collections.nCopies(numFeatures, 0.0));
        double initBias = 0.0;

        AggregatedModel aggregatedModel = new AggregatedModel();
        aggregatedModel.setCoef(Collections.singletonList(initWeights));
        aggregatedModel.setIntercept(Collections.singletonList(initBias));
        aggregatedModel.setModelType(modelType);

        saveModelWeights(aggregatedModel);
        return aggregatedModel;
    }

    public AggregatedModel fetchOrInitializeModel(int numFeatures, ModelType modelType){
        AggregatedModel lastSavedModel = aggregatedModelRepository.findTopByModelTypeOrderByTimestampDesc(modelType);
        if(lastSavedModel != null){
            System.out.println("Found existing model in DB. Sending the last saved model.");
            return lastSavedModel;
        }
        else{
            System.out.println("No model found in DB. Initializing with zeros.");
            return initialize(numFeatures, modelType);
        }
    }

    public void broadCastInitialModel(AggregatedModel aggregatedModel, ModelType modelType){
        String routingKey = switch (modelType){
            case ANEMIA -> "anemia";
            case DIABETES -> "diabetes";
            case HEART_ATTACK -> "heart_attack";
        };
        rabbitTemplate.convertAndSend("aggregated_weights_exchange", routingKey, aggregatedModel);
        System.out.println("Broadcast initial model to clients.");
    }

    public void initializeAndSendToEdge() {
        ModelType[] modelTypes = ModelType.values();

        for (ModelType modelType : modelTypes) {
            int numFeatures = modelType.getNumFeatures();

            AggregatedModel latest = aggregatedModelRepository.findTopByModelTypeOrderByRoundDesc(modelType);
            int round = (latest != null) ? latest.getRound() : 0;

            AggregatedModel initModel = fetchOrInitializeModel(numFeatures, modelType);
            initModel.setModelType(modelType);
            initModel.setRound(round);

            broadCastInitialModel(initModel, modelType);
        }
    }


    @RabbitListener(queues = "received_weights_diabetes_queue")
    public void receiveDiabetesWeights(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }

    @RabbitListener(queues = "received_weights_anemia_queue")
    public void receiveAnemiaWeights(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }

    @RabbitListener(queues = "received_weights_heart_attack_queue")
    public void receiveHeartAttackWeights(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }


    @RabbitListener(queues = "received_weights_diabetes_queue_2")
    public void receiveDiabetesWeights2(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }

    @RabbitListener(queues = "received_weights_anemia_queue_2")
    public void receiveAnemiaWeights2(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }

    @RabbitListener(queues = "received_weights_heart_attack_queue_2")
    public void receiveHeartAttackWeights2(ModelWeights modelWeights){
        recieveClientUpdate(modelWeights);
    }


    public void recieveClientUpdate(ModelWeights modelWeights){
            System.out.println("Received updated weights from client: " + modelWeights.getClientId());

            // Use the round from the client
            int round = modelWeights.getRound();
            String key = modelWeights.getModelType().name() + "-" + round;

            synchronized (updatesByRoundAndType) {
                List<ModelWeights> currentList = updatesByRoundAndType.computeIfAbsent(key, k -> new ArrayList<>());

                boolean alreadySent = currentList.stream()
                        .anyMatch(m -> m.getClientId().equals(modelWeights.getClientId()));
                if (alreadySent) {
                    System.out.println("Duplicate update from " + modelWeights.getClientId() + " for round " + round);
                    return;
                }

                currentList.add(modelWeights);

                waitingStartTimes.putIfAbsent(key, System.currentTimeMillis());

                if (currentList.size() == expectedClients) {
                    System.out.println("All updates for " + key + " received. Aggregating...");
                    List<ModelWeights> updates = updatesByRoundAndType.remove(key);
//                    aggregateList(updates, round);
                    waitingStartTimes.remove(key);
                      aggregateList(updates);
                } else {
                    System.out.println("Waiting for more updates for " + key + ": " + currentList.size() + "/" + expectedClients);
                }
            }

    }



    private void aggregateList(List<ModelWeights> updates) {
        if (updates.isEmpty()) return;

        ModelType modelType = updates.get(0).getModelType();

        AggregatedModel latest = aggregatedModelRepository.findTopByModelTypeOrderByRoundDesc(modelType);
        int currentRound = (latest != null) ? latest.getRound() : -1;

        int numFeatures = updates.get(0).getCoef().get(0).size();
        List<List<Double>> aggregatedCoef = new ArrayList<>();
        List<Double> aggregatedIntercept = new ArrayList<>();

        for (int i = 0; i < updates.get(0).getCoef().size(); i++) {
            List<Double> coefRow = new ArrayList<>(Collections.nCopies(numFeatures, 0.0));
            for (ModelWeights update : updates) {
                List<Double> clientRow = update.getCoef().get(i);
                for (int j = 0; j < clientRow.size(); j++) {
                    coefRow.set(j, coefRow.get(j) + clientRow.get(j));
                }
            }
            for (int j = 0; j < coefRow.size(); j++) {
                coefRow.set(j, coefRow.get(j) / updates.size());
            }
            aggregatedCoef.add(coefRow);
        }

        int interceptSize = updates.get(0).getIntercept().size();
        for (int i = 0; i < interceptSize; i++) {
            double sum = 0.0;
            for (ModelWeights update : updates) {
                sum += update.getIntercept().get(i);
            }
            aggregatedIntercept.add(sum / updates.size());
        }

        AggregatedModel newModel = new AggregatedModel();
        newModel.setCoef(aggregatedCoef);
        newModel.setIntercept(aggregatedIntercept);
        newModel.setRound(currentRound + 1);
        newModel.setModelType(modelType);
        newModel.setClientId("aggregated");

        saveModelWeights(newModel);
        System.out.println("Aggregated model for round " + ( currentRound + 1) + " of type " + modelType);
    }


}

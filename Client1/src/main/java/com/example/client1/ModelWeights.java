package com.example.client1;

import jakarta.persistence.Convert;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class ModelWeights {
    public List<List<Double>> coef;

    public List<Double> intercept;

    public double accuracy;

    @Convert(converter = ModelTypeConverter.class)
    public ModelType modelType;

    public int modelTypeId;

    public int round;

    @Value("${spring.application.name}")
    public String clientId;

    public ModelWeights() {
    }

    public List<List<Double>> getCoef() {
        return coef;
    }

    public void setCoef(List<List<Double>> coef) {
        this.coef = coef;
    }

    public List<Double> getIntercept() {
        return intercept;
    }

    public void setIntercept(List<Double> intercept) {
        this.intercept = intercept;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(int modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}

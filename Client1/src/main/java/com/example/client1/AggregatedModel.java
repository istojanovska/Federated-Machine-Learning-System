package com.example.client1;

import jakarta.persistence.Convert;

import java.util.List;

public class AggregatedModel {
    public List<List<Double>> coef;
    public List<Double> intercept;

    @Convert(converter = ModelTypeConverter.class)
    public ModelType modelType;

    public int round;

    public String clientId;

    public AggregatedModel() {
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

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

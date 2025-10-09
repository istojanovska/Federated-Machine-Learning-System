package com.example.flserver;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;



@Entity
@Table(name = "aggregated_model_entries")
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long Id;

        @Convert(converter = ModelTypeConverter.class)
        public ModelType modelType;

        @Type(JsonBinaryType.class)
        @Column(columnDefinition = "jsonb")
        public List<List<Double>> coef;

        @Type(JsonBinaryType.class)
        @Column(columnDefinition = "jsonb")
        public List<Double> intercept;

        public int round;

        @Transient
        public String clientId;

        public List<List<Double>> getCoef() {
            return coef;
        }


        @CreationTimestamp
        @Column(name = "timestamp", updatable = false)
        private LocalDateTime timestamp;

        public void setCoef(List<List<Double>> coef) {
            this.coef = coef;
        }

        public List<Double> getIntercept() {
            return intercept;
        }

        public void setIntercept(List<Double> intercept) {
            this.intercept = intercept;
        }

        public Long getId() {
            return Id;
        }

        public void setId(Long id) {
            Id = id;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
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

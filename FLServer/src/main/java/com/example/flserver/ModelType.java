package com.example.flserver;

public enum ModelType {
    DIABETES(0, 8),
    ANEMIA(1, 5),
    HEART_ATTACK(2, 19);

    private final int code;

    public int numFeatures;

    ModelType(int code, int numFeatures){
        this.code = code;
        this.numFeatures = numFeatures;
    }
    public int getCode(){
        return code;
    }

    public int getNumFeatures(){ return numFeatures; }

    public static ModelType fromCode(int code) {
        for (ModelType type : ModelType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ModelType code: " + code);
    }
}

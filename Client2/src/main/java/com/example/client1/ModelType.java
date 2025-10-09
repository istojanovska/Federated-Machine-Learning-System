package com.example.client1;

public enum ModelType {
    DIABETES(0),
    ANEMIA(1),
    HEART_ATTACK(2);

    private final int code;


    ModelType(int code){
        this.code = code;
    }
    public int getCode(){
        return code;
    }


    public static ModelType fromCode(int code) {
        for (ModelType type : ModelType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ModelType code: " + code);
    }
}

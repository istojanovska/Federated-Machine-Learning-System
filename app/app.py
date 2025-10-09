from fastapi import FastAPI
from pydantic import BaseModel
import os
import pandas as pd
import numpy as np
from sklearn.linear_model import SGDClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import asyncio
from concurrent.futures import ThreadPoolExecutor


app = FastAPI()
executor = ThreadPoolExecutor(max_workers=3)

BASE_PATH = r"C:\Users\User\Downloads\FederatedLearningParalel\app\datasets"

class ModelWeights(BaseModel):
    coef: list[list[float]]
    intercept: list[float]
    accuracy: float = 0.0
    modelTypeId: int
    round: int

def blocking_train(globalWeights: ModelWeights) -> ModelWeights:
    if globalWeights.modelTypeId == 0:
        CSV_PATH = os.path.join(BASE_PATH, "diabetes.csv")
        FEATURES = ['Pregnancies', 'Glucose', 'BloodPressure', 'SkinThickness', 'Insulin', 'BMI', 'DiabetesPedigreeFunction', 'Age']
        TARGET = 'Outcome'

    elif globalWeights.modelTypeId == 1:
        CSV_PATH = os.path.join(BASE_PATH, "anemia.csv")
        FEATURES = ['Gender', 'Hemoglobin', 'MCH', 'MCHC', 'MCV']
        TARGET = 'Result'

    elif globalWeights.modelTypeId == 2:
        CSV_PATH = os.path.join(BASE_PATH, "heart_attack.csv")
        FEATURES = ['Age', 'Sex', 'Cholesterol', 'Heart Rate', 'Diabetes', 'Family History',
                    'Smoking', 'Obesity', 'Alcohol Consumption', 'Exercise Hours Per Week', 'Previous Heart Problems',
                    'Medication Use', 'Stress Level', 'Sedentary Hours Per Day', 'Income', 'BMI', 'Triglycerides',
                    'Physical Activity Days Per Week', 'Sleep Hours Per Day']
        TARGET = 'Heart Attack Risk'

    if not os.path.exists(CSV_PATH):
        raise FileNotFoundError(f"Dataset not found at {CSV_PATH}")

    df = pd.read_csv(CSV_PATH)
    #print(f"Loading dataset from: {CSV_PATH}")
    #print(df.head())


    if globalWeights.modelTypeId == 2:
        df['Sex'] = df['Sex'].map({'Male': 1, 'Female': 0})


    round_count = 5  # or any number you want to split the data into
    round_index = max(0, min(globalWeights.round, round_count - 1))
    data_splits = np.array_split(df, round_count)
    df_round = data_splits[round_index]
    print(f"Using round {round_index} of {round_count}, with {len(df_round)} records")

    X = df_round[FEATURES]
    y = df_round[TARGET]

    #X = df[FEATURES]
    #y = df[TARGET]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=42)

    model = SGDClassifier(loss='log_loss', max_iter=1, warm_start=True, random_state=42)
    model.coef_ = np.array(globalWeights.coef)
    model.intercept_ = np.array(globalWeights.intercept)
    model.classes_ = np.unique(y)
    model.fit(X_train, y_train)

    coef = model.coef_.tolist()
    intercept = model.intercept_.tolist()
    preds = model.predict(X_test)
    accuracy = accuracy_score(y_test, preds)

    globalWeights.coef = coef
    globalWeights.intercept = intercept
    globalWeights.accuracy = accuracy
    return globalWeights


@app.post("/train-local-model")
async def train_local_model(globalWeights: ModelWeights):
    loop = asyncio.get_running_loop()
    result = await loop.run_in_executor(executor, blocking_train, globalWeights)
    return result

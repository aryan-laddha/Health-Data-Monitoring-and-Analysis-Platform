from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
from flask_cors import CORS
import numpy as np

model = load_model('heart.h5')

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/')
def home():
    return 'Server is running.'

@app.route('/predict', methods=['POST'])
def predict():
    if request.method == 'POST':
        data = request.json  # Get JSON data from request

        # Extract features from JSON data
        age = float(data['age'])
        sex = float(data['sex'])
        cp = float(data['cp'])
        trestbps = float(data['trestbps'])
        chol = float(data['chol'])
        fbs = float(data['fbs'])
        restecg = float(data['restecg'])
        thalach = float(data['thalach'])
        exang = float(data['exang'])
        oldpeak = float(data['oldpeak'])
        slope = float(data['slope'])
        ca = float(data['ca'])
        thal = float(data['thal'])

        # Make prediction
        input_data = np.array([[age, sex, cp, trestbps, chol, fbs, restecg, thalach, exang, oldpeak, slope, ca, thal]])
        prediction = model.predict(input_data).tolist()  # Convert prediction to list

        # Set prediction text
        prediction_text = 'Severe' if prediction[0][0] > 0.45 else 'Normal'

        # Return prediction as JSON response
        return jsonify({'prediction': prediction_text})

if __name__ == '__main__':
    app.run(debug=True)

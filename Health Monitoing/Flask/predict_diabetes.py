class diabetes_predictor:
    def __init__(self, attributes):
        self.attributes = attributes

    def prediction(self):
        # Load model
        with open('model.pkl', 'rb') as f:
            model = pickle.load(f)

        attributes = self.attributes

        result = model.predict(attributes)
        print(result)
        if result > 0.5:  # Adjust threshold as needed
            prediction = 'Positive'
            print(prediction)
        else:
            prediction = 'Negative'
            print(prediction)

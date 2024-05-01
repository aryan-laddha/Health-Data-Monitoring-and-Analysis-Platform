import React, { useState, useEffect } from 'react';
import { ref, get, set, getDatabase } from 'firebase/database';
import './MiddleComponent.css'; // Import custom CSS for styling
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { InfluxDB } from '@influxdata/influxdb-client';
import axios from 'axios'; // Import Axios for making HTTP requests

const chestPainOptions = {
  0: 'Typical Angina',
  1: 'Atypical Angina',
  2: 'Non-Anginal Pain',
  3: 'Asymptomatic'
};

const restecgOptions = {
  0: 'Normal',
  1: 'Having ST-T wave abnormality',
  2: 'Showing probable or definite left ventricular hypertrophy by Estes criteria'
};

const oldpeakOptions = {
  1: '1',
  2: '2',
  3: '3',
  4: '4'
};

const exangOptions = {
  0: 'False',
  1: 'True'
};

const slopeOptions = {
  0: 'Upsloping',
  1: 'Flat',
  2: 'Downsloping'
};

const caOptions = {
  0: '0',
  1: '1',
  2: '2',
  3: '3'
};

const thalOptions = {
  0: 'Normal',
  1: 'Fixed defect',
  2: 'Reversible defect'
};

const MiddleComponent = ({ userId }) => {
  const [userData, setUserData] = useState(null);
  const [editMode, setEditMode] = useState(false); // State to track edit mode
  const [bloodGroup, setBloodGroup] = useState('');
  const [height, setHeight] = useState('');
  const [weight, setWeight] = useState('');
  const [fbs, setFbs] = useState('');
  const [bps, setBps] = useState('');
  const [cholesterol, setCholesterol] = useState('');
  const [exerciseInducedAngina, setExerciseInducedAngina] = useState('');
  const [chestPain, setChestPain] = useState('');
  const [restecg, setRestecg] = useState('');
  const [oldpeak, setOldpeak] = useState('');
  const [slope, setSlope] = useState('');
  const [ca, setCa] = useState('');
  const [heartRate, setHeartRate] = useState(null);
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('');
  const [thal, setThal] = useState('');
  const [prediction, setPrediction] = useState(null); // State to store prediction result
  const [pregnancies, setPregnancies] = useState('');
  const [skinThickness, setSkinThickness] = useState('');
  const [insulin, setInsulin] = useState('');
  const [diabetesPedigreeFunction, setDiabetesPedigreeFunction] = useState('');
  const [bmi, setBMI] = useState(null);



  useEffect(() => {
    const fetchData = async () => {
      const db = getDatabase();
      try {
        const snapshot = await get(ref(db, `Users/${userId}`));
        if (snapshot.exists()) {
          const userData = snapshot.val();
          setUserData(userData);
          setAge(userData.Age || '');
          setGender(userData.Gender || '');
          setBloodGroup(userData['Blood Group'] || ''); // Ensure BloodGroup is set properly
          setHeight(userData.Height || '');
          setWeight(userData.Weight || '');
          setFbs(userData.FBS || '');
          setBps(userData.BPS || '');
          setCholesterol(userData.Cholesterol || '');
          setExerciseInducedAngina(userData['Exercise Induced Angina (exang)'] || '');
          setChestPain(userData['Chest Pain'] || '');
          setRestecg(userData.restecg || '');
          setOldpeak(userData.oldpeak || '');
          setSlope(userData.slope || '');
          setCa(userData.ca || '');
          setThal(userData.Thal || '');
          setPregnancies(userData.Pregnancies || '');
          setSkinThickness(userData.SkinThickness || '');
          setInsulin(userData.Insulin || '');
          setDiabetesPedigreeFunction(userData.DiabetesPedigreeFunction || '');
          const heightInMeter = userData.Height / 100; // Convert height from cm to meters
          const weightInKg = userData.Weight;
          const bmiValue = weightInKg / (heightInMeter * heightInMeter);
          setBMI(bmiValue);


          const jsonData = {
            age: userData.Age || '',
            gender: userData.Gender === 'Male' ? 1 : 0,
            cp: userData['Chest Pain'] || '',
            trestbps: userData.BPS || '',
            chol: userData.Cholesterol || '',
            fbs: userData.FBS || '',
            restecg: userData.restecg || '',
            thalach: userData.heartRate || '',
            exang: userData['Exercise Induced Angina (exang)'] || '',
            oldpeak: userData.oldpeak || '',
            slope: userData.slope || '',
            ca: userData.ca || '',
            thal: userData.Thal || ''
          };

          const jsonDataDiab = {

            Pregnancies: parseInt(userData.Pregnancies ||''),
            fbs: parseInt(userData.FBS ||''),
            trestbps: parseInt(userData.BPS ||''),
            SkinThickness: parseInt(userData.SkinThickness ||''),
            Insulin: parseInt(userData.Insulin ||''),
            BMI: parseFloat(bmiValue),
            DiabetesPedigreeFunction: parseFloat(userData.DiabetesPedigreeFunction ||''),
            age: parseInt(userData.Age ||''),
          };

          // Log JSON data
          console.log(jsonData);
              // Log JSON data
            console.log(jsonDataDiab);
        } else {
          console.log("No data available for this user");
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();

    // Fetch heart rate data from InfluxDB
    const fetchHeartRate = async () => {
      const token = 'CM0ViQjV3oRXdhtAUcPT1Mzzok0cgJw7tAXhicqxGTx7VjTC-cZk9FLfLTIF0B1S-po3xhLYYHIFnunyo1kEkQ==';
      const org = 'Bharati Vidyapeeth College of Engineering';
      const bucket = 'heart_database';

      const client = new InfluxDB({
        url: 'http://127.0.0.1:8086',
        token: token,
      });

      const queryApi = client.getQueryApi(org);

      const endDate = new Date(); // Today's date
      const startDate = new Date();
      startDate.setDate(startDate.getDate() - 30); // Subtract 30 days from today

      // Format dates as ISO strings
      const endISOString = endDate.toISOString();
      const startISOString = startDate.toISOString();

      const fluxQuery = `
        from(bucket: "${bucket}")
        |> range(start: ${startISOString}, stop: ${endISOString})
        |> filter(fn: (r) => r["_measurement"] == "heartRate")
        |> filter(fn: (r) => r["_field"] == "rate")
        |> filter(fn: (r) => r["userId"] == "${userId}")
        |> last()
        |> yield(name: "mean")
      `;
      const result = await queryApi.collectRows(fluxQuery);
      const heartRateValue = result[0]?.['_value'];

      setHeartRate(heartRateValue);
    };

    fetchHeartRate();

    // Clean up function to remove listeners or subscriptions if any
    return () => {
      // Cleanup code goes here if needed
    };
  }, [userId]);

  const handleUpdate = async () => {
    const db = getDatabase();
    try {
      await set(ref(db, `Users/${userId}`), {
        ...userData,
        'Blood Group': bloodGroup,
        Height: height,
        Weight: weight,
        FBS: fbs,
        BPS: bps,
        Cholesterol: cholesterol,
        'Exercise Induced Angina (exang)': exerciseInducedAngina,
        'Chest Pain': chestPain,
        restecg,
        oldpeak,
        slope,
        ca,
        'Thal': thal,
        Pregnancies: pregnancies,
        SkinThickness: skinThickness,
        Insulin: insulin,
        DiabetesPedigreeFunction: diabetesPedigreeFunction
      });
      toast.success("Data updated successfully");
      setEditMode(false); // Exit edit mode after updating
    } catch (error) {
      console.error("Error updating data:", error);
      toast.error("Failed to update data");
    }
  };

  const handlePrediction = async () => {
    const jsonData = {
      age: parseInt(age),
      sex: gender === 'Male' ? 1 : 0,
      cp: parseInt(chestPain),
      trestbps: parseInt(bps),
      chol: parseInt(cholesterol),
      fbs: parseInt(fbs),
      restecg: parseInt(restecg),
      thalach: parseInt(heartRate),
      exang: parseInt(exerciseInducedAngina),
      oldpeak: parseFloat(oldpeak),
      slope: parseInt(slope),
      ca: parseInt(ca),
      thal: parseInt(thal)
    };

    try {
      const response = await axios.post('http://localhost:5000/predict', jsonData); // Change the URL accordingly
      const prediction = response.data.prediction;
      setPrediction(prediction);
    } catch (error) {
      console.error('Error predicting:', error);
      toast.error('Failed to make prediction');
    }
  };

  return (
    <div>
      <div className="middle-component">
        <div className="patient-info-header">
          <h2>Patient Information</h2>
          {editMode ? <button onClick={handleUpdate}>Save</button> : <button onClick={() => setEditMode(true)}>Edit</button>}
        </div>
        <div className="patient-info">
          <p><strong>Heart Rate:</strong> {heartRate !== null ? heartRate : 'Loading...'}</p>
          <p><strong>Blood Group:</strong> {editMode ? <input type="text" value={bloodGroup} onChange={(e) => setBloodGroup(e.target.value)} /> : bloodGroup}</p>
          <p><strong>Height:</strong> {editMode ? <input type="text" value={height} onChange={(e) => setHeight(e.target.value)} /> : height}</p>
          <p><strong>Weight:</strong> {editMode ? <input type="text" value={weight} onChange={(e) => setWeight(e.target.value)} /> : weight}</p>
          <p><strong>FBS:</strong> {editMode ? <input type="text" value={fbs} onChange={(e) => setFbs(e.target.value)} /> : fbs}</p>
          <p><strong>BPS:</strong> {editMode ? <input type="text" value={bps} onChange={(e) => setBps(e.target.value)} /> : bps}</p>
          <p><strong>Cholesterol:</strong> {editMode ? <input type="text" value={cholesterol} onChange={(e) => setCholesterol(e.target.value)} /> : cholesterol}</p>
          <p><strong>Exercise Induced Angina: </strong>
            {editMode ?
              <select value={exerciseInducedAngina} onChange={(e) => setExerciseInducedAngina(e.target.value)}>
                {Object.entries(exangOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              exangOptions[exerciseInducedAngina]}
          </p>
          <p><strong>Chest Pain: </strong>
            {editMode ?
              <select value={chestPain} onChange={(e) => setChestPain(e.target.value)}>
                {Object.entries(chestPainOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              chestPainOptions[chestPain]}
          </p>
          <p><strong>Resting ECG: </strong>
            {editMode ?
              <select value={restecg} onChange={(e) => setRestecg(e.target.value)}>
                {Object.entries(restecgOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              restecgOptions[restecg]}
          </p>
          <p><strong>Oldpeak: </strong>
            {editMode ?
              <select value={oldpeak} onChange={(e) => setOldpeak(e.target.value)}>
                {Object.entries(oldpeakOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              oldpeak}
          </p>
          <p><strong>Slope: </strong>
            {editMode ?
              <select value={slope} onChange={(e) => setSlope(e.target.value)}>
                {Object.entries(slopeOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              slopeOptions[slope]}
          </p>
          <p><strong>Number of major vessels colored by fluoroscopy: </strong>
            {editMode ?
              <select value={ca} onChange={(e) => setCa(e.target.value)}>
                {Object.entries(caOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              caOptions[ca]}
          </p>
          <p><strong>Thal: </strong>
            {editMode ?
              <select value={thal} onChange={(e) => setThal(e.target.value)}>
                {Object.entries(thalOptions).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
              :
              thalOptions[thal]}
          </p>
          {/* Display the new fields here */}
        <p><strong>Pregnancies:</strong> {editMode ? <input type="text" value={pregnancies} onChange={(e) => setPregnancies(e.target.value)} /> : pregnancies}</p>
        <p><strong>Skin Thickness:</strong> {editMode ? <input type="text" value={skinThickness} onChange={(e) => setSkinThickness(e.target.value)} /> : skinThickness}</p>
        <p><strong>Insulin:</strong> {editMode ? <input type="text" value={insulin} onChange={(e) => setInsulin(e.target.value)} /> : insulin}</p>
        <p><strong>Diabetes Pedigree Function:</strong> {editMode ? <input type="text" value={diabetesPedigreeFunction} onChange={(e) => setDiabetesPedigreeFunction(e.target.value)} /> : diabetesPedigreeFunction}</p>
        <p><strong>BMI:</strong> {editMode ? <input type="text" value={bmi} onChange={(e) => setBMI(e.target.value)} /> : bmi}</p>
        </div>
        <div className="prediction-container">
          <h2>Prediction</h2>
          {prediction !== null ? (
            <p><strong>Heart abnormality:</strong> {prediction}</p>
          ) : (
            <button onClick={handlePrediction}>Predict</button>
          )}
        </div>
      </div>

      <ToastContainer /> {/* To render toast notifications */}
    </div>
  );
};

export default MiddleComponent;

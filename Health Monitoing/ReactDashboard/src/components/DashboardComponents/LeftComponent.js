import React, { useState, useEffect } from 'react';
import { getAuth, signOut } from 'firebase/auth'; 
import { ref, get, getDatabase, update } from 'firebase/database';
import './LeftComponent.css'; // Import custom CSS for styling
import { database } from '../../firebase';
import { toast } from 'react-toastify'; // Import toast

const handleSignOut = () => {
  console.log("Signing out...");
  const auth = getAuth(); // Retrieve the auth object
  signOut(auth)
    .then(() => {
      // Sign-out successful.
      console.log("User signed out successfully.");
      // Replace current URL with the sign-up page URL
      window.location.replace('http://localhost:3001/');
    })
    .catch((error) => {
      // An error happened.
      console.error("Error signing out:", error);
    });
};
const LeftComponent = ({ firebase, userId }) => {
  const [userData, setUserData] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [diagnosisName, setDiagnosisName] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      const db = getDatabase(firebase);
      try {
        const snapshot = await get(ref(db, `Users/${userId}`));
        if (snapshot.exists()) {
          setUserData(snapshot.val());
        } else {
          console.log("No data available for this user");
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();

    // Clean up function to remove listeners or subscriptions if any
    return () => {
      // Cleanup code goes here if needed
    };
  }, [firebase, userId]);

  const handleFormToggle = () => {
    setShowForm(!showForm);
  };

  const addDiagnosis = async () => {
    const userDiagnosisRef = ref(database, `Users/${userId}/Diagnosis`);
    const snapshot = await get(userDiagnosisRef);
    const diagnosisIndex = snapshot.exists() ? Object.keys(snapshot.val()).length + 1 : 1;
    const currentDate = new Date().toISOString().slice(0, 10);
    const newDiagnosis = {
      Date: currentDate,
      Description: diagnosisName
    };

    try {
      await update(ref(database, `Users/${userId}/Diagnosis/Diagnosis${diagnosisIndex}`), newDiagnosis);
      toast.success("Diagnosis added successfully!");
      setShowForm(false);
      setDiagnosisName('');
      
      // Fetch the updated user data after adding diagnosis
      const updatedSnapshot = await get(ref(database, `Users/${userId}`));
      if (updatedSnapshot.exists()) {
        setUserData(updatedSnapshot.val());
      }
    } catch (error) {
      console.error("Error adding diagnosis: ", error);
      toast.error("Failed to add diagnosis");
    }
  };

  return (
    <div className="left-component">
      <button className="sign-out-button" onClick={handleSignOut}>Sign Out</button>
      {userData ? (
        <div className="user-info">
          <p className="name">{userData.Name || 'Unknown'}</p>
          <p className="info">{userData.Age || 'Unknown'}, {userData.Gender || 'Unknown'}</p>
          <p className="userid">{userId}</p>
          <div className="diagnosis">
            <h2>Diagnosis <button className="add-diagnosis-button" onClick={handleFormToggle}>+</button></h2>
            {userData.Diagnosis ? (
              <ul>
                {Object.keys(userData.Diagnosis).map((diagnosisKey, index) => (
                  <li key={index}>
                    {userData.Diagnosis[diagnosisKey].Description} ({userData.Diagnosis[diagnosisKey].Date})
                  </li>
                ))}
              </ul>
            ) : (
              <p>No diagnosis data available</p>
            )}
          </div>
          {showForm && (
            <div className="diagnosis-form">
              <FaTimes className="cross-icon" onClick={handleFormToggle} />
              <h3>Add New Diagnosis</h3>
              <input 
                type="text" 
                placeholder="Diagnosis Name" 
                value={diagnosisName} 
                onChange={(e) => setDiagnosisName(e.target.value)} 
              />
              <button onClick={addDiagnosis}>Add Diagnosis</button>
            </div>
          )}
        </div>
      ) : (
        <p>Loading...</p>
      )}
    </div>
  );
};

export default LeftComponent;

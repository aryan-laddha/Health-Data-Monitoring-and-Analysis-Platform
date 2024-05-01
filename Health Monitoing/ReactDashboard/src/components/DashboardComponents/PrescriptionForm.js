import React, { useState, useEffect } from 'react';
import { ref, get, getDatabase } from 'firebase/database';
import { getStorage, ref as storageRef, uploadBytes } from 'firebase/storage';
import html2pdf from 'html2pdf.js';
import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './PrescriptionForm.css';

const PrescriptionForm = ({ userId, closeModal }) => { // Pass closeModal as a prop
  const [userData, setUserData] = useState(null);
  const [medications, setMedications] = useState([{ medication: '', durationOrCondition: '' }]);
  const [currentDate, setCurrentDate] = useState(new Date().toISOString().substr(0, 10));

  useEffect(() => {
    const fetchData = async () => {
      const db = getDatabase();
      try {
        const snapshot = await get(ref(db, `Users/${userId}`));
        if (snapshot.exists()) {
          const userData = snapshot.val();
          setUserData(userData);
        } else {
          console.log("No data available for this user");
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();

    return () => {
      // Cleanup code goes here if needed
    };
  }, [userId]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    
    // Construct PDF content
    const pdfContent = `
      <h1>Prescription Details</h1>
      <p><strong>Name:</strong> ${userData ? userData.Name : ''}</p>
      <p><strong>Age:</strong> ${userData ? userData.Age : ''}</p>
      <p><strong>Prescription Date:</strong> ${currentDate}</p>
      <h2>Medications</h2>
      <ul>
        ${medications.map((medication, index) => `
          <li>
            <strong>Medication:</strong> ${medication.medication}<br>
            <strong>Duration/Condition:</strong> ${medication.durationOrCondition}
          </li>
        `).join('')}
      </ul>
    `;
    
    // Generate PDF
    const pdfBlob = await html2pdf().from(pdfContent).outputPdf('blob');
    
    // Upload PDF to Firebase Storage
    const storage = getStorage();
    const formattedDate = currentDate.split('-').reverse().join('-');
    const uploadRef = storageRef(storage, `users/${userId}/Prescription/Prescription_${formattedDate}.pdf`);
    await uploadBytes(uploadRef, pdfBlob);

    // Display toast message
    toast.success('PDF uploaded successfully', {
      position: "bottom-right",
      autoClose: 3000,
      hideProgressBar: true,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
    });

    // Close the modal
    closeModal(); // Call the closeModal function passed as a prop
  };

  const handleAddMedication = () => {
    setMedications([...medications, { medication: '', durationOrCondition: '' }]);
  };

  const handleChangeMedication = (index, event) => {
    const { name, value } = event.target;
    const updatedMedications = [...medications];
    updatedMedications[index][name] = value;
    setMedications(updatedMedications);
  };

  return (
    <div className='Prescription-generate-container'>
      <h2>Hello, {userData && userData.Name}</h2>
      <p>User ID: {userId}</p>
      <p>Age: {userData && userData.Age}</p>
      <form onSubmit={handleSubmit}>
        <label htmlFor="name">Name:</label>
        <input 
          type="text" 
          id="name" 
          name="name" 
          required 
          value={userData ? userData.Name : ''}
          readOnly
        />
        <label htmlFor="age">Age:</label>
        <input 
          type="text" 
          id="age" 
          name="age" 
          required 
          value={userData ? userData.Age : ''}
          readOnly
        />
        <label htmlFor="prescriptionDate">Prescription Date:</label>
        <input 
          type="date" 
          id="prescriptionDate" 
          name="prescriptionDate" 
          required 
          value={currentDate}
          onChange={(e) => setCurrentDate(e.target.value)}
        />

        {medications.map((medication, index) => (
          <div key={index}>
            <label htmlFor={`medication${index}`}>Medication:</label>
            <input 
              type="text" 
              id={`medication${index}`} 
              name="medication" 
              value={medication.medication}
              onChange={(e) => handleChangeMedication(index, e)}
            />
            <label htmlFor={`durationOrCondition${index}`}>Duration/Condition:</label>
            <input 
              type="text" 
              id={`durationOrCondition${index}`} 
              name="durationOrCondition" 
              value={medication.durationOrCondition}
              onChange={(e) => handleChangeMedication(index, e)}
            />
          </div>
        ))}

        <button type="button" onClick={handleAddMedication}>+</button>
        <button type="submit">Generate PDF and Save to Firebase Storage</button>
      </form>
    </div>
  );
};

export default PrescriptionForm;

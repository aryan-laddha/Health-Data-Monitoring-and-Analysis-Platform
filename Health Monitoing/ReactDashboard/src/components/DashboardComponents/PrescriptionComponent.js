// PrescriptionComponent.js

import React, { useState, useEffect } from 'react';
import { getStorage, ref, listAll, getDownloadURL, uploadBytes } from 'firebase/storage';
import './PrescriptionComponent.css';
import PrescriptionForm from './PrescriptionForm'; // Import the form component

const PrescriptionComponent = ({ userId }) => {
  const [file, setFile] = useState(null);
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [containerHeight, setContainerHeight] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [showGenerateModal, setShowGenerateModal] = useState(false);
  const [prescriptions, setPrescriptions] = useState([]);

  useEffect(() => {
    const container = document.querySelector('.prescription-container');
    const header = document.querySelector('.header');
    const containerHeight = container.offsetHeight - header.offsetHeight;
    setContainerHeight(containerHeight);

    const fetchPrescriptions = async () => {
      try {
        const storage = getStorage();
        const userFolderRef = ref(storage, `users/${userId}/Prescription`);
        const files = await listAll(userFolderRef);

        const prescriptionURLs = await Promise.all(files.items.map(async (item) => {
          const downloadURL = await getDownloadURL(item);
          return { name: item.name, downloadURL };
        }));

        setPrescriptions(prescriptionURLs);
      } catch (error) {
        console.error('Error fetching prescriptions:', error);
      }
    };

    fetchPrescriptions();
  }, [userId]);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
    }
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file.');
      return;
    }

    const storage = getStorage();
    const storageRef = ref(storage, `users/${userId}/Prescription/${file.name}`);

    setUploading(true);

    try {
      await uploadBytes(storageRef, file);
      console.log('File uploaded successfully');

      const downloadURL = await getDownloadURL(storageRef);
      console.log('Download URL:', downloadURL);

      setFile(null);
      setError(null);
      setUploading(false);
      setShowModal(false);
      fetchPrescriptions();
    } catch (error) {
      console.error('Error uploading file:', error);
      setError('Error uploading file.');
      setUploading(false);
    }
  };

  const openModal = () => {
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  const openGenerateModal = () => {
    setShowGenerateModal(true);
  };

  const closeGenerateModal = () => {
    setShowGenerateModal(false);
  };

  const handleGenerate = () => {
    openGenerateModal();
  };

  const handleSubmitForm = () => {
    // Logic to handle form submission
    console.log('Form submitted');
    // Add your logic here to handle form submission, e.g., API call
    // After form submission, you can close the modal
    closeGenerateModal();
  };

  return (
    <div className="prescription-container">
      <div className="header">
        <h2>Prescriptions</h2>
        <button onClick={openModal}>+</button>
      </div>
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <span className="close" onClick={closeModal}>&times;</span>
            <h2>Upload Prescription</h2>
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload} disabled={uploading}>
              {uploading ? 'Uploading...' : 'Upload'}
            </button>
            <button onClick={handleGenerate}>Generate</button>
            {error && <p>{error}</p>}
          </div>
        </div>
      )}
      {showGenerateModal && (
        <div className="modal" style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0, 0, 0, 0.5)' }}>
          <div className="modal-content" style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', backgroundColor: '#fff', padding: '20px', borderRadius: '5px' }}>
            <span className="close" onClick={closeGenerateModal}>&times;</span>
            <h2>Generate Prescription</h2>
            <PrescriptionForm userId={userId} closeModal={closeModal} />
          </div>
        </div>
      )}
      {prescriptions.length > 0 && (
        <div className="prescription-list">
          <ul style={{ height: containerHeight }}>
            {prescriptions.map(prescription => (
              <li key={prescription.name}>
                <a href={prescription.downloadURL} target="_blank" rel="noopener noreferrer">{prescription.name}</a>
                <div className="line"></div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default PrescriptionComponent;

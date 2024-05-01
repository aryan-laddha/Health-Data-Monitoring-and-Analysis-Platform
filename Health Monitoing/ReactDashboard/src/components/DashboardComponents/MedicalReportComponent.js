import React, { useState, useEffect } from 'react';
import { getStorage, ref, listAll, getDownloadURL } from 'firebase/storage';
import { uploadBytes } from 'firebase/storage';
import './MedicalReportComponent.css';

const MedicalReportComponent = ({ userId }) => {
  const [file, setFile] = useState(null);
  const [error, setError] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [containerHeight, setContainerHeight] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [medicalReports, setMedicalReports] = useState([]);

  useEffect(() => {
    // Fetch medical reports for the user
    const container = document.querySelector('.medical-report-container');
    const header = document.querySelector('.header');
    const containerHeight = container.offsetHeight - header.offsetHeight;
    setContainerHeight(containerHeight);


    const fetchMedicalReports = async () => {
      try {
        const storage = getStorage();
        const userFolderRef = ref(storage, `users/${userId}/medical_records`);
        const files = await listAll(userFolderRef);

        const reportURLs = await Promise.all(files.items.map(async (item) => {
          const downloadURL = await getDownloadURL(item);
          return { name: item.name, downloadURL };
        }));

        setMedicalReports(reportURLs);
      } catch (error) {
        console.error('Error fetching medical reports:', error);
      }
    };

    fetchMedicalReports();
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
    const storageRef = ref(storage, `users/${userId}/medical_records/${file.name}`);

    setUploading(true);

    try {
      await uploadBytes(storageRef, file);
      console.log('File uploaded successfully');

      // Optional: Get the download URL of the uploaded file
      const downloadURL = await getDownloadURL(storageRef);
      console.log('Download URL:', downloadURL);

      setFile(null);
      setError(null);
      setUploading(false);
      setShowModal(false); // Close the modal after successful upload
      
      // Refresh medical reports
      fetchMedicalReports();
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

  return (
    <div className="medical-report-container">
      <div className="header">
        <h2>Medical Report</h2>
        <button onClick={openModal}>+</button>
      </div>
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <span className="close" onClick={closeModal}>&times;</span>
            <h2>Upload Medical Report</h2>
            <input type="file" onChange={handleFileChange} />
            <button onClick={handleUpload} disabled={uploading}>
              {uploading ? 'Uploading...' : 'Upload'}
            </button>
            {error && <p>{error}</p>}
          </div>
        </div>
      )}
    {medicalReports.length > 0 && (
        <div className="medical-reports-list">
          <ul style={{ height: containerHeight }}>
            {medicalReports.map(report => (
              <li key={report.name}>
                <a href={report.downloadURL} target="_blank" rel="noopener noreferrer">{report.name}</a>
                <div className="line"></div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default MedicalReportComponent;

import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import LeftComponent from "./DashboardComponents/LeftComponent";
import MiddleComponent from "./DashboardComponents/MiddleComponent";
import GrafanaViewer from "./DashboardComponents/GrafanaViewer";
import { firebaseApp } from '../firebase'; // Import Firebase configuration
import PrescriptionComponent from "./DashboardComponents/PrescriptionComponent";
import MedicalReportComponent from "./DashboardComponents/MedicalReportComponent";
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import './Dashboard.css';

const Dashboard = () => {
  const location = useLocation();
  const userId = location.state?.userId;

  useEffect(() => {
    console.log('Dashboard component rendered.');
    console.log('User ID:', userId);
  }, [userId]);

  return (
    <>
      <div className="Dashboard-container">
        <div className="Left-and-Main">
          <LeftComponent firebase={firebaseApp} userId={userId} className="LeftComponent" />
          <div className='Main-outer'>
            <div className="middle-and-grafana">
              <MiddleComponent userId={userId} />
              <GrafanaViewer userId={userId} />
            </div>
            <div className="Prescription-and-Report">
              <PrescriptionComponent userId={userId} />
              <MedicalReportComponent userId={userId} />
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Dashboard;

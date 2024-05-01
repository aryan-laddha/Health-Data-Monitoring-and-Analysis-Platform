import React, { useState } from 'react';
import './GrafanaViewer.css'; // Import custom CSS for styling
import { signOut, getAuth } from 'firebase/auth'; // Import signOut and getAuth functions from Firebase auth module

const GrafanaViewer = ({ userId }) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [frequency, setFrequency] = useState('none');

  const handleSubmit = (event) => {
    event.preventDefault();

    // Construct URL based on user input and provided userId
    let url = 'http://localhost:3000/d-solo/adgfib43jv668a/heart-rate-dashboard?orgId=1&var-userId=' + userId;

    if (startDate && endDate) {
      url += '&from=' + new Date(startDate).getTime() + '&to=' + new Date(endDate).getTime();
    } else if (frequency !== 'none') {
      url += '&from=now-' + frequency;
    }

    // Update iframe sources
    document.getElementById('panel1').src = url + '&theme=dark&panelId=1';
    document.getElementById('panel2').src = url + '&theme=light&panelId=2';
    document.getElementById('panel3').src = url + '&theme=light&panelId=3';
    document.getElementById('panel4').src = url + '&theme=light&panelId=4';
  };

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
  /*
  const handleBack = () => {
    const url = new URL('http://localhost:3001/userlist');
    url.searchParams.append('userId', userId);
    window.location.replace(url.toString());
  };*/
  return (
    <div className="grafana-viewer">  
        <div className="navigation-buttons">
{/* <button className="back-button" onClick={handleBack}>Back</button> */}

        <button className="sign-out-button" onClick={handleSignOut}>Sign Out</button>
    </div>
      <br/>      <br/>    <br/>
      <form id="dataForm" onSubmit={handleSubmit}>
        <input type="hidden" id="userId" name="userId" value={userId} />

        <label htmlFor="dateRange">Date-Time Range:</label>
        <input type="datetime-local" id="startDate" name="startDate" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        <label htmlFor="endDate"> to </label>
        <input type="datetime-local" id="endDate" name="endDate" value={endDate} onChange={(e) => setEndDate(e.target.value)} />

        <label htmlFor="frequency">Frequency:</label>
        <select id="frequency" name="frequency" value={frequency} onChange={(e) => setFrequency(e.target.value)}>
          <option value="none">None</option>
          <option value="15min">15 Minutes</option>
          <option value="30min">30 Minutes</option>
          <option value="1h">1 Hour</option>
          <option value="3h">3 Hours</option>
          <option value="6h">6 Hours</option>
          <option value="9h">9 Hours</option>
          <option value="12h">12 Hours</option>
          <option value="1d">1 Day</option>
          <option value="3d">3 Days</option>
          <option value="6d">6 Days</option>
          <option value="15d">15 Days</option>
          <option value="30d">30 Days</option>
        </select>

        <button type="submit">Submit</button>
      </form>

      <div className="panels">
        <iframe id="panel1" className="panel panel1" title="Panel 1"></iframe>
        <div className="panel-group">
          <iframe id="panel2" className="panel" title="Panel 2"></iframe>
          <iframe id="panel3" className="panel" title="Panel 3"></iframe>
          <iframe id="panel4" className="panel" title="Panel 4"></iframe>
        </div>
      </div>
    </div>
  );
};

export default GrafanaViewer;

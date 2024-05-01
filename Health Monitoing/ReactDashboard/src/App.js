// App.js
import './App.css';
import MuiSignUp from './components/auth/MuiSignUp';
import MuiLogin from './components/auth/MuiLogIn';

import { BrowserRouter, Route, Routes } from "react-router-dom";

import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import UserList from './components/UserList'; // Updated import
import Dashboard from './components/Dashboard'; // Corrected import


import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';


function App() {

  return (
    <div className="App">
      <BrowserRouter>
        <ToastContainer />
        <Routes>
          <Route 
            path='/' 
            element= 
            {<div>
              <MuiSignUp/>
              {/* <SignIn/>
              <SignUp/> */}
            </div>} 
          />
          <Route
            path='/login'
            element={<MuiLogin/>}
          />
          <Route
            path='/userlist' // Changed path to /userlist
            element={<UserList/>} // Changed component to UserList
          />
          <Route
            path='/dashboard' // Changed path to /userlist
            element={<Dashboard showBackgroundImage={false} />} // Pass showBackgroundImage prop
          />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;

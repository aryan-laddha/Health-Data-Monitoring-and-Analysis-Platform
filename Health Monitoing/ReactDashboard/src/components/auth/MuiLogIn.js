import React, { useState, useEffect } from 'react';
import { Typography, InputLabel, Input, Stack, Checkbox, Link, Button, Box} from '@mui/material';
import { signInWithEmailAndPassword, signInWithPopup } from "firebase/auth";
import { auth, provider } from "../../firebase"; // Assuming you have auth imported from firebase
import { getDatabase, ref, get } from "firebase/database"; // Importing the database functions from Firebase Modular SDK
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

export default function MuiLogin() {

  const navigate = useNavigate();  // To navigate

  // useState hooks to save email and password
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [doctorId, setDoctorId] = useState(""); // State to store DoctorId

  useEffect(() => {
    // Fetch DoctorId when the component mounts
    if (auth.currentUser) {
      const uid = auth.currentUser.uid;
      fetchDoctorId(uid);
    }
  }, []);

  // Function to fetch DoctorId from database
  const fetchDoctorId = async (uid) => {
    const database = getDatabase(); // Get a reference to the database
    const doctorRef = ref(database, `Docters/${uid}`);
    
    try {
      const snapshot = await get(doctorRef);
      if (snapshot.exists()) {
        const doctorData = snapshot.val();
        setDoctorId(doctorData.DocterId);
        console.log("DoctorId:", doctorData.DocterId); // Log the fetched doctorId
        navigate('/userlist', { state: { doctorId: doctorData.DocterId } });
      }
    } catch (error) {
      console.error("Error fetching doctorId:", error);
    }
  };

  // When user press SignUP button.
  const signIn = (e) => {
    e.preventDefault();
    signInWithEmailAndPassword(auth, email, password)
      .then((userCredential) => {
        const user = userCredential.user;
        const uid = user.uid; // Fetching the UID
        console.log("User UID:", uid); // Logging UID in console
        fetchDoctorId(uid); // Fetch DoctorId after successful sign-in
        toast.success("Logged In Success")
        setEmail('')
        setPassword('')
      })
      .catch((error) => {
        console.log(error);
        toast.error(error.message)
      });
  };

  const handleGoogle = () => {
    signInWithPopup(auth, provider).then((data)=>{
      setEmail(data.user.email)
      // Assuming you also fetch doctorId when signing in with Google
      // If not, you can call fetchDoctorId() here as well
      toast.success("Sign In with Google Success")
    })
  }

  return (
<div className='bgset'>
<Box className='Main-container'>
      <form onSubmit={signIn}>
        <Box className='Card'>
          <Typography variant="p" className='Main-text'>
            Login
          </Typography>
          
          <Stack className='Input-line' spacing={1} direction="row">
            <img alt="Img not found"  src="https://purecodestorageprod.blob.core.windows.net/images-svg/Signin1_fa15f5a5-1750-483b-adbd-3a39c10858e6.svg" width="20px" height="20px" />
            <Input
              className='Input'
              placeholder='Email'
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disableUnderline />
          </Stack>
          
          <Stack className='Input-line' spacing={1} direction="row">
            <img alt="Img not found"  src="https://purecodestorageprod.blob.core.windows.net/images-svg/Signin1_3f4e1036-748c-4f7c-b288-021186172a29.svg" width="20px" height="20px" />
            <Input 
              className='Input'
              type="text"
              placeholder='Password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disableUnderline />
          </Stack>
          
          <Stack className='Stack1' direction="row">
            <Stack spacing={1} direction="row">
              <Checkbox className='Checkbox' />
              <InputLabel className='Remember-text'>
                Remember me
              </InputLabel>
            </Stack>
            <Link className='Forget-text'>
              Forgot password?
            </Link>
          </Stack>
          
          <Button disableElevation variant="contained" type='submit' className='Button'>
            Login
          </Button>

          <Typography variant='p' color='white'>
            ------------------- OR -------------------
          </Typography>

          <Button variant= 'outlined' className='Google-button' onClick={handleGoogle}>
              <img 
                alt="Img not found"  
                src="https://purecodestorageprod.blob.core.windows.net/images-svg/Signin_549cd131-671d-4613-ab2d-429124d8492d.svg" 
                width="20px" 
                height="20px" 
                style={{marginRight: 10}} 
              />
              Sign In with Google
          </Button>

          <Stack spacing={1} direction="row">
            <Typography variant="p" color='gray'>
              Don't have an account?
            </Typography>
            <Link onClick={()=> navigate('/')} className='Link-text'>
              Register
            </Link>
          </Stack>
        </Box>
      </form>
    </Box>
</div>
  );
}



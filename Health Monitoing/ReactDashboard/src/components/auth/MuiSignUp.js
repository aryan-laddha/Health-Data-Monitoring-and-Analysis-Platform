import React, { useState } from 'react';
import { Typography, Input, Stack, Link, Button, Box } from '@mui/material';
import { createUserWithEmailAndPassword, signInWithPopup } from "firebase/auth";
import { auth, provider, database } from "../../firebase"; // Import auth, provider, and database from firebase module
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import { ref, set } from 'firebase/database'; // Import ref and set functions from firebase/database

export default function Mui() {

  const navigate = useNavigate();

  // useState hooks to save email, password, first name, last name, and DocterId
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [DocterId, setDocterId] = useState("");

  // When user press SignUP button.
  const signUp = (e) => {
    e.preventDefault();

    // Create user with email and password
    createUserWithEmailAndPassword(auth, email, password)
      .then((userCredential) => {
        const userId = userCredential.user.uid;

        // Combine first and last names into a single string
        const fullName = `${firstName} ${lastName}`;

        // Save user's data to the database under 'Docters' node with the DocterId as key
        const userData = {
          Name: fullName,
          Email: email,
          DocterId: DocterId
        };

        // Write the data to Firebase
        writeDataToFirebase(userId, userData);
        console.log("User UID : ", userId)
        // Display success message and navigate to login page
        toast.success("Account Created");
        setEmail('');
        setPassword('');
        setFirstName('');
        setLastName('');
        setDocterId('');
        navigate('/login');
      })
      .catch((error) => {
        console.log(error);
        toast.error(error.message);
      });
  };

  const handleGoogle = () => {
    signInWithPopup(auth, provider).then((data) => {
      setEmail(data.user.email);
      toast.success("Sign In with Google Success");
      navigate('/dashboard');
    });
  };

  // Function to write data to Firebase
  const writeDataToFirebase = (DocterId, data) => {
    // Specify the reference to the location where you want to write data
    const userRef = ref(database, `Docters/${DocterId}`);

    // Write the data to the specified location
    set(userRef, data)
      .then(() => {
        console.log("Data written successfully!");
      })
      .catch((error) => {
        console.error("Error writing data: ", error);
      });
  };

  return (
<div className='bgset'>
<Box className='Main-container'>
      <form onSubmit={signUp}>
        <Box className='Card'>
          <Typography variant="p" className='Main-text'>
            Signup
          </Typography>
          
          <Stack direction="row" spacing={2} >
            <Input 
              className='Name-input'
              type="text"
              placeholder='First Name'
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              disableUnderline />
            <Input 
              className='Name-input'
              type="text"
              placeholder='Last Name'
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              disableUnderline />          
          </Stack>

          <Stack spacing={1} direction="row" className='Input-line'>
            <img alt="Img not found" src="https://purecodestorageprod.blob.core.windows.net/images-svg/Signin1_fa15f5a5-1750-483b-adbd-3a39c10858e6.svg" width="25px" height="25px" />
            <Input
              className='Input'
              placeholder='Email'
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disableUnderline />
          </Stack>

          <Stack spacing={1} direction="row" className='Input-line'>
            <img alt="Img not found" src="https://purecodestorageprod.blob.core.windows.net/images-svg/Signin1_3f4e1036-748c-4f7c-b288-021186172a29.svg" width="20px" height="20px" />
            <Input 
              className='Input'
              type="text"
              placeholder='Password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disableUnderline />
          </Stack>

          {/* Add a field for DocterId */}
          <Stack spacing={1} direction="row" className='Input-line'>
            <Input 
              className='Input'
              type="text"
              placeholder='DocterId'
              value={DocterId}
              onChange={(e) => setDocterId(e.target.value)}
              disableUnderline />
          </Stack>
          
          <Button disableElevation variant="contained" type='submit' className='Button'>
            SignUp
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
              Already have an account?
            </Typography>
            <Link onClick={()=> navigate('/login')} className='Link-text'>
              LogIn
            </Link>
          </Stack>
        </Box>
      </form>
    </Box>
</div>
  );
}

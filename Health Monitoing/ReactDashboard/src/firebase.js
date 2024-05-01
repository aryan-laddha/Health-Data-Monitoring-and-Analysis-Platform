import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { getDatabase } from "firebase/database"; // Import getDatabase function from firebase/database

// Your web app's Firebase configuration
// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyA2W8MvKLMIyDsAPNi4FAaEAOdzUXvfDGM",
  authDomain: "heart-dc60d.firebaseapp.com",
  databaseURL: "https://heart-dc60d-default-rtdb.firebaseio.com",
  projectId: "heart-dc60d",
  storageBucket: "heart-dc60d.appspot.com",
  messagingSenderId: "202214961634",
  appId: "1:202214961634:web:fe91e21a15aae41552c9bb"
};

const firebaseApp  = initializeApp(firebaseConfig);

// Initialize Firebase Authentication and get a reference to the service
const auth = getAuth(firebaseApp);
const provider = new GoogleAuthProvider();

// Initialize Firebase Realtime Database
const database = getDatabase(firebaseApp); // Use getDatabase function to initialize the database

export { auth, provider, database, firebaseApp  }; // Export auth, provider, and database
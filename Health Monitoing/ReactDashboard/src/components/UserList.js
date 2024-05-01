// UserList.js
import { onAuthStateChanged, signOut } from "firebase/auth";
import { useState, useEffect } from "react";
import { auth, database } from "../firebase"; // Assuming you have set up Firebase database
import { Button, Typography, Box, Table, TableHead, TableBody, TableRow, TableCell } from "@mui/material";
import { useNavigate, useLocation } from "react-router-dom";
import { toast } from "react-toastify";
import { ref, query, orderByChild, equalTo, onValue } from "firebase/database";




export default function UserList() { // Changed component name to UserList
  const [authUser, setAuthUser] = useState(null);
  const [matchingUsers, setMatchingUsers] = useState([]); // State to hold matching users
  const navigate = useNavigate(); // To navigate
  const location = useLocation();
  const doctorId = location.state?.doctorId; // Retrieve doctorId from query parameters

  

  // Checks if user is logged in.
  useEffect(() => {
    const listen = onAuthStateChanged(auth, (user) => {
      if (user) {
        setAuthUser(user);
      } else {
        setAuthUser(null);
      }
    });

    return () => {
      listen();
    };
  }, []);

  // Fetch users with matching DoctorId
  useEffect(() => {
    if (doctorId) {
      const usersRef = ref(database, "Users");
      const usersQuery = query(usersRef, orderByChild("DocterId"), equalTo(doctorId));
      
      onValue(usersQuery, (snapshot) => {
        if (snapshot.exists()) {
          const users = snapshot.val();
          const matchingUsersArray = Object.keys(users).map((key) => ({
            id: key, // This is the user ID
            ...users[key],
          }));
          setMatchingUsers(matchingUsersArray);
        } else {
          setMatchingUsers([]);
        }
      });
    }
  }, [doctorId]);

  // Signout button functionality
  const userSignOut = () => {
    signOut(auth)
      .then(() => {
        navigate("/");
        toast.success("Signout Success");
      })
      .catch((error) => console.log(error));
  };

  // Function to handle navigating to user details page
  const handleUserDetails = (userId) => {
    navigate('/dashboard', { state: { userId } });
  };

  // Log Hello message along with doctorId
  useEffect(() => {
    if (doctorId) {
      console.log(`Hello from UserList, DoctorId: ${doctorId}`);
    }
  }, [doctorId]);

  return (
    <div className="UserList bgset">
      <Box className="Main-container">
        {authUser ? (
          <Box className="UserList-card">
            <Typography variant="h4" color="white">{`Welcome, ${authUser.email}`}</Typography>
            {/* Display doctorId if available */}
            {doctorId && (
              <Typography variant="h6"color="white">{`Your DoctorId: ${doctorId}`}</Typography>
            )}
            {/* Display matching users in a table */}
            {matchingUsers.length > 0 && (
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>User ID</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Age</TableCell>
                    <TableCell>Gender</TableCell>
                    <TableCell>Blood Group</TableCell>
                    <TableCell>Action</TableCell> {/* New column for action */}
                    {/* Add more table cells as needed */}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {matchingUsers.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>{user.id}</TableCell>
                      <TableCell>{user.Name}</TableCell>
                      <TableCell>{user.Age}</TableCell>
                      <TableCell>{user.Gender}</TableCell>
                      <TableCell>{user['Blood Group']}</TableCell>
                      <TableCell>
                        <Button variant="outlined" onClick={() => handleUserDetails(user.id)}>View Details</Button>
                      </TableCell>
                      {/* Add more table cells as needed */}
                    </TableRow>
                  ))}
                </TableBody>          
              </Table>
            )}
            <Button className="UserList-button"  onClick={userSignOut}>
              Sign Out
            </Button>
          </Box>
        ) : (
          <Typography variant="h2">
            Signed Out
          </Typography>
        )}
      </Box>
    </div>
  );
}

function toggleForms() {
  // Toggle the visibility of the login and register forms
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');
  const registerLink = document.getElementById('registerLink');
  const registerResponse = document.getElementById('registerResponse');

  if (loginForm.style.display === 'none') {
    loginForm.style.display = 'block';
    registerForm.style.display = 'none';
    registerLink.innerHTML = "Don't have an account? <a href='#' onclick='toggleForms()'>Register</a>";
    registerResponse.innerHTML = ''; // Clear previous registration response
  } else {
    loginForm.style.display = 'none';
    registerForm.style.display = 'block';
    registerLink.innerHTML = 'Already have an account? <a href="#" onclick="toggleForms()">Login</a>';
    registerResponse.innerHTML = ''; // Clear previous registration response
  }
}




function performRegister() {

  const username = document.getElementById('username1').value;
  const password = document.getElementById('password1').value;
  const registerResponse = document.getElementById('registerResponse'); // New line

  if (username.trim() === '' || password.trim() === '') {
    registerResponse.innerHTML = 'Please enter both username and password.';
    registerResponse.className = 'alert alert-danger';
    registerResponse.style.display = 'block';
    return;
  }

  console.log('Performing registration with username:', username);

  const payload = {

    username: username,
    password: password
  };

  fetch('/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
    .then(response => {
       if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
       }
      return response.text();
    })
    .then(data => {
      console.log('Register response:', data);
      // Handle non-JSON response
      if (data.includes('User registered successfully')) {
        //   // Delay the redirection by 2 seconds (adjust as needed)
           setTimeout(() => {
        //     // Handle the redirection manually here
        //     // You can redirect to the users page or any other URL
             window.location.href = '/login';
           }, 1500);

        registerResponse.innerText = 'Registration successful! You can now log in.';
        // Optionally, clear the registration form fields
        document.getElementById('username1').value = '';
        document.getElementById('password1').value = '';
      } else {
        // Handle other cases (e.g., display an error message)
        alert(`Registration failed. ${data}`);
      }
    })
    .catch(error => {
      console.error('Error:', error);
    });
}

function performLogin() {
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;
  const loginResponse = document.getElementById('loginResponse');

  loginResponse.style.display = 'none';

  if (username.trim() === '' || password.trim() === '') {
    loginResponse.innerHTML = 'Please enter both username and password.';
    loginResponse.className = 'alert alert-danger';
    loginResponse.style.display = 'block';
    return;
  }

  const payload = { username, password };

  fetch('/login', {
    method: 'POST',
    body: JSON.stringify(payload),
    headers: { 'Content-Type': 'application/json' }
  })
    .then(response => {
      if (response.ok) {
        return response.json();
      } else if (response.status === 401) {
        throw new Error('Invalid username or password.');
      } else {
        throw new Error('An unexpected error occurred. Please try again.');
      }
    })
    .then(data => {
      if (data.jwtToken) {
        localStorage.setItem('jwtToken', data.jwtToken); // Store the JWT token
        loginResponse.innerHTML = 'Login successful! Redirecting...';
        loginResponse.className = 'alert alert-success';
        loginResponse.style.display = 'block';
        fetchUserData(); // Fetch user data on successful login
      } else {
        throw new Error('Login failed or no token received.');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      loginResponse.innerHTML = error.message;
      loginResponse.className = 'alert alert-danger';
      loginResponse.style.display = 'block';
    });
}

function fetchUserData() {
  const token = localStorage.getItem('jwtToken');
  if (!token) {
    console.error('JWT token is not available');
    return;
  }

  fetch('/users', {
    method: 'GET',
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('jwtToken'),
      'Content-Type': 'application/json'
    }
  })
    .then(response => {
      if (response.ok) {
        return response.json();
      } else {
        // Specific handling for unauthorized access
        if (response.status === 401) {
          throw new Error('Unauthorized access. Please login again.');
        }
        throw new Error('Failed to fetch user data. Status: ' + response.status);
      }
    })
    .then(data => {
      console.log('User data:', data);
      // Redirect to the users' page or update the UI with user data
      //window.location.href = '/users.html'; // Redirect to a specific page
      window.location.href = data.redirect || '/users';
    })
    .catch(error => {
     // console.error('Error fetching user data:', error);
      // Clear JWT token in case of an authorization error
      if (error.message.includes('Unauthorized')) {
        localStorage.removeItem('jwtToken');
      }
      // Update UI to reflect the error
      // Example: Show error message on UI
    });
}
// function performLogin() {
//   const username = document.getElementById('username').value;
//   const password = document.getElementById('password').value;
//   const loginResponse = document.getElementById('loginResponse');
//
//   loginResponse.style.display = 'none';
//
//   if (username.trim() === '' || password.trim() === '') {
//     loginResponse.innerHTML = 'Please enter both username and password.';
//     loginResponse.className = 'alert alert-danger';
//     loginResponse.style.display = 'block';
//     return;
//   }
//
//   const payload = { username, password };
//
//   fetch('/login', {
//     method: 'POST',
//     headers: { 'Content-Type': 'application/json' },
//     body: JSON.stringify(payload)
//   })
//     .then(response => {
//       if (response.ok) {
//         return response.json();
//       } else if (response.status === 401) {
//         throw new Error('Invalid username or password.');
//       } else {
//         throw new Error('An error occurred during login. Please try again.');
//       }
//     })
//     .then(data => {
//       if (data && data.jwtToken) {
//         localStorage.setItem('jwtToken', data.jwtToken);
//         loginResponse.innerHTML = 'Login successful! Redirecting...';
//         loginResponse.className = 'alert alert-success';
//         loginResponse.style.display = 'block';
//         window.location.href = '/users';
//       } else {
//         throw new Error('Login failed: No JWT token received.');
//       }
//     })
//     .catch(error => {
//       console.error('Login Error:', error);
//       loginResponse.innerHTML = error.message;
//       loginResponse.className = 'alert alert-danger';
//       loginResponse.style.display = 'block';
//     });
//
// }

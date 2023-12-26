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
    alert('Please enter both username and password.');
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
      // if (!response.ok) {
      //   throw new Error(`HTTP error! Status: ${response.status}`);
      // }
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
             window.location.href = '/';
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
  // Get values from the input fields
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  // Check if username or password is empty
  if (username.trim() === '' || password.trim() === '') {
    alert('Please enter both username and password.');
    return;
  }

  console.log('Performing login with username:', username);

  // Create JSON payload
  const payload = {
    username: username,
    password: password
  };

  // Make a POST request to /api/login
  fetch('/login', {
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
      // Check if the response is JSON
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return response.json();
      } else {
        // Handle non-JSON response (e.g., redirect or display an error)
        console.log('Non-JSON response:', response);
        // You can decide how to handle this based on your server behavior
        // For now, let's assume a successful login and redirect
        window.location.href = '/users';
      }
    })
    .then(data => {
      // Handle the JSON response (if any)
      console.log('Login response:', data);
      if (data.success) {
        // Check if the response contains a 'redirect' property
        if (data.redirect) {
          // Redirect to the path specified in the 'redirect' property
          window.location.href = data.redirect;
        } else {
          // If no specific redirect path, fallback to '/users'
          window.location.href = '/users';
        }
      } else {
        alert('Login failed. Please check your credentials.');

      }
    })
    .catch(error => {
      // Handle errors
      console.error('Error:', error);
    });
}

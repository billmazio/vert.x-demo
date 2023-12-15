function performRegister() {
  // Get values from the registration form
  const username = document.getElementById('username1').value;
  const password = document.getElementById('password1').value;

  // Check if username or password is empty
  if (username.trim() === '' || password.trim() === '') {
    // Display an alert or handle the empty fields as needed
    alert('Please enter both username and password.');
    return; // Stop the function if fields are empty
  }

  console.log('Performing registration with username:', username);

  // Create JSON payload
  const payload = {
    username: username,
    password: password
  };

  // Make a POST request to /api/register
  fetch('/api/register', {
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
      return response.json();
    })
    .then(data => {
      // Handle the response data
      console.log('Registration response:', data);

      // Redirect to the login page on successful registration
      window.location.href = '/login';
    })
    .catch(error => {
      // Handle errors
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
      return response.json();
    })
    .then(data => {
      console.log(data);
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
      console.error('Error:', error);
    });
}

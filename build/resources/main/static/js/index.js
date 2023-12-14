document.addEventListener('DOMContentLoaded', () => {
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');

  usernameInput.addEventListener('input', handleInputChange);
  passwordInput.addEventListener('input', handleInputChange);
});

function handleInputChange() {
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');

  if (usernameInput.value.trim() === '') {
    usernameInput.classList.add('empty');
    usernameInput.classList.remove('valid');
  } else {
    usernameInput.classList.remove('empty');
    usernameInput.classList.add('valid');
  }

  if (passwordInput.value.trim() === '') {
    passwordInput.classList.add('empty');
    passwordInput.classList.remove('valid');
  } else {
    passwordInput.classList.remove('empty');
    passwordInput.classList.add('valid');
  }
}

function performLogin() {
  // Get values from the input fields
  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  // Check if username or password is empty
  if (username.trim() === '' || password.trim() === '') {
    // Display an alert or handle the empty fields as needed
    alert('Please enter both username and password.');
    return; // Stop the function if fields are empty
  }

  // Create JSON payload
  const payload = {
    username: username,
    password: password
  };

  // Make a POST request to /api/login
  fetch('/api/login', {
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
      console.log(data);
    })
    .catch(error => {
      // Handle errors
      console.error('Error:', error);
    });
}

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

 // console.log('Performing login with username:', username);

  const payload = {
    username: username,
    password: password
  };

  fetch('/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
    .then(response => {
      console.log('Response received:', response);
      if (response.ok) {
        return response.text;
      } else {
        // If the response is not OK, still try to read the text to show the message
        return response.text().then(text => {
          loginResponse.innerHTML = text || 'Login failed. Please check your credentials.';
          loginResponse.className = 'alert alert-danger';
          loginResponse.style.display = 'block';
          throw new Error('Login failed');
        });
      }
    })
    .then(data => {
      console.log('Login response:', data);
      // Handle the success case
      loginResponse.innerHTML = 'Login successful! Redirecting...';
      loginResponse.className = 'alert alert-success';
      loginResponse.style.display = 'block';

      setTimeout(() => {
        window.location.href = data.redirect || '/users';
      }, 1500);
    })
    .catch(error => {
      console.error('Error:', error);
      // If this catch block is reached without setting the loginResponse,
      // it means there was a network error or some unexpected issue
      if (loginResponse.innerHTML === '') {
        loginResponse.innerHTML = 'Error: ' + error;
        loginResponse.className = 'alert alert-danger';
        loginResponse.style.display = 'block';
      }
    });
}

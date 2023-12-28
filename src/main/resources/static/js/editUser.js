function submitForm() {
  let form = document.getElementById('updateForm');
  let updateMessage = document.getElementById('updateMessage');

  // Hide the update message initially
  updateMessage.style.display = 'none';

  // Check if the form is valid
  if (form.checkValidity()) {
    // Perform AJAX request using Fetch API
    fetch(form.action, {
      method: form.method,
      body: new FormData(form)
    })
      .then(response => {
        if (response.ok) {
          // Show and update the success message
          updateMessage.innerHTML = 'User updated successfully!';
          updateMessage.className = 'alert alert-success';
          updateMessage.style.display = 'block';

          // Redirect to the users page after a delay
          setTimeout(() => {
            window.location.href = '/users';
          }, 1500);
        } else {
          throw new Error('Failed to update user');
        }
      })
      .catch(error => {
        // Show and update the error message
        updateMessage.innerHTML = 'Error: ' + error.message;
        updateMessage.className = 'alert alert-danger';
        updateMessage.style.display = 'block';
      });
  } else {
    // Show and update the form validation error message
    updateMessage.innerHTML = 'Error: Form is not valid';
    updateMessage.className = 'alert alert-danger';
    updateMessage.style.display = 'block';
  }
}

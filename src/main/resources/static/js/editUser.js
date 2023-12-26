function submitForm() {
  let form = document.getElementById('updateForm');
  let updateMessageDiv = document.getElementById('updateMessage');

  // Check if the form is valid
  if (form.checkValidity()) {
    // Perform AJAX request using Fetch API
    fetch(form.action, {
      method: form.method,
      body: new FormData(form)
    })
      .then(response => response.json())
      .then(data => {
        // Show the update message if present
        if (data.updateMessage) {
          updateMessageDiv.innerText = data.updateMessage;

          // Delay the redirection by 2 seconds (adjust as needed)
           setTimeout(() => {
          // Handle the redirection manually here
          // You can redirect to the users page or any other URL
          window.location.href = '/users';
            }, 1500);
        }
      })
      .catch(error => console.error('Error:', error));
  } else {
    // If the form is not valid, you can display an error message or take appropriate action
    console.error('Form is not valid');
  }
}

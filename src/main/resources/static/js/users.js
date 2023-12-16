function updateUser() {
  const userId = document.getElementById("userId").value;
  const field1 = document.getElementById("username").value;
  const field2 = document.getElementById("password").value;

  const formData = {
    userId: userId,
    field1: field1,
    field2: field2,
  };

  console.log('Form Data:', formData);

  fetch(`/updateUser/${userId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(formData),
  })
    .then(response => {
      if (!response.ok) {
        console.error(`HTTP error! Status: ${response.status}, StatusText: ${response.statusText}`);
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return response.text();
    })
    .then(data => {
      console.log(data);
      // Handle success
    })
    .catch(error => {
      console.error('Error:', error);
      // Handle error
    });
}

// function updateUser(id, username, password) {
//   const formData = {
//     userId: id,
//     field1: username,
//     field2: password,
//   };
//
//   console.log('Form Data:', formData);
//
//   fetch(`/updateUser/${id}`, {
//     method: 'POST', // Use POST method
//     headers: {
//       'Content-Type': 'application/json',
//       'X-HTTP-Method-Override': 'PUT', // Use the X-HTTP-Method-Override header
//     },
//     body: JSON.stringify(formData),
//   })
//     .then(response => {
//       if (!response.ok) {
//         console.error(`HTTP error! Status: ${response.status}, StatusText: ${response.statusText}`);
//         throw new Error(`HTTP error! Status: ${response.status}`);
//       }
//       return response.text();
//     })
//     .then(data => {
//       console.log(data);
//       // Handle success
//     })
//     .catch(error => {
//       console.error('Error:', error);
//       // Handle error
//     });
// }

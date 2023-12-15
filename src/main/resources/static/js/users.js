// document.addEventListener('DOMContentLoaded', () => {
//   // Add event listener to the button
//   document.getElementById('fetchUsersButton').addEventListener('click', () => {
//     // Make a POST request to fetch users from the server
//     fetch('/users', {
//       method: 'POST',
//       headers: {
//         'Content-Type': 'application/json',
//       },
//       body: JSON.stringify({}), // You may need to pass specific data in the body
//     })
//       .then(response => {
//         if (!response.ok) {
//           throw new Error('Network response was not ok');
//         }
//         return response.json();
//       })
//       .then(users => {
//         // Get the table body element
//         const tableBody = document.getElementById('users-table-body');
//
//         // Clear existing rows in the table
//         tableBody.innerHTML = '';
//
//         // Iterate over users and add rows to the table
//         users.forEach(user => {
//           const row = tableBody.insertRow();
//           row.insertCell().textContent = user.id;
//           row.insertCell().textContent = user.username;
//           row.insertCell().textContent = user.password;
//         });
//       })
//       .catch(error => console.error('Error:', error));
//   });
// });

export default function Profile() {
  const user = (() => {
    try {
      return JSON.parse(localStorage.getItem("user") || "null");
    } catch {
      return null;
    }
  })();

  return (
    <div>
      <h1>My Profile</h1>
      {user ? (
        <div className="mt-3 text-sm text-gray-700">
          <div>
            <strong>Name:</strong> {user.userName}
          </div>
          <div>
            <strong>Email:</strong> {user.userEmail}
          </div>
          <div>
            <strong>Contact:</strong> {user.userContact}
          </div>
          <div>
            <strong>Type:</strong> {user.userType}
          </div>
          <div>
            <strong>Address:</strong> {user.userAddress}
          </div>
        </div>
      ) : (
        <p className="text-gray-600 mt-2">No user information available.</p>
      )}
    </div>
  );
}

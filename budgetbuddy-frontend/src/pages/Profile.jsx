// src/pages/Profile.jsx
import { useEffect, useState } from 'react';
import axios from 'axios';
import toast, { Toaster } from 'react-hot-toast';

export default function Profile() {
  const token = localStorage.getItem('token');

  // Profile form state
  const [formData, setFormData] = useState({
    username: '',
    email:    '',
    password: '',
  });

  // UI & loading states
  const [loading, setLoading] = useState(true);
  const [saving,  setSaving]  = useState(false);
  const [editing, setEditing] = useState(false);

  // Field validation
  const [usernameError, setUsernameError] = useState('');

  // Fetch profile on mount
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axios.get(
          'http://localhost:8081/auth/profile',
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setFormData({
          username: res.data.username,
          email:    res.data.email,
          password: '',
        });
      } catch (err) {
        console.error('Error fetching profile:', err);
        toast.error('Failed to load profile. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      fetchProfile();
    } else {
      toast.error('Unauthorized. Please log in again.');
      setLoading(false);
    }
  }, [token]);

  // Handle form input changes & validation
  const handleChange = (e) => {
    const { name, value } = e.target;

    // Username length validation
    if (name === 'username') {
      if (value.trim().length < 3) {
        setUsernameError('Username must be at least 3 characters.');
      } else {
        setUsernameError('');
      }
    }

    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // Submit updated profile
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (usernameError) {
      toast.error('Please fix form errors before saving.');
      return;
    }

    setSaving(true);
    try {
      await axios.put(
        'http://localhost:8081/auth/profile',
        {
          username: formData.username,
          password: formData.password || undefined, // only send if non-empty
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      toast.success('Profile updated!');
      setEditing(false);
      setFormData((prev) => ({ ...prev, password: '' }));
    } catch (err) {
      console.error('Error updating profile:', err);
      toast.error('Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  // While loading, show skeleton placeholders
  if (loading) {
    return (
      <div className="min-h-screen flex flex-col justify-center items-center bg-gray-100 px-4">
        <div className="h-6 w-1/2 mb-3 bg-gray-200 animate-pulse rounded" />
        <div className="h-6 w-1/3 bg-gray-200 animate-pulse rounded" />
      </div>
    );
  }

  return (
    <>
      {/* Toasts */}
      <Toaster position="top-right" />

      <div className="min-h-screen bg-gray-50 flex justify-center items-center px-4">
        <div className="bg-white p-8 rounded shadow-lg max-w-md w-full">
          <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">
            My Profile
          </h2>

          {/* VIEW MODE */}
          {!editing ? (
            <div className="space-y-4">
              {/* Email */}
              <div>
                <label className="block text-sm text-gray-600">Email</label>
                <p className="text-gray-800">{formData.email}</p>
              </div>

              {/* Username */}
              <div>
                <label className="block text-sm text-gray-600">Username</label>
                <p className="text-gray-800">{formData.username}</p>
              </div>

              {/* Edit Button */}
              <button
                onClick={() => setEditing(true)}
                className="mt-4 w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 rounded"
              >
                Edit Profile
              </button>
            </div>
          ) : (
            /* EDIT MODE */
            <form onSubmit={handleSubmit} className="space-y-5">
              {/* Email (read-only) */}
              <div>
                <label className="block text-sm text-gray-700 mb-1">
                  Email (read-only)
                </label>
                <input
                  type="email"
                  value={formData.email}
                  disabled
                  className="w-full px-4 py-2 border border-gray-300 bg-gray-100 text-gray-900 rounded focus:outline-none focus:ring-2 focus:ring-indigo-300"
                />
              </div>

              {/* Username */}
              <div>
                <label className="block text-sm text-gray-700 mb-1">
                  Username
                </label>
                <input
                  name="username"
                  type="text"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className={`
                    w-full px-4 py-2 border rounded
                    ${usernameError ? 'border-red-500' : 'border-gray-300'}
                    text-gray-900
                    focus:outline-none focus:ring-2 focus:ring-indigo-300
                  `}
                />
                {usernameError && (
                  <p className="text-red-500 text-sm mt-1">
                    {usernameError}
                  </p>
                )}
              </div>

              {/* New Password */}
              <div>
                <label className="block text-sm text-gray-700 mb-1">
                  New Password (optional)
                </label>
                <input
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Leave blank to keep current"
                  className="w-full px-4 py-2 border border-gray-300 rounded text-gray-900 focus:outline-none focus:ring-2 focus:ring-indigo-300"
                />
              </div>

              {/* Form Buttons */}
              <div className="flex justify-between">
                <button
                  type="button"
                  onClick={() => {
                    setEditing(false);
                    setFormData(prev => ({ ...prev, password: '' }));
                    setUsernameError('');
                  }}
                  className="bg-gray-400 hover:bg-gray-500 text-white px-4 py-2 rounded"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving || !!usernameError}
                  className={`
                    bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded
                    ${saving || usernameError ? 'opacity-50 cursor-not-allowed' : ''}
                  `}
                >
                  {saving ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </>
  );
}

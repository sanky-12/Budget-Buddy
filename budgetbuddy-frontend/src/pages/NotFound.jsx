import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="w-screen h-screen bg-[#1f2937] flex items-center justify-center">
      <div className="bg-white p-10 rounded shadow-lg text-center max-w-md w-full">
        <h1 className="text-6xl font-extrabold text-red-600 mb-4">404</h1>
        <p className="text-xl text-gray-700 mb-6">
          Oops! The page you're looking for doesn't exist.
        </p>
        <Link
          to="/login"
          className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-2 rounded transition"
        >
          Go to Login
        </Link>
      </div>
    </div>
  );
}

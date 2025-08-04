import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';

export default function LayoutShell() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="h-screen w-screen bg-gray-50 overflow-hidden">
      {/* Sidebar (Fixed Left) */}
      <Sidebar onLogout={handleLogout} />

      {/* Header (Fixed Top, Right of Sidebar) */}
      <Header />

      {/* Main Content Area */}
      <main className="pt-16 pl-64 h-full overflow-y-auto">
        <div className="p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

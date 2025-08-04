import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Wallet,
  TrendingUp,
  Banknote,
  BarChart2,
  LogOut,
  UserIcon
} from 'lucide-react';

const Sidebar = ({ onLogout }) => {
  const menuItems = [
    { label: 'Dashboard', icon: <LayoutDashboard size={18} />, path: '/dashboard' },
    { label: 'Expenses', icon: <Wallet size={18} />, path: '/expenses' },
    { label: 'Income', icon: <TrendingUp size={18} />, path: '/income' },
    { label: 'Budgets', icon: <Banknote size={18} />, path: '/budgets' },
    { label: 'Analytics', icon: <BarChart2 size={18} />, path: '/analytics' },
    { label: 'Profile', icon: <UserIcon />, path: '/profile' }
  ];

  return (
    <aside className="w-64 bg-[#111827] text-white flex flex-col fixed top-0 bottom-0 left-0 z-50 pt-14">
      <nav className="flex-1 overflow-y-auto py-6 px-4 space-y-2">
        {menuItems.map((item) => (
          <NavLink
            key={item.label}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2 rounded hover:bg-gray-700 transition ${
                isActive ? 'bg-gray-700 font-semibold' : ''
              }`
            }
          >
            {item.icon}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
      <button
        onClick={onLogout}
        className="w-full flex items-center justify-center gap-2 py-3 text-sm bg-red-600 hover:bg-red-700"
      >
        <LogOut size={16} /> Logout
      </button>
    </aside>
  );
};

export default Sidebar;
import { useLocation } from 'react-router-dom';

const pageTitles = {
  '/dashboard': 'Dashboard',
  '/expenses': 'Expenses',
  '/income': 'Income',
  '/budgets': 'Budgets',
  '/analytics': 'Analytics',
  '/profile': 'Profile',
};

export default function Header() {
  const location = useLocation();
  const title = pageTitles[location.pathname] || 'BudgetBuddy';

  return (
    <header className="fixed top-0 left-64 right-0 h-24 bg-[#1f2937] text-white flex items-center px-6 shadow z-40">
      <h1 className="text-xl font-semibold">{title}</h1>
    </header>
  );
}

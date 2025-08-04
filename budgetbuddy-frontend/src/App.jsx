// import { BrowserRouter, Routes, Route } from 'react-router-dom';
// import Login from './pages/Login';
// import Register from './pages/Register'; // 👈 import at top
// import Dashboard from './pages/Dashboard';
// import Expenses from './pages/Expenses';
// import Income from './pages/Income';
// import Budgets from './pages/Budget';
// import Analytics from './pages/Analytics';
// import ProtectedRoute from './routes/ProtectedRoute';
// import NotFound from './pages/NotFound';
// import './App.css'

// function App() {
//   return (
//     <BrowserRouter>
//       <Routes>
//         <Route path="/" element={<Login />} />
//         <Route path="/login" element={<Login />} />
//         <Route path="/register" element={<Register />} />
//         <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
//         <Route path="/expenses" element={<ProtectedRoute><Expenses /></ProtectedRoute>} />
//         <Route path="/income" element={<ProtectedRoute><Income /></ProtectedRoute>} />
//         <Route path="/budgets" element={<ProtectedRoute><Budgets /></ProtectedRoute>} />
//         <Route path="/analytics" element={<ProtectedRoute><Analytics /></ProtectedRoute>} />
//         <Route path="*" element={<NotFound />} />
//       </Routes>
//     </BrowserRouter>
//   );
// }

// export default App;

import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Expenses from './pages/Expenses';
import Income from './pages/Income';
import Budgets from './pages/Budget';
import Analytics from './pages/Analytics';
import Register from './pages/Register';
import ProtectedRoute from './routes/ProtectedRoute';
import LayoutShell from './layout/LayoutShell';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          element={
            <ProtectedRoute>
              <LayoutShell />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/expenses" element={<Expenses />} />
          <Route path="/income" element={<Income />} />
          <Route path="/budgets" element={<Budgets />} />
          <Route path="/analytics" element={<Analytics />} />
          <Route path="/profile" element={<Profile />} />
        </Route>
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
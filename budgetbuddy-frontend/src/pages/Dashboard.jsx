import { useEffect, useState } from 'react';
import analyticsApi from '../services/analyticsService';
import expenseApi from '../services/expenseService';
import incomeApi from '../services/incomeService';
import { LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from 'recharts';

export default function Dashboard() {
  const [analytics, setAnalytics] = useState(null);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchAnalytics();
    fetchRecentTransactions();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const response = await analyticsApi.get('/analytics/summary');
      setAnalytics(response.data);
    } catch (error) {
      console.error('Error fetching analytics:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchRecentTransactions = async () => {
    try {
      const [expensesRes, incomesRes] = await Promise.all([
        expenseApi.get('/expenses'),
        incomeApi.get('/income')
      ]);
      const expenses = expensesRes.data.map(txn => ({
        ...txn,
        type: 'Expense'
      }));
      const incomes = incomesRes.data.map(txn => ({
        ...txn,
        type: 'Income'
      }));
      const combined = [...expenses, ...incomes]
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .slice(0, 5);
      setRecentTransactions(combined);
    } catch (error) {
      console.error('Error fetching transactions:', error);
    }
  };

  const generateLineChartData = () => {
    if (!recentTransactions.length) return [];

    const grouped = recentTransactions.reduce((acc, txn) => {
      const date = new Date(txn.date).toISOString().split('T')[0];
      if (!acc[date]) acc[date] = { date, income: 0, expense: 0 };
      if (txn.type === 'Income') acc[date].income += txn.amount;
      if (txn.type === 'Expense') acc[date].expense += txn.amount;
      return acc;
    }, {});

    return Object.values(grouped).sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  const lineData = generateLineChartData();

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Welcome to Your Dashboard</h1>

      {loading || !analytics ? (
        <p className="text-gray-500">Loading...</p>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div className="bg-white p-6 rounded shadow text-center">
              <h2 className="text-lg font-semibold text-gray-700">Total Income</h2>
              <p className="text-2xl text-green-600 font-bold mt-2">
                ${analytics.totalIncome.toFixed(2)}
              </p>
            </div>
            <div className="bg-white p-6 rounded shadow text-center">
              <h2 className="text-lg font-semibold text-gray-700">Total Expenses</h2>
              <p className="text-2xl text-red-500 font-bold mt-2">
                ${analytics.totalExpenses.toFixed(2)}
              </p>
            </div>
            <div className="bg-white p-6 rounded shadow text-center">
              <h2 className="text-lg font-semibold text-gray-700">Net Savings</h2>
              <p className={`text-2xl font-bold mt-2 ${analytics.netSavings >= 0 ? 'text-green-600' : 'text-red-500'}`}>
                ${analytics.netSavings.toFixed(2)}
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Line Chart */}
            <div className="bg-white p-6 rounded shadow">
              <h2 className="text-xl font-bold text-gray-700 mb-4">Income vs Expenses (Recent)</h2>
              {lineData.length === 0 ? (
                <p className="text-gray-500">No transaction data available.</p>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={lineData} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip formatter={(value) => `$${value.toFixed(2)}`} />
                    <CartesianGrid stroke="#e5e7eb" strokeDasharray="3 3" />
                    <Line type="monotone" dataKey="income" stroke="#10B981" strokeWidth={2} name="Income" />
                    <Line type="monotone" dataKey="expense" stroke="#EF4444" strokeWidth={2} name="Expense" />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>

            {/* Recent Transactions */}
            <div className="bg-white p-6 rounded shadow">
              <h2 className="text-xl font-bold text-gray-700 mb-4">Recent Transactions</h2>
              <ul className="divide-y divide-gray-200">
                {recentTransactions.map((txn, index) => (
                  <li key={index} className="py-3 flex justify-between items-center">
                    <div>
                      <p className="text-sm font-medium text-gray-800">
                        {txn.type}: {txn.description || txn.source}
                      </p>
                      <p className="text-xs text-gray-500">{new Date(txn.date).toLocaleDateString()}</p>
                    </div>
                    <div className={`text-sm font-semibold ${txn.type === 'Income' ? 'text-green-600' : 'text-red-500'}`}>
                      {txn.type === 'Income' ? '+' : '-'}${txn.amount.toFixed(2)}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
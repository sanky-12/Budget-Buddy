// src/pages/Analytics.jsx
import { useEffect, useState, useCallback } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { PieChart, Pie, Cell, Tooltip, Legend } from 'recharts';
import analyticsApi from '../services/analyticsService';

// Color palette for Pie slices:
const COLORS = [
  '#0088FE', '#00C49F', '#FFBB28', '#FF8042',
  '#8B5CF6', '#EC4899', '#F59E0B', '#10B981',
  '#3B82F6', '#EF4444', '#6366F1', '#F43F5E'
];

// Small spinner component
function Spinner() {
  return (
    <svg className="animate-spin h-8 w-8 text-gray-500" viewBox="0 0 24 24">
      <circle
        className="opacity-25"
        cx="12" cy="12" r="10"
        stroke="currentColor" strokeWidth="4" fill="none"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8v4a4 4 0 100 8v4a8 8 0 01-8-8z"
      />
    </svg>
  );
}

// Card for total income/expense/savings
function SummaryCard({ label, value, color }) {
  return (
    <div className="bg-white p-6 rounded shadow text-center">
      <h2 className="text-lg font-semibold text-gray-700">{label}</h2>
      <p className={`text-2xl font-bold mt-2 ${color}`}>{value}</p>
    </div>
  );
}

// Horizontal progress bar
function ProgressBar({ label, used, total, percent }) {
  const safePercent = Math.min(Math.max(percent, 0), 100);
  const barColor = percent > 100 ? 'bg-red-500' : 'bg-green-500';

  return (
    <div>
      <div className="flex justify-between mb-1">
        <span className="text-sm font-medium text-gray-700">{label}</span>
        <span className="text-sm font-medium text-gray-600">
          ${used.toFixed(2)} / ${total.toFixed(2)} ({percent.toFixed(1)}%)
        </span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-4 overflow-hidden">
        <div
          className={`h-full ${barColor}`}
          style={{ width: `${safePercent}%` }}
        />
      </div>
    </div>
  );
}

export default function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [availableMonths, setAvailableMonths] = useState([]); // ['2025-06', '2025-05', ...]
  const [selectedDate, setSelectedDate] = useState(null);     // Date object or null
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Load saved month from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('analyticsMonth');
    if (saved) {
      setSelectedDate(new Date(`${saved}-01`));
    }
    fetchMonths();
  }, []);

  // Fetch all months that have analytics data
  const fetchMonths = async () => {
    try {
      const { data } = await analyticsApi.get('/analytics/available-months');
      setAvailableMonths(data);
    } catch (err) {
      console.error('Failed to fetch available months:', err);
    }
  };

  // Fetch analytics whenever selectedDate changes
  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      // Persist to localStorage
      const monthYear = selectedDate
        ? selectedDate.toISOString().slice(0, 7)
        : '';
      localStorage.setItem('analyticsMonth', monthYear);

      // Build endpoint
      const endpoint = selectedDate
        ? `/analytics/summary?monthYear=${monthYear}`
        : '/analytics/summary';

      const { data } = await analyticsApi.get(endpoint);
      setAnalytics(data);
    } catch (err) {
      console.error('Error fetching analytics:', err);
      setError('Failed to load analytics.');
    } finally {
      setLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    // Only fetch analytics after we've populated availableMonths (so dropdown displays)
    if (availableMonths.length >= 0) {
      fetchAnalytics();
    }
  }, [availableMonths, fetchAnalytics]);

  // Responsive pie width
  const chartWidth = Math.min(window.innerWidth - 64, 700);
  const chartHeight = 300;

  // Derive text color
  const savingsColor =
    analytics?.netSavings >= 0 ? 'text-green-600' : 'text-red-500';

  // Empty / loading / error states
  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center h-screen bg-gray-50">
        <Spinner />
      </div>
    );
  }

  const monthDisplay = selectedDate
    ? selectedDate.toLocaleString('default', { month: 'long', year: 'numeric' })
    : 'All Time';

  if (!analytics || analytics.budgetUsage.length === 0) {
    return (
      <div className="p-6 bg-gray-50 min-h-screen">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Your Analytics</h1>
          <DatePicker
            selected={selectedDate}
            onChange={(date) => setSelectedDate(date)}
            dateFormat="MMMM yyyy"
            showMonthYearPicker
            placeholderText="All Time"
            className="border border-gray-300 rounded px-3 py-1 bg-white text-gray-800"
          />
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-100 text-red-700 rounded flex justify-between">
            <span>{error}</span>
            <button onClick={fetchAnalytics} className="underline">
              Retry
            </button>
          </div>
        )}

        <div className="text-center text-gray-500 py-16">
          <p>No analytics data for <strong>{monthDisplay}</strong>.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      {/* Header + Month Picker */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Your Analytics</h1>
        <DatePicker
          selected={selectedDate}
          onChange={(date) => setSelectedDate(date)}
          dateFormat="MMMM yyyy"
          showMonthYearPicker
          placeholderText="All Time"
          className="border border-gray-300 rounded px-3 py-1 bg-white text-gray-800"
          aria-label="Select month"
        />
      </div>

      {/* Error Banner */}
      {error && (
        <div className="mb-6 p-4 bg-red-100 text-red-700 rounded flex justify-between">
          <span>{error}</span>
          <button onClick={fetchAnalytics} className="underline">
            Retry
          </button>
        </div>
      )}

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-10">
        <SummaryCard
          label="Total Income"
          value={`$${analytics.totalIncome.toFixed(2)}`}
          color="text-green-600"
        />
        <SummaryCard
          label="Total Expenses"
          value={`$${analytics.totalExpenses.toFixed(2)}`}
          color="text-red-500"
        />
        <SummaryCard
          label="Net Savings"
          value={`$${analytics.netSavings.toFixed(2)}`}
          color={savingsColor}
        />
      </div>

      {/* Budget Usage */}
      <div className="bg-white p-6 rounded shadow mb-10">
        <h2 className="text-xl font-bold text-gray-700 mb-4">
          Budget Usage ({monthDisplay})
        </h2>
        <div className="space-y-6">
          {analytics.budgetUsage.map((usage, idx) => (
            <ProgressBar
              key={`${usage.category}-${idx}`}
              label={usage.category}
              used={usage.spent}
              total={usage.limit}
              percent={usage.percentUsed}
            />
          ))}
        </div>
      </div>

      {/* Pie Chart */}
      <div className="bg-white p-6 rounded shadow">
        <h2 className="text-xl font-bold text-gray-700 mb-4">Expense Distribution</h2>
        <div className="flex justify-center">
          <PieChart width={chartWidth} height={chartHeight}>
            <Pie
              data={analytics.budgetUsage}
              dataKey="spent"
              nameKey="category"
              cx="50%"
              cy="50%"
              outerRadius={Math.min(chartHeight / 2 - 20, chartWidth / 4)}
              label={({ name, percent }) => `${name}: ${Math.round(percent * 100)}%`}
              isAnimationActive
            >
              {analytics.budgetUsage.map((_, idx) => (
                <Cell key={idx} fill={COLORS[idx % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip formatter={(val) => `$${val.toFixed(2)}`} />
            <Legend verticalAlign="bottom" height={36} />
          </PieChart>
        </div>
      </div>
    </div>
  );
}

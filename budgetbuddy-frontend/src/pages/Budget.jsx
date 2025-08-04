import { useEffect, useState, useMemo } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import toast, { Toaster } from 'react-hot-toast';
import budgetApi from '../services/budgetService';
import { categories } from '../constants/categories';

// Format: "YYYY-MM"
const FORMAT_MONTH = (date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

export default function Budgets() {
  const [budgets, setBudgets]           = useState([]);
  const [loading, setLoading]           = useState(true);
  const [formDataList, setFormDataList] = useState([]);
  const [selectedMonth, setSelectedMonth] = useState(() => {
    const saved = localStorage.getItem('lastBudgetMonth');
    return saved || FORMAT_MONTH(new Date());
  });
  const [pickerDate, setPickerDate]     = useState(() => {
    const [y, m] = selectedMonth.split('-');
    return new Date(Number(y), Number(m) - 1, 1);
  });
  const [creating, setCreating] = useState(false);
  const [error, setError]       = useState(null);
  const [editingId, setEditingId]     = useState(null);
  const [draftAmount, setDraftAmount] = useState('');

  // Derived data:
  const existingBudgets = useMemo(
    () => budgets.filter(b => b.monthYear === selectedMonth),
    [budgets, selectedMonth]
  );
  const totalBudget   = useMemo(
    () => existingBudgets.reduce((sum, b) => sum + b.limitAmount, 0),
    [existingBudgets]
  );
  const categoryCount = categories.length;
  const averageBudget = categoryCount ? (totalBudget / categoryCount) : 0;
  const isAlreadySet  = existingBudgets.length > 0;

  // Persist month
  useEffect(() => {
    localStorage.setItem('lastBudgetMonth', selectedMonth);
  }, [selectedMonth]);

  // Fetch budgets on month change
  useEffect(() => {
    fetchBudgets();
    setCreating(false);
    setEditingId(null);
    setError(null);
  }, [selectedMonth]);

  async function fetchBudgets() {
    setLoading(true);
    try {
      const { data } = await budgetApi.get('/budgets');
      setBudgets(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Failed to load budgets.');
    } finally {
      setLoading(false);
    }
  }

  // Create form
  function initializeForm() {
    setFormDataList(
      categories.map(cat => ({
        category:   cat,
        limitAmount:'',
        monthYear:  selectedMonth
      }))
    );
    setCreating(true);
    setError(null);
  }

  // Bulk save
  async function handleSave() {
    if (!formDataList.every(b => b.limitAmount !== '' && parseFloat(b.limitAmount) >= 0)) {
      setError('All amounts must be zero or positive.');
      return;
    }
    const payload = formDataList.map(b => ({
      category:    b.category,
      limitAmount: parseFloat(b.limitAmount),
      monthYear:   b.monthYear
    }));
    try {
      await budgetApi.post('/budgets/bulk', payload);
      setError(null);
      toast.success('Budgets saved!');
      await fetchBudgets();
      setCreating(false);
    } catch {
      setError('Failed to save budgets.');
    }
  }

  // Copy
  const prevMonth = (() => {
    const [y,m] = selectedMonth.split('-');
    return new Date(y, m - 2).toISOString().slice(0,7);
  })();
  const hasPrev = useMemo(() => budgets.some(b => b.monthYear === prevMonth),[budgets,prevMonth]);

  async function handleCopyPreviousMonth() {
    if (!hasPrev) {
      setError(`No budgets for ${prevMonth}`);
      return;
    }
    try {
      await budgetApi.post(`/budgets/copy?from=${prevMonth}&to=${selectedMonth}`);
      setError(null);
      toast.success(`Copied from ${prevMonth}!`);
      await fetchBudgets();
    } catch {
      setError('Copy failed.');
    }
  }

  // Inline save
  async function handleInlineSave(id) {
    const amt = parseFloat(draftAmount);
    if (isNaN(amt) || amt < 0) {
      setError('Enter valid non-negative number');
      return;
    }
    try {
      const original = existingBudgets.find(b => b.id === id);
      await budgetApi.put(`/budgets/${id}`, { ...original, limitAmount: amt });
      setError(null);
      toast.success('Category updated!');
      await fetchBudgets();
      setEditingId(null);
    } catch {
      setError('Failed to update category.');
    }
  }

  const createIsValid = formDataList.every(b => b.limitAmount !== '' && !isNaN(parseFloat(b.limitAmount)));

  return (
    <div className="p-6">
      <Toaster position="top-right" />

      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Monthly Budgets</h1>
        <div className="flex items-center gap-4">
          <DatePicker
            selected={pickerDate}
            onChange={date => {
              setPickerDate(date);
              const fmt = FORMAT_MONTH(date);
              setSelectedMonth(fmt);
            }}
            dateFormat="MMMM yyyy"
            showMonthYearPicker
            className="border border-gray-300 p-2 rounded text-gray-800 bg-white"
          />
          {!creating && !isAlreadySet && (
            <>
              <button onClick={initializeForm}
                className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700">
                Create
              </button>
              {hasPrev && (
                <button onClick={handleCopyPreviousMonth}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                  Copy Previous
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* Summaries */}
      {!loading && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
          <div className="bg-white p-4 rounded shadow text-center">
            <h3 className="text-gray-600">Total Budgeted</h3>
            <p className="text-2xl font-bold text-blue-600">${totalBudget.toFixed(2)}</p>
          </div>
          <div className="bg-white p-4 rounded shadow text-center">
            <h3 className="text-gray-600">Categories</h3>
            <p className="text-2xl font-bold text-gray-800">{categoryCount}</p>
          </div>
          {categoryCount>0 && (
            <div className="bg-white p-4 rounded shadow text-center">
              <h3 className="text-gray-600">Avg/Category</h3>
              <p className="text-2xl font-bold text-green-600">${averageBudget.toFixed(2)}</p>
            </div>
          )}
        </div>
      )}

      {/* Error */}
      {error && (
        <div className="mb-4 p-3 bg-red-100 text-red-700 rounded">{error}</div>
      )}

      {/* Loading */}
      {loading ? (
        <p className="text-gray-600">Loading budgets…</p>

      // {/* Create Mode */}
      ) : creating ? (
        <div className="bg-white shadow rounded-lg p-4">
          <table className="w-full text-sm text-gray-800">
            <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
              <tr>
                <th className="px-4 py-2 text-left">Category</th>
                <th className="px-4 py-2 text-left">Amount</th>
              </tr>
            </thead>
            <tbody>
              {formDataList.map((row,i) => (
                <tr key={row.category} className="border-b">
                  <td className="px-4 py-2">{row.category}</td>
                  <td className="px-4 py-2">
                    <input
                      type="number"
                      min="0"
                      value={row.limitAmount}
                      onChange={e => {
                        const val = e.target.value;
                        const upd = [...formDataList];
                        upd[i].limitAmount = val;
                        setFormDataList(upd);
                      }}
                      className={`border p-1 rounded w-28 text-gray-800 ${
                        (row.limitAmount==='')||parseFloat(row.limitAmount)<0
                          ? 'border-red-500'
                          : ''
                      }`}
                      required
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="mt-4 flex justify-end gap-2">
            <button
              onClick={handleSave}
              disabled={!createIsValid}
              className="bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Save
            </button>
            <button
              onClick={() => setCreating(false)}
              className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
            >
              Cancel
            </button>
          </div>
        </div>

      // {/* View/Inline Edit Mode */}
      ) : isAlreadySet ? (
        <div className="bg-white shadow rounded-lg p-4">
          <table className="w-full text-sm text-gray-800">
            <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
              <tr>
                <th className="px-4 py-2 text-left">Category</th>
                <th className="px-4 py-2 text-left">Amount</th>
                <th className="px-4 py-2 text-left">Action</th>
              </tr>
            </thead>
            <tbody>
              {existingBudgets.map(b => {
                const isRowEditing = editingId === b.id;
                return (
                  <tr key={b.id} className="border-b">
                    <td className="px-4 py-2">{b.category}</td>
                    <td className="px-4 py-2">
                      {isRowEditing
                        ? (
                          <input
                            type="number"
                            min="0"
                            className="border border-gray-300 p-1 rounded w-28 text-gray-800"
                            value={draftAmount}
                            onChange={e => setDraftAmount(e.target.value)}
                          />
                        )
                        : `$${b.limitAmount.toFixed(2)}`
                      }
                    </td>
                    <td className="px-4 py-2 space-x-2">
                      {isRowEditing
                        ? <>
                            <button
                              onClick={() => handleInlineSave(b.id)}
                              className="bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 text-sm"
                            >Save</button>
                            <button
                              onClick={()=>{
                                setEditingId(null);
                                setError(null);
                              }}
                              className="bg-gray-400 text-white px-3 py-1 rounded hover:bg-gray-500 text-sm"
                            >Cancel</button>
                          </>
                        : (
                          <button
                            onClick={()=>{
                              setEditingId(b.id);
                              setDraftAmount(b.limitAmount.toString());
                              setError(null);
                            }}
                            className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 text-sm"
                          >Edit</button>
                        )
                      }
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

      ) : (
        <p className="text-gray-600">No budgets set for {selectedMonth}.</p>
      )}
    </div>
  );
}

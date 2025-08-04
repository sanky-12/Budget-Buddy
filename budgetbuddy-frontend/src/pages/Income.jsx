// src/pages/Income.jsx
import { useEffect, useState, useMemo } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import toast, { Toaster } from 'react-hot-toast';
import incomeApi from '../services/incomeService';

const incomeCategories = [
  "Salary", "Bonus", "Interest", "Investment Returns", "Gift",
  "Freelancing", "Rental Income", "Other"
];
const ITEMS_PER_PAGE = 5;

export default function Income() {
  // --- State ---
  const [incomes, setIncomes]     = useState([]);
  const [loading, setLoading]     = useState(true);
  const [filters, setFilters]     = useState({ startDate: null, endDate: null });
  const [sortBy, setSortBy]       = useState('date');
  const [sortDir, setSortDir]     = useState('desc');
  const [page, setPage]           = useState(1);

  // Form State
  const [formData, setFormData]   = useState({
    id: '',
    source: '',
    amount: '',
    date: null
  });
  const [showForm, setShowForm]   = useState(false);
  const [formMode, setFormMode]   = useState('add'); // 'add' | 'edit'

  // --- Fetch with filters ---
  const fetchIncomes = async () => {
    setLoading(true);
    try {
      let url = '/income';
      const params = [];
      if (filters.startDate && filters.endDate) {
        // build YYYY-MM-DD
        const sd = filters.startDate.toISOString().split('T')[0];
        const ed = filters.endDate  .toISOString().split('T')[0];
        params.push(`startDate=${sd}`);
        params.push(`endDate=${ed}`);
      }
      if (params.length) {
        url += `?${params.join('&')}`;
      }

      const { data } = await incomeApi.get(url);
      setIncomes(Array.isArray(data) ? data : []);
      setPage(1);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load incomes.');
    } finally {
      setLoading(false);
    }
  };

  // initial load
  useEffect(() => {
    fetchIncomes();
  }, []);

  // --- Sorting & Pagination ---
  const sorted = useMemo(() => {
    return [...incomes].sort((a, b) => {
      let A = a[sortBy], B = b[sortBy];
      // if sorting by date, compare YYYY-MM-DD
      if (sortBy === 'date') {
        A = a.date.split('T')[0];
        B = b.date.split('T')[0];
      }
      if (sortDir === 'asc')  return A > B ? 1 : -1;
      else                   return A < B ? 1 : -1;
    });
  }, [incomes, sortBy, sortDir]);

  const paged = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return sorted.slice(start, start + ITEMS_PER_PAGE);
  }, [sorted, page]);

  const totalPages = Math.ceil(incomes.length / ITEMS_PER_PAGE);

  // --- Handlers ---
  const handleFilterChange = (name, value) => {
    setFilters(f => ({ ...f, [name]: value }));
  };

  const applyFilters = e => {
    e.preventDefault();
    // only if both picked
    if (filters.startDate && filters.endDate) {
      fetchIncomes();
    }
  };

  const toggleSort = fld => {
    if (sortBy === fld) {
      setSortDir(d => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(fld);
      setSortDir('asc');
    }
  };

  // --- Form Open/Edit ---
  const openAdd = () => {
    setFormMode('add');
    setFormData({ id: '', source: '', amount: '', date: null });
    setShowForm(true);
  };

  const openEdit = inc => {
    setFormMode('edit');
    setFormData({
      id: inc.id,
      source: inc.source,
      amount: inc.amount.toString(),
      date: new Date(inc.date.split('T')[0])
    });
    setShowForm(true);
  };

  // Form change
  const handleFormChange = (name, value) => {
    setFormData(d => ({ ...d, [name]: value }));
  };

  // form validity
  const formIsValid =
    formData.source !== '' &&
    parseFloat(formData.amount) > 0 &&
    formData.date instanceof Date;

  // Submit add/edit
  const handleFormSubmit = async e => {
    e.preventDefault();
    if (!formIsValid) return;

    const payload = {
      source: formData.source,
      amount: parseFloat(formData.amount),
      date: formData.date.toISOString().split('T')[0]
    };

    try {
      if (formMode === 'edit') {
        await incomeApi.put(`/income/${formData.id}`, payload);
        toast.success('Income updated');
      } else {
        await incomeApi.post('/income', payload);
        toast.success('Income added');
      }
      setShowForm(false);
      fetchIncomes();
    } catch (err) {
      console.error(err);
      toast.error('Failed to save income.');
    }
  };

  // Delete
  const handleDelete = async id => {
    if (!window.confirm('Delete this record?')) return;
    try {
      await incomeApi.delete(`/income/${id}`);
      toast.success('Income deleted');
      fetchIncomes();
    } catch (err) {
      console.error(err);
      toast.error('Failed to delete.');
    }
  };

  // --- Render ---
  return (
    <>
      <Toaster position="top-right" />

      <div className="p-6 bg-gray-50 min-h-screen">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Your Income</h1>
          <button
            onClick={openAdd}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded"
          >
            Add Income
          </button>
        </div>

        {/* Filters */}
        <form
          onSubmit={applyFilters}
          className="bg-white p-4 rounded shadow mb-6 grid grid-cols-1 md:grid-cols-3 gap-4"
        >
          <DatePicker
            selected={filters.startDate}
            onChange={d => handleFilterChange('startDate', d)}
            dateFormat="yyyy-MM-dd"
            placeholderText="Start Date"
            className="border p-2 rounded w-full text-gray-800 bg-white"
          />
          <DatePicker
            selected={filters.endDate}
            onChange={d => handleFilterChange('endDate', d)}
            dateFormat="yyyy-MM-dd"
            minDate={filters.startDate}
            placeholderText="End Date"
            className="border p-2 rounded w-full text-gray-800 bg-white"
          />
          <button
            type="submit"
            disabled={!(filters.startDate && filters.endDate)}
            className="bg-gray-600 hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded"
          >
            Apply Filters
          </button>
        </form>

        {/* Loading / Empty / Table */}
        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : paged.length === 0 ? (
          <p className="text-gray-500">No records found.</p>
        ) : (
          <div className="overflow-x-auto bg-white shadow rounded-lg">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                <tr>
                  {['source','amount','date'].map(fld => (
                    <th
                      key={fld}
                      className="px-6 py-3 cursor-pointer"
                      onClick={() => toggleSort(fld)}
                    >
                      {fld.charAt(0).toUpperCase() + fld.slice(1)}
                      {sortBy===fld && (sortDir==='asc'?' ▲':' ▼')}
                    </th>
                  ))}
                  <th className="px-6 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {paged.map(inc => (
                  <tr key={inc.id} className="border-b hover:bg-gray-50">
                    <td className="px-6 py-4 text-gray-800">{inc.source}</td>
                    <td className="px-6 py-4 text-gray-800">
                      ${inc.amount.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-gray-800">
                      {inc.date.split('T')[0]}
                    </td>
                    <td className="px-6 py-4 space-x-2">
                      <button
                        onClick={() => openEdit(inc)}
                        className="text-sm px-3 py-1 bg-yellow-400 hover:bg-yellow-500 rounded text-white"
                      >Edit</button>
                      <button
                        onClick={() => handleDelete(inc.id)}
                        className="text-sm px-3 py-1 bg-red-500 hover:bg-red-600 rounded text-white"
                      >Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {incomes.length > ITEMS_PER_PAGE && (
          <div className="flex justify-center mt-4 gap-4">
            <button
              disabled={page===1}
              onClick={()=>setPage(p=>p-1)}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >Previous</button>
            <span className="text-gray-700">Page {page} of {totalPages}</span>
            <button
              disabled={page===totalPages}
              onClick={()=>setPage(p=>p+1)}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >Next</button>
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      {showForm && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
        >
          <div className="bg-white p-6 rounded shadow-lg w-full max-w-md">
            <h2 className="text-xl font-bold mb-4 text-gray-800">
              {formMode==='edit'?'Edit Income':'Add Income'}
            </h2>
            <form onSubmit={handleFormSubmit} className="space-y-4">
              {/* Source */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Source</label>
                <select
                  name="source"
                  value={formData.source || ''}
                  onChange={e=>handleFormChange('source', e.target.value)}
                  required
                  className="w-full border border-gray-300 rounded p-2 mt-1 text-gray-800"
                >
                  <option value="">Select source</option>
                  {incomeCategories.map(src=>(
                    <option key={src} value={src}>{src}</option>
                  ))}
                </select>
              </div>

              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Amount</label>
                <input
                  name="amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={formData.amount}
                  onChange={e=>handleFormChange('amount', e.target.value)}
                  required
                  className="w-full border border-gray-300 rounded p-2 mt-1 text-gray-800"
                />
              </div>

              {/* Date */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Date</label>
                <DatePicker
                  selected={formData.date}
                  onChange={d=>handleFormChange('date', d)}
                  dateFormat="yyyy-MM-dd"
                  maxDate={new Date()}
                  required
                  className="w-full border border-gray-300 rounded p-2 mt-1 text-gray-800 bg-white"
                />
              </div>

              {/* Buttons */}
              <div className="flex justify-end space-x-2 mt-4">
                <button
                  type="button"
                  onClick={()=>setShowForm(false)}
                  className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
                >Cancel</button>
                <button
                  type="submit"
                  disabled={!formIsValid}
                  className="bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded hover:bg-green-700"
                >Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

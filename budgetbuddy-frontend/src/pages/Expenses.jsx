// src/pages/Expenses.jsx
import { useEffect, useState, useMemo } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import toast, { Toaster } from 'react-hot-toast';
import expenseApi from '../services/expenseService';
import { categories } from '../constants/categories';

const ITEMS_PER_PAGE = 5;

export default function Expenses() {
  // Data + Filters
  const [expenses, setExpenses]         = useState([]);
  const [loading, setLoading]           = useState(true);
  const [filters, setFilters]           = useState({ category: '', startDate: null, endDate: null });
  // Pagination + Sorting
  const [page, setPage]                 = useState(1);
  const [sortBy, setSortBy]             = useState('date');
  const [sortDir, setSortDir]           = useState('desc');
  // Add/Edit Form
  const [formData, setFormData]         = useState({ id: '', description: '', amount: '', category: '', date: null });
  const [showForm, setShowForm]         = useState(false);
  const [formMode, setFormMode]         = useState('add'); // 'add' | 'edit'
  // Input validation state
  const [formErrors, setFormErrors]     = useState({});

  // Fetch expenses with filters
  const fetchExpenses = async () => {
    setLoading(true);
    try {
      let url = '/expenses';
      const params = [];
      if (filters.category) {
        params.push(`category=${encodeURIComponent(filters.category)}`);
      }
      if (filters.startDate && filters.endDate) {
        const sd = filters.startDate.toISOString().split('T')[0];
        const ed = filters.endDate.toISOString().split('T')[0];
        params.push(`startDate=${sd}&endDate=${ed}`);
      }
      if (params.length) url += `?${params.join('&')}`;

      const { data } = await expenseApi.get(url);
      setExpenses(Array.isArray(data) ? data : []);
      setPage(1);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load expenses.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExpenses();
  }, []);

  // Sorting + pagination
  const sorted = useMemo(() => {
    return [...expenses].sort((a, b) => {
      let A = a[sortBy], B = b[sortBy];
      // if sorting by date, compare strings YYYY-MM-DD
      if (sortBy === 'date') {
        A = a.date.split('T')[0];
        B = b.date.split('T')[0];
      }
      if (sortDir === 'asc') return A > B ? 1 : -1;
      return A < B ? 1 : -1;
    });
  }, [expenses, sortBy, sortDir]);

  const paged = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return sorted.slice(start, start + ITEMS_PER_PAGE);
  }, [sorted, page]);

  const totalPages = Math.ceil(expenses.length / ITEMS_PER_PAGE);

  // Handlers
  const handleFilterChange = (name, value) => {
    setFilters(f => ({ ...f, [name]: value }));
  };

  const applyFilters = e => {
    e.preventDefault();
    if (filters.startDate && filters.endDate) {
      fetchExpenses();
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

  // Open Add or Edit form
  const openAdd = () => {
    setFormMode('add');
    setFormData({ id: '', description: '', amount: '', category: '', date: null });
    setFormErrors({});
    setShowForm(true);
  };
  const openEdit = exp => {
    setFormMode('edit');
    setFormData({
      id: exp.id,
      description: exp.description,
      amount: exp.amount,
      category: exp.category,
      date: new Date(exp.date.split('T')[0])
    });
    setFormErrors({});
    setShowForm(true);
  };

  // Form input change & validation
  const validateField = (name, value) => {
    let err = '';
    if (!value || (name === 'amount' && parseFloat(value) <= 0)) {
      err = name === 'description'
        ? 'Required'
        : name === 'amount'
        ? 'Must be > 0'
        : 'Required';
    }
    return err;
  };

  const handleFormChange = (name, value) => {
    setFormData(d => ({ ...d, [name]: value }));
    // validate on change
    setFormErrors(errs => ({ ...errs, [name]: validateField(name, value) }));
  };

  // Check form validity
  const formIsValid = useMemo(() => {
    const errs = {};
    errs.description = validateField('description', formData.description);
    errs.amount      = validateField('amount', formData.amount);
    errs.category    = validateField('category', formData.category);
    errs.date        = formData.date ? '' : 'Required';
    setFormErrors(errs);

    return Object.values(errs).every(e => !e);
  }, [formData]);

  // Submit Add/Edit
  const handleFormSubmit = async e => {
    e.preventDefault();
    if (!formIsValid) return;

    try {
      const body = {
        description: formData.description,
        amount: parseFloat(formData.amount),
        category: formData.category,
        date: formData.date.toISOString().split('T')[0]
      };

      if (formMode === 'edit') {
        await expenseApi.put(`/expenses/${formData.id}`, body);
        toast.success('Expense updated');
      } else {
        await expenseApi.post('/expenses', body);
        toast.success('Expense added');
      }

      setShowForm(false);
      fetchExpenses();
    } catch (err) {
      console.error(err);
      toast.error('Failed to save expense.');
    }
  };

  // Delete
  const handleDelete = async id => {
    if (!window.confirm('Delete this expense?')) return;
    try {
      await expenseApi.delete(`/expenses/${id}`);
      toast.success('Expense deleted');
      fetchExpenses();
    } catch (err) {
      console.error(err);
      toast.error('Failed to delete.');
    }
  };

  // JSX
  return (
    <>
      <Toaster position="top-right" />

      <div className="p-6 bg-gray-50 min-h-screen">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Your Expenses</h1>
          <button
            onClick={openAdd}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
          >
            Add Expense
          </button>
        </div>

        {/* Filters */}
        <form
          onSubmit={applyFilters}
          className="bg-white p-4 rounded shadow mb-6 grid grid-cols-1 md:grid-cols-4 lg:grid-cols-6 gap-4"
        >
          {/* Category */}
          <select
            value={filters.category}
            onChange={e => handleFilterChange('category', e.target.value)}
            className="border p-2 rounded bg-white text-gray-800 focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Categories</option>
            {categories.sort().map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>

          {/* Start Date */}
          <DatePicker
            selected={filters.startDate}
            onChange={date => handleFilterChange('startDate', date)}
            // onFocus={e => e.currentTarget.showPicker?.()}
            dateFormat="yyyy-MM-dd"
            placeholderText="Start Date"
            className="border p-2 rounded w-full text-gray-800 bg-white"
          />

          {/* End Date */}
          <DatePicker
            selected={filters.endDate}
            onChange={date => handleFilterChange('endDate', date)}
            // onFocus={e => e.currentTarget.showPicker?.()}
            minDate={filters.startDate}
            dateFormat="yyyy-MM-dd"
            placeholderText="End Date"
            className="border p-2 rounded w-full text-gray-800 bg-white"
          />

          {/* Apply */}
          <button
            type="submit"
            disabled={!(filters.startDate && filters.endDate)}
            className="bg-gray-600 hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-4 py-2 rounded"
          >
            Apply Filters
          </button>
        </form>

        {/* Loading */}
        {loading && <p className="text-gray-500">Loading...</p>}

        {/* Table */}
        {!loading && (
          <div className="overflow-x-auto bg-white shadow rounded-lg">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-gray-100 text-gray-600 uppercase text-xs">
                <tr>
                  {['description','category','amount','date'].map(fld => (
                    <th
                      key={fld}
                      className="px-6 py-3 cursor-pointer"
                      onClick={() => toggleSort(fld)}
                    >
                      {fld.charAt(0).toUpperCase() + fld.slice(1)}
                      {sortBy === fld && (sortDir==='asc' ? ' ▲' : ' ▼')}
                    </th>
                  ))}
                  <th className="px-6 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {paged.map(exp => (
                  <tr key={exp.id} className="border-b hover:bg-gray-50">
                    <td className="px-6 py-4 text-gray-800">{exp.description}</td>
                    <td className="px-6 py-4 text-gray-800">{exp.category}</td>
                    <td className="px-6 py-4 text-gray-800">
                      ${exp.amount.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-gray-800">
                      {exp.date.split('T')[0]}
                    </td>
                    <td className="px-6 py-4 space-x-2">
                      <button
                        onClick={() => openEdit(exp)}
                        className="text-sm px-3 py-1 rounded bg-yellow-400 hover:bg-yellow-500 text-white"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(exp.id)}
                        className="text-sm px-3 py-1 rounded bg-red-500 hover:bg-red-600 text-white"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}

                {paged.length === 0 && (
                  <tr>
                    <td colSpan={5} className="p-4 text-center text-gray-500">
                      No records found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {expenses.length > ITEMS_PER_PAGE && (
          <div className="flex justify-center mt-4 gap-4">
            <button
              disabled={page===1}
              onClick={()=>setPage(p=>p-1)}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >
              Previous
            </button>
            <span className="text-gray-700">
              Page {page} of {totalPages}
            </span>
            <button
              disabled={page===totalPages}
              onClick={()=>setPage(p=>p+1)}
              className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50"
            >
              Next
            </button>
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
          <div className="bg-white p-6 rounded shadow-lg w-full max-w-lg">
            <h2 className="text-xl font-bold mb-4 text-gray-800">
              {formMode==='edit' ? 'Edit Expense' : 'Add Expense'}
            </h2>

            <form onSubmit={handleFormSubmit} className="space-y-4">
              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Description</label>
                <input
                  name="description"
                  type="text"
                  value={formData.description}
                  onChange={e => handleFormChange('description', e.target.value)}
                  className={`w-full border p-2 rounded mt-1 text-gray-800 placeholder-gray-400 ${
                    formErrors.description ? 'border-red-500' : 'border-gray-300'
                  }`}
                  required
                />
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
                  onChange={e => handleFormChange('amount', e.target.value)}
                  className={`w-full border p-2 rounded mt-1 text-gray-800 placeholder-gray-400 ${
                    formErrors.amount ? 'border-red-500' : 'border-gray-300'
                  }`}
                  required
                />
              </div>

              {/* Category */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Category</label>
                <select
                  name="category"
                  value={formData.category}
                  onChange={e => handleFormChange('category', e.target.value)}
                  className={`w-full border p-2 rounded mt-1 text-gray-800 ${
                    formErrors.category ? 'border-red-500' : 'border-gray-300'
                  }`}
                  required
                >
                  <option value="">Select Category</option>
                  {categories.sort().map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>

              {/* Date */}
              <div>
                <label className="block text-sm font-medium text-gray-700">Date</label>
                <DatePicker
                  selected={formData.date}
                  onChange={date => handleFormChange('date', date)}
                  // onFocus={e => e.currentTarget.showPicker?.()}
                  dateFormat="yyyy-MM-dd"
                  maxDate={new Date()}
                  className={`w-full border p-2 rounded mt-1 text-gray-800 placeholder-gray-400 ${
                    formErrors.date ? 'border-red-500' : 'border-gray-300'
                  } bg-white`}
                  required
                />
              </div>

              {/* Buttons */}
              <div className="flex justify-end space-x-2 mt-4">
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!formIsValid}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {formMode==='edit' ? 'Save' : 'Add'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

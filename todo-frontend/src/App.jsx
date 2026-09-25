import { useState, useEffect } from 'react';
import api from './api';
import './App.css';
const capitalize = (str) =>
  str ? str.charAt(0).toUpperCase() + str.slice(1) : '';

function App() {
  const [user, setUser] = useState(null);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [todos, setTodos] = useState([]);
  const [newTitle, setNewTitle] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/auth/me')
      .then(res => setUser(res.data.username))
      .catch(() => setUser(null));
  }, []);

  useEffect(() => {
    if (user) loadTodos();
  }, [user]);

  const loadTodos = async () => {
    try {
      const res = await api.get('/todos');
      setTodos(res.data);
    } catch {
      setError('Failed to load todos');
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await api.post('/auth/login', { username, password });
      setUser(res.data.username);
    } catch {
      setError('Invalid username or password');
    }
  };

  const handleLogout = async () => {
    await api.post('/auth/logout');
    setUser(null);
    setTodos([]);
    setUsername('');
    setPassword('');
  };

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!newTitle.trim()) {
      setError('Title cannot be empty');
      return;
    }
    try {
      await api.post('/todos', { title: newTitle });
      setNewTitle('');
      setError('');
      loadTodos();
    } catch {
      setError('Failed to add todo');
    }
  };

  const handleToggle = async (todo) => {
    try {
      await api.put(`/todos/${todo.id}`, { completed: !todo.completed });
      loadTodos();
    } catch {
      setError('Failed to update todo');
    }
  };

  const handleRename = async (todo) => {
    const newName = prompt('New title:', todo.title);
    if (newName && newName.trim()) {
      try {
        await api.put(`/todos/${todo.id}`, { title: newName });
        loadTodos();
      } catch {
        setError('Failed to rename todo');
      }
    }
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/todos/${id}`);
      loadTodos();
    } catch {
      setError('Failed to delete todo');
    }
  };

  // ---------- LOGIN PAGE ----------
  if (!user) {
    return (
      <div className="page login-page">
        <div className="login-card">
          <h1>Login</h1>
          {error && <p className="error">{error}</p>}
          <form onSubmit={handleLogin}>
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={e => setUsername(e.target.value)}
              autoComplete="username"
            />
            <div className="password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="Password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="eye-button"
                onClick={() => setShowPassword(v => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? '🙈' : '👁'}
              </button>
            </div>
            <button type="submit" className="primary-button">Login</button>
          </form>
        </div>
      </div>
    );
  }

   // ---------- TODO PAGE ----------
  return (
    <div className="page todo-page">
      <header className="todo-header">
        <h1 className="welcome-heading">Welcome, {capitalize(user)}</h1>
        <button className="logout-button" onClick={handleLogout}>Logout</button>
      </header>

      {error && <p className="error">{error}</p>}

      <form onSubmit={handleAdd} className="add-form">
        <input
          type="text"
          placeholder="New todo title"
          value={newTitle}
          onChange={e => setNewTitle(e.target.value)}
        />
        <button type="submit" className="primary-button">Add</button>
      </form>

      <h2 className="section-heading">Your Todo List</h2>

      {todos.length === 0 ? (
        <p className="empty-message">No todos yet. Add one above!</p>
      ) : (
        <ul className="todo-list">
          {todos.map(todo => (
            <li key={todo.id} className={todo.completed ? 'done' : ''}>
              <input
                type="checkbox"
                checked={todo.completed}
                onChange={() => handleToggle(todo)}
              />
              <span className="todo-title">{todo.title}</span>
              <button className="small-button" onClick={() => handleRename(todo)}>Rename</button>
              <button className="small-button danger" onClick={() => handleDelete(todo.id)}>Delete</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default App;
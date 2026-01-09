import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  FiUser, FiLock, FiFileText, FiPlus,
  FiLogOut, FiHome, FiGrid, FiArchive,
  FiSearch, FiTag, FiRefreshCw, FiCheck,
  FiMail, FiKey, FiEdit, FiTrash2, FiSettings,
  FiX, FiFilter, FiCalendar, FiEye, FiSave
} from 'react-icons/fi';
import Swal from 'sweetalert2';
import './App.css';

function App() {
  // Estados
  const [user, setUser] = useState(null);
  const [notes, setNotes] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [showNoteModal, setShowNoteModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [currentView, setCurrentView] = useState('dashboard');
  const [selectedNote, setSelectedNote] = useState(null);

  // Formularios
  const [loginData, setLoginData] = useState({
    username: 'admin',
    password: 'admin123'
  });

  const [registerData, setRegisterData] = useState({
    nombre: '',
    username: '',
    email: '',
    password: ''
  });

  const [noteForm, setNoteForm] = useState({
    id: null,
    titulo: '',
    contenido: '',
    selectedCategoryIds: []
  });

  const [categoryForm, setCategoryForm] = useState({
    id: null,
    nombre: '',
    color: '#4f46e5'
  });

  // Colores predefinidos para categorías
  const categoryColors = [
    '#4f46e5', '#10b981', '#f59e0b', '#3b82f6', '#8b5cf6',
    '#ef4444', '#06b6d4', '#84cc16', '#f97316', '#a855f7'
  ];

  // Check user on load
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (savedUser && savedUser !== 'undefined') {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.clear();
      }
    }
  }, []);

  // Cargar datos cuando el usuario inicia sesión
  useEffect(() => {
    if (user) {
      loadData();
    }
  }, [user]);

  const loadData = async () => {
    await Promise.all([loadNotes(), loadCategories()]);
  };

  // ========== LOGIN ==========
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await axios.post('/api/auth/login', {
        username: loginData.username,
        password: loginData.password
      });

      const { token, username } = response.data;

      const userData = {
        username: username,
        nombre: username === 'admin' ? 'Administrador' :
                username === 'usuario' ? 'Usuario Regular' :
                username.charAt(0).toUpperCase() + username.slice(1),
        email: `${username}@notasapp.com`,
        rol: username === 'admin' ? 'ADMIN' : 'USER'
      };

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);

      Swal.fire({
        icon: 'success',
        title: `¡Bienvenido ${userData.nombre}!`,
        showConfirmButton: false,
        timer: 1500
      });

    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Error de Login',
        text: error.response?.data?.message || 'Credenciales incorrectas'
      });
    } finally {
      setLoading(false);
    }
  };

  // ========== REGISTRO ==========
  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await axios.post('/api/auth/registro', {
        username: registerData.username,
        password: registerData.password,
        email: registerData.email,
        nombre: registerData.nombre || registerData.username,
        rol: 'USER'
      });

      Swal.fire({
        icon: 'success',
        title: '¡Usuario creado!',
        text: `Usuario ${registerData.username} registrado exitosamente`,
        confirmButtonText: 'Iniciar sesión'
      }).then(() => {
        setShowRegister(false);
        setLoginData({
          username: registerData.username,
          password: registerData.password
        });
      });

      setRegisterData({
        nombre: '',
        username: '',
        email: '',
        password: ''
      });

    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Error en registro',
        text: error.response?.data?.message || 'No se pudo crear el usuario'
      });
    } finally {
      setLoading(false);
    }
  };

  // ========== CATEGORÍAS ==========
  const loadCategories = async () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      setLoadingCategories(true);

      // SIEMPRE obtenemos TODAS las categorías de la tabla SQL
      // El backend debe tener un endpoint que devuelva TODAS las categorías
      const response = await axios.get('/api/categorias/todas', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      console.log('📋 Todas las categorías cargadas:', response.data);
      setCategories(response.data || []);
    } catch (error) {
      console.error('Error loading categories:', error);
      // Si el endpoint /todas no existe, intentamos con el normal
      try {
        const response = await axios.get('/api/categorias', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        console.log('📋 Categorías cargadas (endpoint normal):', response.data);
        setCategories(response.data || []);
      } catch (secondError) {
        console.error('Error cargando categorías:', secondError);
        // Si no hay conexión, dejamos el array vacío
        setCategories([]);
      }
    } finally {
      setLoadingCategories(false);
    }
  };

  // Función para crear categoría - SOLO ADMIN
  const handleCreateCategory = async () => {
    if (!categoryForm.nombre.trim()) {
      Swal.fire({
        icon: 'warning',
        title: 'Nombre requerido',
        text: 'Por favor ingresa un nombre para la categoría'
      });
      return;
    }

    // Verificar que el usuario sea ADMIN
    if (user?.rol !== 'ADMIN') {
      Swal.fire({
        icon: 'error',
        title: 'Permiso denegado',
        text: 'Solo los administradores pueden crear categorías'
      });
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post('/api/categorias', {
        nombre: categoryForm.nombre,
        color: categoryForm.color
      }, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      // Agregar la nueva categoría a la lista
      setCategories([...categories, response.data]);
      setShowCategoryModal(false);
      setCategoryForm({
        id: null,
        nombre: '',
        color: '#4f46e5'
      });

      Swal.fire({
        icon: 'success',
        title: 'Categoría creada',
        text: `Categoría "${categoryForm.nombre}" creada exitosamente`,
        timer: 1500
      });

    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: error.response?.data?.message || 'No se pudo crear la categoría'
      });
    }
  };

  // ========== NOTAS ==========
  const loadNotes = async () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
      setLoading(true);
      const response = await axios.get('/api/notas', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      console.log('📝 Notas cargadas del backend:', response.data);
      setNotes(response.data || []);
    } catch (error) {
      console.error('Error loading notes:', error);
      // Notas de ejemplo
      setNotes([
        {
          id: 1,
          titulo: 'Bienvenido a NotasApp',
          contenido: 'Tu sistema de notas está funcionando correctamente.',
          categoriaIds: [1],
          fechaCreacion: new Date().toISOString()
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateNote = async () => {
    if (!noteForm.titulo.trim()) {
      Swal.fire({
        icon: 'warning',
        title: 'Título requerido',
        text: 'Por favor ingresa un título para la nota'
      });
      return;
    }

    try {
      const token = localStorage.getItem('token');

      // Preparar datos para ManyToMany
      const notaData = {
        titulo: noteForm.titulo,
        contenido: noteForm.contenido,
        categoriaIds: noteForm.selectedCategoryIds
      };

      console.log('📤 Enviando nota:', notaData);

      if (noteForm.id) {
        // Editar nota existente
        const response = await axios.put(`/api/notas/${noteForm.id}`, notaData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        setNotes(notes.map(note =>
          note.id === noteForm.id ? response.data : note
        ));

        Swal.fire({
          icon: 'success',
          title: 'Nota actualizada',
          showConfirmButton: false,
          timer: 1500
        });
      } else {
        // Crear nueva nota
        const response = await axios.post('/api/notas', notaData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        setNotes([response.data, ...notes]);
        Swal.fire({
          icon: 'success',
          title: 'Nota creada',
          showConfirmButton: false,
          timer: 1500
        });
      }

      setShowNoteModal(false);
      setNoteForm({
        id: null,
        titulo: '',
        contenido: '',
        selectedCategoryIds: []
      });

    } catch (error) {
      console.error('Error saving note:', error.response || error);
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: error.response?.data?.message || 'No se pudo guardar la nota'
      });
    }
  };

  // ========== FUNCIONES UTILES CORREGIDAS ==========

  // Obtener categorías COMPLETAS de una nota (por IDs)
  const getNoteCategories = (note) => {
    if (!note.categoriaIds || note.categoriaIds.length === 0) {
      return [];
    }

    // Buscar las categorías por ID en el estado local (TODAS las categorías)
    return categories.filter(cat => note.categoriaIds.includes(cat.id));
  };

  // Obtener todas las categorías de una nota
  const getAllNoteCategories = (note) => {
    if (note.categorias && note.categorias.length > 0) {
      return note.categorias;
    }
    return getNoteCategories(note);
  };

  const handleEditNote = (note) => {
    console.log('✏️ Editando nota:', note);
    // Usar categoriaIds si existe, sino usar las categorías mapeadas a IDs
    const categoryIds = note.categoriaIds ||
      (note.categorias ? note.categorias.map(cat => cat.id) : []);

    setNoteForm({
      id: note.id,
      titulo: note.titulo || '',
      contenido: note.contenido || '',
      selectedCategoryIds: categoryIds
    });
    setShowNoteModal(true);
  };

  const handleViewNote = (note) => {
    setSelectedNote(note);
    setShowViewModal(true);
  };

  const handleDeleteNote = async (id) => {
    const result = await Swal.fire({
      title: '¿Eliminar nota?',
      text: 'Esta acción no se puede deshacer',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar'
    });

    if (result.isConfirmed) {
      try {
        const token = localStorage.getItem('token');
        await axios.delete(`/api/notas/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        setNotes(notes.filter(note => note.id !== id));

        Swal.fire({
          icon: 'success',
          title: 'Nota eliminada',
          showConfirmButton: false,
          timer: 1500
        });
      } catch (error) {
        Swal.fire({
          icon: 'error',
          title: 'Error',
          text: 'No se pudo eliminar la nota'
        });
      }
    }
  };

  // ========== UTILIDADES ==========
  const handleLogout = () => {
    Swal.fire({
      title: '¿Cerrar sesión?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sí, salir',
      cancelButtonText: 'Cancelar'
    }).then((result) => {
      if (result.isConfirmed) {
        localStorage.clear();
        setUser(null);
        setNotes([]);
        setCategories([]);
        Swal.fire({
          icon: 'success',
          title: 'Sesión cerrada',
          showConfirmButton: false,
          timer: 1000
        });
      }
    });
  };

  // Obtener nombres de categorías únicas para filtros
  const getCategoryNames = () => {
    const allNames = notes.flatMap(note =>
      getAllNoteCategories(note).map(cat => cat.nombre)
    );
    return ['all', ...new Set(allNames.filter(name => name))];
  };

  // Filtrar notas CORREGIDO
  const filteredNotes = notes.filter(note => {
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      const noteCategories = getAllNoteCategories(note);
      const matchesSearch =
        (note.titulo && note.titulo.toLowerCase().includes(search)) ||
        (note.contenido && note.contenido.toLowerCase().includes(search)) ||
        noteCategories.some(cat =>
          cat.nombre && cat.nombre.toLowerCase().includes(search)
        );
      if (!matchesSearch) return false;
    }

    if (selectedCategory !== 'all') {
      const noteCategories = getAllNoteCategories(note);
      const hasCategory = noteCategories.some(cat => cat.nombre === selectedCategory);
      if (!hasCategory) return false;
    }

    return true;
  });

  // Calcular estadísticas CORREGIDO
  const getUniqueCategoriesCount = () => {
    const allCategoryNames = notes.flatMap(note =>
      getAllNoteCategories(note).map(cat => cat.nombre)
    );
    return [...new Set(allCategoryNames.filter(name => name))].length;
  };

  // Contar notas por categoría
  const getNotesCountByCategory = (categoryId) => {
    return notes.filter(note => {
      if (note.categoriaIds) {
        return note.categoriaIds.includes(categoryId);
      } else if (note.categorias) {
        return note.categorias.some(cat => cat.id === categoryId);
      }
      return false;
    }).length;
  };

  // ========== RENDER LOGIN/REGISTER ==========
  if (!user) {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <div className="logo">
              <FiFileText size={40} />
              <h1>NotasApp</h1>
            </div>
            <p>Sistema de gestión de notas - Spring Boot + React</p>
          </div>

          <div className="auth-tabs">
            <button
              className={`tab ${!showRegister ? 'active' : ''}`}
              onClick={() => setShowRegister(false)}
            >
              <FiKey /> Iniciar Sesión
            </button>
            <button
              className={`tab ${showRegister ? 'active' : ''}`}
              onClick={() => setShowRegister(true)}
            >
              <FiUser /> Registrarse
            </button>
          </div>

          {!showRegister ? (
            <form onSubmit={handleLogin} className="auth-form">
              <div className="form-group">
                <label><FiUser /> Usuario</label>
                <input
                  type="text"
                  value={loginData.username}
                  onChange={(e) => setLoginData({...loginData, username: e.target.value})}
                  placeholder="Usuario"
                  required
                />
              </div>

              <div className="form-group">
                <label><FiLock /> Contraseña</label>
                <input
                  type="password"
                  value={loginData.password}
                  onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                  placeholder="Contraseña"
                  required
                />
              </div>

              <button type="submit" className="auth-button" disabled={loading}>
                {loading ? 'Conectando...' : 'Ingresar'}
              </button>

              <div className="demo-buttons">
                <button
                  type="button"
                  className="demo-button admin"
                  onClick={() => setLoginData({username: 'admin', password: 'admin123'})}
                >
                  <FiCheck /> Usar Admin
                </button>

                <button
                  type="button"
                  className="demo-button user"
                  onClick={() => setLoginData({username: 'usuario', password: 'usuario123'})}
                >
                  <FiCheck /> Usar Usuario
                </button>
              </div>

              <div className="auth-info">
                <p><strong>✅ Login confirmado:</strong> El backend funciona correctamente</p>
                <p>Usa las credenciales creadas por DatabaseConfig.java</p>
              </div>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="auth-form">
              <div className="form-group">
                <label><FiUser /> Nombre</label>
                <input
                  type="text"
                  value={registerData.nombre}
                  onChange={(e) => setRegisterData({...registerData, nombre: e.target.value})}
                  placeholder="Tu nombre completo"
                  required
                />
              </div>

              <div className="form-group">
                <label><FiUser /> Usuario</label>
                <input
                  type="text"
                  value={registerData.username}
                  onChange={(e) => setRegisterData({...registerData, username: e.target.value})}
                  placeholder="Nombre de usuario"
                  required
                />
              </div>

              <div className="form-group">
                <label><FiMail /> Email</label>
                <input
                  type="email"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                  placeholder="correo@ejemplo.com"
                  required
                />
              </div>

              <div className="form-group">
                <label><FiLock /> Contraseña</label>
                <input
                  type="password"
                  value={registerData.password}
                  onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                  placeholder="Mínimo 6 caracteres"
                  required
                  minLength="6"
                />
              </div>

              <button type="submit" className="auth-button" disabled={loading}>
                {loading ? 'Registrando...' : 'Crear cuenta'}
              </button>

              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowRegister(false)}
              >
                Volver al Login
              </button>
            </form>
          )}
        </div>
      </div>
    );
  }

  // ========== RENDER APP PRINCIPAL ==========
  return (
    <div className="app-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <FiFileText />
            <span>NotasApp</span>
          </div>
          <div className="user-info">
            <div className="avatar">
              {user.nombre?.charAt(0) || user.username?.charAt(0) || 'U'}
            </div>
            <div>
              <strong>{user.nombre || user.username}</strong>
              <small>{user.rol || 'Usuario'}</small>
            </div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button
            className={`nav-item ${currentView === 'dashboard' ? 'active' : ''}`}
            onClick={() => setCurrentView('dashboard')}
          >
            <FiHome /> Dashboard
          </button>
          <button
            className={`nav-item ${currentView === 'all-notes' ? 'active' : ''}`}
            onClick={() => setCurrentView('all-notes')}
          >
            <FiGrid /> Todas las Notas
          </button>
          <button
            className={`nav-item ${currentView === 'settings' ? 'active' : ''}`}
            onClick={() => setCurrentView('settings')}
          >
            <FiSettings /> Configuración
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="nav-item logout" onClick={handleLogout}>
            <FiLogOut /> Cerrar Sesión
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="top-bar">
          <div className="search-box">
            <FiSearch />
            <input
              type="text"
              placeholder="Buscar notas..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="top-bar-actions">
            <div className="category-filter">
              <FiFilter />
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="category-select"
              >
                <option value="all">Todas las categorías</option>
                {getCategoryNames()
                  .filter(name => name !== 'all')
                  .map(name => (
                    <option key={name} value={name}>
                      {name}
                    </option>
                  ))}
              </select>
            </div>

            <button
              className="new-note-button"
              onClick={() => {
                setNoteForm({
                  id: null,
                  titulo: '',
                  contenido: '',
                  selectedCategoryIds: []
                });
                setShowNoteModal(true);
              }}
            >
              <FiPlus /> Nueva Nota
            </button>
          </div>
        </header>

        <div className="content">
          {currentView === 'settings' ? (
            <div className="settings-section">
              <div className="content-header">
                <h1>Configuración</h1>
                <p>Configura tu cuenta y preferencias</p>
              </div>

              <div className="settings-card">
                <h3><FiUser /> Información de la cuenta</h3>
                <div className="settings-info">
                  <div className="info-item">
                    <strong>Usuario:</strong>
                    <span>{user.username}</span>
                  </div>
                  <div className="info-item">
                    <strong>Nombre:</strong>
                    <span>{user.nombre || 'No especificado'}</span>
                  </div>
                  <div className="info-item">
                    <strong>Email:</strong>
                    <span>{user.email || 'No especificado'}</span>
                  </div>
                  <div className="info-item">
                    <strong>Rol:</strong>
                    <span className={`role-badge ${user.rol === 'ADMIN' ? 'admin' : 'user'}`}>
                      {user.rol}
                    </span>
                  </div>
                </div>

                <div className="stats-info">
                  <h3><FiFileText /> Estadísticas</h3>
                  <div className="stats-row">
                    <div className="stat">
                      <span className="stat-label">Total notas:</span>
                      <span className="stat-value">{notes.length}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-label">Categorías usadas:</span>
                      <span className="stat-value">
                        {getUniqueCategoriesCount()}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="categories-management">
                  <h3><FiTag /> Gestión de Categorías</h3>
                  {loadingCategories ? (
                    <div className="loading-categories">
                      <div className="loader small"></div>
                      <p>Cargando categorías...</p>
                    </div>
                  ) : (
                    <>
                      <div className="categories-list">
                        {categories.length === 0 ? (
                          <p className="no-categories">No hay categorías disponibles</p>
                        ) : (
                          categories.map(category => (
                            <div key={category.id} className="category-item">
                              <span
                                className="category-color"
                                style={{backgroundColor: category.color}}
                              />
                              <span className="category-name">{category.nombre}</span>
                              <span className="category-count">
                                {getNotesCountByCategory(category.id)} notas
                              </span>
                            </div>
                          ))
                        )}
                      </div>
                      {/* SOLO ADMIN puede crear categorías */}
                      {user.rol === 'ADMIN' && (
                        <button
                          className="secondary-button"
                          onClick={() => setShowCategoryModal(true)}
                        >
                          <FiPlus /> Nueva Categoría
                        </button>
                      )}
                    </>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <>
              <div className="content-header">
                <h1>
                  {currentView === 'dashboard' ? 'Mis Notas' : 'Todas las Notas'}
                </h1>
                <p>
                  {currentView === 'dashboard'
                    ? `Bienvenido, ${user.nombre || user.username}. Tienes ${notes.length} notas.`
                    : `Mostrando ${filteredNotes.length} de ${notes.length} notas`}
                </p>
              </div>

              {currentView === 'dashboard' && (
                <div className="stats-grid">
                  <div className="stat-card">
                    <div className="stat-icon" style={{background: '#4f46e5'}}>
                      <FiFileText />
                    </div>
                    <div className="stat-info">
                      <h3>Total Notas</h3>
                      <p className="stat-number">{notes.length}</p>
                    </div>
                  </div>

                  <div className="stat-card">
                    <div className="stat-icon" style={{background: '#10b981'}}>
                      <FiGrid />
                    </div>
                    <div className="stat-info">
                      <h3>Activas</h3>
                      <p className="stat-number">{notes.length}</p>
                    </div>
                  </div>

                  <div className="stat-card">
                    <div className="stat-icon" style={{background: '#f59e0b'}}>
                      <FiArchive />
                    </div>
                    <div className="stat-info">
                      <h3>Categorías usadas</h3>
                      <p className="stat-number">
                        {getUniqueCategoriesCount()}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Notes Section */}
              <div className="notes-section">
                <div className="section-header">
                  <h2>
                    {currentView === 'dashboard' ? 'Notas Recientes' : 'Todas las Notas'}
                  </h2>
                  <div className="filters-info">
                    {searchTerm && (
                      <span className="search-info">
                        Buscando: "{searchTerm}"
                      </span>
                    )}
                    {selectedCategory !== 'all' && (
                      <span className="category-info">
                        <FiFilter /> Categoría: {selectedCategory}
                      </span>
                    )}
                    <span className="results-info">
                      ({filteredNotes.length} resultados)
                    </span>
                  </div>
                </div>

                {loading ? (
                  <div className="loading">
                    <div className="loader"></div>
                    <p>Cargando notas...</p>
                  </div>
                ) : filteredNotes.length === 0 ? (
                  <div className="empty-state">
                    <FiFileText size={48} />
                    <h3>No hay notas</h3>
                    <p>
                      {searchTerm || selectedCategory !== 'all'
                        ? 'No se encontraron notas con los filtros aplicados'
                        : 'Crea tu primera nota para comenzar'}
                    </p>
                    <button
                      className="primary-button"
                      onClick={() => {
                        setNoteForm({
                          id: null,
                          titulo: '',
                          contenido: '',
                          selectedCategoryIds: []
                        });
                        setShowNoteModal(true);
                      }}
                    >
                      <FiPlus /> Crear Nota
                    </button>
                  </div>
                ) : (
                  <div className="notes-grid">
                    {filteredNotes.map(note => {
                      const noteCategories = getAllNoteCategories(note);
                      return (
                        <div
                          key={note.id}
                          className="note-card"
                          onClick={() => handleViewNote(note)}
                        >
                          <div className="note-content">
                            <div className="note-header">
                              <h3>{note.titulo || 'Sin título'}</h3>
                              <div className="note-actions" onClick={(e) => e.stopPropagation()}>
                                <button
                                  className="edit-button"
                                  onClick={() => handleEditNote(note)}
                                  title="Editar"
                                >
                                  <FiEdit />
                                </button>
                                <button
                                  className="delete-button"
                                  onClick={() => handleDeleteNote(note.id)}
                                  title="Eliminar"
                                >
                                  <FiTrash2 />
                                </button>
                              </div>
                            </div>
                            <p className="note-text">
                              {note.contenido
                                ? (note.contenido.length > 150
                                    ? `${note.contenido.substring(0, 150)}...`
                                    : note.contenido)
                                : 'Sin contenido'}
                            </p>

                            {/* Mostrar categorías de la nota */}
                            {noteCategories.length > 0 && (
                              <div className="note-categories">
                                {noteCategories.map(cat => (
                                  <span
                                    key={cat.id}
                                    className="note-category"
                                    style={{backgroundColor: cat.color || '#4f46e5'}}
                                  >
                                    <FiTag /> {cat.nombre}
                                  </span>
                                ))}
                              </div>
                            )}

                            <div className="note-footer">
                              <span className="note-date">
                                <FiCalendar /> {note.fechaCreacion ?
                                  new Date(note.fechaCreacion).toLocaleDateString() :
                                  'Sin fecha'}
                              </span>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </main>

      {/* Modal para crear/editar nota */}
      {showNoteModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>{noteForm.id ? 'Editar Nota' : 'Nueva Nota'}</h2>
              <button
                className="close-button"
                onClick={() => setShowNoteModal(false)}
              >
                <FiX />
              </button>
            </div>

            <div className="modal-body">
              <div className="form-group">
                <label>Título *</label>
                <input
                  type="text"
                  value={noteForm.titulo}
                  onChange={(e) => setNoteForm({...noteForm, titulo: e.target.value})}
                  placeholder="Título de la nota"
                />
              </div>

              <div className="form-group">
                <label>Contenido</label>
                <textarea
                  value={noteForm.contenido}
                  onChange={(e) => setNoteForm({...noteForm, contenido: e.target.value})}
                  placeholder="Escribe el contenido de tu nota..."
                  rows={6}
                />
              </div>

              <div className="form-group">
                <label>Categorías</label>
                <div className="categories-selector">
                  {categories.map(category => (
                    <button
                      key={category.id}
                      type="button"
                      className={`category-select-button ${
                        noteForm.selectedCategoryIds.includes(category.id) ? 'selected' : ''
                      }`}
                      onClick={() => {
                        const newIds = noteForm.selectedCategoryIds.includes(category.id)
                          ? noteForm.selectedCategoryIds.filter(id => id !== category.id)
                          : [...noteForm.selectedCategoryIds, category.id];
                        setNoteForm({...noteForm, selectedCategoryIds: newIds});
                      }}
                      style={{
                        backgroundColor: noteForm.selectedCategoryIds.includes(category.id)
                          ? category.color
                          : 'transparent',
                        borderColor: category.color,
                        color: noteForm.selectedCategoryIds.includes(category.id)
                          ? 'white'
                          : category.color
                      }}
                    >
                      <FiTag /> {category.nombre}
                    </button>
                  ))}
                  {categories.length === 0 && (
                    <p className="no-categories-message">
                      No hay categorías disponibles. Contacta al administrador.
                    </p>
                  )}
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button
                className="secondary-button"
                onClick={() => setShowNoteModal(false)}
              >
                Cancelar
              </button>
              <button
                className="primary-button"
                onClick={handleCreateNote}
                disabled={!noteForm.titulo.trim()}
              >
                {noteForm.id ? 'Actualizar Nota' : 'Crear Nota'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para ver nota completa */}
      {showViewModal && selectedNote && (
        <div className="modal-overlay">
          <div className="modal view-modal">
            <div className="modal-header">
              <h2>{selectedNote.titulo || 'Sin título'}</h2>
              <button
                className="close-button"
                onClick={() => setShowViewModal(false)}
              >
                <FiX />
              </button>
            </div>

            <div className="modal-body">
              <div className="note-view-content">
                <div className="note-meta">
                  {getAllNoteCategories(selectedNote).length > 0 && (
                    <div className="note-categories-view">
                      {getAllNoteCategories(selectedNote).map(cat => (
                        <span
                          key={cat.id}
                          className="note-category-large"
                          style={{backgroundColor: cat.color}}
                        >
                          <FiTag /> {cat.nombre}
                        </span>
                      ))}
                    </div>
                  )}
                  {selectedNote.fechaCreacion && (
                    <span className="note-date-large">
                      <FiCalendar /> Creada: {new Date(selectedNote.fechaCreacion).toLocaleDateString()}
                    </span>
                  )}
                </div>

                <div className="note-content-full">
                  {selectedNote.contenido ? (
                    <div className="content-text">
                      {selectedNote.contenido.split('\n').map((paragraph, index) => (
                        <p key={index}>{paragraph}</p>
                      ))}
                    </div>
                  ) : (
                    <p className="no-content">Esta nota no tiene contenido.</p>
                  )}
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button
                className="secondary-button"
                onClick={() => setShowViewModal(false)}
              >
                Cerrar
              </button>
              <button
                className="primary-button"
                onClick={() => {
                  setShowViewModal(false);
                  handleEditNote(selectedNote);
                }}
              >
                <FiEdit /> Editar Nota
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para crear categoría - SOLO ADMIN lo puede abrir */}
      {showCategoryModal && user?.rol === 'ADMIN' && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2>Nueva Categoría</h2>
              <button
                className="close-button"
                onClick={() => setShowCategoryModal(false)}
              >
                <FiX />
              </button>
            </div>

            <div className="modal-body">
              <div className="form-group">
                <label>Nombre *</label>
                <input
                  type="text"
                  value={categoryForm.nombre}
                  onChange={(e) => setCategoryForm({...categoryForm, nombre: e.target.value})}
                  placeholder="Nombre de la categoría"
                />
              </div>

              <div className="form-group">
                <label>Color</label>
                <div className="color-selector">
                  {categoryColors.map(color => (
                    <button
                      key={color}
                      type="button"
                      className={`color-option ${categoryForm.color === color ? 'selected' : ''}`}
                      style={{backgroundColor: color}}
                      onClick={() => setCategoryForm({...categoryForm, color})}
                      title={color}
                    />
                  ))}
                </div>
                <div className="selected-color">
                  <span>Color seleccionado:</span>
                  <div
                    className="color-preview"
                    style={{backgroundColor: categoryForm.color}}
                  />
                  <span className="color-hex">{categoryForm.color}</span>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button
                className="secondary-button"
                onClick={() => setShowCategoryModal(false)}
              >
                Cancelar
              </button>
              <button
                className="primary-button"
                onClick={handleCreateCategory}
                disabled={!categoryForm.nombre.trim()}
              >
                <FiSave /> Crear Categoría
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
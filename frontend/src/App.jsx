import { useState } from "react";
import "./App.css";

function App() {
  const [vista, setVista] = useState("login");

  const [formulario, setFormulario] = useState({
    nombre: "",
    primerApellido: "",
    segundoApellido: "",
    email: "",
    telefono: "",
    contrasena: "",
    salario: "",
    presupuesto: "",
    departamento: "",
    municipio: ""
  });

  const departamentos = {
    QUINDIO: [
      "Armenia",
      "Calarcá",
      "Circasia",
      "Montenegro",
      "Quimbaya",
      "Salento",
      "Filandia"
    ],
    RISARALDA: [
      "Pereira",
      "Dosquebradas",
      "Santa Rosa de Cabal"
    ],
    CALDAS: [
      "Manizales",
      "Chinchiná",
      "Villamaría"
    ],
    ANTIOQUIA: [
      "Medellín",
      "Envigado",
      "Bello"
    ],
    VALLE_DEL_CAUCA: [
      "Cali",
      "Palmira",
      "Buga"
    ]
  };

  const manejarCambio = (evento) => {
    const { name, value } = evento.target;

    setFormulario((anterior) => ({
      ...anterior,
      [name]: value,
      ...(name === "departamento" ? { municipio: "" } : {})
    }));
  };

  const formatearCOP = (valor) => {
    if (!valor) return "";

    const numero = valor.replace(/\D/g, "");

    return new Intl.NumberFormat("es-CO").format(numero);
  };

  const manejarMonto = (evento) => {
    const { name, value } = evento.target;

    setFormulario((anterior) => ({
      ...anterior,
      [name]: value.replace(/\D/g, "")
    }));
  };

  const iniciarSesion = (evento) => {
    evento.preventDefault();

    alert("El inicio de sesión se conectará al backend próximamente.");
  };

  const crearCuenta = (evento) => {
    evento.preventDefault();

    alert("El registro se conectará al backend próximamente.");
  };

  return (
      <div className="app-container">

        {/* Barra superior */}
        <header className="topbar">

          <div className="brand">
            <img
                src="https://static.vecteezy.com/system/resources/thumbnails/021/828/953/small/coffee-bean-icon-logo-illustration-vector.jpg"
                alt="Logo"
                className="logo-img"
            />

            <div className="brand-name">
              <strong>Venta</strong>
              <span>Condominio</span>
            </div>
          </div>

          <div className="topbar-info">
            <span>Gestión inmobiliaria</span>
          </div>

        </header>


        {/* Contenido */}
        <main className="main-content">

          <div className="auth-card">

            {/* Logo dentro de la tarjeta */}
            <div className="auth-header">

              <div className="auth-icon">
                <img
                    src="https://static.vecteezy.com/system/resources/thumbnails/021/828/953/small/coffee-bean-icon-logo-illustration-vector.jpg"
                    alt="Logo"
                />
              </div>

              <h1>Venta Condominio</h1>

              <p>
                Gestión de unidades y clientes
              </p>

            </div>


            {/* ================= LOGIN ================= */}

            {vista === "login" && (

                <form
                    className="auth-form"
                    onSubmit={iniciarSesion}
                >

                  <h2>Iniciar sesión</h2>

                  <p className="form-description">
                    Ingresa a tu cuenta para continuar
                  </p>

                  <div className="input-group">
                    <label>Correo electrónico</label>

                    <input
                        type="email"
                        name="email"
                        placeholder="correo@ejemplo.com"
                        value={formulario.email}
                        onChange={manejarCambio}
                        required
                    />
                  </div>

                  <div className="input-group">
                    <label>Contraseña</label>

                    <input
                        type="password"
                        name="contrasena"
                        placeholder="••••••••"
                        value={formulario.contrasena}
                        onChange={manejarCambio}
                        required
                    />
                  </div>

                  <div className="forgot-password">
                    <button
                        type="button"
                        onClick={() =>
                            alert("Esta opción se implementará próximamente.")
                        }
                    >
                      ¿Olvidaste tu contraseña?
                    </button>
                  </div>

                  <button
                      type="submit"
                      className="primary-button"
                  >
                    Iniciar sesión
                  </button>

                  <div className="separator">
                    <span>o</span>
                  </div>

                  <div className="register-question">
                    <span>¿No tienes una cuenta?</span>

                    <button
                        type="button"
                        onClick={() => setVista("registro")}
                    >
                      Crear cuenta
                    </button>
                  </div>

                </form>

            )}


            {/* ================= REGISTRO ================= */}

            {vista === "registro" && (

                <form
                    className="auth-form register-form"
                    onSubmit={crearCuenta}
                >

                  <div className="form-top">

                    <button
                        type="button"
                        className="back-button"
                        onClick={() => setVista("login")}
                    >
                      ← Volver
                    </button>

                  </div>

                  <h2>Crear cuenta</h2>

                  <p className="form-description">
                    Registra tus datos para comenzar
                  </p>


                  <div className="form-grid">

                    <div className="input-group">
                      <label>Nombre</label>

                      <input
                          name="nombre"
                          placeholder="Nombre"
                          value={formulario.nombre}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Primer apellido</label>

                      <input
                          name="primerApellido"
                          placeholder="Primer apellido"
                          value={formulario.primerApellido}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Segundo apellido</label>

                      <input
                          name="segundoApellido"
                          placeholder="Segundo apellido"
                          value={formulario.segundoApellido}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Teléfono</label>

                      <input
                          name="telefono"
                          placeholder="3001234567"
                          maxLength="10"
                          value={formulario.telefono}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group full-width">
                      <label>Correo electrónico</label>

                      <input
                          type="email"
                          name="email"
                          placeholder="correo@ejemplo.com"
                          value={formulario.email}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group full-width">
                      <label>Contraseña</label>

                      <input
                          type="password"
                          name="contrasena"
                          placeholder="Crea una contraseña"
                          value={formulario.contrasena}
                          onChange={manejarCambio}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Salario mensual</label>

                      <input
                          type="text"
                          inputMode="numeric"
                          name="salario"
                          placeholder="$ 0"
                          value={
                            formulario.salario
                                ? `$ ${formatearCOP(formulario.salario)}`
                                : ""
                          }
                          onChange={manejarMonto}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Presupuesto</label>

                      <input
                          type="text"
                          inputMode="numeric"
                          name="presupuesto"
                          placeholder="$ 0"
                          value={
                            formulario.presupuesto
                                ? `$ ${formatearCOP(formulario.presupuesto)}`
                                : ""
                          }
                          onChange={manejarMonto}
                          required
                      />
                    </div>


                    <div className="input-group">
                      <label>Departamento</label>

                      <select
                          name="departamento"
                          value={formulario.departamento}
                          onChange={manejarCambio}
                          required
                      >
                        <option value="" disabled>
                          Seleccionar
                        </option>

                        {Object.keys(departamentos).map(
                            (departamento) => (
                                <option
                                    key={departamento}
                                    value={departamento}
                                >
                                  {departamento.replaceAll("_", " ")}
                                </option>
                            )
                        )}

                      </select>
                    </div>


                    <div className="input-group">
                      <label>Municipio</label>

                      <select
                          name="municipio"
                          value={formulario.municipio}
                          onChange={manejarCambio}
                          disabled={!formulario.departamento}
                          required
                      >
                        <option value="" disabled>
                          Seleccionar
                        </option>

                        {formulario.departamento &&
                            departamentos[
                                formulario.departamento
                                ].map((municipio) => (
                                <option
                                    key={municipio}
                                    value={municipio}
                                >
                                  {municipio}
                                </option>
                            ))}

                      </select>
                    </div>

                  </div>


                  <button
                      type="submit"
                      className="primary-button"
                  >
                    Crear cuenta
                  </button>


                  <div className="register-question">

                    <span>¿Ya tienes una cuenta?</span>

                    <button
                        type="button"
                        onClick={() => setVista("login")}
                    >
                      Iniciar sesión
                    </button>

                  </div>

                </form>

            )}

          </div>

        </main>

      </div>
  );
}

export default App;
import { useState } from "react";
import "./App.css";

function App() {
  const [vista, setVista] = useState("login");
  const [codigoVerificacion, setCodigoVerificacion] = useState("");

  const [formulario, setFormulario] = useState({
    nombre: "",
    primerApellido: "",
    segundoApellido: "",
    cedula: "",
    email: "",
    telefono: "",
    contrasena: "",
    salario: "",
    presupuesto: "",
    tipoInmueble: "",
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

    alert(
        "El inicio de sesión se conectará al backend próximamente."
    );
  };

  // =========================================================
  // GENERAR CÓDIGO DE VERIFICACIÓN
  // =========================================================

  const crearCuenta = async (evento) => {
    evento.preventDefault();

    try {
      const respuesta = await fetch(
          "http://localhost:8080/verificacion-email/generar",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              email: formulario.email
            })
          }
      );

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        alert(
            datos.mensaje ||
            "No se pudo generar el código"
        );
        return;
      }

      // Por ahora mostramos el código en la consola
      // porque todavía no tenemos envío real de correo.
      console.log(
          "Código generado:",
          datos.codigo
      );

      setVista("verificacion");

    } catch (error) {
      console.error(error);

      alert(
          "No se pudo conectar con el servidor"
      );
    }
  };

  // =========================================================
  // VERIFICAR CORREO Y REGISTRAR CLIENTE
  // =========================================================

  const verificarCorreo = async (evento) => {
    evento.preventDefault();

    try {
      // -----------------------------------------------------
      // 1. VERIFICAR CÓDIGO
      // -----------------------------------------------------

      const respuestaVerificacion = await fetch(
          "http://localhost:8080/verificacion-email/verificar",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              email: formulario.email,
              codigo: codigoVerificacion
            })
          }
      );

      const datosVerificacion =
          await respuestaVerificacion.json();

      if (!respuestaVerificacion.ok) {
        alert(
            datosVerificacion.mensaje ||
            "El código de verificación es incorrecto"
        );

        return;
      }

      // -----------------------------------------------------
      // 2. REGISTRAR CLIENTE
      // -----------------------------------------------------
      console.log("DATOS ENVIADOS A /clientes:", {
        nombre: formulario.nombre,
        primerApellido: formulario.primerApellido,
        segundoApellido: formulario.segundoApellido,
        cedula: formulario.cedula,
        email: formulario.email,
        telefono: formulario.telefono,
        contrasena: formulario.contrasena,
        salario: Number(formulario.salario),
        presupuesto: Number(formulario.presupuesto),
        tipoInmueble: formulario.tipoInmueble,
        departamento: formulario.departamento,
        municipio: formulario.municipio
      });

      const respuestaCliente = await fetch(
          "http://localhost:8080/clientes",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              nombre: formulario.nombre,
              primerApellido: formulario.primerApellido,
              segundoApellido: formulario.segundoApellido,
              cedula: formulario.cedula,
              email: formulario.email,
              telefono: formulario.telefono,
              contrasena: formulario.contrasena,
              salario: Number(formulario.salario),
              presupuesto: Number(formulario.presupuesto),
              tipoInmueble: formulario.tipoInmueble,
              departamento: formulario.departamento,
              municipio: formulario.municipio
            })
          }
      );

      const textoCliente =
          await respuestaCliente.text();

      let datosCliente = {};

      if (textoCliente) {
        try {
          datosCliente = JSON.parse(textoCliente);
        } catch {
          datosCliente = {
            mensaje: textoCliente
          };
        }
      }

      if (!respuestaCliente.ok) {
        console.error("Error al registrar cliente:", {
          status: respuestaCliente.status,
          respuesta: datosCliente
        });

        alert(
            `Error ${respuestaCliente.status}: ${
                datosCliente.mensaje ||
                textoCliente ||
                "No se pudo registrar el cliente"
            }`
        );

        return;
      }

      // -----------------------------------------------------
      // 3. REGISTRO EXITOSO
      // -----------------------------------------------------

      alert(
          datosCliente.mensaje ||
          "Cliente registrado correctamente"
      );

      // Volver al login
      setVista("login");

      // Limpiar código
      setCodigoVerificacion("");

    } catch (error) {
      console.error(error);

      alert(
          "No se pudo conectar con el servidor"
      );
    }
  };

  return (
      <div className="app-container">

        {/* =====================================================
          BARRA SUPERIOR
      ====================================================== */}

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

        {/* =====================================================
          CONTENIDO PRINCIPAL
      ====================================================== */}

        <main className="main-content">

          <div className="auth-card">

            {/* =================================================
              ENCABEZADO
          ================================================== */}

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

            {/* =================================================
              LOGIN
          ================================================== */}

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

                    <label>
                      Correo electrónico
                    </label>

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

                    <label>
                      Contraseña
                    </label>

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
                            alert(
                                "Esta opción se implementará próximamente."
                            )
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

                <span>
                  ¿No tienes una cuenta?
                </span>

                    <button
                        type="button"
                        onClick={() =>
                            setVista("registro")
                        }
                    >
                      Crear cuenta
                    </button>

                  </div>

                </form>
            )}

            {/* =================================================
              REGISTRO
          ================================================== */}

            {vista === "registro" && (

                <form
                    className="auth-form register-form"
                    onSubmit={crearCuenta}
                >

                  <div className="form-top">

                    <button
                        type="button"
                        className="back-button"
                        onClick={() =>
                            setVista("login")
                        }
                    >
                      ← Volver
                    </button>

                  </div>

                  <h2>Crear cuenta</h2>

                  <p className="form-description">
                    Registra tus datos para comenzar
                  </p>

                  <div className="form-grid">

                    {/* Nombre */}

                    <div className="input-group">

                      <label>
                        Nombre
                      </label>

                      <input
                          name="nombre"
                          placeholder="Nombre"
                          value={formulario.nombre}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Primer apellido */}

                    <div className="input-group">

                      <label>
                        Primer apellido
                      </label>

                      <input
                          name="primerApellido"
                          placeholder="Primer apellido"
                          value={formulario.primerApellido}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Segundo apellido */}

                    <div className="input-group">

                      <label>
                        Segundo apellido
                      </label>

                      <input
                          name="segundoApellido"
                          placeholder="Segundo apellido"
                          value={formulario.segundoApellido}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Cédula */}

                    <div className="input-group">

                      <label>
                        Cédula
                      </label>

                      <input
                          name="cedula"
                          placeholder="Número de cédula"
                          value={formulario.cedula}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Teléfono */}

                    <div className="input-group">

                      <label>
                        Teléfono
                      </label>

                      <input
                          name="telefono"
                          placeholder="3001234567"
                          maxLength="10"
                          value={formulario.telefono}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Correo */}

                    <div className="input-group full-width">

                      <label>
                        Correo electrónico
                      </label>

                      <input
                          type="email"
                          name="email"
                          placeholder="correo@ejemplo.com"
                          value={formulario.email}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Contraseña */}

                    <div className="input-group full-width">

                      <label>
                        Contraseña
                      </label>

                      <input
                          type="password"
                          name="contrasena"
                          placeholder="Crea una contraseña"
                          value={formulario.contrasena}
                          onChange={manejarCambio}
                          required
                      />

                    </div>

                    {/* Salario */}

                    <div className="input-group">

                      <label>
                        Salario mensual
                      </label>

                      <input
                          type="text"
                          inputMode="numeric"
                          name="salario"
                          placeholder="$ 0"
                          value={
                            formulario.salario
                                ? `$ ${formatearCOP(
                                    formulario.salario
                                )}`
                                : ""
                          }
                          onChange={manejarMonto}
                          required
                      />

                    </div>

                    {/* Presupuesto */}

                    <div className="input-group">

                      <label>
                        Presupuesto
                      </label>

                      <input
                          type="text"
                          inputMode="numeric"
                          name="presupuesto"
                          placeholder="$ 0"
                          value={
                            formulario.presupuesto
                                ? `$ ${formatearCOP(
                                    formulario.presupuesto
                                )}`
                                : ""
                          }
                          onChange={manejarMonto}
                          required
                      />

                    </div>

                    {/* Tipo de inmueble */}

                    <div className="input-group">

                      <label>
                        Tipo de inmueble
                      </label>

                      <select
                          name="tipoInmueble"
                          value={formulario.tipoInmueble}
                          onChange={manejarCambio}
                          required
                      >

                        <option
                            value=""
                            disabled
                        >
                          Seleccionar
                        </option>

                        <option value="APARTAMENTO">
                          Apartamento
                        </option>

                        <option value="CASA">
                          Casa
                        </option>

                      </select>

                    </div>

                    {/* Departamento */}

                    <div className="input-group">

                      <label>
                        Departamento
                      </label>

                      <select
                          name="departamento"
                          value={formulario.departamento}
                          onChange={manejarCambio}
                          required
                      >

                        <option
                            value=""
                            disabled
                        >
                          Seleccionar
                        </option>

                        {Object.keys(departamentos).map(
                            (departamento) => (

                                <option
                                    key={departamento}
                                    value={departamento}
                                >
                                  {departamento.replaceAll(
                                      "_",
                                      " "
                                  )}
                                </option>

                            )
                        )}

                      </select>

                    </div>

                    {/* Municipio */}

                    <div className="input-group">

                      <label>
                        Municipio
                      </label>

                      <select
                          name="municipio"
                          value={formulario.municipio}
                          onChange={manejarCambio}
                          disabled={
                            !formulario.departamento
                          }
                          required
                      >

                        <option
                            value=""
                            disabled
                        >
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

                            ))
                        }

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

                <span>
                  ¿Ya tienes una cuenta?
                </span>

                    <button
                        type="button"
                        onClick={() =>
                            setVista("login")
                        }
                    >
                      Iniciar sesión
                    </button>

                  </div>

                </form>
            )}

            {/* =================================================
              VERIFICACIÓN DE CORREO
          ================================================== */}

            {vista === "verificacion" && (

                <form
                    className="auth-form"
                    onSubmit={verificarCorreo}
                >

                  <h2>
                    Verificar correo
                  </h2>

                  <p className="form-description">
                    Ingresa el código que recibiste
                    en tu correo
                  </p>

                  <div className="input-group">

                    <label>
                      Código de verificación
                    </label>

                    <input
                        type="text"
                        inputMode="numeric"
                        placeholder="Código de 6 dígitos"
                        maxLength="6"
                        value={codigoVerificacion}
                        onChange={(evento) =>
                            setCodigoVerificacion(
                                evento.target.value.replace(
                                    /\D/g,
                                    ""
                                )
                            )
                        }
                        required
                    />

                  </div>

                  <button
                      type="submit"
                      className="primary-button"
                  >
                    Verificar correo
                  </button>

                  <div className="register-question">

                    <button
                        type="button"
                        onClick={() => {
                          setVista("registro");
                          setCodigoVerificacion("");
                        }}
                    >
                      ← Volver al registro
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
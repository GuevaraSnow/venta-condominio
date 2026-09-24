import { useState } from "react";
import "./App.css";

const PESTANAS_POR_ROL = {
  CLIENTE: ["unidades", "perfil"],
  ASESOR_COMERCIAL: ["unidades", "perfil"],
  ABOGADO: ["unidades", "perfil"],
  DIRECTOR_COMERCIAL: ["unidades", "crear", "perfil"],
  ADMINISTRADOR_CONJUNTO: ["unidades", "crear", "perfil"],
};

const ETIQUETAS_PESTANA = {
  unidades: "Unidades disponibles",
  crear: "Crear unidad",
  perfil: "Mi perfil",
};

function App() {
  const [vista, setVista] = useState("login");
  const [codigoVerificacion, setCodigoVerificacion] = useState("");
  const [usuarioActual, setUsuarioActual] = useState(null);
  const [pestanaActiva, setPestanaActiva] = useState("unidades");

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
    municipio: "",
  });

  // =========================================================
  // ESTADO DE UNIDADES (F-02)
  // =========================================================

  const [unidades, setUnidades] = useState([]);
  const [cargandoUnidades, setCargandoUnidades] = useState(false);
  const [errorUnidades, setErrorUnidades] = useState("");
  const [unidadSeleccionada, setUnidadSeleccionada] = useState(null);

  // Filtros del listado (VC-39)
  const [filtroDepartamento, setFiltroDepartamento] = useState("");
  const [filtroTipo, setFiltroTipo] = useState("");

  const [formUnidad, setFormUnidad] = useState({
    identificador: "",
    tipoInmueble: "",
    precioLista: "",
    area: "",
    habitaciones: "",
    banos: "",
    descripcion: "",
    departamento: "",
    municipio: "",
  });

  const departamentos = {
    QUINDIO: [
      "Armenia",
      "Calarcá",
      "Circasia",
      "Montenegro",
      "Quimbaya",
      "Salento",
      "Filandia",
    ],
    RISARALDA: ["Pereira", "Dosquebradas", "Santa Rosa de Cabal"],
    CALDAS: ["Manizales", "Chinchiná", "Villamaría"],
    ANTIOQUIA: ["Medellín", "Envigado", "Bello"],
    VALLE_DEL_CAUCA: ["Cali", "Palmira", "Buga"],
  };

  const manejarCambio = (evento) => {
    const { name, value } = evento.target;

    setFormulario((anterior) => ({
      ...anterior,
      [name]: value,
      ...(name === "departamento" ? { municipio: "" } : {}),
    }));
  };

  const manejarCambioUnidad = (evento) => {
    const { name, value } = evento.target;

    setFormUnidad((anterior) => ({
      ...anterior,
      [name]: value,
      ...(name === "departamento" ? { municipio: "" } : {}),
    }));
  };

  const formatearCOP = (valor) => {
    if (!valor) return "";

    const numero = valor.replace(/\D/g, "");

    return new Intl.NumberFormat("es-CO").format(numero);
  };

  const formatearPrecio = (numero) => {
    if (numero === null || numero === undefined) return "";

    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      maximumFractionDigits: 0,
    }).format(numero);
  };

  const manejarMonto = (evento) => {
    const { name, value } = evento.target;

    setFormulario((anterior) => ({
      ...anterior,
      [name]: value.replace(/\D/g, ""),
    }));
  };

  const manejarMontoUnidad = (evento) => {
    const { name, value } = evento.target;

    setFormUnidad((anterior) => ({
      ...anterior,
      [name]: value.replace(/\D/g, ""),
    }));
  };

  // =========================================================
  // LOGIN
  // =========================================================

  const iniciarSesion = async (evento) => {
    evento.preventDefault();

    try {
      const respuesta = await fetch("http://localhost:8080/usuarios/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: formulario.email,
          contrasena: formulario.contrasena,
        }),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        alert(datos.mensaje || "Correo o contraseña incorrectos");
        return;
      }

      setUsuarioActual(datos);

      const pestanasDisponibles = PESTANAS_POR_ROL[datos.rol] || ["unidades"];

      setPestanaActiva(pestanasDisponibles[0]);
      setVista("panel");

      if (pestanasDisponibles.includes("unidades")) {
        await cargarUnidadesDisponibles();
      }
    } catch (error) {
      console.error(error);
      alert("No se pudo conectar con el servidor");
    }
  };

  const cerrarSesion = () => {
    setUsuarioActual(null);
    setVista("login");
    setFormulario((anterior) => ({
      ...anterior,
      email: "",
      contrasena: "",
    }));
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
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              email: formulario.email,
            }),
          },
      );

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        alert(datos.mensaje || "No se pudo generar el código");
        return;
      }

      setVista("verificacion");
    } catch (error) {
      console.error(error);

      alert("No se pudo conectar con el servidor");
    }
  };

  // =========================================================
  // VERIFICAR CORREO Y REGISTRAR CLIENTE
  // =========================================================

  const verificarCorreo = async (evento) => {
    evento.preventDefault();

    try {
      console.log("EMAIL ENVIADO:", formulario.email);
      console.log("CODIGO INGRESADO:", codigoVerificacion);

      const respuestaVerificacion = await fetch(
          "http://localhost:8080/verificacion-email/verificar",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              email: formulario.email,
              codigo: codigoVerificacion,
            }),
          },
      );

      const datosVerificacion = await respuestaVerificacion.json();

      console.log("RESPUESTA VERIFICACIÓN:", datosVerificacion);

      if (!respuestaVerificacion.ok) {
        alert(
            datosVerificacion.mensaje ||
            "El código de verificación es incorrecto",
        );

        return;
      }

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
        municipio: formulario.municipio,
      });

      const respuestaCliente = await fetch("http://localhost:8080/clientes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
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
          municipio: formulario.municipio,
        }),
      });

      const textoCliente = await respuestaCliente.text();

      let datosCliente = {};

      if (textoCliente) {
        try {
          datosCliente = JSON.parse(textoCliente);
        } catch {
          datosCliente = {
            mensaje: textoCliente,
          };
        }
      }

      if (!respuestaCliente.ok) {
        console.error(
            "Error al registrar cliente:",
            respuestaCliente.status,
            JSON.stringify(datosCliente),
        );

        alert(
            `Error ${respuestaCliente.status}: ${
                datosCliente.mensaje ||
                textoCliente ||
                "No se pudo registrar el cliente"
            }`,
        );

        return;
      }

      alert(datosCliente.mensaje || "Cliente registrado correctamente");

      setVista("login");
      setCodigoVerificacion("");
    } catch (error) {
      console.error(error);

      alert("No se pudo conectar con el servidor");
    }
  };

  // =========================================================
  // F-02: CARGAR Y MOSTRAR UNIDADES DISPONIBLES
  // =========================================================

  const cargarUnidadesDisponibles = async () => {
    setCargandoUnidades(true);
    setErrorUnidades("");

    try {
      const respuesta = await fetch(
          "http://localhost:8080/unidades/disponibles",
      );

      if (!respuesta.ok) {
        throw new Error("El servidor respondió con estado " + respuesta.status);
      }

      const datos = await respuesta.json();

      setUnidades(datos);
    } catch (error) {
      console.error(error);
      setErrorUnidades(
          "No se pudieron cargar las unidades disponibles. Verifica que el backend esté corriendo.",
      );
    } finally {
      setCargandoUnidades(false);
    }
  };

  const verDetalleUnidad = (unidad) => {
    setUnidadSeleccionada(unidad);
    setVista("detalleUnidad");
  };

  const volverAlPanel = () => {
    setUnidadSeleccionada(null);
    setPestanaActiva("unidades");
    setVista("panel");
  };

  // =========================================================
  // CREAR UNIDAD (Administrador / Director comercial)
  // =========================================================

  const crearUnidad = async (evento) => {
    evento.preventDefault();

    try {
      const respuesta = await fetch("http://localhost:8080/unidades", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          identificador: formUnidad.identificador,
          tipoInmueble: formUnidad.tipoInmueble,
          precioLista: Number(formUnidad.precioLista),
          area: Number(formUnidad.area),
          habitaciones: Number(formUnidad.habitaciones),
          banos: Number(formUnidad.banos),
          descripcion: formUnidad.descripcion,
          departamento: formUnidad.departamento,
          municipio: {
            nombre: formUnidad.municipio,
            departamento: formUnidad.departamento,
          },
          estado: "DISPONIBLE",
        }),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        alert(datos.mensaje || "No se pudo crear la unidad");
        return;
      }

      alert(datos.mensaje || "Unidad creada correctamente");

      setFormUnidad({
        identificador: "",
        tipoInmueble: "",
        precioLista: "",
        area: "",
        habitaciones: "",
        banos: "",
        descripcion: "",
        departamento: "",
        municipio: "",
      });

      await cargarUnidadesDisponibles();
      setPestanaActiva("unidades");
    } catch (error) {
      console.error(error);
      alert("No se pudo conectar con el servidor");
    }
  };

  const pestanasDelUsuario = usuarioActual
      ? PESTANAS_POR_ROL[usuarioActual.rol] || ["unidades"]
      : [];

  // Filtrado en el cliente: ya tenemos todas las disponibles cargadas
  const unidadesFiltradas = unidades.filter((unidad) => {
    const coincideDepartamento =
        !filtroDepartamento || unidad.departamento === filtroDepartamento;

    const coincideTipo = !filtroTipo || unidad.tipoInmueble === filtroTipo;

    return coincideDepartamento && coincideTipo;
  });

  // =========================================================
  // FOTOS DE UNIDAD (VC-38, solo Director comercial / Administrador)
  // =========================================================

  const [subiendoFotoUnidad, setSubiendoFotoUnidad] = useState(false);

  const manejarCambioFotoUnidad = (evento) => {
    const archivo = evento.target.files?.[0];

    if (!archivo || !unidadSeleccionada) return;

    const lector = new FileReader();

    lector.onload = async () => {
      const fotoBase64 = lector.result;

      setSubiendoFotoUnidad(true);

      try {
        const respuesta = await fetch(
            `http://localhost:8080/unidades/${unidadSeleccionada.identificador}/fotos`,
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify({ foto: fotoBase64 }),
            },
        );

        const datos = await respuesta.json();

        if (!respuesta.ok) {
          alert(datos.mensaje || "No se pudo agregar la foto");
          return;
        }

        setUnidadSeleccionada((anterior) => ({
          ...anterior,
          fotos: datos.fotos,
        }));

        // También refrescamos el listado para que la miniatura se vea actualizada
        setUnidades((anteriores) =>
            anteriores.map((u) =>
                u.identificador === unidadSeleccionada.identificador
                    ? { ...u, fotos: datos.fotos }
                    : u,
            ),
        );
      } catch (error) {
        console.error(error);
        alert("No se pudo conectar con el servidor");
      } finally {
        setSubiendoFotoUnidad(false);
      }
    };

    lector.readAsDataURL(archivo);
  };

  // =========================================================
  // MI PERFIL: SOLO SE PUEDE CAMBIAR LA FOTO
  // =========================================================

  const [subiendoFoto, setSubiendoFoto] = useState(false);

  const manejarCambioFoto = (evento) => {
    const archivo = evento.target.files?.[0];

    if (!archivo) return;

    const lector = new FileReader();

    lector.onload = async () => {
      const fotoBase64 = lector.result;

      setSubiendoFoto(true);

      try {
        const respuesta = await fetch(
            `http://localhost:8080/usuarios/${usuarioActual.id}/foto`,
            {
              method: "PUT",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify({ foto: fotoBase64 }),
            },
        );

        const datos = await respuesta.json();

        if (!respuesta.ok) {
          alert(datos.mensaje || "No se pudo actualizar la foto");
          return;
        }

        setUsuarioActual(datos);
      } catch (error) {
        console.error(error);
        alert("No se pudo conectar con el servidor");
      } finally {
        setSubiendoFoto(false);
      }
    };

    lector.readAsDataURL(archivo);
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
            {usuarioActual ? (
                <span>
              {usuarioActual.nombre} · {usuarioActual.rol.replaceAll("_", " ")}
            </span>
            ) : (
                <span>Gestión inmobiliaria</span>
            )}
          </div>
        </header>

        {/* =====================================================
          CONTENIDO PRINCIPAL
      ====================================================== */}

        <main className="main-content">
          <div className="auth-card">
            {/* =================================================
              ENCABEZADO (solo antes de iniciar sesión)
          ================================================== */}

            {!usuarioActual && (
                <div className="auth-header">
                  <div className="auth-icon">
                    <img
                        src="https://static.vecteezy.com/system/resources/thumbnails/021/828/953/small/coffee-bean-icon-logo-illustration-vector.jpg"
                        alt="Logo"
                    />
                  </div>

                  <h1>Venta Condominio</h1>

                  <p>Gestión de unidades y clientes</p>
                </div>
            )}

            {/* =================================================
              LOGIN
          ================================================== */}

            {vista === "login" && (
                <form className="auth-form" onSubmit={iniciarSesion}>
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

                  <button type="submit" className="primary-button">
                    Iniciar sesión
                  </button>

                  <div className="separator">
                    <span>o</span>
                  </div>

                  <div className="register-question">
                    <span>¿No tienes una cuenta?</span>

                    <button type="button" onClick={() => setVista("registro")}>
                      Crear cuenta
                    </button>
                  </div>
                </form>
            )}

            {/* =================================================
              REGISTRO
          ================================================== */}

            {vista === "registro" && (
                <form className="auth-form register-form" onSubmit={crearCuenta}>
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
                      <label>Cédula</label>
                      <input
                          name="cedula"
                          placeholder="Número de cédula"
                          value={formulario.cedula}
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
                      <label>Tipo de inmueble</label>
                      <select
                          name="tipoInmueble"
                          value={formulario.tipoInmueble}
                          onChange={manejarCambio}
                          required
                      >
                        <option value="" disabled>
                          Seleccionar
                        </option>
                        <option value="APARTAMENTO">Apartamento</option>
                        <option value="CASA">Casa</option>
                      </select>
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
                        {Object.keys(departamentos).map((departamento) => (
                            <option key={departamento} value={departamento}>
                              {departamento.replaceAll("_", " ")}
                            </option>
                        ))}
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
                            departamentos[formulario.departamento].map(
                                (municipio) => (
                                    <option key={municipio} value={municipio}>
                                      {municipio}
                                    </option>
                                ),
                            )}
                      </select>
                    </div>
                  </div>

                  <button type="submit" className="primary-button">
                    Crear cuenta
                  </button>

                  <div className="register-question">
                    <span>¿Ya tienes una cuenta?</span>
                    <button type="button" onClick={() => setVista("login")}>
                      Iniciar sesión
                    </button>
                  </div>
                </form>
            )}

            {/* =================================================
              VERIFICACIÓN DE CORREO
          ================================================== */}

            {vista === "verificacion" && (
                <form className="auth-form" onSubmit={verificarCorreo}>
                  <h2>Verificar correo</h2>

                  <p className="form-description">
                    Ingresa el código que recibiste en tu correo
                  </p>

                  <div className="input-group">
                    <label>Código de verificación</label>
                    <input
                        type="text"
                        inputMode="numeric"
                        placeholder="Código de 6 dígitos"
                        maxLength="6"
                        value={codigoVerificacion}
                        onChange={(evento) =>
                            setCodigoVerificacion(
                                evento.target.value.replace(/\D/g, ""),
                            )
                        }
                        required
                    />
                  </div>

                  <button type="submit" className="primary-button">
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

            {/* =================================================
              PANEL PRINCIPAL (después de iniciar sesión)
              Pestañas según el rol del usuario
          ================================================== */}

            {vista === "panel" && usuarioActual && (
                <div className="panel-view">
                  <div className="panel-tabs">
                    {pestanasDelUsuario.map((pestana) => (
                        <button
                            key={pestana}
                            type="button"
                            className={
                              pestana === pestanaActiva
                                  ? "tab-button tab-button-activo"
                                  : "tab-button"
                            }
                            onClick={() => setPestanaActiva(pestana)}
                        >
                          {ETIQUETAS_PESTANA[pestana]}
                        </button>
                    ))}

                    <button
                        type="button"
                        className="tab-button tab-button-salir"
                        onClick={cerrarSesion}
                    >
                      Cerrar sesión
                    </button>
                  </div>

                  {/* ---- Pestaña: Unidades disponibles ---- */}

                  {pestanaActiva === "unidades" && (
                      <div className="units-view">
                        {cargandoUnidades && <p>Cargando unidades...</p>}

                        {!cargandoUnidades && errorUnidades && (
                            <p className="error-text">{errorUnidades}</p>
                        )}

                        {!cargandoUnidades &&
                            !errorUnidades &&
                            unidades.length === 0 && (
                                <p>No hay unidades disponibles en este momento.</p>
                            )}

                        {!cargandoUnidades && unidades.length > 0 && (
                            <>
                              <div className="units-filtros">
                                <div className="input-group">
                                  <label>Departamento</label>
                                  <select
                                      value={filtroDepartamento}
                                      onChange={(evento) =>
                                          setFiltroDepartamento(evento.target.value)
                                      }
                                  >
                                    <option value="">Todos</option>
                                    {Object.keys(departamentos).map((departamento) => (
                                        <option key={departamento} value={departamento}>
                                          {departamento.replaceAll("_", " ")}
                                        </option>
                                    ))}
                                  </select>
                                </div>

                                <div className="input-group">
                                  <label>Tipo de inmueble</label>
                                  <select
                                      value={filtroTipo}
                                      onChange={(evento) =>
                                          setFiltroTipo(evento.target.value)
                                      }
                                  >
                                    <option value="">Todos</option>
                                    <option value="APARTAMENTO">Apartamento</option>
                                    <option value="CASA">Casa</option>
                                  </select>
                                </div>

                                {(filtroDepartamento || filtroTipo) && (
                                    <button
                                        type="button"
                                        className="secondary-button units-filtros-limpiar"
                                        onClick={() => {
                                          setFiltroDepartamento("");
                                          setFiltroTipo("");
                                        }}
                                    >
                                      Limpiar filtros
                                    </button>
                                )}
                              </div>

                              {unidadesFiltradas.length === 0 ? (
                                  <p>Ninguna unidad coincide con los filtros.</p>
                              ) : (
                                  <div className="units-grid">
                                    {unidadesFiltradas.map((unidad) => (
                                        <div
                                            key={unidad.identificador}
                                            className="unit-card"
                                        >
                                          {unidad.fotos && unidad.fotos.length > 0 ? (
                                              <img
                                                  src={unidad.fotos[0]}
                                                  alt={unidad.identificador}
                                                  className="unit-card-foto"
                                              />
                                          ) : (
                                              <div className="unit-card-foto unit-card-foto-vacia">
                                                Sin foto
                                              </div>
                                          )}

                                          <div className="unit-card-header">
                                            <strong>{unidad.identificador}</strong>
                                            <span className="unit-badge">
                                  {unidad.estado}
                                </span>
                                          </div>

                                          <p className="unit-tipo">{unidad.tipoInmueble}</p>

                                          <p className="unit-precio">
                                            {formatearPrecio(unidad.precioLista)}
                                          </p>

                                          <p className="unit-ubicacion">
                                            {unidad.municipio?.nombre},{" "}
                                            {unidad.departamento?.replaceAll("_", " ")}
                                          </p>

                                          <p className="unit-specs">
                                            {unidad.area} m² · {unidad.habitaciones} hab ·{" "}
                                            {unidad.banos} baños
                                          </p>

                                          <button
                                              type="button"
                                              className="primary-button"
                                              onClick={() => verDetalleUnidad(unidad)}
                                          >
                                            Ver detalle
                                          </button>
                                        </div>
                                    ))}
                                  </div>
                              )}
                            </>
                        )}
                      </div>
                  )}

                  {/* ---- Pestaña: Crear unidad (Administrador / Director comercial) ---- */}

                  {pestanaActiva === "crear" && (
                      <form className="auth-form" onSubmit={crearUnidad}>
                        <h2>Crear unidad</h2>

                        <p className="form-description">
                          Registra un nuevo condominio disponible para la venta
                        </p>

                        <div className="form-grid">
                          <div className="input-group">
                            <label>Identificador</label>
                            <input
                                name="identificador"
                                placeholder="APT-101"
                                value={formUnidad.identificador}
                                onChange={manejarCambioUnidad}
                                required
                            />
                          </div>

                          <div className="input-group">
                            <label>Tipo de inmueble</label>
                            <select
                                name="tipoInmueble"
                                value={formUnidad.tipoInmueble}
                                onChange={manejarCambioUnidad}
                                required
                            >
                              <option value="" disabled>
                                Seleccionar
                              </option>
                              <option value="APARTAMENTO">Apartamento</option>
                              <option value="CASA">Casa</option>
                            </select>
                          </div>

                          <div className="input-group">
                            <label>Precio de lista</label>
                            <input
                                type="text"
                                inputMode="numeric"
                                name="precioLista"
                                placeholder="$ 0"
                                value={
                                  formUnidad.precioLista
                                      ? `$ ${formatearCOP(formUnidad.precioLista)}`
                                      : ""
                                }
                                onChange={manejarMontoUnidad}
                                required
                            />
                          </div>

                          <div className="input-group">
                            <label>Área (m²)</label>
                            <input
                                type="number"
                                name="area"
                                placeholder="65"
                                value={formUnidad.area}
                                onChange={manejarCambioUnidad}
                                required
                            />
                          </div>

                          <div className="input-group">
                            <label>Habitaciones</label>
                            <input
                                type="number"
                                name="habitaciones"
                                placeholder="3"
                                value={formUnidad.habitaciones}
                                onChange={manejarCambioUnidad}
                            />
                          </div>

                          <div className="input-group">
                            <label>Baños</label>
                            <input
                                type="number"
                                name="banos"
                                placeholder="2"
                                value={formUnidad.banos}
                                onChange={manejarCambioUnidad}
                            />
                          </div>

                          <div className="input-group">
                            <label>Departamento</label>
                            <select
                                name="departamento"
                                value={formUnidad.departamento}
                                onChange={manejarCambioUnidad}
                                required
                            >
                              <option value="" disabled>
                                Seleccionar
                              </option>
                              {Object.keys(departamentos).map((departamento) => (
                                  <option key={departamento} value={departamento}>
                                    {departamento.replaceAll("_", " ")}
                                  </option>
                              ))}
                            </select>
                          </div>

                          <div className="input-group">
                            <label>Municipio</label>
                            <select
                                name="municipio"
                                value={formUnidad.municipio}
                                onChange={manejarCambioUnidad}
                                disabled={!formUnidad.departamento}
                                required
                            >
                              <option value="" disabled>
                                Seleccionar
                              </option>
                              {formUnidad.departamento &&
                                  departamentos[formUnidad.departamento].map(
                                      (municipio) => (
                                          <option key={municipio} value={municipio}>
                                            {municipio}
                                          </option>
                                      ),
                                  )}
                            </select>
                          </div>

                          <div className="input-group full-width">
                            <label>Descripción</label>
                            <input
                                name="descripcion"
                                placeholder="Apartamento con vista al parque"
                                value={formUnidad.descripcion}
                                onChange={manejarCambioUnidad}
                            />
                          </div>
                        </div>

                        <button type="submit" className="primary-button">
                          Crear unidad
                        </button>
                      </form>
                  )}

                  {/* ---- Pestaña: Mi perfil (solo la foto es editable) ---- */}

                  {pestanaActiva === "perfil" && (
                      <div className="profile-view">
                        <h2>Mi perfil</h2>

                        <div className="profile-photo-wrapper">
                          {usuarioActual.fotoPerfil ? (
                              <img
                                  src={usuarioActual.fotoPerfil}
                                  alt="Foto de perfil"
                                  className="profile-photo"
                              />
                          ) : (
                              <div className="profile-photo profile-photo-vacia">
                                Sin foto
                              </div>
                          )}
                        </div>

                        <label className="secondary-button profile-upload-label">
                          {subiendoFoto ? "Subiendo..." : "Cambiar foto"}
                          <input
                              type="file"
                              accept="image/*"
                              onChange={manejarCambioFoto}
                              disabled={subiendoFoto}
                              hidden
                          />
                        </label>

                        <div className="profile-info-grid">
                          <div>
                            <label>Nombre</label>
                            <p>{usuarioActual.nombre}</p>
                          </div>

                          <div>
                            <label>Correo electrónico</label>
                            <p>{usuarioActual.email}</p>
                          </div>

                          <div>
                            <label>Rol</label>
                            <p>{usuarioActual.rol.replaceAll("_", " ")}</p>
                          </div>
                        </div>

                        <p className="form-description">
                          Los demás datos de tu perfil no se pueden editar desde aquí.
                        </p>
                      </div>
                  )}
                </div>
            )}

            {/* =================================================
              FICHA TÉCNICA / DETALLE DE UNIDAD (F-02)
          ================================================== */}

            {vista === "detalleUnidad" && unidadSeleccionada && (
                <div className="unit-detail-view">
                  <div className="form-top">
                    <button
                        type="button"
                        className="back-button"
                        onClick={volverAlPanel}
                    >
                      ← Volver al listado
                    </button>
                  </div>

                  <h2>{unidadSeleccionada.identificador}</h2>

                  <span className="unit-badge">{unidadSeleccionada.estado}</span>

                  <p className="unit-precio-grande">
                    {formatearPrecio(unidadSeleccionada.precioLista)}
                  </p>

                  {unidadSeleccionada.fotos &&
                  unidadSeleccionada.fotos.length > 0 ? (
                      <div className="unit-gallery">
                        {unidadSeleccionada.fotos.map((foto, indice) => (
                            <img
                                key={indice}
                                src={foto}
                                alt={`${unidadSeleccionada.identificador} foto ${indice + 1}`}
                                className="unit-gallery-foto"
                            />
                        ))}
                      </div>
                  ) : (
                      <p className="form-description">
                        Esta unidad todavía no tiene fotos.
                      </p>
                  )}

                  {(usuarioActual?.rol === "DIRECTOR_COMERCIAL" ||
                      usuarioActual?.rol === "ADMINISTRADOR_CONJUNTO") && (
                      <label className="secondary-button profile-upload-label">
                        {subiendoFotoUnidad ? "Subiendo..." : "Agregar foto"}
                        <input
                            type="file"
                            accept="image/*"
                            onChange={manejarCambioFotoUnidad}
                            disabled={subiendoFotoUnidad}
                            hidden
                        />
                      </label>
                  )}

                  {unidadSeleccionada.descripcion && (
                      <p className="form-description">
                        {unidadSeleccionada.descripcion}
                      </p>
                  )}

                  <div className="unit-detail-grid">
                    <div>
                      <label>Tipo de inmueble</label>
                      <p>{unidadSeleccionada.tipoInmueble}</p>
                    </div>

                    <div>
                      <label>Área</label>
                      <p>{unidadSeleccionada.area} m²</p>
                    </div>

                    <div>
                      <label>Habitaciones</label>
                      <p>{unidadSeleccionada.habitaciones}</p>
                    </div>

                    <div>
                      <label>Baños</label>
                      <p>{unidadSeleccionada.banos}</p>
                    </div>

                    <div>
                      <label>Departamento</label>
                      <p>{unidadSeleccionada.departamento?.replaceAll("_", " ")}</p>
                    </div>

                    <div>
                      <label>Municipio</label>
                      <p>{unidadSeleccionada.municipio?.nombre}</p>
                    </div>
                  </div>
                </div>
            )}
          </div>
        </main>
      </div>
  );
}

export default App;
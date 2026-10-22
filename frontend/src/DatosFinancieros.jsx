import { useEffect, useState } from "react";
import { API_URL } from "./api";

// =========================================================
// Datos financieros de los clientes (asesor / director)
// - El cliente declara salario y presupuesto (cuota inicial) al registrarse
//   y NO puede cambiarlos.
// - Solo el equipo comercial los actualiza, después de revisar soportes.
// - Cada cambio queda en el historial (quién, cuándo, antes, después, soporte).
// =========================================================

const pesos = (numero) =>
  "$ " + new Intl.NumberFormat("es-CO", { maximumFractionDigits: 0 }).format(Number(numero) || 0);

const soloDigitos = (texto) => String(texto || "").replace(/\D/g, "");

export default function DatosFinancieros({ usuario }) {
  const [clientes, setClientes] = useState([]);
  const [emailCliente, setEmailCliente] = useState("");
  const [actuales, setActuales] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [form, setForm] = useState({ salario: "", presupuesto: "", motivo: "" });
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    fetch(`${API_URL}/clientes`)
      .then((r) => (r.ok ? r.json() : []))
      .then(setClientes)
      .catch(() => setClientes([]));
  }, []);

  const cargarCliente = async (email) => {
    setEmailCliente(email);
    setMensaje("");
    setError("");
    setActuales(null);
    setHistorial([]);

    if (!email) return;

    const solicitante = encodeURIComponent(usuario.email);
    const cliente = encodeURIComponent(email);

    try {
      const [rDatos, rHistorial] = await Promise.all([
        fetch(`${API_URL}/clientes/${cliente}/datos-financieros?email=${solicitante}`),
        fetch(`${API_URL}/clientes/${cliente}/historial-financiero?email=${solicitante}`),
      ]);

      const datos = await rDatos.json();
      if (!rDatos.ok) throw new Error(datos.mensaje || "No se pudieron cargar los datos");

      setActuales(datos);
      setForm({ salario: String(datos.salario), presupuesto: String(datos.presupuesto), motivo: "" });
      setHistorial(rHistorial.ok ? await rHistorial.json() : []);
    } catch (e) {
      setError(e.message);
    }
  };

  const guardar = async (evento) => {
    evento.preventDefault();
    setMensaje("");
    setError("");
    setGuardando(true);

    try {
      const respuesta = await fetch(
        `${API_URL}/clientes/${encodeURIComponent(emailCliente)}/datos-financieros`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            emailAsesor: usuario.email,
            salario: Number(form.salario),
            presupuesto: Number(form.presupuesto),
            motivo: form.motivo,
          }),
        },
      );

      const datos = await respuesta.json();
      if (!respuesta.ok) throw new Error(datos.mensaje || "No se pudo guardar");

      await cargarCliente(emailCliente);
      setMensaje("Datos actualizados. El cambio quedó registrado en el historial.");
    } catch (e) {
      setError(e.message);
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div className="df-view">
      <h2>Datos financieros de clientes</h2>
      <p>
        Los ingresos del hogar y el dinero disponible para la cuota inicial los declara el cliente al
        registrarse. Solo el equipo comercial puede actualizarlos, con un soporte.
      </p>

      <div className="input-group">
        <label>Cliente</label>
        <select value={emailCliente} onChange={(e) => cargarCliente(e.target.value)}>
          <option value="">Selecciona un cliente</option>
          {clientes.map((c) => (
            <option key={c.email} value={c.email}>
              {c.nombre} — {c.email}
            </option>
          ))}
        </select>
      </div>

      {error && <p className="error-text">{error}</p>}
      {mensaje && <p className="df-ok">{mensaje}</p>}

      {actuales && (
        <form className="df-form" onSubmit={guardar}>
          <p>
            Actual: ingresos del hogar <strong>{pesos(actuales.salario)}</strong> · cuota inicial disponible{" "}
            <strong>{pesos(actuales.presupuesto)}</strong>
          </p>

          <div className="input-group">
            <label>Nuevos ingresos mensuales del hogar</label>
            <input
              type="text"
              inputMode="numeric"
              value={form.salario ? pesos(form.salario) : ""}
              onChange={(e) => setForm({ ...form, salario: soloDigitos(e.target.value) })}
              required
            />
          </div>

          <div className="input-group">
            <label>Nuevo dinero disponible para la cuota inicial</label>
            <input
              type="text"
              inputMode="numeric"
              value={form.presupuesto ? pesos(form.presupuesto) : ""}
              onChange={(e) => setForm({ ...form, presupuesto: soloDigitos(e.target.value) })}
              required
            />
          </div>

          <div className="input-group">
            <label>Soporte que justifica el cambio</label>
            <input
              type="text"
              placeholder="Ej. certificado laboral, ingresos del codeudor, extracto bancario"
              value={form.motivo}
              onChange={(e) => setForm({ ...form, motivo: e.target.value })}
              required
            />
          </div>

          <button type="submit" className="primary-button" disabled={guardando}>
            {guardando ? "Guardando..." : "Actualizar datos"}
          </button>
        </form>
      )}

      {actuales && (
        <div>
          <h3>Historial de cambios</h3>
          {historial.length === 0 ? (
            <p>Sin cambios: conserva los datos que declaró al registrarse.</p>
          ) : (
            <table className="df-tabla">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Ingresos del hogar</th>
                  <th>Cuota inicial disponible</th>
                  <th>Hecho por</th>
                  <th>Soporte</th>
                </tr>
              </thead>
              <tbody>
                {historial.map((c) => (
                  <tr key={c.id}>
                    <td>{String(c.fecha).replace("T", " ")}</td>
                    <td>
                      {pesos(c.salarioAnterior)} → {pesos(c.salarioNuevo)}
                    </td>
                    <td>
                      {pesos(c.presupuestoAnterior)} → {pesos(c.presupuestoNuevo)}
                    </td>
                    <td>{c.modificadoPor}</td>
                    <td>{c.motivo}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

import { useEffect, useState } from "react";
import { API_URL } from "./api";
import "./Cotizaciones.css";

// =========================================================
// F-02 RECORRIDOS POR EL CONJUNTO
// - Cliente: agenda desde la ficha de la unidad (KAN-181) y ve "Mis recorridos"
// - Asesor / director: agenda de recorridos (KAN-182): agendar para un
//   cliente, iniciar (unidad EN_RECORRIDO), finalizar (vuelve a DISPONIBLE)
// R001: solo clientes registrados pueden agendar (lo valida el backend).
// El backend también impide horas pasadas, cruces de horario del cliente y
// dos recorridos pendientes del mismo cliente a la misma unidad.
// =========================================================

const HORAS = ["08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

const ESTADOS = {
  AGENDADO: { texto: "Agendado", clase: "generada" },
  EN_CURSO: { texto: "En curso", clase: "pendiente" },
  FINALIZADO: { texto: "Finalizado", clase: "firmada" },
  CANCELADO: { texto: "Cancelado", clase: "vencida" },
};

const hoyIso = () => {
  const d = new Date();
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};

const mananaIso = () => {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
};

// Si el día es hoy, solo se ofrecen las horas que aún no han pasado
const horasDisponibles = (dia) => {
  if (dia !== hoyIso()) return HORAS;

  const ahora = new Date();
  const minutosAhora = ahora.getHours() * 60 + ahora.getMinutes();

  return HORAS.filter((h) => {
    const [hh, mm] = h.split(":").map(Number);
    return hh * 60 + mm > minutosAhora;
  });
};

// Primer día con horas libres (hoy, o mañana si ya pasó el horario)
const primerDia = () => (horasDisponibles(hoyIso()).length > 0 ? hoyIso() : mananaIso());

function SelectorHora({ dia, hora, onChange, name }) {
  const opciones = horasDisponibles(dia);

  // Si la hora elegida ya no es válida para el día, se pasa a la primera libre
  useEffect(() => {
    if (opciones.length > 0 && !opciones.includes(hora)) {
      onChange({ target: { name: name || "hora", value: opciones[0] } });
    }
  }, [dia]); // eslint-disable-line react-hooks/exhaustive-deps

  if (opciones.length === 0) {
    return <p className="cot-mensaje cot-mensaje-error">Ya no quedan horarios hoy. Elige otro día.</p>;
  }

  return (
    <select name={name} value={opciones.includes(hora) ? hora : opciones[0]} onChange={onChange}>
      {opciones.map((h) => (
        <option key={h} value={h}>
          {h}
        </option>
      ))}
    </select>
  );
}

const fecha = (iso) =>
  iso
    ? new Date(iso + "T00:00:00").toLocaleDateString("es-CO", {
        weekday: "short",
        day: "numeric",
        month: "short",
        year: "numeric",
      })
    : "";

async function pedir(ruta, opciones = {}) {
  const respuesta = await fetch(`${API_URL}${ruta}`, {
    headers: { "Content-Type": "application/json" },
    ...opciones,
  });

  const texto = await respuesta.text();
  let datos;

  try {
    datos = texto ? JSON.parse(texto) : {};
  } catch {
    datos = { mensaje: texto };
  }

  if (!respuesta.ok) {
    throw new Error(datos.mensaje || `Error ${respuesta.status}`);
  }

  return datos;
}

function Mensaje({ mensaje }) {
  if (!mensaje) return null;

  return (
    <p className={mensaje.tipo === "error" ? "cot-mensaje cot-mensaje-error" : "cot-mensaje"}>
      {mensaje.texto}
    </p>
  );
}

// ---------------------------------------------------------
// Botón "Agendar recorrido" en la ficha técnica (cliente)
// ---------------------------------------------------------
export function AgendarRecorrido({ usuario, unidad }) {
  const [abierto, setAbierto] = useState(false);
  const [dia, setDia] = useState(primerDia());
  const [hora, setHora] = useState(horasDisponibles(primerDia())[0] || "08:00");
  const [mensaje, setMensaje] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const agendar = async (evento) => {
    evento.preventDefault();
    setEnviando(true);
    setMensaje(null);

    try {
      await pedir("/recorridos", {
        method: "POST",
        body: JSON.stringify({
          identificadorUnidad: unidad.identificador,
          email: usuario.email,
          fecha: dia,
          hora,
        }),
      });

      setMensaje({
        tipo: "ok",
        texto: `Recorrido agendado para el ${fecha(dia)} a las ${hora}. Lo ves en "Mis recorridos".`,
      });
      setAbierto(false);
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="cot-view recorrido-ficha">
      <Mensaje mensaje={mensaje} />

      {!abierto && (
        <button type="button" className="primary-button" onClick={() => setAbierto(true)}>
          Agendar recorrido
        </button>
      )}

      {abierto && (
        <form className="cot-form" onSubmit={agendar}>
          <h3>Agendar recorrido</h3>

          <div className="form-grid">
            <div className="input-group">
              <label>Día</label>
              <input type="date" value={dia} min={hoyIso()} onChange={(e) => setDia(e.target.value)} required />
            </div>

            <div className="input-group">
              <label>Hora</label>
              <SelectorHora dia={dia} hora={hora} onChange={(e) => setHora(e.target.value)} />
            </div>
          </div>

          <div className="cot-acciones">
            <button type="submit" className="primary-button" disabled={enviando}>
              {enviando ? "Agendando..." : "Confirmar recorrido"}
            </button>
            <button type="button" className="secondary-button" onClick={() => setAbierto(false)}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </div>
  );
}

// ---------------------------------------------------------
// Pestaña "Recorridos" / "Mis recorridos"
// ---------------------------------------------------------
export default function Recorridos({ usuario, unidades, onCambioUnidades }) {
  const esCliente = usuario.rol === "CLIENTE";

  const [recorridos, setRecorridos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState(null);
  const [version, setVersion] = useState(0);

  const [form, setForm] = useState({
    emailCliente: "",
    identificadorUnidad: "",
    fecha: primerDia(),
    hora: horasDisponibles(primerDia())[0] || "08:00",
  });

  useEffect(() => {
    let activo = true;

    async function cargar() {
      setCargando(true);

      try {
        const ruta = esCliente ? `/recorridos?cliente=${encodeURIComponent(usuario.email)}` : "/recorridos";

        const [lista, listaClientes] = await Promise.all([
          pedir(ruta),
          esCliente ? Promise.resolve([]) : pedir("/clientes"),
        ]);

        if (activo) {
          setRecorridos(lista);
          setClientes(listaClientes);
        }
      } catch (error) {
        if (activo) setMensaje({ tipo: "error", texto: error.message });
      } finally {
        if (activo) setCargando(false);
      }
    }

    cargar();

    return () => {
      activo = false;
    };
  }, [esCliente, usuario.email, version]);

  const cambiarForm = (evento) => {
    const { name, value } = evento.target;
    setForm((anterior) => ({ ...anterior, [name]: value }));
  };

  const agendar = async (evento) => {
    evento.preventDefault();
    setMensaje(null);

    try {
      await pedir("/recorridos", {
        method: "POST",
        body: JSON.stringify({ ...form, email: usuario.email }),
      });

      setMensaje({ tipo: "ok", texto: "Recorrido agendado." });
      setForm((anterior) => ({ ...anterior, emailCliente: "", identificadorUnidad: "" }));
      setVersion((v) => v + 1);
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    }
  };

  const accion = async (recorrido, tipo, textoOk) => {
    setMensaje(null);

    try {
      await pedir(`/recorridos/${recorrido.id}/${tipo}`, {
        method: "PUT",
        body: JSON.stringify({ email: usuario.email }),
      });

      setMensaje({ tipo: "ok", texto: textoOk });
      setVersion((v) => v + 1);
      if (tipo !== "cancelar" && onCambioUnidades) onCambioUnidades();
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    }
  };

  // Los cancelados no se le muestran al cliente; al equipo solo los de hoy en adelante
  const visibles = recorridos.filter(
    (r) => r.estado !== "CANCELADO" || (!esCliente && r.fecha >= hoyIso()),
  );

  return (
    <div className="cot-view">
      <Mensaje mensaje={mensaje} />

      {!esCliente && (
        <form className="cot-form" onSubmit={agendar}>
          <h3>Agendar recorrido</h3>

          <div className="input-group">
            <label>Cliente</label>
            <select name="emailCliente" value={form.emailCliente} onChange={cambiarForm} required>
              <option value="">Seleccionar</option>
              {clientes.map((c) => (
                <option key={c.email} value={c.email}>
                  {c.nombre} — {c.email}
                </option>
              ))}
            </select>
          </div>

          <div className="input-group">
            <label>Unidad disponible</label>
            <select name="identificadorUnidad" value={form.identificadorUnidad} onChange={cambiarForm} required>
              <option value="">Seleccionar</option>
              {unidades.map((u) => (
                <option key={u.identificador} value={u.identificador}>
                  {u.identificador} — {u.municipio?.nombre}
                </option>
              ))}
            </select>
          </div>

          <div className="form-grid">
            <div className="input-group">
              <label>Día</label>
              <input type="date" name="fecha" value={form.fecha} min={hoyIso()} onChange={cambiarForm} required />
            </div>

            <div className="input-group">
              <label>Hora</label>
              <SelectorHora name="hora" dia={form.fecha} hora={form.hora} onChange={cambiarForm} />
            </div>
          </div>

          <p className="cot-fechas">Horario de visitas: 8:00 a 17:00. Solo clientes registrados.</p>

          <button type="submit" className="primary-button">
            Agendar recorrido
          </button>
        </form>
      )}

      <section className="cot-seccion">
        <h3>{esCliente ? "Mis recorridos" : "Agenda de recorridos"}</h3>

        {cargando && <p>Cargando recorridos...</p>}

        {!cargando && visibles.length === 0 && (
          <p className="cot-vacio">
            {esCliente
              ? "Aún no tienes recorridos. Agéndalo desde la ficha de una unidad."
              : "No hay recorridos agendados."}
          </p>
        )}

        <div className="cot-grid">
          {visibles.map((r) => {
            const estado = ESTADOS[r.estado] || { texto: r.estado, clase: "" };

            return (
              <div key={r.id} className="cot-card">
                <div className="cot-card-header">
                  <div>
                    <strong>{r.identificadorUnidad}</strong>
                    {!esCliente && <span className="cot-cliente">{r.nombreCliente}</span>}
                  </div>
                  <span className={`cot-estado cot-estado-${estado.clase}`}>{estado.texto}</span>
                </div>

                <p className="cot-fechas">
                  {fecha(r.fecha)} · {r.hora}
                  {!esCliente && r.atendidoPor ? ` · atendido por ${r.atendidoPor}` : ""}
                </p>

                <div className="cot-acciones">
                  {!esCliente && r.estado === "AGENDADO" && (
                    <button
                      type="button"
                      className="primary-button"
                      onClick={() => accion(r, "iniciar", "Recorrido iniciado: la unidad queda EN RECORRIDO.")}
                    >
                      Iniciar
                    </button>
                  )}

                  {!esCliente && r.estado === "EN_CURSO" && (
                    <button
                      type="button"
                      className="primary-button"
                      onClick={() => accion(r, "finalizar", "Recorrido finalizado: la unidad vuelve a estar disponible.")}
                    >
                      Finalizar
                    </button>
                  )}

                  {r.estado === "AGENDADO" && (
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={() => accion(r, "cancelar", "Recorrido cancelado.")}
                    >
                      Cancelar
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
}

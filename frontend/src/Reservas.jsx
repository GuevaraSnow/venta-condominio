import { useEffect, useState } from "react";
import { API_URL } from "./api";
import "./Cotizaciones.css";

// =========================================================
// F-04 RESERVAS
// - Cliente: "Mis reservas" (KAN-110)
// - Asesor / director: todas las reservas y opción de cancelar
//   (la unidad vuelve a quedar disponible)
// La reserva se crea desde una cotización firmada (pestaña Cotizaciones).
// =========================================================

const ESTADOS = {
  PENDIENTE_PAGO: { texto: "Pendiente de pago", clase: "pendiente" },
  PAGADA: { texto: "Pagada", clase: "firmada" },
  CANCELADA: { texto: "Cancelada", clase: "vencida" },
};

const pesos = (numero) =>
  new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(Number(numero) || 0);

const fecha = (iso) =>
  iso
    ? new Date(iso + "T00:00:00").toLocaleDateString("es-CO", {
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

export default function Reservas({ usuario, onCambioUnidades }) {
  const esCliente = usuario.rol === "CLIENTE";

  const [reservas, setReservas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let activo = true;

    async function cargar() {
      setCargando(true);

      try {
        const ruta = esCliente
          ? `/reservas?cliente=${encodeURIComponent(usuario.email)}`
          : "/reservas";
        const lista = await pedir(ruta);

        if (activo) setReservas(lista);
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

  const cancelar = async (reserva) => {
    if (!window.confirm(`¿Cancelar la reserva de ${reserva.identificadorUnidad}? La unidad volverá a estar disponible.`)) {
      return;
    }

    setMensaje(null);

    try {
      await pedir(`/reservas/${reserva.id}/cancelar`, {
        method: "PUT",
        body: JSON.stringify({ email: usuario.email }),
      });
      setMensaje({ tipo: "ok", texto: "Reserva cancelada. La unidad volvió a estar disponible." });
      setVersion((v) => v + 1);
      if (onCambioUnidades) onCambioUnidades();
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    }
  };

  return (
    <div className="cot-view">
      {mensaje && (
        <p className={mensaje.tipo === "error" ? "cot-mensaje cot-mensaje-error" : "cot-mensaje"}>
          {mensaje.texto}
        </p>
      )}

      <section className="cot-seccion">
        <h3>{esCliente ? "Mis reservas" : "Reservas"}</h3>

        <p className="cot-fechas">
          La cuota de reserva es el 1 % del precio de lista, con un mínimo de $ 5.000.000.
          Para reservar, firma primero la cotización.
        </p>

        {cargando && <p>Cargando reservas...</p>}

        {!cargando && reservas.length === 0 && (
          <p className="cot-vacio">
            {esCliente
              ? "Aún no tienes reservas. Reserva desde una cotización firmada."
              : "Todavía no hay reservas."}
          </p>
        )}

        <div className="cot-grid">
          {reservas.map((reserva) => {
            const estado = ESTADOS[reserva.estado] || { texto: reserva.estado, clase: "" };

            return (
              <div key={reserva.id} className="cot-card">
                <div className="cot-card-header">
                  <div>
                    <strong>{reserva.identificadorUnidad}</strong>
                    {!esCliente && <span className="cot-cliente">{reserva.nombreCliente}</span>}
                  </div>
                  <span className={`cot-estado cot-estado-${estado.clase}`}>{estado.texto}</span>
                </div>

                <dl className="cot-valores">
                  <dt>Precio de lista</dt>
                  <dd>{pesos(reserva.precioLista)}</dd>

                  <dt>Precio final cotizado</dt>
                  <dd>{pesos(reserva.precioFinal)}</dd>

                  <dt className="cot-total">Cuota de reserva</dt>
                  <dd className="cot-total">{pesos(reserva.cuotaReserva)}</dd>
                </dl>

                <p className="cot-fechas">Reservada el {fecha(reserva.fechaReserva)}</p>
                {reserva.observacion && <p className="cot-fechas">{reserva.observacion}</p>}

                {!esCliente && reserva.estado === "PENDIENTE_PAGO" && (
                  <div className="cot-acciones">
                    <button type="button" className="secondary-button" onClick={() => cancelar(reserva)}>
                      Cancelar reserva
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
}

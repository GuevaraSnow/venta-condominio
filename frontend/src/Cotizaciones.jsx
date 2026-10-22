import { useEffect, useState } from "react";
import { API_URL } from "./api";
import "./Cotizaciones.css";

// =========================================================
// F-03 GENERAR COTIZACIÓN
// - Asesor / director: generan cotizaciones (KAN-100)
// - Director: autoriza descuentos mayores al 2 % (R003, KAN-101)
// - Cliente: ve sus cotizaciones y las firma (KAN-102)
// Las reglas viven en el backend (CotizacionService); aquí solo se
// muestra una vista previa con la misma fórmula.
// =========================================================

const CUOTA_INICIAL = 30;
const DESCUENTO_SIN_AUTORIZACION = 2;
const DESCUENTO_MAXIMO = 10;

// R002: cuota de reserva = 1 % del precio de lista, mínimo $5.000.000
const cuotaReserva = (precioLista) =>
  Math.max(Math.round((Number(precioLista) || 0) / 100), 5000000);

const ESTADOS = {
  GENERADA: { texto: "Lista para firmar", clase: "generada" },
  PENDIENTE_AUTORIZACION: { texto: "Pendiente de autorización", clase: "pendiente" },
  RECHAZADA: { texto: "Descuento rechazado", clase: "rechazada" },
  FIRMADA: { texto: "Firmada", clase: "firmada" },
  VENCIDA: { texto: "Vencida", clase: "vencida" },
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

// fetch que devuelve el JSON o lanza un Error con el mensaje del servidor
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

function calcularVistaPrevia(precioLista, porcentaje) {
  const precio = Number(precioLista) || 0;
  const descuento = Math.round((precio * (Number(porcentaje) || 0)) / 100);
  const precioFinal = precio - descuento;
  const cuotaInicial = Math.round((precioFinal * CUOTA_INICIAL) / 100);

  return {
    descuento,
    precioFinal,
    cuotaInicial,
    saldo: precioFinal - cuotaInicial,
  };
}

function TarjetaCotizacion({ cotizacion, mostrarCliente, acciones }) {
  const estado = ESTADOS[cotizacion.estado] || { texto: cotizacion.estado, clase: "" };

  return (
    <div className="cot-card">
      <div className="cot-card-header">
        <div>
          <strong>{cotizacion.identificadorUnidad}</strong>
          {mostrarCliente && <span className="cot-cliente">{cotizacion.nombreCliente}</span>}
        </div>
        <span className={`cot-estado cot-estado-${estado.clase}`}>{estado.texto}</span>
      </div>

      <dl className="cot-valores">
        <dt>Precio de lista</dt>
        <dd>{pesos(cotizacion.precioLista)}</dd>

        <dt>Descuento ({Number(cotizacion.porcentajeDescuento)} %)</dt>
        <dd>− {pesos(cotizacion.valorDescuento)}</dd>

        <dt className="cot-total">Precio final</dt>
        <dd className="cot-total">{pesos(cotizacion.precioFinal)}</dd>

        <dt>Cuota inicial ({CUOTA_INICIAL} %)</dt>
        <dd>{pesos(cotizacion.cuotaInicial)}</dd>

        <dt>Saldo a financiar</dt>
        <dd>{pesos(cotizacion.saldoFinanciar)}</dd>

        {cotizacion.cuotaMensualEstimada != null && (
          <>
            <dt>Cuota mensual estimada (20 años)</dt>
            <dd>{pesos(cotizacion.cuotaMensualEstimada)}</dd>

            <dt>Ingreso mínimo requerido</dt>
            <dd>{pesos(cotizacion.salarioMinimoRequerido)}</dd>
          </>
        )}
      </dl>

      {cotizacion.presupuestoCliente != null && (
        <p className="cot-fechas">
          Precalificada con presupuesto de {pesos(cotizacion.presupuestoCliente)} e ingresos del hogar de{" "}
          {pesos(cotizacion.salarioCliente)}
        </p>
      )}

      <p className="cot-fechas">
        Generada el {fecha(cotizacion.fechaCreacion)} · vence el {fecha(cotizacion.fechaVencimiento)}
      </p>

      {cotizacion.observacion && <p className="cot-fechas">{cotizacion.observacion}</p>}

      {acciones && <div className="cot-acciones">{acciones}</div>}
    </div>
  );
}

export default function Cotizaciones({ usuario, unidades, onCambioUnidades }) {
  const esCliente = usuario.rol === "CLIENTE";
  const esDirector = usuario.rol === "DIRECTOR_COMERCIAL";

  const [cotizaciones, setCotizaciones] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [reservas, setReservas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensaje, setMensaje] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [version, setVersion] = useState(0);

  const [form, setForm] = useState({
    emailCliente: "",
    identificadorUnidad: "",
    porcentajeDescuento: "0",
  });

  useEffect(() => {
    let activo = true;

    async function cargar() {
      setCargando(true);

      try {
        const ruta = esCliente
          ? `/cotizaciones?cliente=${encodeURIComponent(usuario.email)}`
          : "/cotizaciones";

        const rutaReservas = esCliente
          ? `/reservas?cliente=${encodeURIComponent(usuario.email)}`
          : "/reservas";

        const [lista, listaClientes, listaReservas] = await Promise.all([
          pedir(ruta),
          esCliente ? Promise.resolve([]) : pedir("/clientes"),
          pedir(rutaReservas),
        ]);

        if (activo) {
          setCotizaciones(lista);
          setClientes(listaClientes);
          setReservas(listaReservas);
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

  const unidadElegida = unidades.find((u) => u.identificador === form.identificadorUnidad);
  const vistaPrevia = unidadElegida
    ? calcularVistaPrevia(unidadElegida.precioLista, form.porcentajeDescuento)
    : null;
  const requiereAutorizacion = Number(form.porcentajeDescuento) > DESCUENTO_SIN_AUTORIZACION;

  const cambiarForm = (evento) => {
    const { name, value } = evento.target;
    setForm((anterior) => ({ ...anterior, [name]: value }));
  };

  const generar = async (evento) => {
    evento.preventDefault();
    setEnviando(true);
    setMensaje(null);

    try {
      const creada = await pedir("/cotizaciones", {
        method: "POST",
        body: JSON.stringify({
          identificadorUnidad: form.identificadorUnidad,
          emailCliente: form.emailCliente,
          emailAsesor: usuario.email,
          porcentajeDescuento: Number(form.porcentajeDescuento),
        }),
      });

      setMensaje({
        tipo: "ok",
        texto:
          creada.estado === "PENDIENTE_AUTORIZACION"
            ? "Cotización creada. Queda pendiente de que el director comercial autorice el descuento."
            : "Cotización creada. El cliente ya puede verla y firmarla.",
      });
      setForm({ emailCliente: "", identificadorUnidad: "", porcentajeDescuento: "0" });
      setVersion((v) => v + 1);
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    } finally {
      setEnviando(false);
    }
  };

  const ejecutar = async (id, accion, textoOk) => {
    setMensaje(null);

    try {
      await pedir(`/cotizaciones/${id}/${accion}`, {
        method: "PUT",
        body: JSON.stringify({ email: usuario.email }),
      });
      setMensaje({ tipo: "ok", texto: textoOk });
      setVersion((v) => v + 1);
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    }
  };

  // F-04: reservar la unidad a partir de la cotización firmada (R004)
  const reservar = async (cotizacion) => {
    const cuota = cuotaReserva(cotizacion.precioLista);

    if (!window.confirm(`¿Reservar ${cotizacion.identificadorUnidad}? La cuota de reserva es ${pesos(cuota)}.`)) {
      return;
    }

    setMensaje(null);

    try {
      const reserva = await pedir("/reservas", {
        method: "POST",
        body: JSON.stringify({ idCotizacion: cotizacion.id, email: usuario.email }),
      });

      setMensaje({
        tipo: "ok",
        texto: `Unidad reservada. Cuota de reserva: ${pesos(reserva.cuotaReserva)}. La encuentras en la pestaña de reservas.`,
      });
      setVersion((v) => v + 1);
      if (onCambioUnidades) onCambioUnidades();
    } catch (error) {
      setMensaje({ tipo: "error", texto: error.message });
    }
  };

  const tieneReservaActiva = (cotizacion) =>
    reservas.some((r) => r.idCotizacion === cotizacion.id && r.estado !== "CANCELADA");

  const accionesDe = (cotizacion) => {
    if (esCliente && cotizacion.estado === "GENERADA") {
      return (
        <button
          type="button"
          className="primary-button"
          onClick={() =>
            ejecutar(cotizacion.id, "firmar", "Cotización firmada. Ya puedes reservar la unidad.")
          }
        >
          Firmar cotización
        </button>
      );
    }

    if (cotizacion.estado === "FIRMADA") {
      if (tieneReservaActiva(cotizacion)) {
        return <span className="cot-reservada">Unidad reservada</span>;
      }

      return (
        <button type="button" className="primary-button" onClick={() => reservar(cotizacion)}>
          Reservar unidad · cuota {pesos(cuotaReserva(cotizacion.precioLista))}
        </button>
      );
    }

    return null;
  };

  const pendientes = cotizaciones.filter((c) => c.estado === "PENDIENTE_AUTORIZACION");

  return (
    <div className="cot-view">
      {mensaje && (
        <p className={mensaje.tipo === "error" ? "cot-mensaje cot-mensaje-error" : "cot-mensaje"}>
          {mensaje.texto}
        </p>
      )}

      {/* ---- Formulario del asesor / director ---- */}
      {!esCliente && (
        <form className="cot-form" onSubmit={generar}>
          <h3>Generar cotización</h3>

          <div className="input-group">
            <label>Cliente</label>
            <select name="emailCliente" value={form.emailCliente} onChange={cambiarForm} required>
              <option value="">Seleccionar</option>
              {clientes.map((cliente) => (
                <option key={cliente.email} value={cliente.email}>
                  {cliente.nombre} — {cliente.email}
                </option>
              ))}
            </select>
          </div>

          <div className="input-group">
            <label>Unidad disponible</label>
            <select name="identificadorUnidad" value={form.identificadorUnidad} onChange={cambiarForm} required>
              <option value="">Seleccionar</option>
              {unidades.map((unidad) => (
                <option key={unidad.identificador} value={unidad.identificador}>
                  {unidad.identificador} — {pesos(unidad.precioLista)}
                </option>
              ))}
            </select>
          </div>

          <div className="input-group">
            <label>Descuento (%)</label>
            <input
              type="number"
              name="porcentajeDescuento"
              min="0"
              max={DESCUENTO_MAXIMO}
              step="0.5"
              value={form.porcentajeDescuento}
              onChange={cambiarForm}
              required
            />
          </div>

          {vistaPrevia && (
            <dl className="cot-valores cot-vista-previa">
              <dt>Precio de lista</dt>
              <dd>{pesos(unidadElegida.precioLista)}</dd>
              <dt>Descuento</dt>
              <dd>− {pesos(vistaPrevia.descuento)}</dd>
              <dt className="cot-total">Precio final</dt>
              <dd className="cot-total">{pesos(vistaPrevia.precioFinal)}</dd>
              <dt>Cuota inicial ({CUOTA_INICIAL} %)</dt>
              <dd>{pesos(vistaPrevia.cuotaInicial)}</dd>
              <dt>Saldo a financiar</dt>
              <dd>{pesos(vistaPrevia.saldo)}</dd>
            </dl>
          )}

          {requiereAutorizacion && (
            <p className="cot-aviso">
              Un descuento mayor al {DESCUENTO_SIN_AUTORIZACION} % requiere autorización del director
              comercial.
            </p>
          )}

          <p className="cot-fechas">La cotización tendrá una vigencia de 15 días.</p>

          <button type="submit" className="primary-button" disabled={enviando}>
            {enviando ? "Generando..." : "Generar cotización"}
          </button>
        </form>
      )}

      {/* ---- Bandeja del director (R003) ---- */}
      {esDirector && (
        <section className="cot-seccion">
          <h3>Descuentos por autorizar ({pendientes.length})</h3>

          {pendientes.length === 0 && <p className="cot-vacio">No hay descuentos pendientes.</p>}

          <div className="cot-grid">
            {pendientes.map((cotizacion) => (
              <TarjetaCotizacion
                key={cotizacion.id}
                cotizacion={cotizacion}
                mostrarCliente
                acciones={
                  <>
                    <button
                      type="button"
                      className="primary-button"
                      onClick={() => ejecutar(cotizacion.id, "autorizar", "Descuento autorizado")}
                    >
                      Autorizar
                    </button>
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={() => ejecutar(cotizacion.id, "rechazar", "Descuento rechazado")}
                    >
                      Rechazar
                    </button>
                  </>
                }
              />
            ))}
          </div>
        </section>
      )}

      {/* ---- Listado ---- */}
      <section className="cot-seccion">
        <h3>{esCliente ? "Mis cotizaciones" : "Cotizaciones generadas"}</h3>

        {cargando && <p>Cargando cotizaciones...</p>}

        {!cargando && cotizaciones.length === 0 && (
          <p className="cot-vacio">
            {esCliente
              ? "Aún no tienes cotizaciones. Tu asesor comercial te enviará una."
              : "Todavía no hay cotizaciones."}
          </p>
        )}

        <div className="cot-grid">
          {cotizaciones.map((cotizacion) => (
            <TarjetaCotizacion
              key={cotizacion.id}
              cotizacion={cotizacion}
              mostrarCliente={!esCliente}
              acciones={accionesDe(cotizacion)}
            />
          ))}
        </div>
      </section>
    </div>
  );
}

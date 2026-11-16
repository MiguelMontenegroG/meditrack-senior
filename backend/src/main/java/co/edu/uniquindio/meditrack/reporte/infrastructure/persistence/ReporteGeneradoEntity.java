package co.edu.uniquindio.meditrack.reporte.infrastructure.persistence;

import co.edu.uniquindio.meditrack.reporte.domain.TipoReporte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entidad JPA de la tabla reporte_generado (migracion V2).
 *
 * <p>Mapea exactamente las columnas de V2: id, paciente_id, generado_por, tipo,
 * rango_desde, rango_hasta, ruta_almacenamiento, creado_en. No guarda el PDF:
 * solo metadatos y una ruta referencial. La fila es append-only: no hay columnas
 * de actualizacion ni actualizado_en.</p>
 */
@Entity
@Table(name = "reporte_generado")
public class ReporteGeneradoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "generado_por", nullable = false)
    private Long generadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoReporte tipo;

    @Column(name = "rango_desde")
    private Instant rangoDesde;

    @Column(name = "rango_hasta")
    private Instant rangoHasta;

    @Column(name = "ruta_almacenamiento", length = 300)
    private String rutaAlmacenamiento;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected ReporteGeneradoEntity() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getGeneradoPor() {
        return generadoPor;
    }

    public void setGeneradoPor(Long generadoPor) {
        this.generadoPor = generadoPor;
    }

    public TipoReporte getTipo() {
        return tipo;
    }

    public void setTipo(TipoReporte tipo) {
        this.tipo = tipo;
    }

    public Instant getRangoDesde() {
        return rangoDesde;
    }

    public void setRangoDesde(Instant rangoDesde) {
        this.rangoDesde = rangoDesde;
    }

    public Instant getRangoHasta() {
        return rangoHasta;
    }

    public void setRangoHasta(Instant rangoHasta) {
        this.rangoHasta = rangoHasta;
    }

    public String getRutaAlmacenamiento() {
        return rutaAlmacenamiento;
    }

    public void setRutaAlmacenamiento(String rutaAlmacenamiento) {
        this.rutaAlmacenamiento = rutaAlmacenamiento;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
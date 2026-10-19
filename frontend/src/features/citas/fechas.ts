// Reexporta las utilidades de fecha/hora de America/Bogota desde lib. Se
// movieron a lib/fechas-bogota.ts para compartirlas con la bitacora de signos
// vitales sin duplicarlas; este archivo conserva los imports del modulo de citas.
export {
  aInstanteUtc,
  agruparPorDia,
  diaEnEspanol,
  fechaBogota,
  fechaHoraBogota,
  horaBogota,
  hoyBogota,
  instanteYaPaso,
  sumarDias,
  yaPaso,
} from '@/lib/fechas-bogota'

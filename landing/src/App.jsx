import novaLogo from "./assets/nova-logo-horizontal.png";

function App() {
  return (
    <main className="min-h-screen bg-[#FFF8FD] text-slate-950">
      <section className="relative overflow-hidden">
        <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-[#82368c]/20 blur-3xl" />
        <div className="absolute top-32 -left-32 h-80 w-80 rounded-full bg-[#1600b5]/15 blur-3xl" />

        <header className="relative mx-auto flex max-w-7xl items-center justify-between px-6 py-6">
          <img
            src={novaLogo}
            alt="NOVA Aplicación Educativa"
            className="h-14 w-auto object-contain"
          />

          <nav className="hidden items-center gap-8 text-sm font-medium text-slate-700 md:flex">
            <a href="#problema" className="hover:text-[#82368c]">
              Problema
            </a>
            <a href="#solucion" className="hover:text-[#82368c]">
              Solución
            </a>
            <a href="#funciones" className="hover:text-[#82368c]">
              Funciones
            </a>
            <a href="#demo" className="hover:text-[#82368c]">
              Demo
            </a>
          </nav>

          <a
            href="#demo"
            className="rounded-full bg-[#82368c] px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-[#82368c]/20 transition hover:bg-[#6f2c78]"
          >
            Ver demo
          </a>
        </header>

        <div className="relative mx-auto grid max-w-7xl items-center gap-12 px-6 pb-24 pt-14 lg:grid-cols-2 lg:pt-20">
          <div>
            <p className="mb-5 inline-flex rounded-full border border-[#82368c]/20 bg-white px-4 py-2 text-sm font-semibold text-[#82368c] shadow-sm">
              Plataforma educativa gamificada
            </p>

            <h1 className="max-w-3xl text-5xl font-black leading-tight tracking-tight text-slate-950 md:text-6xl">
              Aprender sobre equidad también puede ser interactivo.
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-600">
              NOVA transforma contenidos sobre derechos, dignidad, igualdad y
              respeto en misiones, retos y experiencias educativas diseñadas
              para estudiantes, docentes e instituciones.
            </p>

            <div className="mt-9 flex flex-col gap-4 sm:flex-row">
              <a
                href="#demo"
                className="rounded-full bg-[#82368c] px-7 py-4 text-center font-semibold text-white shadow-xl shadow-[#82368c]/25 transition hover:-translate-y-0.5 hover:bg-[#6f2c78]"
              >
                Conocer NOVA
              </a>

              <a
                href="#funciones"
                className="rounded-full border border-[#82368c]/30 bg-white px-7 py-4 text-center font-semibold text-[#82368c] shadow-sm transition hover:-translate-y-0.5 hover:border-[#82368c]"
              >
                Ver funcionalidades
              </a>
            </div>
          </div>

          <div className="relative">
            <div className="rounded-[2.5rem] bg-gradient-to-br from-[#82368c] to-[#1600b5] p-4 shadow-2xl shadow-[#82368c]/25">
              <div className="rounded-[2rem] bg-white/95 p-8">
                <div className="mb-8 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold text-[#82368c]">
                      Ruta de aprendizaje
                    </p>
                    <h2 className="text-2xl font-black text-slate-950">
                      Misión activa
                    </h2>
                  </div>
                  <span className="rounded-full bg-[#82368c]/10 px-4 py-2 text-sm font-bold text-[#82368c]">
                    Nivel 1
                  </span>
                </div>

                <div className="space-y-4">
                  <article className="rounded-3xl border border-slate-100 bg-[#FFF8FD] p-5">
                    <p className="text-sm font-bold text-[#82368c]">
                      Reto educativo
                    </p>
                    <h3 className="mt-2 text-xl font-black">
                      Derechos y dignidad de la mujer
                    </h3>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                      Actividad breve con retroalimentación inmediata para
                      reforzar el aprendizaje.
                    </p>
                  </article>

                  <article className="rounded-3xl border border-slate-100 bg-white p-5">
                    <div className="flex items-center justify-between">
                      <p className="font-bold text-slate-900">
                        Progreso del estudiante
                      </p>
                      <p className="font-black text-[#1600b5]">72%</p>
                    </div>
                    <div className="mt-4 h-3 rounded-full bg-slate-100">
                      <div className="h-3 w-[72%] rounded-full bg-[#1600b5]" />
                    </div>
                  </article>

                  <article className="grid gap-4 sm:grid-cols-2">
                    <div className="rounded-3xl bg-[#82368c] p-5 text-white">
                      <p className="text-3xl font-black">+12</p>
                      <p className="mt-1 text-sm text-white/80">
                        Misiones educativas
                      </p>
                    </div>
                    <div className="rounded-3xl bg-[#1600b5] p-5 text-white">
                      <p className="text-3xl font-black">3</p>
                      <p className="mt-1 text-sm text-white/80">
                        Roles conectados
                      </p>
                    </div>
                  </article>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="problema" className="bg-white px-6 py-20">
        <div className="mx-auto max-w-7xl">
          <div className="max-w-3xl">
            <p className="text-sm font-bold uppercase tracking-wide text-[#82368c]">
              Problema
            </p>
            <h2 className="mt-3 text-4xl font-black tracking-tight">
              La enseñanza tradicional necesita experiencias más dinámicas.
            </h2>
            <p className="mt-5 text-lg leading-8 text-slate-600">
              Muchos contenidos importantes se trabajan de forma poco
              interactiva. NOVA propone una alternativa accesible, visual y
              motivadora para reforzar aprendizajes desde el aula o desde
              dispositivos móviles.
            </p>
          </div>
        </div>
      </section>

      <section id="solucion" className="px-6 py-20">
        <div className="mx-auto grid max-w-7xl gap-8 lg:grid-cols-3">
          <div className="rounded-[2rem] bg-white p-8 shadow-xl shadow-slate-200/60">
            <h3 className="text-2xl font-black text-[#82368c]">
              Para estudiantes
            </h3>
            <p className="mt-4 leading-7 text-slate-600">
              Misiones, preguntas, retos y recompensas que ayudan a avanzar a
              su propio ritmo.
            </p>
          </div>

          <div className="rounded-[2rem] bg-white p-8 shadow-xl shadow-slate-200/60">
            <h3 className="text-2xl font-black text-[#82368c]">
              Para docentes
            </h3>
            <p className="mt-4 leading-7 text-slate-600">
              Grupos, códigos de acceso, seguimiento de avance y herramientas
              para acompañar el aprendizaje.
            </p>
          </div>

          <div className="rounded-[2rem] bg-white p-8 shadow-xl shadow-slate-200/60">
            <h3 className="text-2xl font-black text-[#82368c]">
              Para instituciones
            </h3>
            <p className="mt-4 leading-7 text-slate-600">
              Organización por centros educativos, roles y datos que permiten
              visualizar el progreso general.
            </p>
          </div>
        </div>
      </section>

      <section id="funciones" className="bg-[#82368c] px-6 py-20 text-white">
        <div className="mx-auto max-w-7xl">
          <p className="text-sm font-bold uppercase tracking-wide text-white/70">
            Funcionalidades
          </p>
          <h2 className="mt-3 max-w-3xl text-4xl font-black tracking-tight">
            Una experiencia educativa conectada al backend de NOVA.
          </h2>

          <div className="mt-10 grid gap-5 md:grid-cols-2 lg:grid-cols-4">
            {[
              "Inicio de sesión por rol",
              "Unión a grupos por código",
              "Misiones educativas",
              "Seguimiento de progreso",
            ].map((item) => (
              <div
                key={item}
                className="rounded-3xl border border-white/15 bg-white/10 p-6 backdrop-blur"
              >
                <p className="text-lg font-bold">{item}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="demo" className="px-6 py-24">
        <div className="mx-auto max-w-5xl rounded-[2.5rem] bg-gradient-to-br from-[#1600b5] to-[#82368c] p-10 text-center text-white shadow-2xl shadow-[#1600b5]/20">
          <h2 className="text-4xl font-black tracking-tight">
            NOVA ya cuenta con backend funcional y versión móvil en desarrollo.
          </h2>
          <p className="mx-auto mt-5 max-w-2xl text-lg leading-8 text-white/80">
            El proyecto integra autenticación, roles, grupos, códigos de acceso
            y datos educativos para conectar la experiencia móvil con servicios
            reales.
          </p>

          <a
            href="#"
            className="mt-8 inline-flex rounded-full bg-white px-8 py-4 font-bold text-[#82368c] transition hover:-translate-y-0.5"
          >
            Solicitar demostración
          </a>
        </div>
      </section>
    </main>
  );
}

export default App;
import novaLogo from "./assets/nova-logo-horizontal.png";

const steps = [
  {
    number: "01",
    title: "El docente crea un grupo",
    text: "Organiza a sus estudiantes y genera un código de acceso para iniciar la ruta educativa.",
  },
  {
    number: "02",
    title: "El estudiante se une",
    text: "Ingresa con su usuario o código de grupo y accede a una experiencia guiada.",
  },
  {
    number: "03",
    title: "Completa misiones",
    text: "Avanza por retos interactivos sobre igualdad, dignidad, respeto y derechos.",
  },
  {
    number: "04",
    title: "Se visualiza el progreso",
    text: "Docentes e instituciones pueden acompañar el avance del aprendizaje.",
  },
];

const features = [
  "Misiones educativas",
  "Retos interactivos",
  "Rutas por nivel",
  "Recompensas visuales",
  "Grupos con código",
  "Seguimiento docente",
];

function PhoneMockup() {
  return (
    <div className="relative mx-auto w-[310px] rounded-[2.5rem] bg-white p-3 shadow-2xl shadow-[#1600b5]/25 ring-8 ring-[#1600b5]/10 lg:w-[360px]">
      <div className="overflow-hidden rounded-[2rem] bg-[#d9bbff]">
        <div className="h-48 bg-gradient-to-br from-[#1600b5] via-[#82368c] to-[#c47ad6]">
          <div className="flex h-full items-end justify-center px-6 pb-8">
            <img src={novaLogo} alt="NOVA" className="w-64 rounded-2xl bg-white/10 p-2" />
          </div>
        </div>

        <div className="px-6 py-7 text-center">
          <p className="text-sm font-black text-slate-950">
            Emprende tu vuelo hacia la equidad.
          </p>

          <button className="mt-7 w-full rounded-full bg-white px-5 py-3 text-sm font-bold text-slate-800 shadow">
            Continuar con Google
          </button>

          <div className="my-5 flex items-center gap-3 text-xs font-bold text-slate-700">
            <span className="h-px flex-1 bg-[#82368c]/30" />
            O inicia sesión con
            <span className="h-px flex-1 bg-[#82368c]/30" />
          </div>

          <div className="space-y-3">
            <div className="rounded-2xl bg-white px-4 py-3 text-left text-sm text-slate-400">
              ID de usuario
            </div>
            <div className="rounded-2xl bg-white px-4 py-3 text-left text-sm text-slate-400">
              Contraseña
            </div>
            <button className="w-full rounded-2xl bg-[#1600b5] px-4 py-3 text-sm font-bold text-white">
              Continuar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function LessonCard() {
  return (
    <div className="rounded-[2rem] bg-white p-6 shadow-xl shadow-[#82368c]/10">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <p className="text-sm font-bold text-[#82368c]">Ruta activa</p>
          <h3 className="text-2xl font-black text-slate-950">Lecciones</h3>
        </div>
        <span className="rounded-full bg-[#82368c]/10 px-4 py-2 text-sm font-bold text-[#82368c]">
          Primaria Alta
        </span>
      </div>

      <div className="relative h-72">
        <div className="absolute bottom-4 left-8 flex h-16 w-16 items-center justify-center rounded-full bg-[#1600b5] text-xl font-black text-white shadow-lg">
          01
        </div>
        <div className="absolute left-20 top-28 flex h-16 w-16 items-center justify-center rounded-full bg-[#d9bbff] text-xl font-black text-[#1600b5] shadow-lg">
          02
        </div>
        <div className="absolute right-24 top-16 flex h-16 w-16 items-center justify-center rounded-full bg-[#1600b5] text-xl font-black text-white shadow-lg">
          03
        </div>
        <div className="absolute right-6 top-40 flex h-16 w-16 items-center justify-center rounded-full bg-[#82368c] text-xl font-black text-white shadow-lg">
          04
        </div>

        <div className="absolute bottom-6 left-28 rounded-2xl bg-slate-100 px-5 py-4">
          <p className="font-black text-slate-950">Bienvenido a NOVA</p>
          <p className="text-sm text-slate-600">
            Primera misión introductoria.
          </p>
        </div>
      </div>
    </div>
  );
}

function App() {
  return (
    <main className="min-h-screen bg-[#fff7fd] text-slate-950">
      <header className="sticky top-0 z-50 border-b border-[#82368c]/10 bg-white/80 backdrop-blur-xl">
        <div className="flex items-center justify-between px-8 py-4">
          <img src={novaLogo} alt="NOVA Aplicación Educativa" className="h-14 w-auto" />

          <nav className="hidden items-center gap-8 text-sm font-bold text-slate-700 md:flex">
            <a href="#funciona" className="hover:text-[#82368c]">Cómo funciona</a>
            <a href="#usuarios" className="hover:text-[#82368c]">Usuarios</a>
            <a href="#funciones" className="hover:text-[#82368c]">Funciones</a>
            <a href="#demo" className="hover:text-[#82368c]">Demo</a>
          </nav>

          <a
            href="#demo"
            className="rounded-full bg-[#82368c] px-6 py-3 text-sm font-black text-white shadow-lg shadow-[#82368c]/25"
          >
            Solicitar demo
          </a>
        </div>
      </header>

      <section className="relative overflow-hidden px-8 py-14 lg:py-20">
        <div className="absolute -right-24 top-10 h-96 w-96 rounded-full bg-[#82368c]/20 blur-3xl" />
        <div className="absolute -left-24 bottom-0 h-96 w-96 rounded-full bg-[#1600b5]/10 blur-3xl" />

        <div className="relative grid items-center gap-12 lg:grid-cols-[1fr_0.9fr]">
          <div className="max-w-4xl">
            <p className="mb-6 inline-flex rounded-full bg-white px-5 py-3 text-sm font-black text-[#82368c] shadow-md shadow-[#82368c]/10">
              Plataforma educativa gamificada
            </p>

            <h1 className="text-6xl font-black leading-[0.98] tracking-tight md:text-7xl xl:text-8xl">
              Emprende tu vuelo hacia una educación más justa.
            </h1>

            <p className="mt-7 max-w-3xl text-xl leading-9 text-slate-700 md:text-2xl">
              NOVA convierte el aprendizaje sobre derechos, dignidad, igualdad
              y respeto en misiones interactivas guiadas por una experiencia
              visual, cercana y motivadora.
            </p>

            <div className="mt-9 flex flex-col gap-4 sm:flex-row">
              <a
                href="#demo"
                className="rounded-full bg-[#82368c] px-8 py-4 text-center text-lg font-black text-white shadow-xl shadow-[#82368c]/30"
              >
                Ver demo
              </a>
              <a
                href="#funciona"
                className="rounded-full border-2 border-[#82368c]/25 bg-white px-8 py-4 text-center text-lg font-black text-[#82368c]"
              >
                Conocer cómo funciona
              </a>
            </div>
          </div>

          <div className="grid items-center gap-6 md:grid-cols-[0.8fr_1fr] lg:grid-cols-1 xl:grid-cols-[0.8fr_1fr]">
            <PhoneMockup />
            <div className="hidden xl:block">
              <LessonCard />
            </div>
          </div>
        </div>
      </section>

      <section id="funciona" className="bg-white px-8 py-16">
        <div className="mx-auto max-w-7xl">
          <div className="mb-10 max-w-3xl">
            <p className="text-sm font-black uppercase tracking-wide text-[#82368c]">
              Cómo funciona
            </p>
            <h2 className="mt-3 text-5xl font-black tracking-tight">
              Una ruta educativa simple, guiada y medible.
            </h2>
          </div>

          <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-4">
            {steps.map((step) => (
              <article
                key={step.number}
                className="rounded-[2rem] border border-[#82368c]/10 bg-[#fff7fd] p-7 shadow-sm"
              >
                <span className="text-5xl font-black text-[#82368c]">
                  {step.number}
                </span>
                <h3 className="mt-5 text-2xl font-black">{step.title}</h3>
                <p className="mt-3 text-base leading-7 text-slate-600">
                  {step.text}
                </p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section id="usuarios" className="px-8 py-16">
        <div className="grid gap-8 lg:grid-cols-3">
          <article className="rounded-[2.5rem] bg-white p-8 shadow-xl shadow-[#82368c]/10">
            <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-[#82368c] text-3xl text-white">
              ✦
            </div>
            <h3 className="text-3xl font-black text-[#82368c]">
              Para estudiantes
            </h3>
            <p className="mt-4 text-lg leading-8 text-slate-600">
              Aprenden mediante misiones, retos, niveles y retroalimentación
              visual que hacen más cercana la experiencia educativa.
            </p>
          </article>

          <article className="rounded-[2.5rem] bg-white p-8 shadow-xl shadow-[#82368c]/10">
            <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-[#1600b5] text-3xl text-white">
              ✓
            </div>
            <h3 className="text-3xl font-black text-[#82368c]">
              Para docentes
            </h3>
            <p className="mt-4 text-lg leading-8 text-slate-600">
              Gestionan grupos, comparten códigos de acceso y acompañan el
              avance de sus estudiantes desde una experiencia organizada.
            </p>
          </article>

          <article className="rounded-[2.5rem] bg-white p-8 shadow-xl shadow-[#82368c]/10">
            <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-[#82368c] text-3xl text-white">
              ◈
            </div>
            <h3 className="text-3xl font-black text-[#82368c]">
              Para instituciones
            </h3>
            <p className="mt-4 text-lg leading-8 text-slate-600">
              Fortalecen procesos educativos sobre igualdad, respeto y dignidad
              con herramientas digitales accesibles.
            </p>
          </article>
        </div>
      </section>

      <section id="funciones" className="bg-[#82368c] px-8 py-16 text-white">
        <div className="grid items-start gap-10 lg:grid-cols-[0.8fr_1.2fr]">
          <div>
            <p className="text-sm font-black uppercase tracking-wide text-white/70">
              Funcionalidades
            </p>
            <h2 className="mt-3 text-5xl font-black tracking-tight">
              Diseñada para aprender jugando, no solo para responder preguntas.
            </h2>
            <p className="mt-5 text-xl leading-9 text-white/80">
              NOVA combina contenido educativo, seguimiento y gamificación para
              crear una experiencia útil dentro y fuera del aula.
            </p>
          </div>

          <div className="grid gap-5 sm:grid-cols-2">
            {features.map((feature) => (
              <div
                key={feature}
                className="rounded-[2rem] border border-white/15 bg-white/10 p-6 backdrop-blur"
              >
                <p className="text-2xl font-black">{feature}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="demo" className="px-8 py-16">
        <div className="grid items-center gap-10 rounded-[3rem] bg-gradient-to-br from-[#1600b5] to-[#82368c] p-10 text-white shadow-2xl shadow-[#1600b5]/20 lg:grid-cols-[1fr_0.8fr]">
          <div>
            <p className="text-sm font-black uppercase tracking-wide text-white/70">
              Demo visual
            </p>
            <h2 className="mt-3 text-5xl font-black tracking-tight">
              Una app educativa con identidad, propósito y seguimiento.
            </h2>
            <p className="mt-5 text-xl leading-9 text-white/80">
              NOVA está pensada para mostrar rutas de aprendizaje, misiones,
              progreso y acompañamiento docente en una experiencia móvil
              sencilla y accesible.
            </p>

            <a
              href="#"
              className="mt-8 inline-flex rounded-full bg-white px-8 py-4 text-lg font-black text-[#82368c]"
            >
              Solicitar demostración
            </a>
          </div>

          <div className="scale-90 lg:scale-100">
            <PhoneMockup />
          </div>
        </div>
      </section>

      <footer className="border-t border-[#82368c]/10 bg-white px-8 py-8">
        <div className="flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
          <img src={novaLogo} alt="NOVA" className="h-12 w-auto" />
          <p className="max-w-2xl text-sm leading-6 text-slate-600">
            NOVA es una aplicación educativa gamificada orientada al aprendizaje
            sobre igualdad, dignidad, respeto y derechos de la mujer.
          </p>
        </div>
      </footer>
    </main>
  );
}

export default App;